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

import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import data.Cell;
import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class Utils {

  /**
   * 
   * @param options
   * @param question
   * @param title
   * @return
   */
  public int analysisSelectionDialog(Object[] options, String question, String title) {
    int n = JOptionPane.showOptionDialog(null, question, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, // do not use a custom Icon
        options, // the titles of buttons
        options[0]); // default button title
    return n;
  }
  /**
   * @param orig
   * @return
   */
  public int[] convertLongArrayToInt(long[] orig) {
    int[] arrayInt = new int[orig.length];
    for (int i = 0; i < orig.length; i++)
      arrayInt[i] = (int) orig[i];
    return arrayInt;
  }

  /******************************************************/
  /**
   * @param id
   * @param spermatozoa
   * @return
   */
  public Cell getCell(String id, List spermatozoa) {
    Cell cell = null;
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Cell candidate = (Cell) j.next();
      if (candidate.id.equals(id) && id != "***") {
        cell = candidate;
        break;
      }
    }
    return cell;
  }
  
  public int getTrackNr(List track){
	  Cell cell = (Cell) track.get(0);
	  return cell.trackNr;
  }

  /******************************************************/
  /**
   * @param theTracks
   *          2D-ArrayList with all the tracks
   * @return String with the results in tsv format (tab separated values)
   */
  public String printXYCoords(List theTracks) {
    int nTracks = theTracks.size();
    // strings to print out all of the data gathered, point by point
    String xyPts = " ";
    // initialize variables
    int trackNr = 0;
    int displayTrackNr = 0;
    int line = 1;
    String output = "Line" + "\tTrack" + "\tRelative_Frame" + "\tX" + "\tY";
    // loop through all sperm tracks
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      int frame = 0;
      trackNr++;
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Analyzing Tracks...");
      List bTrack = (List) iT.next();
      // keeps track of the current track
      displayTrackNr++;
      ListIterator jT = bTrack.listIterator();
      Cell oldCell = (Cell) jT.next();
      Cell firstCell = new Cell();
      firstCell.copy(oldCell);
      // For each instant (Cell) in the track
      String outputline = "";
      for (; jT.hasNext();) {
        Cell newCell = (Cell) jT.next();
        xyPts = "\t" + displayTrackNr + "\t" + frame + "\t" + (int) newCell.x + "\t" + (int) newCell.y;
        frame++;
        oldCell = newCell;
        outputline += "\n" + line + xyPts;
        line++;
      }
      output += outputline;
    }
    return output;
  }
  
}
