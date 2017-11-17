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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SerializableList;
import data.Cell;

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
  public SerializableList averageTracks(SerializableList theTracks) {

    SerializableList avgTracks = new SerializableList();
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
      Cell p = (Cell) iT.next();
      decimatedTrack.add(p);
      for (int i = 1; i < factor; i++) {
        if (iT.hasNext())
          p = (Cell) iT.next();
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
  public SerializableList filterTracksByLength(SerializableList theTracks) {
    SerializableList filteredTracks = new SerializableList();
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
  public SerializableList filterTracksByMotility(SerializableList theTracks) {
    SerializableList filteredTracks = new SerializableList();
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
        Cell aCell = (Cell) track.get(j - k);
        avgX += (int) aCell.x;
        avgY += (int) aCell.y;
      }
      avgX = avgX / wSize;
      avgY = avgY / wSize;
      Cell newCell = new Cell();
      newCell.x = (float) avgX;
      newCell.y = (float) avgY;
      avgTrack.add(newCell);
    }
    return avgTrack;
  }
}
