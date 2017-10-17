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
public class SignalProcessing {

  /******************************************************/
  /**
   * Fuction to calculate the average path of all tracks using a moving average
   * filter
   * 
   * @param theTracks
   *          2D-ArrayList with all the tracks
   * @return 2D-ArrayList with the averaged tracks
   */
  public SList averageTracks(SList theTracks) {

    SList avgTracks = new SList();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      List avgTrack = movingAverage(aTrack);
      avgTracks.add(avgTrack);
    }
    return avgTracks;
  }

  /******************************************************/
  /**
   * Fuction to decimate a track
   * 
   * @param track
   *          - a track
   * @param factor
   *          - decimation factor
   * @return Decimated track
   */
  public List decimateTrack(List track, int factor) {
    List decimatedTrack = new ArrayList();
    for (ListIterator iT = track.listIterator(); iT.hasNext();) {
      Spermatozoon p = (Spermatozoon) iT.next();
      decimatedTrack.add(p);
      for (int i = 1; i < factor; i++) {
        if (iT.hasNext())
          p = (Spermatozoon) iT.next();
      }
    }
    return decimatedTrack;
  }

  /******************************************************/
  /**
   * Function to decimate all tracks
   * 
   * @param theTracks - 2D-ArrayList with all the tracks
   * @param factor - decimation factor
   * @return 2D-ArrayList with all the tracks decimated
   */
  public List decimateTracks(List theTracks, int factor) {
    List decimatedTracks = new ArrayList();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      decimatedTracks.add(decimateTrack(aTrack, factor));
    }
    return decimatedTracks;
  }

  /******************************************************/
  /**
   * @param theTracks - 2D-ArrayList with all the tracks
   * @return 2D-ArrayList with all the tracks that have passed the filter
   */
  public SList filterTracksByLength(SList theTracks) {
    SList filteredTracks = new SList();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      if (aTrack.size() >= Params.minTrackLength)
        filteredTracks.add(aTrack);
    }
    return filteredTracks;
  }

  /******************************************************/
  /**
   * @param theTracks - 2D-ArrayList with all the tracks
   * @return 2D-ArrayList with all the tracks that have passed the filter
   */
  public SList filterTracksByMotility(SList theTracks) {
    SList filteredTracks = new SList();
    Kinematics K = new Kinematics();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      if (K.motilityTest(aTrack))
        filteredTracks.add(aTrack);
    }
    return filteredTracks;
  }

  /**
   * @param points
   * @param wSize
   * @return
   */
  public float[] movingAverage(float[] points, int wSize) {
    int nPoints = points.length;
    int count = 0;
    float[] avgPoints = new float[nPoints - wSize + 1];
    for (int i = wSize - 1; i < nPoints; i++) {
      for (int k = wSize - 1; k >= 0; k--) {
        avgPoints[i - wSize + 1] += points[i - k];
      }
      avgPoints[i - wSize + 1] /= (float) wSize;
    }
    return avgPoints;
  }

  /**
   * @param track
   * @return
   */
  public List movingAverage(List track) {
    return movingAverage(track, Params.wSize);
  }

  /******************************************************/
  /**
   * Function to calculate the average path of a track using a moving average
   * filter
   * 
   * @param track
   *          Array list that stores one track
   * @param wSize
   * @return ArrayList with the averaged track
   */
  public List movingAverage(List track, int wSize) {
    int nPoints = track.size();
    List avgTrack = new ArrayList();
    for (int j = wSize - 1; j < nPoints; j++) {
      int avgX = 0;
      int avgY = 0;
      for (int k = wSize - 1; k >= 0; k--) {
        Spermatozoon aSpermatozoon = (Spermatozoon) track.get(j - k);
        avgX += (int) aSpermatozoon.x;
        avgY += (int) aSpermatozoon.y;
      }
      avgX = avgX / wSize;
      avgY = avgY / wSize;
      Spermatozoon newSpermatozoon = new Spermatozoon();
      newSpermatozoon.x = (float) avgX;
      newSpermatozoon.y = (float) avgY;
      avgTrack.add(newSpermatozoon);
    }
    return avgTrack;
  }
}
