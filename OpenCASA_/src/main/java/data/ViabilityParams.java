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

import java.io.File;
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
public class ViabilityParams {

  /**   */
  public static String date = "";
  /**   */
  public static String genericField = "";
  /**   */
  public static String male = "";
  /** maximum sperm size */
  public static float maxSize = 100;
  /** Microns per pixel */
  public static double micronPerPixel = 0.481;
  /** minimum sperm size */
  public static float minSize = 10;
  /**   */
  public static double pixelHeight = 1.0;
  /**   */
  public static double pixelWidth = 1.0;
  /**   */
  private static Preferences prefs;

  private static void setDefault(){
    ViabilityParams.date = "";
    ViabilityParams.genericField = "";
    ViabilityParams.male = "";
    ViabilityParams.maxSize = 100;
    ViabilityParams.micronPerPixel = 0.481;
    ViabilityParams.minSize = 10;
    ViabilityParams.pixelHeight = 1.0;
    ViabilityParams.pixelWidth = 1.0;
  }
  /**   */
  public static void resetParams() {

    ViabilityParams.setDefault();
    prefs = Preferences.userNodeForPackage(ViabilityParams.class);    
    try {
      prefs.clear();
    } catch (BackingStoreException e1) {
      e1.printStackTrace();
    }      
    try {
      String os = System.getProperty("os.name").toLowerCase();
      String fSeparator = "";
      if(os.indexOf("win") >= 0)
        fSeparator = File.separator+File.separator;
      else
        fSeparator = File.separator;
      String settingsPath = System.getProperty("user.dir") + fSeparator + "settings.config";
      FileInputStream streamIn = new FileInputStream(settingsPath);
      ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
      Preferences.importPreferences(objectinputstream);
    } catch (Exception e) {
      // IJ.handleException(e);
      // System.out.println("Fallo de lectura");
    }
    ViabilityParams.prefs = Preferences.userNodeForPackage(ViabilityParams.class);
    ViabilityParams.minSize = ViabilityParams.prefs.getFloat("Via_minSize", ViabilityParams.minSize);
    ViabilityParams.maxSize = ViabilityParams.prefs.getFloat("Via_maxSize", ViabilityParams.maxSize);
    ViabilityParams.micronPerPixel = ViabilityParams.prefs.getDouble("Via_micronPerPixel", ViabilityParams.micronPerPixel);
  }

  /**
   * 
   */
  public static void saveParams() {

    ViabilityParams.prefs = Preferences.userNodeForPackage(ViabilityParams.class);
    ViabilityParams.prefs.putFloat("Via_minSize", ViabilityParams.minSize);
    ViabilityParams.prefs.putFloat("Via_maxSize", ViabilityParams.maxSize);
    ViabilityParams.prefs.putDouble("Via_micronPerPixel", ViabilityParams.micronPerPixel);

    try {
      String os = System.getProperty("os.name").toLowerCase();
      String fSeparator = "";
      if(os.indexOf("win") >= 0)
        fSeparator = File.separator+File.separator;
      else
        fSeparator = File.separator;
      String settingsPath = System.getProperty("user.dir") + fSeparator + "settings.config";
      FileOutputStream fos = new FileOutputStream(settingsPath);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      ViabilityParams.prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      IJ.handleException(e);
      // e.printStackTrace();
    }
  }
  
  public static void setGlobalParams(){
    Params.minSize = ViabilityParams.minSize;
    Params.maxSize = ViabilityParams.maxSize;
    Params.micronPerPixel = ViabilityParams.micronPerPixel;
    Params.date = ViabilityParams.date;
    Params.male = ViabilityParams.male;
    Params.genericField = ViabilityParams.genericField;
  }
  
  public static void printParams(){
    System.out.println("---------- VIABILITY PARAMETERS ---------");
    System.out.println("ViabilityParams.minSize: "+ViabilityParams.minSize);
    System.out.println("ViabilityParams.maxSize: "+ViabilityParams.maxSize);
    System.out.println("ViabilityParams.micronPerPixel: "+ViabilityParams.micronPerPixel);  
  }
}
