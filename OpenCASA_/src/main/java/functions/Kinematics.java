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
import java.awt.geom.Point2D;
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
  
  private enum Orientation {VERTICAL, HORIZONTAL, OBLIQUE, NULL}
  
  
  /******************************************************/
  /**
   * @param track
   *          - a track
   * @param avgTrack
   *          -
   * @return ALH (mean and max) (um)
   */
//  public float[] alh(List track, List avgTrack) {
//
////    LinearInterpolator interpol = new LinearInterpolator();
//    //track interpolation
//    double[] trackX = new double[track.size()];
//    double[] trackY = new double[track.size()];
//    double[] index = new double[track.size()];
//    for(int i=0;i<track.size();i++){
//      index[i] = (double)i;
//      trackX[i]=(double)((Cell)track.get(i)).x;
//      trackY[i]=(double)((Cell)track.get(i)).y;
//    }
////    PolynomialSplineFunction trackInterpolX = interpol.interpolate(index,trackX);
////    PolynomialSplineFunction trackInterpolY = interpol.interpolate(index,trackY);
//    
//    //Average path interpolation
//    double[] avgX = new double[avgTrack.size()];
//    double[] avgY = new double[avgTrack.size()];
//    for(int i=0;i<avgTrack.size();i++){
//      avgX[i]=((Cell)avgTrack.get(i)).x;
//      avgY[i]=((Cell)avgTrack.get(i)).y;
//    }
////    PolynomialSplineFunction avg = s.interpolate(avgX,avgY);
//    
////    int length = avgTrack.size();
//    float alh[] = new float[2];
////    float alhMax = 0;
////    float alhMean = 0;
////    for (int i = 0; i < length; i++) {
//////      Cell origCell = (Cell) track.get(i + Params.wSize - 1);
////      Cell avgCell = (Cell) avgTrack.get(i);
////      Cell trackCell = new Cell();
////      trackCell.x = (float) trackInterpolX.value((double)avgCell.x);
////      trackCell.y = (float) trackInterpolY.value((double)avgCell.y);
////      float distance = trackCell.distance(avgCell);
////      alhMean += distance;
////      if (distance > alhMax)
////        alhMax = distance;
////    }
////    // Mean value
////    alhMean = alhMean / length;
////    // convert pixels to micrometers
////    alh[0] = alhMean * (float) Params.micronPerPixel;
////    alh[1] = alhMax * (float) Params.micronPerPixel;
//
//    alh[0]=0;
//    alh[1]=1;
//    return alh;
//  }
  
  /**
   * This function checks if the point(x,y) is in the range [s2.x-s1.x,s2.y-s1.y]
   * beeing s1-s2 a segment
   */
  private boolean isInTheSegmentRange(double x, double y, double[] s1, double[] s2){
    
    double minX = Math.min(s1[0], s2[0]);
    double minY = Math.min(s1[1], s2[1]);
    double maxX = Math.max(s1[0], s2[0]);
    double maxY = Math.max(s1[1], s2[1]);
    
    if((x>=minX && x<=maxX)
        && (y>=minY && y<=maxY))
      return true;
    else
      return false;
  }

  /**
   * This function returns the intersection point between the segment {p1=>p2} and the
   * perpendicular straight line of the segment {q1=>q2} that cross it in the middle point of the segment
   */
  private double[] perpendicularIntersection(double[] p1, double[] p2, double[] q1, double[] q2){
   
    Orientation segmentP_Orientation;
    Orientation segmentQ_Orientation;
    //vp = directional vector of the segment
    double[] vp = new double[2];
    vp[0] = p2[0]-p1[0];
    vp[1] = p2[1]-p1[1];
    // check orientation of the vector
    if(vp[0]==0){ //dx=0
      if(vp[1]!=0)
        segmentP_Orientation = Orientation.VERTICAL;
      else{
        segmentP_Orientation = Orientation.NULL; //the segment is actually a point
        return null;
      }
    }else{//dx!=0
      if(vp[1]==0)
        segmentP_Orientation = Orientation.HORIZONTAL;
      else
        segmentP_Orientation = Orientation.OBLIQUE;
    }
    
    //vq = directional vector of the segment {q1=>q2}
    double[] vq = new double[2];
    vq[0] = q2[0]-q1[0];
    vq[1] = q2[1]-q1[1];
    // check orientation of the vector
    if(vq[0]==0){ //dx=0
      if(vq[1]!=0)
        segmentQ_Orientation = Orientation.VERTICAL;
      else{
        segmentQ_Orientation = Orientation.NULL; //the segment is actually a point
        return null;
      }
    }else{//dx!=0
      if(vq[1]==0)
        segmentQ_Orientation = Orientation.HORIZONTAL;
      else
        segmentQ_Orientation = Orientation.OBLIQUE;
    }
    
    //CASO 4
    if(((segmentP_Orientation == Orientation.VERTICAL)&&(segmentQ_Orientation == Orientation.HORIZONTAL))
      || ((segmentP_Orientation == Orientation.HORIZONTAL)&&(segmentQ_Orientation == Orientation.VERTICAL)))
      return null; // the segments are perpendicular, so the straight line perpendicular 
                   // to one segment is parallel to the other
    
    ////We calculate the straight line that contains the segment {p1=>p2}
    double mp=0;
    double np=0;
    if(segmentP_Orientation != Orientation.VERTICAL){
      // we calculate the slope of the straight line
      mp = vp[1]/vp[0];
      //we calculate the independent term of the straight line equation 
      np = p2[1]- mp*p2[0];
    }
    
    //// We calculate the straight line perpendicular to the segment {q1=>q2} in the middle point
    //qm = middle point of the segment1
    double[] qm = new double[2];
    qm[0] = (q1[0]+q2[0])/2;
    qm[1] = (q1[1]+q2[1])/2;
    double mq=0;
    double nq=0;
    
    if(segmentQ_Orientation!=Orientation.HORIZONTAL){// Horizontal because we are calculating 
                                                     // the perpendicular straigh line, i.e. Vertical
                                                     
      // we calculate the slope of th straight line perperndicular to segment1
      mq = -vq[0]/vq[1];
      //we calculate the independent term of the straight line
      nq = qm[1]-mq*qm[0];
    }

    
    if((segmentP_Orientation == Orientation.VERTICAL) && (segmentQ_Orientation == Orientation.OBLIQUE)){//CASO 1
     double xi = p1[0];
     double yi = mq*xi+nq;
     double[] intersection = {xi,yi};
     if(isInTheSegmentRange(xi, yi, p1, p2))
       return intersection;
     else
       return null;
    }else if((segmentP_Orientation == Orientation.OBLIQUE) && (segmentQ_Orientation == Orientation.HORIZONTAL)){//CASO 2
      double xi = qm[0];
      double yi = mp*xi+np;
      double[] intersection = {xi,yi};
      if(isInTheSegmentRange(xi, yi, p1, p2))
        return intersection;
      else
        return null;
    }else if((segmentP_Orientation == Orientation.OBLIQUE) && (segmentQ_Orientation == Orientation.OBLIQUE)){
      if(mp==mq) //CASOS 3 Y 5
        return null; //The lines are parallels
      else{ //CASO 6
        //// we calculate the intersection point between the two straight lines
        double xi = (nq-np)/(mp-mq);
        double yi = mp*xi+np;    
        double[] intersection = {xi,yi};
        
        if(isInTheSegmentRange(xi, yi, p1, p2))
          return intersection;
        else
          return null;
      }
    }
    return null;
  }
  
  public void testPerpendicularIntersection(){
    double[] p1 = {1,1};
    double[] p2 = {3,2};
    double[] q1 = {1,2};
    double[] q2 = {3,7};
    
    double[] i = perpendicularIntersection(q1,q2,p1,p2);
    
    if(i!=null)
      System.out.println("xi: "+i[0]+"; yi: "+i[1]);
    else
      System.out.println("Null");
  }
  
  public float[] alh(List track, List avgTrack){
    float alh[] = new float[2];
    float alhMax = 0;
    float alhMean = 0;
    int nPoints = track.size();
    for (int j = Params.wSize - 1; j < nPoints; j++) {
      double minDist = 999999;
      Cell avgCell = (Cell)avgTrack.get(j-Params.wSize+1);
      for (int k = Params.wSize - 1; k >= 0; k--) {
        Cell aCell = (Cell) track.get(j - k);
        double dist = aCell.distance(avgCell);
        if(dist<minDist)
          minDist=dist;
      }
      alhMean+=minDist/(avgTrack.size());
      if(minDist>alhMax)
        alhMax=(float)minDist;
    }
    alh[0] = alhMean * (float) Params.micronPerPixel;
    alh[1] = alhMax * (float) Params.micronPerPixel;
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
