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
public class CellCountParams {

	/**   */
	public static String date = "";
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
	/** Side length of each Square */
	public static float sideS = 100;
	/** Depth of the camera */
	public static float depthC = 10;
	/**   */
	private static Preferences prefs;

	private static void setDefault() {
		CellCountParams.date = "";
		CellCountParams.genericField = "";
		CellCountParams.male = "";
		CellCountParams.maxSize = 100;
		CellCountParams.micronPerPixel = 1;
		CellCountParams.minSize = 10;
		CellCountParams.sideS = 100;
		CellCountParams.depthC = 10;
	}

	/**   */
	public static void resetParams() {

		CellCountParams.setDefault();
		prefs = Preferences.userNodeForPackage(CellCountParams.class);
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
		prefs = Preferences.userNodeForPackage(CellCountParams.class);
		CellCountParams.minSize = CellCountParams.prefs.getFloat("Cc_minSize", CellCountParams.minSize);
		CellCountParams.maxSize = CellCountParams.prefs.getFloat("Cc_maxSize", CellCountParams.maxSize);
		CellCountParams.micronPerPixel = CellCountParams.prefs.getDouble("Cc_micronPerPixel",
				CellCountParams.micronPerPixel);
		CellCountParams.sideS = CellCountParams.prefs.getFloat("Cc_sideS", CellCountParams.sideS);
		CellCountParams.depthC = CellCountParams.prefs.getFloat("Cc_depthC", CellCountParams.depthC);
		CellCountParams.saveParams();
	}

	/**
	 * 
	 */
	public static void saveParams() {

		CellCountParams.prefs = Preferences.userNodeForPackage(CellCountParams.class);
		CellCountParams.prefs.putFloat("Cc_minSize", CellCountParams.minSize);
		CellCountParams.prefs.putFloat("Cc_maxSize", CellCountParams.maxSize);
		CellCountParams.prefs.putDouble("Cc_micronPerPixel", CellCountParams.micronPerPixel);
		CellCountParams.prefs.putFloat("Cc_sideS", CellCountParams.sideS);
		CellCountParams.prefs.putFloat("Cc_depthC", CellCountParams.depthC);

		try {
			FileOutputStream fos = new FileOutputStream(
					System.getProperty("user.dir") + File.separator + "settings.config");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			CellCountParams.prefs.exportSubtree(oos);
			oos.close();
			fos.close();
		} catch (Exception e) {
			IJ.handleException(e);
			// e.printStackTrace();
		}
	}

	public static void setGlobalParams() {
		Params.minSize = CellCountParams.minSize;
		Params.maxSize = CellCountParams.maxSize;
		Params.micronPerPixel = CellCountParams.micronPerPixel;
		Params.sideS = CellCountParams.sideS;
		Params.depthC = CellCountParams.depthC;
		Params.date = CellCountParams.date;
		Params.male = CellCountParams.male;
		Params.genericField = CellCountParams.genericField;
	}

	public static void printParams() {
		System.out.println("---------- CellCount PARAMETERS ---------");
		System.out.println("CellCountParams.minSize: " + CellCountParams.minSize);
		System.out.println("CellCountParams.maxSize: " + CellCountParams.maxSize);
		System.out.println("CellCountParams.micronPerPixel: " + CellCountParams.micronPerPixel);
	}
}
