/*
 *   OpenCASA software v2.0 for video and image analysis
 *   Copyright (C) 2019  Jorge Yagüe
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
 * @author Jorge Yagüe
 *
 */
public class AccumulationParams {

	/**   */
	public static String date = "";
	/** frame rate */
	public static float frameRate = 60;
	/** Frame number to interpolate */
	public static int frameInt = 10;
	/** radius to analyze */
	public static int radius = 100;
	/** Opacity */
	public static int opacity = 50;
	/** Window size */
	public static int window = 1;
	/**   */
	public static String genericField = "";
	/**   */
	public static String male = "";
	/** maximum sperm size */
	public static float maxSize = 100;
	/** Microns per pixel */
	public static double micronPerPixel = 1;
	/** minimum sperm size */
	public static float minSize = 10;
	public static boolean maxConstant = false;
	public static int maxConstantV = -1;
	/**   */
	private static Preferences prefs;

	public static float firstFrame = 0; // in seconds
	public static float lastFrame = -1; // in seconds

	private static void setDefault() {
		AccumulationParams.date = "";
		AccumulationParams.genericField = "";
		AccumulationParams.male = "";
		AccumulationParams.maxSize = 100;
		AccumulationParams.micronPerPixel = 1;
		AccumulationParams.minSize = 10;
		AccumulationParams.frameRate = 60;
		AccumulationParams.frameInt = 10;
		AccumulationParams.radius = 100;
		AccumulationParams.opacity = 50;
		AccumulationParams.window = 1;
		AccumulationParams.firstFrame = 0;
		AccumulationParams.lastFrame = -1;
		AccumulationParams.maxConstant = false;
		AccumulationParams.maxConstantV = -1;
	}

	/**   */
	public static void resetParams() {

		AccumulationParams.setDefault();
		prefs = Preferences.userNodeForPackage(AccumulationParams.class);
		try {
			prefs.clear();
		} catch (BackingStoreException e1) {
			e1.printStackTrace();
		}
		try {
			FileInputStream streamIn = new FileInputStream(
					System.getProperty("user.dir") + File.separator + "settings.config");
			ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
			Preferences.importPreferences(objectinputstream);
		} catch (Exception e) {
			// IJ.handleException(e);
			// System.out.println("Fallo de lectura");
		}
		prefs = Preferences.userNodeForPackage(AccumulationParams.class);
		AccumulationParams.minSize = AccumulationParams.prefs.getFloat("Acc_minSize", AccumulationParams.minSize);
		AccumulationParams.maxSize = AccumulationParams.prefs.getFloat("Acc_maxSize", AccumulationParams.maxSize);
		AccumulationParams.micronPerPixel = AccumulationParams.prefs.getDouble("Acc_micronPerPixel",
				AccumulationParams.micronPerPixel);
		AccumulationParams.frameRate = AccumulationParams.prefs.getFloat("Acc_frameRate", AccumulationParams.frameRate);
		AccumulationParams.frameInt = AccumulationParams.prefs.getInt("Acc_frameInt", AccumulationParams.frameInt);
		AccumulationParams.radius = AccumulationParams.prefs.getInt("Acc_radius", AccumulationParams.radius);
		AccumulationParams.opacity = AccumulationParams.prefs.getInt("Acc_opacity", AccumulationParams.opacity);
		AccumulationParams.window = AccumulationParams.prefs.getInt("Acc_window", AccumulationParams.window);
		AccumulationParams.maxConstant = AccumulationParams.prefs.getBoolean("Acc_maxConstant",
				AccumulationParams.maxConstant);
		AccumulationParams.maxConstantV = AccumulationParams.prefs.getInt("Acc_maxConstantV",
				AccumulationParams.maxConstantV);
		AccumulationParams.saveParams();
	}

	/**
	 * 
	 */
	public static void saveParams() {

		AccumulationParams.prefs = Preferences.userNodeForPackage(AccumulationParams.class);
		AccumulationParams.prefs.putFloat("Acc_minSize", AccumulationParams.minSize);
		AccumulationParams.prefs.putFloat("Acc_maxSize", AccumulationParams.maxSize);
		AccumulationParams.prefs.putDouble("Acc_micronPerPixel", AccumulationParams.micronPerPixel);
		AccumulationParams.prefs.putFloat("Acc_frameRate", AccumulationParams.frameRate);
		AccumulationParams.prefs.putInt("Acc_frameInt", AccumulationParams.frameInt);
		AccumulationParams.prefs.putInt("Acc_radius", AccumulationParams.radius);
		AccumulationParams.prefs.putInt("Acc_opacity", AccumulationParams.opacity);
		AccumulationParams.prefs.putInt("Acc_window", AccumulationParams.window);
		AccumulationParams.prefs.putBoolean("Acc_maxConstant", AccumulationParams.maxConstant);
		AccumulationParams.prefs.putInt("Acc_maxConstantV", AccumulationParams.maxConstantV);

		try {
			FileOutputStream fos = new FileOutputStream(
					System.getProperty("user.dir") + File.separator + "settings.config");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			AccumulationParams.prefs.exportSubtree(oos);
			oos.close();
			fos.close();
		} catch (Exception e) {
			IJ.handleException(e);
			// e.printStackTrace();
		}
	}

	public static void setGlobalParams() {
		Params.minSize = AccumulationParams.minSize;
		Params.maxSize = AccumulationParams.maxSize;
		Params.micronPerPixel = AccumulationParams.micronPerPixel;
		Params.frameRate = AccumulationParams.frameRate;
		Params.frameInt = AccumulationParams.frameInt;
		Params.radius = AccumulationParams.radius;
		Params.opacity = AccumulationParams.opacity;
		Params.window = AccumulationParams.window;
		Params.date = AccumulationParams.date;
		Params.male = AccumulationParams.male;
		Params.genericField = AccumulationParams.genericField;
		Params.firstFrame = AccumulationParams.firstFrame;
		Params.lastFrame = AccumulationParams.lastFrame;
		Params.maxConstant = AccumulationParams.maxConstant;
		Params.maxConstantV = AccumulationParams.maxConstantV;
	}

	public static void printParams() {
		System.out.println("---------- Accumulation PARAMETERS ---------");
		System.out.println("AccumulationParams.minSize: " + AccumulationParams.minSize);
		System.out.println("AccumulationParams.maxSize: " + AccumulationParams.maxSize);
		System.out.println("AccumulationParams.micronPerPixel: " + AccumulationParams.micronPerPixel);
		System.out.println("AccumulationParams.frameRate: " + AccumulationParams.frameRate);
		System.out.println("AccumulationParams.frameInt: " + AccumulationParams.frameInt);
		System.out.println("AccumulationParams.radius: " + AccumulationParams.radius);
		System.out.println("AccumulationParams.opacity: " + AccumulationParams.opacity);
		System.out.println("AccumulationParams.window: " + AccumulationParams.window);
		System.out.println("AccumulationParams.maxConstant: " + AccumulationParams.maxConstant);
		System.out.println("AccumulationParams.maxConstantV: " + AccumulationParams.maxConstantV);
		System.out.println("AccumulationParams.firstFrame: " + AccumulationParams.firstFrame);
		System.out.println("AccumulationParams.lastFrame: " + AccumulationParams.lastFrame);
	}
}
