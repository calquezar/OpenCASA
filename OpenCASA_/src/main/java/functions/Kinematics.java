/*
 *   OpenCASA software v0.8 for video and image analysis
 *   Copyright (C) 2017  Carlos Alquézar
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

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SerializableList;
import ij.IJ;
import data.Cell;

/**
 * @author Carlos Alquezar
 *
 */
public class Kinematics {
  /******************************************************/
  /**
   * @param track
   *          - a track
   * @param avgTrack
   *          -
   * @return ALH (mean and max) (um)
   */
  public float[] alh(List track, List avgTrack) {

//    LinearInterpolator interpol = new LinearInterpolator();
    //track interpolation
    double[] trackX = new double[track.size()];
    double[] trackY = new double[track.size()];
    double[] index = new double[track.size()];
    for(int i=0;i<track.size();i++){
      index[i] = (double)i;
      trackX[i]=(double)((Cell)track.get(i)).x;
      trackY[i]=(double)((Cell)track.get(i)).y;
    }
//    PolynomialSplineFunction trackInterpolX = interpol.interpolate(index,trackX);
//    PolynomialSplineFunction trackInterpolY = interpol.interpolate(index,trackY);
    
    //Average path interpolation
    double[] avgX = new double[avgTrack.size()];
    double[] avgY = new double[avgTrack.size()];
    for(int i=0;i<avgTrack.size();i++){
      avgX[i]=((Cell)avgTrack.get(i)).x;
      avgY[i]=((Cell)avgTrack.get(i)).y;
    }
//    PolynomialSplineFunction avg = s.interpolate(avgX,avgY);
    
//    int length = avgTrack.size();
    float alh[] = new float[2];
//    float alhMax = 0;
//    float alhMean = 0;
//    for (int i = 0; i < length; i++) {
////      Cell origCell = (Cell) track.get(i + Params.wSize - 1);
//      Cell avgCell = (Cell) avgTrack.get(i);
//      Cell trackCell = new Cell();
//      trackCell.x = (float) trackInterpolX.value((double)avgCell.x);
//      trackCell.y = (float) trackInterpolY.value((double)avgCell.y);
//      float distance = trackCell.distance(avgCell);
//      alhMean += distance;
//      if (distance > alhMax)
//        alhMax = distance;
//    }
//    // Mean value
//    alhMean = alhMean / length;
//    // convert pixels to micrometers
//    alh[0] = alhMean * (float) Params.micronPerPixel;
//    alh[1] = alhMax * (float) Params.micronPerPixel;

    alh[0]=0;
    alh[1]=1;
    return alh;
  }
  
  public void test_alh(){
//    SplineInterpolator l = new SplineInterpolator();
    
    double[] x = {1.0,10.0,20.0};
    double[] y = {1.0,20.0,40.0};
//    PolynomialSplineFunction p = l.interpolate(x,y);
//    IJ.log("x=15; y= "+p.value(15)); 
    
  }

  /******************************************************/

  /**
   * @param track
   *          - a track
   * @param avgTrack
   *          -
   * @return BCF (Hz)
   */
   public float bcf(List track,List avgTrack){
     
     int intersections=0;
     for (int i=0;i<track.size()-1;i++){
       Cell origP0 = (Cell)track.get(i);
       Cell origP1 = (Cell)track.get(i+1);
       Line2D origLine = new Line2D.Float();
       origLine.setLine(origP0.x,origP0.y,origP1.x,origP1.y);
       for (int j=0;j<avgTrack.size()-1;j++){
         Cell avgP0 = (Cell)avgTrack.get(j);
         Cell avgP1 = (Cell)avgTrack.get(j+1);
         Line2D avgLine = new Line2D.Float();
         avgLine.setLine(avgP0.x,avgP0.y,avgP1.x,avgP1.y);
         boolean intersection = origLine.intersectsLine(avgLine);
         if(intersection){
           intersections++;
           break; 
         }
       }
     }
     int length = avgTrack.size();
     float bcf_value = (float)intersections*Params.frameRate/(float)(length-1);
     return bcf_value;
   }

   public void test_bcf(){
     
     List track = new ArrayList();
     int N = 10;
     for(int i=0;i<N;i++){
       Cell c = new Cell();
       c.x=i;
       c.y=2*(i%2)-1;
       IJ.log(c.x +"  "+c.y);
       track.add(c);
     }
     IJ.log("----------------------------");
     IJ.log("----------------------------");
     SignalProcessing s = new SignalProcessing();
     List avgTrack = s.movingAverage(track,3);
     
     for(int i=0;i<avgTrack.size();i++){
       Cell c = (Cell) avgTrack.get(i);
       System.out.println(c.x +"  "+c.y);
       IJ.log(c.x +"  "+c.y);
     }
     IJ.log("----------------------------");
     IJ.log("----------------------------");
     int intersections=0;
     for (int i=0;i<track.size()-1;i++){
       Cell origP0 = (Cell)track.get(i);
       Cell origP1 = (Cell)track.get(i+1);
       Line2D origLine = new Line2D.Float();
       origLine.setLine(origP0.x,origP0.y,origP1.x,origP1.y);
       for (int j=0;j<avgTrack.size()-1;j++){
         Cell avgP0 = (Cell)avgTrack.get(j);
         Cell avgP1 = (Cell)avgTrack.get(j+1);
         Line2D avgLine = new Line2D.Float();
         avgLine.setLine(avgP0.x,avgP0.y,avgP1.x,avgP1.y);
         boolean intersection = origLine.intersectsLine(avgLine);
         if(intersection){
           intersections++;
           break; 
         }
       }
     }
     int length = avgTrack.size();
     float bcf_value = (float)intersections*Params.frameRate/(float)(length-1);
     IJ.log("BCF: "+bcf_value);
     IJ.log("Frame rate: "+Params.frameRate);
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
      float diffY = -(newCell.y - oldCell.y); //it is the negative because the java coordinate system
      double angle = (360 + Math.atan2(diffY, diffX) * 180 / Math.PI) % (360); // Absolute angle
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
