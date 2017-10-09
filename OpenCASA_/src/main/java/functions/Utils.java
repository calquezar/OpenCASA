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
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import data.Spermatozoon;
import data.Trial;
import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public abstract class Utils {

  /**
   * @param orig
   * @return
   */
  public static int[] convertLongArrayToInt(long[] orig) {
    int[] arrayInt = new int[orig.length];
    for (int i = 0; i < orig.length; i++)
      arrayInt[i] = (int) orig[i];
    return arrayInt;
  }

  /**
   * @return
   */
  public static String getAbsoluteFileName() {
    JFileChooser chooser = new JFileChooser();
    // chooser.setCurrentDirectory(new
    // java.io.File("C:\\Users\\Carlos\\Documents\\Vet - Bioquimica\\1 -
    // Zaragoza\\data"));
    chooser.setDialogTitle("Select a folder...");
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      // System.out.println("Directory: "+chooser.getSelectedFile());
      File folder = chooser.getSelectedFile();
      return folder.getAbsolutePath();
    }
    return null;
  }

  /**
   * @param path
   * @return
   */
  public static String getFileName(String path) {
    String[] parts = path.split("\\\\");
    return parts[parts.length - 1];
  }

  /**
   * @return
   */
  public static String[] getFileNames() {
    JFileChooser chooser = new JFileChooser();
    // chooser.setCurrentDirectory(new
    // java.io.File("C:\\Users\\Carlos\\Documents\\Vet - Bioquimica\\1 -
    // Zaragoza\\data"));
    chooser.setDialogTitle("Select a folder...");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      // System.out.println("Directory: "+chooser.getSelectedFile());
      File folder = chooser.getSelectedFile();
      File[] listOfFiles = folder.listFiles();
      String[] listOfNames = new String[listOfFiles.length];
      for (int i = 0; i < listOfFiles.length; i++)
        listOfNames[i] = folder.getAbsolutePath() + "\\" + listOfFiles[i].getName();
      return listOfNames;
    }
    return null;
  }

  /**
   * @param dir
   * @return
   */
  public static String[] getFileNames(String dir) {

    File folder = new File(dir);
    File[] listOfFiles = folder.listFiles();
    String[] listOfNames = new String[listOfFiles.length];
    for (int i = 0; i < listOfFiles.length; i++)
      listOfNames[i] = folder.getAbsolutePath() + "\\" + listOfFiles[i].getName();
    return listOfNames;
  }

  /**
   * @param path
   * @return
   */
  public static String getParentDirectory(String path) {
    String[] parts = path.split("\\\\");
    return parts[parts.length - 2];
  }

  /******************************************************/
  /**
   * @param id
   * @param spermatozoa
   * @return
   */
  public static Spermatozoon getSpermatozoon(String id, List spermatozoa) {
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

  /**
   * @param filename
   * @return
   */
  public static boolean isAVI(String filename) {
    String[] parts = filename.split("\\.");
    if (parts[1].equals("avi"))
      return true;
    else
      return false;
  }

  /******************************************************/
  /**
   * @param theTracks
   *          2D-ArrayList with all the tracks
   * @return String with the results in tsv format (tab separated values)
   */
  public static String printXYCoords(List theTracks) {
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

  /**
   * @return
   */
  public static Map<String, Trial> readTrials() {
    Map<String, Trial> trials = null;
    try {
      String file = Utils.selectFile();
      if (file == null)
        return null;
      FileInputStream streamIn = new FileInputStream(file);
      ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
      trials = (HashMap<String, Trial>) objectinputstream.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return trials;
  }

  /**
   * @param trials
   */
  public static void saveTrials(Map<String, Trial> trials) {

    String filename = "";
    String dir = "";
    JFileChooser c = new JFileChooser();
    int rVal = c.showSaveDialog(null);
    if (rVal == JFileChooser.APPROVE_OPTION) {
      filename = c.getSelectedFile().getName();
      dir = c.getCurrentDirectory().toString();
    }
    System.out.println(dir);
    try {
      // String folder = Utils.selectFolder();
      if (dir == null || dir.equals(""))
        return;
      FileOutputStream fos = new FileOutputStream(dir + "\\" + filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(trials);
      oos.close();
      fos.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * @return
   */
  public static String selectFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a file...");
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      return file.getAbsolutePath();
    }
    return null;
  }

  /**
   * @return
   */
  public static String selectFolder() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a folder...");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      // System.out.println("Directory: "+chooser.getSelectedFile());
      File folder = chooser.getSelectedFile();
      return folder.getAbsolutePath();
    }
    return null;
  }
  
}
