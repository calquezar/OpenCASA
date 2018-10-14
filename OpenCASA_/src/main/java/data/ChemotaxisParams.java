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
import java.util.prefs.PreferencesFactory;

import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class ChemotaxisParams {

  /**   */
  public static float angleAmplitude = 60;
  /**
   * This parameter is used to analyze the directionality angle between instant
   * t and instant (t+angleDelta).
   */
  public static int angleDelta = 5;
  /** Angles used to clasify chemotactic trajectories */
  public static float angleDirection = 0;
  /** parameters used to compute BCF (equivalent to angleDelta) */
  // public static int bcf_shift = 0;
  /** */
  public static boolean compareOppositeDirections = false;
  /**   */
  public static String date = "";
  /** frame rate */
  public static float frameRate = 100;
  /**   */
  public static String genericField = "";
  /**   */
  public static String male = "";
  /**
   * maximum displacement of one spermatozoon between consecutive frames (um)
   */
  public static float maxDisplacement = 20; // um
  /** Used to calculate OR ratios */
  public static int MAXINSTANGLES = 20000;
  /** maximum sperm size */
  public static float maxSize = 100;
  /** Microns per pixel */
  public static double micronPerPixel = 0.481; 
  /** minimum sperm size */
  public static float minSize = 10;
  /** minimum length of sperm track (in frames) */
  public static int minTrackLength = 50;
  /**   */
  public static int NUMSAMPLES = 10000;
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
  /** Motility filter for motile and non motile sperm */
  public static float vclMin = 10;
  /** Window size for moving average method (um) */
  public static int wSize = 5;
  
  public static int firstFrame = 1;
  public static int lastFrame = -1;

  private static void setDefault(){
    ChemotaxisParams.angleAmplitude = 60;
    ChemotaxisParams.angleDelta = 5;
    ChemotaxisParams.angleDirection = 0;
    ChemotaxisParams.compareOppositeDirections = false;
    ChemotaxisParams.date = "";
    ChemotaxisParams.frameRate = 100;
    ChemotaxisParams.genericField = "";
    ChemotaxisParams.male = "";
    ChemotaxisParams.maxDisplacement = 20; // um
    ChemotaxisParams.MAXINSTANGLES = 20000;
    ChemotaxisParams.maxSize = 100;
    ChemotaxisParams.micronPerPixel = 0.481; 
    ChemotaxisParams.minSize = 10;
    ChemotaxisParams.minTrackLength = 50;
    ChemotaxisParams.NUMSAMPLES = 10000;
    ChemotaxisParams.pixelHeight = 1.0;
    ChemotaxisParams.pixelWidth = 1.0;
    ChemotaxisParams.printXY = false;
    ChemotaxisParams.vclMin = 10;
    ChemotaxisParams.wSize = 5;
    ChemotaxisParams.firstFrame = 1;
    ChemotaxisParams.lastFrame = -1;    
  }

  public static void resetParams() {
    
    ChemotaxisParams.setDefault();
    prefs = Preferences.userNodeForPackage(ChemotaxisParams.class);    
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
      // System.out.println("Fallo de lectura");
    }
    ChemotaxisParams.prefs = Preferences.userNodeForPackage(ChemotaxisParams.class);
    ChemotaxisParams.minSize = ChemotaxisParams.prefs.getFloat("Chemo_minSize", ChemotaxisParams.minSize);
    ChemotaxisParams.maxSize = ChemotaxisParams.prefs.getFloat("Chemo_maxSize", ChemotaxisParams.maxSize);
    ChemotaxisParams.minTrackLength = ChemotaxisParams.prefs.getInt("Chemo_minTrackLength", ChemotaxisParams.minTrackLength);
    ChemotaxisParams.maxDisplacement = ChemotaxisParams.prefs.getFloat("Chemo_maxDisplacement", ChemotaxisParams.maxDisplacement);
    ChemotaxisParams.wSize = ChemotaxisParams.prefs.getInt("Chemo_wSize", ChemotaxisParams.wSize);
    ChemotaxisParams.vclMin = ChemotaxisParams.prefs.getFloat("Chemo_vclMin", ChemotaxisParams.vclMin);
    ChemotaxisParams.angleDelta = ChemotaxisParams.prefs.getInt("Chemo_angleDelta", ChemotaxisParams.angleDelta);
    ChemotaxisParams.angleDirection = ChemotaxisParams.prefs.getFloat("Chemo_angleDirection", ChemotaxisParams.angleDirection);
    ChemotaxisParams.angleAmplitude = ChemotaxisParams.prefs.getFloat("Chemo_angleAmplitude", ChemotaxisParams.angleAmplitude);
    ChemotaxisParams.compareOppositeDirections = ChemotaxisParams.prefs.getBoolean("Chemo_compareOppositeDirections", ChemotaxisParams.compareOppositeDirections);
    ChemotaxisParams.printXY = ChemotaxisParams.prefs.getBoolean("Chemo_printXY", ChemotaxisParams.printXY);
    ChemotaxisParams.frameRate = ChemotaxisParams.prefs.getFloat("Chemo_frameRate", ChemotaxisParams.frameRate);
    // male = prefs.get("male", male);
    // date = prefs.get("date", date);
    // genericField = prefs.get("genericField", genericField);
    // bcf_shift = prefs.getInt("bcf_shift", bcf_shift);
    ChemotaxisParams.micronPerPixel = ChemotaxisParams.prefs.getDouble("Chemo_micronPerPixel", ChemotaxisParams.micronPerPixel);
    ChemotaxisParams.NUMSAMPLES = ChemotaxisParams.prefs.getInt("Chemo_NUMSAMPLES", ChemotaxisParams.NUMSAMPLES);
    ChemotaxisParams.saveParams();
  }

  /**
   * 
   */
  public static void saveParams() {

    ChemotaxisParams.prefs = Preferences.userNodeForPackage(ChemotaxisParams.class);
    ChemotaxisParams.prefs.putFloat("Chemo_minSize", ChemotaxisParams.minSize);
    ChemotaxisParams.prefs.putFloat("Chemo_maxSize", ChemotaxisParams.maxSize);
    ChemotaxisParams.prefs.putInt("Chemo_minTrackLength", ChemotaxisParams.minTrackLength);
    ChemotaxisParams.prefs.putFloat("Chemo_maxDisplacement", ChemotaxisParams.maxDisplacement);
    ChemotaxisParams.prefs.putInt("Chemo_wSize", ChemotaxisParams.wSize);
    ChemotaxisParams.prefs.putFloat("Chemo_vclMin", ChemotaxisParams.vclMin);
    ChemotaxisParams.prefs.putInt("Chemo_angleDelta", ChemotaxisParams.angleDelta);
    ChemotaxisParams.prefs.putFloat("Chemo_angleDirection", ChemotaxisParams.angleDirection);
    ChemotaxisParams.prefs.putFloat("Chemo_angleAmplitude", ChemotaxisParams.angleAmplitude);
    ChemotaxisParams.prefs.putBoolean("Chemo_compareOppositeDirections", ChemotaxisParams.compareOppositeDirections);
    ChemotaxisParams.prefs.putBoolean("Chemo_printXY", ChemotaxisParams.printXY);
    ChemotaxisParams.prefs.putFloat("Chemo_frameRate", ChemotaxisParams.frameRate);
    ChemotaxisParams.prefs.putDouble("Chemo_micronPerPixel", ChemotaxisParams.micronPerPixel);
    ChemotaxisParams.prefs.putInt("Chemo_NUMSAMPLES", ChemotaxisParams.NUMSAMPLES);

    try {
      FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\settings.config");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      ChemotaxisParams.prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      IJ.handleException(e);
      // e.printStackTrace();
    }
  }
  
  public static void setGlobalParams(){
    
    Params.minSize = ChemotaxisParams.minSize;
    Params.maxSize = ChemotaxisParams.maxSize;
    Params.minTrackLength = ChemotaxisParams.minTrackLength;
    Params.maxDisplacement = ChemotaxisParams.maxDisplacement;
    Params.wSize = ChemotaxisParams.wSize;
    Params.vclMin = ChemotaxisParams.vclMin;
    Params.angleDelta = ChemotaxisParams.angleDelta;
    Params.angleDirection = ChemotaxisParams.angleDirection;
    Params.angleAmplitude = ChemotaxisParams.angleAmplitude;
    Params.compareOppositeDirections = ChemotaxisParams.compareOppositeDirections;
    Params.printXY = ChemotaxisParams.printXY;
    Params.frameRate = ChemotaxisParams.frameRate;
    Params.micronPerPixel = ChemotaxisParams.micronPerPixel;
    Params.NUMSAMPLES = ChemotaxisParams.NUMSAMPLES;
    Params.date = ChemotaxisParams.date;
    Params.male = ChemotaxisParams.male;
    Params.genericField = ChemotaxisParams.genericField;
    Params.firstFrame = ChemotaxisParams.firstFrame;
    Params.lastFrame = ChemotaxisParams.lastFrame;
  }
  
  public static void printParams(){
    System.out.println("---------- CHEMOTAXIS PARAMETERS ---------");
    System.out.println("ChemotaxisParams.minSize: "+ChemotaxisParams.minSize);
    System.out.println("ChemotaxisParams.maxSize: "+ChemotaxisParams.maxSize);
    System.out.println("ChemotaxisParams.minTrackLength: "+ChemotaxisParams.minTrackLength);
    System.out.println("ChemotaxisParams.maxDisplacement: "+ChemotaxisParams.maxDisplacement);
    System.out.println("ChemotaxisParams.wSize: "+ChemotaxisParams.wSize);
    System.out.println("ChemotaxisParams.vclMin: "+ChemotaxisParams.vclMin);
    System.out.println("ChemotaxisParams.angleDelta: "+ChemotaxisParams.angleDelta);
    System.out.println("ChemotaxisParams.angleDirection: "+ChemotaxisParams.angleDirection);
    System.out.println("ChemotaxisParams.angleAmplitude: "+ChemotaxisParams.angleAmplitude);
    System.out.println("ChemotaxisParams.compareOppositeDirections: "+ChemotaxisParams.compareOppositeDirections);
    System.out.println("ChemotaxisParams.printXY: "+ChemotaxisParams.printXY);
    System.out.println("ChemotaxisParams.frameRate: "+ChemotaxisParams.frameRate);
    System.out.println("ChemotaxisParams.micronPerPixel: "+ChemotaxisParams.micronPerPixel);
    System.out.println("ChemotaxisParams.NUMSAMPLES: "+ChemotaxisParams.NUMSAMPLES);
  }
}
