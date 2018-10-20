/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package functions;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Cell;
import data.Params;
import data.SerializableList;
import ij.IJ;
import ij.gui.Plot;
import ij.gui.PlotWindow;

/**
 * @author Carlos Alquezar
 *
 */
public class Kinematics {

  private enum Orientation {
    VERTICAL, HORIZONTAL, OBLIQUE, NULL
  }

  public double riser(int j,List track,List avgTrack){
    
    Cell avgCell1 = (Cell) avgTrack.get(j);
    Cell avgCell2 = (Cell) avgTrack.get(j + 1);
    double[] avgPoint1 = { (double) avgCell1.x, (double) avgCell1.y };
    double[] avgPoint2 = { (double) avgCell2.x, (double) avgCell2.y };
    double minDist = 999999;
    for (int k = j; k < j + Params.wSize - 1; k++) {
      Cell cell1 = (Cell) track.get(k);
      Cell cell2 = (Cell) track.get(k + 1);
      double[] point1 = { (double) cell1.x, (double) cell1.y };
      double[] point2 = { (double) cell2.x, (double) cell2.y };
      double dist = distanceBetweenSegments(point1, point2, avgPoint1, avgPoint2);
      if (dist > 0 && dist < minDist) {
        minDist = dist;
      }
    }
    return minDist;
  }
  public double[] alh(List track, List avgTrack) {
    double alh[] = new double[2];
    double alhMax = 0;
    double alhMean = 0;
    double nRisers = 0;
    for (int j = 0; j < avgTrack.size() - 3; j++) {
      double dist0 = riser(j,track,avgTrack);
      double dist1 = riser(j+1,track,avgTrack);
      double dist2 = riser(j+2,track,avgTrack);
      if (dist0 < 999999 && dist1 < 999999 && dist2 < 999999 ) {
        if(dist1 > dist0 && dist1 > dist2){//it means the point dist1 is a relative maximum
          alhMean += dist1;
          nRisers++;
          if (dist1 > alhMax)
            alhMax = dist1;
        }
      }
    }
    if(nRisers!=0)
      alhMean = alhMean/nRisers;
    
    alh[0] = 2*alhMean * Params.micronPerPixel;
    alh[1] = 2*alhMax * Params.micronPerPixel;

    return alh;

  }

  /**
   * @param track
   *          - a track
   * @param avgTrack
   *          -
   * @return BCF (Hz)
   */
  public float bcf(List track, List avgTrack) {

    int intersections = 0;
    for (int i = 0; i < track.size() - 1; i++) {
      Cell origP0 = (Cell) track.get(i);
      Cell origP1 = (Cell) track.get(i + 1);
      Line2D origLine = new Line2D.Float();
      origLine.setLine(origP0.x, origP0.y, origP1.x, origP1.y);
      for (int j = 0; j < avgTrack.size() - 1; j++) {
        Cell avgP0 = (Cell) avgTrack.get(j);
        Cell avgP1 = (Cell) avgTrack.get(j + 1);
        Line2D avgLine = new Line2D.Float();
        avgLine.setLine(avgP0.x, avgP0.y, avgP1.x, avgP1.y);
        boolean intersection = origLine.intersectsLine(avgLine);
        if (intersection) {
          intersections++;
          break;
        }
      }
    }
    int length = avgTrack.size();
    float bcf_value = (float) intersections * Params.frameRate / (float) (length - 1);
    return bcf_value;
  }

  private double distance(double[] p1, double[] p2) {
    return Math.sqrt(Math.pow(p2[0] - p1[0], 2) + Math.pow(p2[1] - p1[1], 2));
  }

  /**
   * This function calculates the perpendicular straight line to the segment {q1=>q2}
   * that cross it at the middle point (Mp). Also calculates at which point (P) this line crosses
   * the segment {p1=>p2}. The function returns the distance between the points Mp and P.
   * In case that the straight line does not cross the segment {p1=>p2}, the function returns 0.
   */
  private double distanceBetweenSegments(double[] p1, double[] p2, double[] q1, double[] q2) {

    Orientation segmentP_Orientation;
    Orientation segmentQ_Orientation;
    // vp = directional vector of the segment
    double[] vp = new double[2];
    vp[0] = p2[0] - p1[0];
    vp[1] = p2[1] - p1[1];
    // check orientation of the vector
    if (vp[0] == 0) { // dx=0
      if (vp[1] != 0)
        segmentP_Orientation = Orientation.VERTICAL;
      else {
        segmentP_Orientation = Orientation.NULL; // the segment is actually a
                                                 // point
        return 0.0;
      }
    } else {// dx!=0
      if (vp[1] == 0)
        segmentP_Orientation = Orientation.HORIZONTAL;
      else
        segmentP_Orientation = Orientation.OBLIQUE;
    }

    // vq = directional vector of the segment {q1=>q2}
    double[] vq = new double[2];
    vq[0] = q2[0] - q1[0];
    vq[1] = q2[1] - q1[1];
    // check orientation of the vector
    if (vq[0] == 0) { // dx=0
      if (vq[1] != 0)
        segmentQ_Orientation = Orientation.VERTICAL;
      else {
        segmentQ_Orientation = Orientation.NULL; // the segment is actually a
                                                 // point
        return 0.0;
      }
    } else {// dx!=0
      if (vq[1] == 0)
        segmentQ_Orientation = Orientation.HORIZONTAL;
      else
        segmentQ_Orientation = Orientation.OBLIQUE;
    }

    if (((segmentP_Orientation == Orientation.VERTICAL) && (segmentQ_Orientation == Orientation.HORIZONTAL))
        || ((segmentP_Orientation == Orientation.HORIZONTAL) && (segmentQ_Orientation == Orientation.VERTICAL)))
      return 0.0; // the segments are perpendicular, so the straight line
                  // perpendicular
                  // to one segment is parallel to the other

    //// We calculate the straight line that contains the segment {p1=>p2}
    double mp = 0;
    double np = 0;
    if (segmentP_Orientation != Orientation.VERTICAL) {
      // we calculate the slope of the straight line
      mp = vp[1] / vp[0];
      // we calculate the independent term of the straight line equation
      np = p2[1] - mp * p2[0];
    }

    //// We calculate the straight line perpendicular to the segment {q1=>q2} in
    //// the middle point
    // qm = middle point of the segment1
    double[] qm = new double[2];
    qm[0] = (q1[0] + q2[0]) / 2;
    qm[1] = (q1[1] + q2[1]) / 2;
    double mq = 0;
    double nq = 0;

    if (segmentQ_Orientation != Orientation.HORIZONTAL) {// Horizontal because
                                                         // we are calculating
                                                         // the perpendicular
                                                         // straigh line, i.e.
                                                         // Vertical

      // we calculate the slope of the straight line perperndicular to segment1
      mq = -vq[0] / vq[1];
      // we calculate the independent term of the straight line
      nq = qm[1] - mq * qm[0];
    }

    if ((segmentP_Orientation == Orientation.VERTICAL) && (segmentQ_Orientation == Orientation.OBLIQUE)) {
      double xi = p1[0];
      double yi = mq * xi + nq;
      double[] intersection = { xi, yi };
      if (isInTheSegmentRange(xi, yi, p1, p2)) {
        return distance(intersection, qm);
      } else
        return 0.0;
    } else if ((segmentP_Orientation == Orientation.OBLIQUE) && (segmentQ_Orientation == Orientation.HORIZONTAL)) {
      double xi = qm[0];
      double yi = mp * xi + np;
      double[] intersection = { xi, yi };
      if (isInTheSegmentRange(xi, yi, p1, p2))
        return distance(intersection, qm);
      else
        return 0.0;
    } else {
      if (mp == mq)
        return 0.0; // The lines are parallels
      else {
        //// we calculate the intersection point between the two straight lines
        double xi = (nq - np) / (mp - mq);
        double yi = mp * xi + np;
        double[] intersection = { xi, yi };

        if (isInTheSegmentRange(xi, yi, p1, p2))
          return distance(intersection, qm);
        else
          return 0.0;
      }
    }
  }
  
  public float fractalDimension(List track) {
    
    Cell origCell = (Cell) track.get(0);
    Cell cell0 = origCell;
    double n = (double)track.size()-1;
    double curvL = 0;
    double d = 0;
    for (int i = 1; i < track.size(); i++) {
      Cell cell1 = (Cell) track.get(i);
      curvL=curvL+(double)cell0.distance(cell1);
      double dist = (double)origCell.distance(cell1);
      if(dist>d){d=dist;}
      cell0=cell1;
    }
    return (float)(Math.log(n)/(Math.log(n)+Math.log(d/curvL)));
  }
  /******************************************************/
  /**
   * @param track
   * @return
   */
  public String getVelocityTrackType(List track) {
    SignalProcessing sp = new SignalProcessing();
    if (vcl(track) < Params.vclLowerTh)
      return "Slow";
    else if (vcl(track) > Params.vclUpperTh)
      return "Fast";
    else
      return "Normal";
  }

  /******************************************************/

  /******************************************************/
  /**
   * This function checks if the point(x,y) is in the range
   * [s2.x-s1.x,s2.y-s1.y] beeing s1-s2 a segment
   */
  private boolean isInTheSegmentRange(double x, double y, double[] s1, double[] s2) {

    double minX = Math.min(s1[0], s2[0]);
    double minY = Math.min(s1[1], s2[1]);
    double maxX = Math.max(s1[0], s2[0]);
    double maxY = Math.max(s1[1], s2[1]);

    if ((x >= minX && x <= maxX) && (y >= minY && y <= maxY))
      return true;
    else
      return false;
  }

  /******************************************************/
  /**
   * @param track
   *          - a track
   * @return MAD - (degrees)
   */
  public float mad(List track) {
    int nPoints = track.size();
    float totalDegrees = 0;
    for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
      Cell oldCell = (Cell) track.get(j);
      Cell newCell = (Cell) track.get(j + Params.angleDelta);
      float diffX = newCell.x - oldCell.x;
      float diffY = -(newCell.y - oldCell.y); // it is the negative because the
                                              // java coordinate system
      double angle = (360 + Math.atan2(diffY, diffX) * 180 / Math.PI) % (360); // Absolute
                                                                               // angle
      totalDegrees += angle;
    }
    // mean angle
    float meanAngle = totalDegrees / (nPoints - Params.angleDelta);
    return meanAngle;
  }

  /******************************************************/
  /**
   * @param track
   * @return
   */
  public boolean motilityTest(List track) {

    Kinematics K = new Kinematics();
    int nPoints = track.size();
    Cell firstCell = (Cell) track.get(0);
    Cell lastCell = (Cell) track.get(nPoints - 1);
    float distance = lastCell.distance(firstCell);
    SignalProcessing sp = new SignalProcessing();
    List avgTrack = sp.movingAverage(track);
    float vap = K.vcl(avgTrack) / K.vsl(avgTrack);
    // Kinematics filter
    double minPixelDistance = 10;// 10/Params.micronPerPixel;
    if (K.vcl(track) > Params.vclMin && (distance > minPixelDistance) && (vap > 0))
      return true;
    else
      return false;
  }

  /******************************************************/
  /**
   * @param theTracks
   * @return
   */
  public int[] motilityTest(SerializableList theTracks) {
    int motile = 0;
    int nonMotile = 0;
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      if (motilityTest(aTrack))
        motile++;
      else
        nonMotile++;
    }
    int[] results = new int[2];
    results[0] = motile;
    results[1] = nonMotile;
    return results;
  }

  public void test_alh() {

    List<Cell> track = new ArrayList<Cell>();
    /////////////////////////
    Cell c = new Cell();
    c.x = 1;
    c.y = 1;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 2;
    c.y = 3;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 3;
    c.y = 3;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 4;
    c.y = 6;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 5;
    c.y = 4;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 7;
    c.y = 10;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 9;
    c.y = 5;
    track.add(c);
    /////////////////////////
    c = new Cell();
    c.x = 12;
    c.y = 15;
    track.add(c);
    /////////////////////////
    SignalProcessing sp = new SignalProcessing();
    List<Cell> avgTrack = sp.movingAverage(track, 5);

    double[] alhRes = alh(track, avgTrack);
    IJ.log("alhMean: " + alhRes[0] / Params.micronPerPixel + "; alhMax: " + alhRes[1] / Params.micronPerPixel);

  }

  public void test_bcf() {

    List track = new ArrayList();
    int N = 10;
    for (int i = 0; i < N; i++) {
      Cell c = new Cell();
      c.x = i;
      c.y = 2 * (i % 2) - 1;
      IJ.log(c.x + "  " + c.y);
      track.add(c);
    }
    IJ.log("----------------------------");
    IJ.log("----------------------------");
    SignalProcessing s = new SignalProcessing();
    List avgTrack = s.movingAverage(track, 3);

    for (int i = 0; i < avgTrack.size(); i++) {
      Cell c = (Cell) avgTrack.get(i);
      System.out.println(c.x + "  " + c.y);
      IJ.log(c.x + "  " + c.y);
    }
    IJ.log("----------------------------");
    IJ.log("----------------------------");
    int intersections = 0;
    for (int i = 0; i < track.size() - 1; i++) {
      Cell origP0 = (Cell) track.get(i);
      Cell origP1 = (Cell) track.get(i + 1);
      Line2D origLine = new Line2D.Float();
      origLine.setLine(origP0.x, origP0.y, origP1.x, origP1.y);
      for (int j = 0; j < avgTrack.size() - 1; j++) {
        Cell avgP0 = (Cell) avgTrack.get(j);
        Cell avgP1 = (Cell) avgTrack.get(j + 1);
        Line2D avgLine = new Line2D.Float();
        avgLine.setLine(avgP0.x, avgP0.y, avgP1.x, avgP1.y);
        boolean intersection = origLine.intersectsLine(avgLine);
        if (intersection) {
          intersections++;
          break;
        }
      }
    }
    int length = avgTrack.size();
    float bcf_value = (float) intersections * Params.frameRate / (float) (length - 1);
    IJ.log("BCF: " + bcf_value);
    IJ.log("Frame rate: " + Params.frameRate);
  }
  
  public void test_fractal() {

    List<Cell> track = new ArrayList<Cell>();
    Cell c = new Cell();
    c.x = (int)(Math.random()*100);
    c.y = (int)(Math.random()*100);
    track.add(c);
    int x0 = (int) c.x;
    int y0 = (int) c.y;
    int L = 100;
    for(int i=0;i<L;i++){
      int dx = (int)(Math.random()*10)-5;
      int dy = (int)(Math.random()*10)-5;
      Cell c2 = new Cell();
      c2.x=x0+dx;
      c2.y=y0+dy;
      track.add(c2);
      x0=(int) c2.x;
      y0=(int) c2.y;
    }

    float fd = fractalDimension(track);
    IJ.log("FD: "+fd);

  }

  /******************************************************/
  /**
   * @param track
   *          - a track
   * @return VCL (um/second)
   */
  public float vcl(List track) {

    int length = track.size();
    ListIterator jT = track.listIterator();
    Cell oldCell = (Cell) jT.next();
    float distance = 0;
    for (; jT.hasNext();) {
      Cell newCell = (Cell) jT.next();
      distance += newCell.distance(oldCell);
      oldCell = newCell;
    }
    // convert pixels to micrometers
    distance = distance * (float) Params.micronPerPixel;
    // Seconds
    float elapsedTime = (length - 1) / Params.frameRate;
    // return um/second
    return distance / elapsedTime;
  }

  /******************************************************/
  /**
   * @param track
   *          - a track
   * @return VSL (um/second)
   */
  public float vsl(List track) {

    int length = track.size();
    Cell first = (Cell) track.get(0);
    Cell last = (Cell) track.get(length - 1);
    // Distance (pixels)
    float distance = last.distance(first);
    // convert pixels to micrometers
    distance = distance * (float) Params.micronPerPixel;
    // Seconds
    float elapsedTime = (length - 1) / Params.frameRate;
    // return um/second
    return distance / elapsedTime;
  }

}
