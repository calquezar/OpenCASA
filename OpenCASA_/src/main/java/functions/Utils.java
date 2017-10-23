package functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import data.Spermatozoon;
import data.Trial;
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
  public Spermatozoon getSpermatozoon(String id, List spermatozoa) {
    Spermatozoon spermatozoon = null;
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon candidate = (Spermatozoon) j.next();
      if (candidate.id.equals(id) && id != "***") {
        spermatozoon = candidate;
        break;
      }
    }
    return spermatozoon;
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
    double x1, y1, x2, y2;
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
      Spermatozoon oldSpermatozoon = (Spermatozoon) jT.next();
      Spermatozoon firstSpermatozoon = new Spermatozoon();
      firstSpermatozoon.copy(oldSpermatozoon);

      // For each instant (Spermatozoon) in the track
      String outputline = "";
      for (; jT.hasNext();) {
        Spermatozoon newSpermatozoon = (Spermatozoon) jT.next();
        xyPts = "\t" + displayTrackNr + "\t" + frame + "\t" + (int) newSpermatozoon.x + "\t" + (int) newSpermatozoon.y;
        frame++;
        oldSpermatozoon = newSpermatozoon;
        outputline += "\n" + line + xyPts;
        line++;
      }
      output += outputline;
    }
    return output;
  }
  
}
