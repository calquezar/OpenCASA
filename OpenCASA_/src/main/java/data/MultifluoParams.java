/*
 *   OpenCASA software v2.0 for video and image analysis
 *   Copyright (C) 2019  Carlos Alquézar
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

import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class MultifluoParams {

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
    MultifluoParams.date = "";
    MultifluoParams.genericField = "";
    MultifluoParams.male = "";
    MultifluoParams.maxSize = 100;
    MultifluoParams.micronPerPixel = 0.081;
    MultifluoParams.minSize = 10;
    MultifluoParams.pixelHeight = 1.0;
    MultifluoParams.pixelWidth = 1.0;
  }
  /**   */
  public static void resetParams() {

    MultifluoParams.setDefault();
    prefs = Preferences.userNodeForPackage(MultifluoParams.class);
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

    MultifluoParams.prefs = Preferences.userNodeForPackage(MultifluoParams.class);
    MultifluoParams.minSize = MultifluoParams.prefs.getFloat("Multifluo_minSize", MultifluoParams.minSize);
    MultifluoParams.maxSize = MultifluoParams.prefs.getFloat("Multifluo_maxSize", MultifluoParams.maxSize);
    MultifluoParams.micronPerPixel = MultifluoParams.prefs.getDouble("Multifluo_micronPerPixel", MultifluoParams.micronPerPixel);
    MultifluoParams.saveParams();
  }

  /**
   * 
   */
  public static void saveParams() {

    MultifluoParams.prefs = Preferences.userNodeForPackage(MultifluoParams.class);
    MultifluoParams.prefs.putFloat("Multifluo_minSize", MultifluoParams.minSize);
    MultifluoParams.prefs.putFloat("Multifluo_maxSize", MultifluoParams.maxSize);
    MultifluoParams.prefs.putDouble("Multifluo_micronPerPixel", MultifluoParams.micronPerPixel);

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
      MultifluoParams.prefs.exportSubtree(oos);
      oos.close();
      fos.close();
    } catch (Exception e) {
      IJ.handleException(e);
      // e.printStackTrace();
    }
  }
  
  public static void setGlobalParams(){
    Params.minSize = MultifluoParams.minSize;
    Params.maxSize = MultifluoParams.maxSize;
    Params.micronPerPixel = MultifluoParams.micronPerPixel;  
    Params.date = MultifluoParams.date;
    Params.male = MultifluoParams.male;
    Params.genericField = MultifluoParams.genericField;
  }
  
  public static void printParams(){
    System.out.println("---------- MULTIFLUO PARAMETERS ---------");
    System.out.println("MultifluoParams.minSize: "+MultifluoParams.minSize);
    System.out.println("MultifluoParams.maxSize: "+MultifluoParams.maxSize);
    System.out.println("MultifluoParams.micronPerPixel: "+MultifluoParams.micronPerPixel);  
  }
}
