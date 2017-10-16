package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.Preferences;

/**
 * @author Carlos Alquezar
 *
 */
public class Params {

  /**   */
  private static Preferences prefs;
  /**   */
  public static double pixelWidth = 1.0;
  /**   */
  public static double pixelHeight = 1.0;
  /** minimum sperm size */
  public static float minSize = 40;
  /** maximum sperm size */
  public static float maxSize = 400; 
  /**  minimum length of sperm track (in frames) */
  public static int minTrackLength = 15;
  /** maximum displacement of one spermatozoon between 
   *  consecutive frames (um) */
  public static float maxDisplacement = 10; // um
  /** Window size for moving average method (um)  */
  public static int wSize = 9;
  /** Motility filter for motile and non motile sperm */
  public static float vclMin = 70;
  /** This parameter is used to analyze the directionality angle
   *  between instant t and instant (t+angleDelta).*/
  public static int angleDelta = 4;
  /**  Angles used to clasify chemotactic trajectories */
  public static float angleDirection = 0;
  /**   */
  public static float angleAmplitude = 90;
  /** frame rate  */
  public static float frameRate = 100;
  /**  parameters used to compute BCF (equivalent to angleDelta) */
  public static int bcf_shift = 0;
  /** Parameter used to determine progressive motility sperm  */
  public static float progressMotility = 80;
  // 10x ==> 0.58
  // 40x ==> 0.1455
  /**  Microns per pixel  */
  public static double micronPerPixel = 1;// 0.58; 10x ISAS
  /** if true, print the xy co-ordinates for all tracks as tsv (tab separated values). */
  public static boolean printXY = false;
  /** Draw original trajectories over the ImagePlus  */
  public static boolean drawOrigTrajectories = true;
  /**  Draw original trajectories over the ImagePlus */
  public static boolean drawAvgTrajectories = true;
  /** Used to calculate OR ratios */
  public static int MAXINSTANGLES = 20000;
  /**   */
  public static int NUMSAMPLES = 100;
  /**   */
  public static float borderSize = 20;
  /**   */
  public static String selectedFilter = "";
  /**   */
  public static String male = "";
  /**   */
  public static String date = "";
  /**   */
  public static float vclLowerTh = 45;
  /**   */
  public static float vclUpperTh = 75;
  /**   */
  public static void resetParams() {

    // minSize = 20;
    // maxSize = 200;
    // minTrackLength = 20;
    // maxDisplacement = 15;
    // wSize = 5;
    // vclMin = 50;
    // angleDelta = 5;
    // angleDirection = 0;
    // angleAmplitude = 60;
    // frameRate = 100;
    // bcf_shift = 0;
    // progressMotility = 80;
    // micronPerPixel = 0.58;
    // printXY = false;
    // calcMotilityParameters = false;
    // calcMeanMotilityParameters = false;
    // drawOrigTrajectories = true;
    // drawAvgTrajectories = true;

    // // GOAT
    // minSize = 100; //um^2
    // maxSize = 10000;//um^2
    // minTrackLength = 5;
    // maxDisplacement = 10;
    // wSize = 2;
    // vclMin = 1;
    // angleDelta = 1;
    // angleDirection = 0;
    // angleAmplitude = 60;
    // frameRate = 25;
    // bcf_shift = 0;
    // progressMotility = 80;
    // micronPerPixel = 0.58;
    // printXY = false;
    //// calcMotilityParameters = false;
    //// calcMeanMotilityParameters = false;
    // drawOrigTrajectories = true;
    // drawAvgTrajectories = true;

    try {
      FileInputStream streamIn = new FileInputStream(System.getProperty("user.dir") + "\\prefs.config");
      ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
      prefs.importPreferences(objectinputstream);
    } catch (Exception e) {
      System.out.println("Fallo de lectura");
    }
    if (prefs == null)
      prefs = Preferences.userNodeForPackage(Params.class);
    minSize = prefs.getFloat("minSize", minSize);
    maxSize = prefs.getFloat("maxSize", maxSize);
    minTrackLength = prefs.getInt("minTrackLength", minTrackLength);
    maxDisplacement = prefs.getFloat("maxDisplacement", maxDisplacement);
    wSize = prefs.getInt("wSize", wSize);
    vclMin = prefs.getFloat("vclMin", vclMin);
    angleDelta = prefs.getInt("angleDelta", angleDelta);
    angleDirection = prefs.getFloat("angleDirection", angleDirection);
    angleAmplitude = prefs.getFloat("angleAmplitude", angleAmplitude);
    frameRate = prefs.getFloat("frameRate", frameRate);
    bcf_shift = prefs.getInt("bcf_shift", bcf_shift);
    progressMotility = prefs.getFloat("progressMotility", progressMotility);
    micronPerPixel = prefs.getDouble("micronPerPixel", micronPerPixel);
    NUMSAMPLES = prefs.getInt("NUMSAMPLES", NUMSAMPLES);
  }

  /**
   * 
   */
  public static void saveParams() {

    prefs = Preferences.userNodeForPackage(Params.class);
    prefs.putFloat("minSize", minSize);
    prefs.putFloat("maxSize", maxSize);
    prefs.putInt("minTrackLength", minTrackLength);
    prefs.putFloat("maxDisplacement", maxDisplacement);
    prefs.putInt("wSize", wSize);
    prefs.putFloat("vclMin", vclMin);
    prefs.putInt("angleDelta", angleDelta);
    prefs.putFloat("angleDirection", angleDirection);
    prefs.putFloat("angleAmplitude", angleAmplitude);
    prefs.putFloat("frameRate", frameRate);
    prefs.putInt("bcf_shift", bcf_shift);
    prefs.putFloat("progressMotility", progressMotility);
    prefs.putDouble("micronPerPixel", micronPerPixel);
    prefs.putInt("NUMSAMPLES", NUMSAMPLES);

    try {
      FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\prefs.config");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
