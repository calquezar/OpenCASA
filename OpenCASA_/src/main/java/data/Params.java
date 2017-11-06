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
  public static float   angleAmplitude       = 90;
  /**
   * This parameter is used to analyze the directionality angle between instant
   * t and instant (t+angleDelta).
   */
  public static int     angleDelta           = 4;
  /** Angles used to clasify chemotactic trajectories */
  public static float   angleDirection       = 0;
  /** parameters used to compute BCF (equivalent to angleDelta) */
//  public static int     bcf_shift            = 0;
  /**   */
  public static float   borderSize           = 20;
  /** */
  public static boolean compareOppositeDirections = false;
  /**   */
  public static String  date                 = "";
  /** Draw original trajectories over the ImagePlus */
  public static boolean drawAvgTrajectories  = true;
  /** Draw original trajectories over the ImagePlus */
  public static boolean drawOrigTrajectories = true;
  /** frame rate */
  public static float   frameRate            = 100;
  /**   */
  public static String  genericField         = "";  
  /**   */
  public static String  male                 = "";
  /**
   * maximum displacement of one spermatozoon between consecutive frames (um)
   */
  public static float   maxDisplacement      = 10;   // um
  /** Used to calculate OR ratios */
  public static int     MAXINSTANGLES        = 20000;
  /** maximum sperm size */
  public static float   maxSize              = 400;
  // 10x ==> 0.58
  // 40x ==> 0.1455
  /** Microns per pixel */
  public static double       micronPerPixel   = 1;    // 0.58; 10x ISAS
  /** minimum sperm size */
  public static float        minSize          = 40;
  /** minimum length of sperm track (in frames) */
  public static int          minTrackLength   = 15;
  /**   */
  public static int          NUMSAMPLES       = 100;
  /**   */
  public static double       pixelHeight      = 1.0;
  /**   */
  public static double       pixelWidth       = 1.0;
  /**   */
  private static Preferences prefs;
  /**
   * if true, print the xy co-ordinates for all tracks as tsv (tab separated
   * values).
   */
  public static boolean      printXY          = false;
  /** Parameter used to determine progressive motility sperm */
  public static float        progressMotility = 80;
  /**   */
  public static float        vclLowerTh       = 45;
  /** Motility filter for motile and non motile sperm */
  public static float        vclMin           = 70;
  /**   */
  public static float        vclUpperTh       = 75;
  /** Window size for moving average method (um) */
  public static int          wSize            = 9;

  /**   */
  public static void resetParams() {

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
    compareOppositeDirections = prefs.getBoolean("compareOppositeDirections", compareOppositeDirections);
    printXY = prefs.getBoolean("printXY", printXY);
    frameRate = prefs.getFloat("frameRate", frameRate);
//    male = prefs.get("male", male);
//    date = prefs.get("date", date);
//    genericField = prefs.get("genericField", genericField);
//    bcf_shift = prefs.getInt("bcf_shift", bcf_shift);
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
    prefs.putBoolean("compareOppositeDirections", compareOppositeDirections);
    prefs.putBoolean("printXY", printXY);
    prefs.putFloat("frameRate", frameRate);
//    prefs.put("male", male);
//    prefs.put("date", date);
//    prefs.put("genericField", genericField);
//    prefs.putInt("bcf_shift", bcf_shift);
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
