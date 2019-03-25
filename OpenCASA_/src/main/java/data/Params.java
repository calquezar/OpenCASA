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
import java.util.prefs.Preferences;

import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class Params {

  /**   */
  public static float angleAmplitude = 60;
  /**
   * This parameter is used to analyze the directionality angle between instant
   * t and instant (t+angleDelta).
   */
  public static int angleDelta = 5;
  /** Angles used to classify chemotactic trajectories */
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
  public static int minTrackLength = 10;
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
  public static boolean saveVideo = false;
  /** Parameter used to determine progressive motility sperm */
  public static float strProgressMotility = 85;
  public static float vapProgressMotility = 120;
  /**   */
  public static float vclLowerTh = 25;
  /** Motility filter for motile and non motile sperm */
  public static float vclMin = 10;
  /**   */
  public static float vclUpperTh = 95;
  public static String vclSlowColor = "White";
  public static String vclNormalColor = "Yellow";
  public static String vclFastColor = "Red";
  /** Window size for moving average method (um) */
  public static int wSize = 5;
  
  public static float firstFrame = 0; //in seconds
  public static float lastFrame = -1; //in seconds
  
  
  public static void printParams(){
    System.out.println("---------- PARAMETERS ---------");
    System.out.println("Params.minSize: "+Params.minSize);
    System.out.println("Params.maxSize: "+Params.maxSize);
    System.out.println("Params.minTrackLength: "+Params.minTrackLength);
    System.out.println("Params.maxDisplacement: "+Params.maxDisplacement);
    System.out.println("Params.wSize: "+Params.wSize);
    System.out.println("Params.vclMin: "+Params.vclMin);
    System.out.println("Params.vclLowerTh: "+Params.vclLowerTh);
    System.out.println("Params.vclUpperTh: "+Params.vclUpperTh);
    System.out.println("Params.strProgressMotility: "+Params.strProgressMotility);
    System.out.println("Params.vapProgressMotility: "+Params.vapProgressMotility);   
    System.out.println("Params.angleDelta: "+Params.angleDelta);
    System.out.println("Params.angleDirection: "+Params.angleDirection);
    System.out.println("Params.angleAmplitude: "+Params.angleAmplitude);
    System.out.println("Params.compareOppositeDirections: "+Params.compareOppositeDirections);
    System.out.println("Params.printXY: "+Params.printXY);
    System.out.println("Params.saveVideo: "+Params.saveVideo);
    System.out.println("Params.frameRate: "+Params.frameRate);
    System.out.println("Params.micronPerPixel: "+Params.micronPerPixel);
    System.out.println("Params.NUMSAMPLES: "+Params.NUMSAMPLES);
    System.out.println("Params.firstFrame: "+Params.firstFrame);
    System.out.println("Params.lastFrame: "+Params.lastFrame);
    
  }
}
