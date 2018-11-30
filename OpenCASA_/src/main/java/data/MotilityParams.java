/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
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

package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class MotilityParams {

  /** parameters used to compute BCF */
  // public static int bcf_shift = 0;
  /**   */
  public static String date = "";
  /** frame rate */
  public static float frameRate = 60;
  /**   */
  public static String genericField = "";
  /**   */
  public static String male = "";
  /**
   * maximum displacement of one spermatozoon between consecutive frames (um)
   */
  public static float maxDisplacement = 20; // um
  /** maximum sperm size */
  public static float maxSize = 100;
  /** Microns per pixel */
  public static double micronPerPixel = 0.481; 
  /** minimum sperm size */
  public static float minSize = 10;
  /** minimum length of sperm track (in frames) */
  public static int minTrackLength = 10;
  /**   */
  public static double pixelHeight = 1.0;
  /**   */
  public static double pixelWidth = 1.0;
  /**   */
  private static Preferences prefs;
  /**
   * if true, print the xy co-ordinates for all tracks as tsv (tab separated
   * values).
   */
  public static boolean printXY = false;
  /** Parameter used to determine progressive motility sperm */
  public static float strProgressMotility = 85;
  public static float vapProgressMotility = 120;
  /**   */
  public static float vclLowerTh = 100;
  /** Motility filter for motile and non motile sperm */
  public static float vclMin = 10;
  /**   */
  public static float vclUpperTh = 200;
  /** Window size for moving average method (um) */
  public static int wSize = 4;
  
  public static float firstFrame = 0; //in seconds
  public static float lastFrame = -1; //in seconds

  
  private static void setDefault(){
    MotilityParams.date = "";
    MotilityParams.frameRate = 60;
    MotilityParams.genericField = "";
    MotilityParams.male = "";
    MotilityParams.maxDisplacement = 20; // um
    MotilityParams.maxSize = 100;
    MotilityParams.micronPerPixel = 0.481; 
    MotilityParams.minSize = 10;
    MotilityParams.minTrackLength = 10;
    MotilityParams.pixelHeight = 1.0;
    MotilityParams.pixelWidth = 1.0;
    MotilityParams.printXY = false;
    MotilityParams.strProgressMotility = 85;
    MotilityParams.vapProgressMotility = 120;
    MotilityParams.vclLowerTh = 100;
    MotilityParams.vclMin = 10;
    MotilityParams.vclUpperTh = 200;
    MotilityParams.wSize = 4; 
    MotilityParams.firstFrame = 0;
    MotilityParams.lastFrame = -1;
  }
  /**   */
  public static void resetParams() {

    MotilityParams.setDefault();
    prefs = Preferences.userNodeForPackage(MotilityParams.class);
    try {
      prefs.clear();
    } catch (BackingStoreException e1) {
      e1.printStackTrace();
    }    
    try {
      FileInputStream streamIn = new FileInputStream(System.getProperty("user.dir") + "\\settings.config");
      ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
      Preferences.importPreferences(objectinputstream);
    } catch (Exception e) {
      // IJ.handleException(e);
      //System.out.println("Fallo de lectura");
    }
    prefs = Preferences.userNodeForPackage(MotilityParams.class);
    MotilityParams.prefs = Preferences.userNodeForPackage(MotilityParams.class);
    MotilityParams.minSize = MotilityParams.prefs.getFloat("Mot_minSize", MotilityParams.minSize);
    MotilityParams.maxSize = MotilityParams.prefs.getFloat("Mot_maxSize", MotilityParams.maxSize);
    MotilityParams.minTrackLength = MotilityParams.prefs.getInt("Mot_minTrackLength", MotilityParams.minTrackLength);
    MotilityParams.maxDisplacement = MotilityParams.prefs.getFloat("Mot_maxDisplacement", MotilityParams.maxDisplacement);
    MotilityParams.wSize = MotilityParams.prefs.getInt("Mot_wSize", MotilityParams.wSize);
    MotilityParams.vclMin = MotilityParams.prefs.getFloat("Mot_vclMin", MotilityParams.vclMin);
    MotilityParams.vclLowerTh = MotilityParams.prefs.getFloat("Mot_vclLowerTh", MotilityParams.vclLowerTh);
    MotilityParams.vclUpperTh = MotilityParams.prefs.getFloat("Mot_vclUpperTh", MotilityParams.vclUpperTh);
    MotilityParams.printXY = MotilityParams.prefs.getBoolean("Mot_printXY", MotilityParams.printXY);
    MotilityParams.frameRate = MotilityParams.prefs.getFloat("Mot_frameRate", MotilityParams.frameRate);
    MotilityParams.strProgressMotility = MotilityParams.prefs.getFloat("Mot_strProgressMotility", MotilityParams.strProgressMotility);
    MotilityParams.vapProgressMotility = MotilityParams.prefs.getFloat("Mot_vapProgressMotility", MotilityParams.vapProgressMotility);
    MotilityParams.micronPerPixel = MotilityParams.prefs.getDouble("Mot_micronPerPixel", MotilityParams.micronPerPixel);
    MotilityParams.saveParams();
  }

  /**
   * 
   */
  public static void saveParams() {

    MotilityParams.prefs = Preferences.userNodeForPackage(MotilityParams.class);
    MotilityParams.prefs.putFloat("Mot_minSize", MotilityParams.minSize);
    MotilityParams.prefs.putFloat("Mot_maxSize", MotilityParams.maxSize);
    MotilityParams.prefs.putInt("Mot_minTrackLength", MotilityParams.minTrackLength);
    MotilityParams.prefs.putFloat("Mot_maxDisplacement", MotilityParams.maxDisplacement);
    MotilityParams.prefs.putInt("Mot_wSize", MotilityParams.wSize);
    MotilityParams.prefs.putFloat("Mot_vclMin", MotilityParams.vclMin);
    MotilityParams.prefs.putFloat("Mot_vclLowerTh", MotilityParams.vclLowerTh);
    MotilityParams.prefs.putFloat("Mot_vclUpperTh", MotilityParams.vclUpperTh);
    MotilityParams.prefs.putBoolean("Mot_printXY", MotilityParams.printXY);
    MotilityParams.prefs.putFloat("Mot_frameRate", MotilityParams.frameRate);
    MotilityParams.prefs.putFloat("Mot_strProgressMotility", MotilityParams.strProgressMotility);
    MotilityParams.prefs.putFloat("Mot_vapProgressMotility", MotilityParams.vapProgressMotility);
    MotilityParams.prefs.putDouble("Mot_micronPerPixel", MotilityParams.micronPerPixel);

    try {
      FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\settings.config");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      MotilityParams.prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      IJ.handleException(e);
      // e.printStackTrace();
    }
  }
  
  public static void setGlobalParams(){
    
    Params.minSize = MotilityParams.minSize;
    Params.maxSize = MotilityParams.maxSize;
    Params.minTrackLength = MotilityParams.minTrackLength;
    Params.maxDisplacement = MotilityParams.maxDisplacement;
    Params.wSize = MotilityParams.wSize;
    Params.vclMin = MotilityParams.vclMin;
    Params.vclLowerTh = MotilityParams.vclLowerTh;
    Params.vclUpperTh = MotilityParams.vclUpperTh;
    Params.strProgressMotility = MotilityParams.strProgressMotility;
    Params.vapProgressMotility = MotilityParams.vapProgressMotility;
    Params.printXY = MotilityParams.printXY;
    Params.frameRate = MotilityParams.frameRate;
    Params.micronPerPixel = MotilityParams.micronPerPixel;
    Params.date = MotilityParams.date;
    Params.male = MotilityParams.male;
    Params.genericField = MotilityParams.genericField;
    Params.firstFrame = MotilityParams.firstFrame;
    Params.lastFrame = MotilityParams.lastFrame;
  }
  
  public static void printParams(){
    System.out.println("---------- MOTILITY PARAMETERS ---------");
    System.out.println("MotilityParams.minSize: "+MotilityParams.minSize);
    System.out.println("MotilityParams.maxSize: "+MotilityParams.maxSize);
    System.out.println("MotilityParams.minTrackLength: "+MotilityParams.minTrackLength);
    System.out.println("MotilityParams.maxDisplacement: "+MotilityParams.maxDisplacement);
    System.out.println("MotilityParams.wSize: "+MotilityParams.wSize);
    System.out.println("MotilityParams.vclMin: "+MotilityParams.vclMin);
    System.out.println("MotilityParams.vclLowerTh: "+MotilityParams.vclLowerTh);
    System.out.println("MotilityParams.vclUpperTh: "+MotilityParams.vclUpperTh);
    System.out.println("MotilityParams.strProgressMotility: "+MotilityParams.strProgressMotility);
    System.out.println("MotilityParams.vapProgressMotility: "+MotilityParams.vapProgressMotility);
    System.out.println("MotilityParams.printXY: "+MotilityParams.printXY);
    System.out.println("MotilityParams.frameRate: "+MotilityParams.frameRate);
    System.out.println("MotilityParams.micronPerPixel: "+MotilityParams.micronPerPixel);
    System.out.println("MotilityParams.firstFrame: "+MotilityParams.firstFrame);
    System.out.println("MotilityParams.lastFrame: "+MotilityParams.lastFrame);
  }
}
