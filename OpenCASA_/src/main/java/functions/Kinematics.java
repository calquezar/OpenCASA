package functions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SList;
import data.Spermatozoon;

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

    int length = avgTrack.size();
    float alh[] = new float[2];
    float alhMax = 0;
    float alhMean = 0;
    for (int i = 0; i < length; i++) {
      Spermatozoon origSpermatozoon = (Spermatozoon) track.get(i + Params.wSize / 2 - 1);
      Spermatozoon avgSpermatozoon = (Spermatozoon) avgTrack.get(i);
      float distance = origSpermatozoon.distance(avgSpermatozoon);
      alhMean += distance;
      if (distance > alhMax)
        alhMax = distance;
    }
    // Mean value
    alhMean = alhMean / length;
    // convert pixels to micrometers
    alh[0] = alhMean * (float) Params.micronPerPixel;
    alh[1] = alhMax * (float) Params.micronPerPixel;

    return alh;
  }

  /******************************************************/

  /**
   * @param track
   *          - a track
   * @param avgTrack
   *          -
   * @return BCF (Hz)
   */
  // public float bcf(List track,List avgTrack){
  //
  // int length = avgTrack.size();
  // int intersections=0;
  // // bcf_shift equal to 1 is not enougth to catch all beat-cross
  // for (int i=1;i<length;i=i+1+Params.bcf_shift){
  // Spermatozoon origP0 =
  // (Spermatozoon)track.get(i-Params.bcf_shift+Params.wSize/2-1);
  // Spermatozoon origP1 = (Spermatozoon)track.get(i+Params.wSize/2-1);
  // Spermatozoon avgP0 = (Spermatozoon)avgTrack.get(i-Params.bcf_shift);
  // Spermatozoon avgP1 = (Spermatozoon)avgTrack.get(i);
  // Line2D origLine = new Line2D.Float();
  // origLine.setLine(origP0.x,origP0.y,origP1.x,origP1.y);
  // Line2D avgLine = new Line2D.Float();
  // avgLine.setLine(avgP0.x,avgP0.y,avgP1.x,avgP1.y);
  //
  // boolean intersection = origLine.intersectsLine(avgLine);
  // if(intersection)
  // intersections++;
  // }
  // float bcf_value = (float)intersections*Params.frameRate/(float)length;
  //
  // return bcf_value;
  // }
  /**
   * @param track
   * @param avgTrack
   * @return
   */
  public float bcf(List track, List avgTrack) {

    int nAvgPoints = avgTrack.size();
    float[] dists = new float[nAvgPoints];
    int[] xPoints = new int[nAvgPoints];
    for (int i = 0; i < nAvgPoints; i++) {
      Spermatozoon origS = (Spermatozoon) track.get(i + Params.wSize / 2 - 1);
      Spermatozoon avgS = (Spermatozoon) avgTrack.get(i);
      dists[i] = origS.distance(avgS);
    }
    SignalProcessing sp = new SignalProcessing();
    dists = sp.movingAverage(dists, 2);
    int intersections = countLocalMaximas(dists);
    float bcf_value = (float) intersections * Params.frameRate / (float) nAvgPoints;
    return bcf_value;
  }

  /**
   * @param points
   * @return
   */
  int countLocalMaximas(float[] points) {
    int nPoints = points.length;
    int count = 0;
    for (int i = 2; i < nPoints; i++) {
      float x0 = points[i - 2];
      float x1 = points[i - 1];
      float x2 = points[i];
      if (((x1 > x0) & (x1 > x2)) || ((x1 < x0) & (x1 < x2)))
        count++;
    }
    return count;
  }

  /******************************************************/
  /**
   * @param track
   * @return
   */
  public String getVelocityTrackType(List track) {

    SignalProcessing sp = new SignalProcessing();
    List avgTrack = sp.movingAverage(track);
    float vap = vcl(avgTrack);
    if ((vsl(track) < Params.vclLowerTh) || (vcl(track) < Params.vclLowerTh) || (vap < Params.vclLowerTh))
      return "Slow";
    else if ((vsl(track) > Params.vclUpperTh) || (vcl(track) > Params.vclUpperTh) || (vap > Params.vclUpperTh))
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

    int length = track.size();
    ListIterator jT = track.listIterator();
    Spermatozoon oldSpermatozoon = (Spermatozoon) jT.next();
    float totalDegrees = 0;
    for (int i = 1; i < length; i++) {
      Spermatozoon newSpermatozoon = (Spermatozoon) track.get(i);
      float diffX = newSpermatozoon.x - oldSpermatozoon.x;
      float diffY = newSpermatozoon.y - oldSpermatozoon.y;
      double angle = (2 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI);
      totalDegrees += angle;
      oldSpermatozoon = newSpermatozoon;
    }
    // mean angle
    float meanAngle = totalDegrees / (length - 1);
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
    Spermatozoon firstSpermatozoon = (Spermatozoon) track.get(0);
    Spermatozoon lastSpermatozoon = (Spermatozoon) track.get(nPoints - 1);
    float distance = lastSpermatozoon.distance(firstSpermatozoon);
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
  public int[] motilityTest(SList theTracks) {
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
    Spermatozoon oldSpermatozoon = (Spermatozoon) jT.next();
    float distance = 0;
    for (; jT.hasNext();) {
      Spermatozoon newSpermatozoon = (Spermatozoon) jT.next();
      distance += newSpermatozoon.distance(oldSpermatozoon);
      oldSpermatozoon = newSpermatozoon;
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
    Spermatozoon first = (Spermatozoon) track.get(0);
    Spermatozoon last = (Spermatozoon) track.get(length - 1);
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
