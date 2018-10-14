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
public class MorphometryParams {

  /**   */
  public static String date = "";
  /**   */
  public static String genericField = "";
  /**   */
  public static String male = "";
  /** maximum sperm size */
  public static float maxSize = 100;
  /** Microns per pixel */
  public static double micronPerPixel = 0.081;
  /** minimum sperm size */
  public static float minSize = 10;
  /**   */
  public static double pixelHeight = 1.0;
  /**   */
  public static double pixelWidth = 1.0;
  /**   */
  private static Preferences prefs;

  private static void setDefault(){
    MorphometryParams.date = "";
    MorphometryParams.genericField = "";
    MorphometryParams.male = "";
    MorphometryParams.maxSize = 100;
    MorphometryParams.micronPerPixel = 0.081;
    MorphometryParams.minSize = 10;
    MorphometryParams.pixelHeight = 1.0;
    MorphometryParams.pixelWidth = 1.0;
  }
  /**   */
  public static void resetParams() {

    MorphometryParams.setDefault();
    prefs = Preferences.userNodeForPackage(MorphometryParams.class);
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

    MorphometryParams.prefs = Preferences.userNodeForPackage(MorphometryParams.class);
    MorphometryParams.minSize = MorphometryParams.prefs.getFloat("Morph_minSize", MorphometryParams.minSize);
    MorphometryParams.maxSize = MorphometryParams.prefs.getFloat("Morph_maxSize", MorphometryParams.maxSize);
    MorphometryParams.micronPerPixel = MorphometryParams.prefs.getDouble("Morph_micronPerPixel", MorphometryParams.micronPerPixel);
    MorphometryParams.saveParams();
  }

  /**
   * 
   */
  public static void saveParams() {

    MorphometryParams.prefs = Preferences.userNodeForPackage(MorphometryParams.class);
    MorphometryParams.prefs.putFloat("Morph_minSize", MorphometryParams.minSize);
    MorphometryParams.prefs.putFloat("Morph_maxSize", MorphometryParams.maxSize);
    MorphometryParams.prefs.putDouble("Morph_micronPerPixel", MorphometryParams.micronPerPixel);

    try {
      FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\settings.config");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      MorphometryParams.prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      IJ.handleException(e);
      // e.printStackTrace();
    }
  }
  
  public static void setGlobalParams(){
    Params.minSize = MorphometryParams.minSize;
    Params.maxSize = MorphometryParams.maxSize;
    Params.micronPerPixel = MorphometryParams.micronPerPixel;  
    Params.date = MorphometryParams.date;
    Params.male = MorphometryParams.male;
    Params.genericField = MorphometryParams.genericField;
  }
  
  public static void printParams(){
    System.out.println("---------- MORPHOMETRY PARAMETERS ---------");
    System.out.println("MorphometryParams.minSize: "+MorphometryParams.minSize);
    System.out.println("MorphometryParams.maxSize: "+MorphometryParams.maxSize);
    System.out.println("MorphometryParams.micronPerPixel: "+MorphometryParams.micronPerPixel);  
  }
}
