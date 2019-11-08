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
package analysis;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import data.Cell;
import data.Params;
import functions.ComputerVision;
import functions.FileManager;
import functions.Utils;
import functions.VideoRecognition;
import gui.AccumulationWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.LUT;
import net.sf.javaml.core.kdtree.KDTree;

/**
 * This class implements all the functions related to accumulation analysis.
 * 
 * @author Jorge Yagüe
 */
public class Accumulation extends SwingWorker<Boolean, String> {

	/**
	 * Constructor. Initialize LUT with file named "Jet.lut"
	 */
	public Accumulation(LUT lut) {
		try {
			Accumulation.lut = lut;
		} catch (Exception ex) {
			IJ.handleException(ex);
		}
	}

	private static LUT lut = null;

	private enum TypeOfAnalysis {
		IMAGE, VIDEO, NONE
	}

	private TypeOfAnalysis analysis = TypeOfAnalysis.NONE;

	private static int MAX = 0;

	/**
	 * This method counts spermatozoa located in the circle with center (x,y) and
	 * radius r, which are contained in tree
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param tree
	 * @return número de esperamtozoides
	 */
	private static int contar(int x, int y, int r, KDTree tree) {
		Object[] nearestO = tree.range(new double[] { x - r, y - r }, new double[] { x + r - 1, y + r - 1 });
		if (nearestO.length > 0) {
			int n = nearestO.length;
			// Se filtran las que están fuera del círculo de radio r
			Cell[] nearest = Arrays.copyOf(nearestO, n, Cell[].class);
			int dist2 = r * r;
			for (Cell c : nearest) {
				int dist = ((int) c.x - x) * ((int) c.x - x) + ((int) c.y - y) * ((int) c.y - y);

				n -= dist < dist2 ? 0 : 1;
			}
			return n;
		} else {
			return 0;
		}
	}

	/**
	 * This method returns the area of the circumference sector with radius r and
	 * height h
	 * 
	 * @param r
	 * @param h
	 * @return
	 */
	private static double areaSector(int r, int h) {
		double alpha = 2 * Math.acos(1 - (double) (r - h) / r);
		return 0.5 * r * r * (alpha - Math.sin(alpha));
	}

	/**
	 * This method returns the integral which is defined by circumference function
	 * between "from" to 0
	 * 
	 * @param r    circumference radius
	 * @param a    circumference center
	 * @param b    circumference center
	 * @param from
	 * @return
	 */
	private static double areaInt(int r, int a, int b, double from) {
		// (x - a)^2 + (y - b)^2 = r^2
		// y = sqrt(r^2 - (x - a)^2) + b
		return -((from - a) * Math.sqrt(r * r - from * from + 2 * a * from - a * a) + a * Math.sqrt(r * r - a * a)
				+ r * r * (Math.asin((from - a) / r) + Math.asin(a / r)) + 2 * b * from) / 2;
	}

	/**
	 * This method returns a matrix[width][height] with the accumulation in pixel
	 * window W with radius r
	 * 
	 * @param width
	 * @param height
	 * @param r
	 * @param tree
	 * @param debug
	 * @return
	 */
	public static int[][] getAccumulation(int width, int height, int r, int W, KDTree tree, boolean debug) {
		if (debug)
			IJ.showStatus("Analizing accumulation...");
		int AC[][] = new int[width][height];
		MAX = 0;

		double A = r * r * Math.PI;
		int l = W / 2;
		boolean div1 = r % W == 0, div2;
		int t = 0, T = width * height;
		// Esquinas
		for (int i = l; i < r; i = div1 || i + W < r ? i + W : i + l + 1) {
			if (debug)
				IJ.showProgress(t, T);
			for (int j = l; j < r; j = div1 || j + W < r ? j + W : j + l + 1) {
				t += W * W * 4;
				double Aizq = areaSector(r, i);
				double Asup = areaSector(r, j);
				double intersec = areaInt(r, -i, -j, i - r);
				/**
				 * Each sector area is substracted and then intersecction is added if it exists
				 */
				double At = A - Aizq - Asup;
				if (intersec > 0) { // There is and intersection
					At += intersec;
				}
				double k = A / At;
				// Up left corner
				AC[i][j] = (int) Math.round(k * contar(i, j, r, tree));
				MAX = AC[i][j] > MAX ? AC[i][j] : MAX;

				// Up right corner
				AC[width - 1 - i][j] = (int) Math.round(k * contar(width - 1 - i, j, r, tree));
				MAX = AC[width - 1 - i][j] > MAX ? AC[width - 1 - i][j] : MAX;

				// Bottom left corner
				AC[i][height - 1 - j] = (int) Math.round(k * contar(i, height - 1 - j, r, tree));
				MAX = AC[i][height - 1 - j] > MAX ? AC[i][height - 1 - j] : MAX;

				// bottom right corner
				AC[width - 1 - i][height - 1 - j] = (int) Math
						.round(k * contar(width - 1 - i, height - 1 - j, r, tree));
				MAX = AC[width - 1 - i][height - 1 - j] > MAX ? AC[width - 1 - i][height - 1 - j] : MAX;

				// Fill the pixel window
				for (int i2 = 0; i2 < W && i - l + i2 < r; i2++) {
					for (int j2 = 0; j2 < W && j - l + j2 < r; j2++) {
						AC[i - l + i2][j - l + j2] = AC[i][j];
						AC[width - 1 - (i - l + i2)][j - l + j2] = AC[width - 1 - i][j];
						AC[i - l + i2][height - 1 - (j - l + j2)] = AC[i][height - 1 - j];
						AC[width - 1 - (i - l + i2)][height - 1 - (j - l + j2)] = AC[width - 1 - i][height - 1 - j];
					}
				}
			}
		}

		// Lateral zones
		for (int i = l; i < r; i = div1 || r + W < r ? i + W : i + l + 1) {
			if (debug)
				IJ.showProgress(t, T);
			double A2 = A - areaSector(r, i);
			double k = A / A2;
			div2 = (height - 2 * r) % W == 0;
			for (int j = r + l; j < height - r; j = div2 || j + W < height - r ? j + W : j + l + 1) {
				t += W * W * 2;
				// Left zone
				AC[i][j] = (int) Math.round(k * contar(i, j, r, tree));
				MAX = AC[i][j] > MAX ? AC[i][j] : MAX;

				// Right zone
				AC[width - 1 - i][j] = (int) Math.round(k * contar(width - 1 - i, j, r, tree));
				MAX = AC[width - 1 - i][j] > MAX ? AC[width - 1 - i][j] : MAX;

				// Fill the pixel window
				for (int i2 = 0; i2 < W && i - l + i2 < r; i2++) {
					for (int j2 = 0; j2 < W && j - l + j2 < height - r; j2++) {
						AC[i - l + i2][j - l + j2] = AC[i][j];
						AC[width - 1 - (i - l + i2)][j - l + j2] = AC[width - 1 - i][j];
					}
				}
			}
			div2 = (width - 2 * r) % W == 0;
			for (int j = r + l; j < width - r; j = div2 || j + W < width - r ? j + W : j + l + 1) {
				{
					t += W * W * 2;
					// Up zone
					AC[j][i] = (int) Math.round(k * contar(j, i, r, tree));
					MAX = AC[j][i] > MAX ? AC[j][i] : MAX;

					// Bottom zone
					AC[j][height - 1 - i] = (int) Math.round(k * contar(j, height - 1 - i, r, tree));
					MAX = AC[j][height - 1 - i] > MAX ? AC[j][height - 1 - i] : MAX;

					for (int i2 = 0; i2 < W && i - l + i2 < r; i2++) {
						for (int j2 = 0; j2 < W && i - l - j2 < width - r; j2++) {
							AC[j - l + j2][i - l + i2] = AC[j][i];
							AC[j - l + j2][height - 1 - (i - l + i2)] = AC[j][height - 1 - i];
						}
					}

				}

			}
		}

		div1 = (width - 2 * r) % W == 0;
		div2 = (height - 2 * r) % W == 0;
		// Central zone
		for (int i = r + l; i < width - r; i = div1 || i + W < width - r ? i + W : i + l + 1) {
			if (debug)
				IJ.showProgress(t, T);
			for (int j = r + l; j < height - r; j = div2 || j + W < height - r ? j + W : j + l + 1) {
				t += W * W;
				AC[i][j] = contar(i, j, r, tree);
				MAX = AC[i][j] > MAX ? AC[i][j] : MAX;

				for (int i2 = 0; i2 < W && i - l + i2 < width - r; i2++) {
					for (int j2 = 0; j2 < W && j - l - j2 < height - r; j2++) {
						AC[i - l + i2][j - l + j2] = AC[i][j];
					}
				}
			}

		}

		if (debug)
			IJ.showProgress(2);
		return AC;
	}

	/**
	 * This method returns an array with the same components as the maximum
	 * accumulation found, it use the LUT to set the max and min, and the rest
	 * colors are interpolated
	 * 
	 * @return
	 */
	public static Color[] getScale(int max) {
		if (max < 0)
			max = MAX;
		Color conc[] = new Color[max + 1];

		double k = 255.0 / max;
		for (int i = 0; i <= max; i++) {
			int index = (int) (k * i);
			conc[i] = new Color(lut.getRGB(index));
		}

		return conc;
	}

	/**
	 * This method analyzes a spermatozoa image and shows the heat map in a separate
	 * window
	 */
	private void analyseImage() {
		FileManager fm = new FileManager();
		List<ImagePlus> images = fm.loadImageFile();
		ImagePlus impOrig = images.get(0);
		impOrig.setTitle(fm.getFilename(impOrig.getTitle()));

		ComputerVision cv = new ComputerVision();
		VideoRecognition vr = new VideoRecognition();
		ImagePlus imp = impOrig.duplicate();
		cv.convertToGrayscale(imp);
		cv.convertToRGB(impOrig);
		cv.equalize(imp);
		cv.autoThresholdImagePlus(imp, "RenyiEntropy");
		List[] spermatozoa = vr.detectCells(imp);
		if (spermatozoa != null && spermatozoa[0].size() > 0) { // Se han detectado espermatozoides en la imagen
			Utils u = new Utils();
			KDTree tree = u.getKDTree(spermatozoa)[0];
			AccumulationWindow aw = new AccumulationWindow(tree, impOrig, spermatozoa[0].size());
		} else {
			IJ.error("Accumulation", "No se han detectado espermatozoides");
		}
	}

	/**
	 * This method analyzes a spermatozoa video and shows the heat map in each frame
	 */
	private void analyseVideo() {
		FileManager fm = new FileManager();
		String file = fm.selectFile();
		if (file == null)
			return;
		ImagePlus impOrig = fm.getAVI(file), imp = impOrig.duplicate();
		impOrig.setTitle(fm.getFilename(file));
		ComputerVision cv = new ComputerVision();
		cv.convertToGrayscale(imp);
		cv.thresholdStack(imp);
		VideoRecognition vr = new VideoRecognition();
		final List[] spermatozoa = vr.detectCells(imp);
		Utils u = new Utils();
		final KDTree[] trees = u.getKDTree(spermatozoa);
		final ImagePlus impDraw = Params.maxConstant ? drawHeatMap1(trees, impOrig) : drawHeatMap(trees, impOrig);
		impDraw.show();
		IJ.setTool("oval");
		ImageWindow impW = impDraw.getWindow();
		ImageCanvas impC = impW.getCanvas();
		impC.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				Roi roi = impDraw.getRoi();
				if (roi == null)
					return;
				int x = (int) (roi.getXBase() + roi.getFloatWidth() / 2);
				int y = (int) (roi.getYBase() + roi.getFloatWidth() / 2);
				int r = (int) (roi.getFloatWidth() / 2);
				ResultsTable rt = new ResultsTable();
				rt.reset();
				for (int i = 0; i < trees.length; i++) {
					rt.incrementCounter();
					// First circumference with radius r
					int n = contar(x, y, r, trees[i]);
					rt.addValue("Spermatozoa  Abs.", n);
					rt.addValue("Spermatozoa Rel.", 100 * n / spermatozoa[i].size() + "%");

					// Second circumference with radius r*2
					n = contar(x, y, r * 2, trees[i]);
					rt.addValue("Spermatozoa Abs. x2", n);
					rt.addValue("Spermatozoa Rel. x2", 100 * n / spermatozoa[i].size() + "%");

					// Third circumference with radius r*3
					n = contar(x, y, r * 3, trees[i]);
					rt.addValue("Spermatozoa Abs. x3", n);
					rt.addValue("Spermatozoa Rel. x3", 100 * n / spermatozoa[i].size() + "%");

					// Total Spermatozoa Number
					rt.addValue("Total", spermatozoa[i].size());

				}
				rt.show("Spermatozoa per Frame (" + (r * Params.micronPerPixel) + " um)");

			}
		});
	}

	/**
	 * This method draws a heat map over impOrig video with spermatozoa contained in
	 * tree
	 * 
	 * @param tree
	 * @param impOrig
	 * @return
	 */
	private ImagePlus drawHeatMap(KDTree[] tree, ImagePlus impOrig) {
		ImagePlus impDraw = impOrig.duplicate();
		ComputerVision cv = new ComputerVision();
		cv.convertToRGB(impDraw);
		impDraw.setTitle(impOrig.getTitle() + "-HeatMap");
		ImageStack stack = impDraw.getStack(), stackOrig = impOrig.getStack();
		int nFrames = impDraw.getStackSize();
		int width = impDraw.getWidth(), height = impDraw.getHeight();
		int AC1[][] = getAccumulation(width, height, Params.radius, Params.window, tree[0], false);
		for (int i = 1; i <= nFrames; i += Params.frameInt) {
			IJ.showStatus("Drawing image... (" + i + ", " + nFrames + ")");
			IJ.showProgress(i, nFrames);

			Color conc[] = getScale(-1);
			ImageProcessor ip = stack.getProcessor(i);
			drawImage(ip, AC1, conc);
			ImageRoi roi = new ImageRoi(0, 0, stackOrig.getProcessor(i));
			roi.setOpacity(Params.opacity / 100.0);
			ip.drawRoi(roi);

			if (i + Params.frameInt <= nFrames) {
				int AC2[][] = getAccumulation(width, height, Params.radius, Params.window,
						tree[i + Params.frameInt - 1], false);
				for (int j = 1; j < Params.frameInt; j++) {
					double p = (double) j / Params.frameInt;
					int AC[][] = interpolar(AC1, AC2, p);
					conc = getScale(-1);
					ip = stack.getProcessor(i + j);
					drawImage(ip, AC, conc);
					roi = new ImageRoi(0, 0, stackOrig.getProcessor(i + j));
					roi.setOpacity(Params.opacity / 100.0);
					ip.drawRoi(roi);
				}
				AC1 = AC2;
			} else {
				for (int j = 1; j < Params.frameInt && j + i <= nFrames; j++) {
					ip = stack.getProcessor(i + j);
					drawImage(ip, AC1, conc);
					roi = new ImageRoi(0, 0, stackOrig.getProcessor(i + j));
					roi.setOpacity(Params.opacity / 100.0);
					ip.drawRoi(roi);
				}
			}
		}
		IJ.showProgress(2);
		return impDraw;
	}

	/**
	 * This method draws a heat map over impOrig video with spermatozoa contained in
	 * tree
	 * 
	 * @param tree
	 * @param impOrig
	 * @return
	 */
	private ImagePlus drawHeatMap1(KDTree[] tree, ImagePlus impOrig) {
		ImagePlus impDraw = impOrig.duplicate();
		ComputerVision cv = new ComputerVision();
		cv.convertToRGB(impDraw);
		impDraw.setTitle(impOrig.getTitle() + "-HeatMap");
		ImageStack stack = impDraw.getStack(), stackOrig = impOrig.getStack();
		int nFrames = impDraw.getStackSize();
		int width = impDraw.getWidth(), height = impDraw.getHeight();
		LinkedList<int[][]> matrixes = new LinkedList<int[][]>();
		int AC1[][] = getAccumulation(width, height, Params.radius, Params.window, tree[0], false);
		int MAX = this.MAX;
		matrixes.add(AC1);
		for (int i = 1; i <= nFrames; i += Params.frameInt) {
			IJ.showStatus("Analyzing accumulation... (" + i + ", " + nFrames + ")");
			IJ.showProgress(i, nFrames);

			if (i + Params.frameInt <= nFrames) {
				int AC2[][] = getAccumulation(width, height, Params.radius, Params.window,
						tree[i + Params.frameInt - 1], false);
				MAX = getMax() > MAX ? getMax() : MAX;
				for (int j = 1; j < Params.frameInt; j++) {
					double p = (double) j / Params.frameInt;
					int AC[][] = interpolar(AC1, AC2, p);
					MAX = getMax() > MAX ? getMax() : MAX;
					matrixes.add(AC);
				}
				AC1 = AC2;
				matrixes.add(AC2);
			} else {
				for (int j = 1; j < Params.frameInt && j + i <= nFrames; j++) {
					matrixes.add(AC1);
				}
			}
		}

		MAX = Params.maxConstantV > 0 ? Params.maxConstantV : MAX;
		Color conc[] = getScale(MAX);
		for (int i = 1; i <= nFrames; i++) {
			IJ.showStatus("Drawing image... (" + i + ", " + nFrames + ")");
			IJ.showProgress(i, nFrames);
			ImageProcessor ip = stack.getProcessor(i);
			drawImage(ip, matrixes.get(i - 1), conc);
			ImageRoi roi = new ImageRoi(0, 0, stackOrig.getProcessor(i));
			roi.setOpacity(Params.opacity / 100.0);
			ip.drawRoi(roi);
		}

		IJ.showProgress(2);
		return impDraw;
	}

	/**
	 * This method returns the interpolated matrix between AC1 and AC2 based on the
	 * distance p
	 * 
	 * @param AC1
	 * @param AC2
	 * @param p
	 * @return
	 */
	private int[][] interpolar(int AC1[][], int AC2[][], double p) {
		int height = AC1.length;
		int width = AC1[0].length;
		int AC[][] = new int[height][width];

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				AC[i][j] = (int) Math.round((1 - p) * AC1[i][j] + p * AC2[i][j]);
				MAX = AC[i][j] > MAX ? AC[i][j] : MAX;
			}
		}

		return AC;
	}

	/**
	 * This method draws on the image ip the heat map using accumulation in AC and
	 * the scale conc
	 * 
	 * @param ip
	 * @param AC
	 * @param conc
	 */
	public static void drawImage(ImageProcessor ip, int AC[][], Color conc[]) {
		for (int i = 0; i < ip.getWidth(); i++) {
			for (int j = 0; j < ip.getHeight(); j++) {
				int index = AC[i][j] < conc.length ? AC[i][j] : conc.length - 1;
				ip.setColor(conc[index]);
				ip.drawPixel(i, j);
			}
		}
	}

	/**
	 * This method is inherit from SwingWorker class and it is the starting point
	 * after the execute() method is called.
	 */
	@Override
	public Boolean doInBackground() {
		switch (analysis) {
		case IMAGE:
			analyseImage();
			break;
		case VIDEO:
			analyseVideo();
			break;
		default:
			break;
		}
		return null;
	}

	/**
	 * This method is executed at the end of the worker thread in the Event Dispatch
	 * Thread.
	 */
	@Override
	protected void done() {
	}

	/**
	 * This method opens a set of dialogs to ask the user which analysis has to be
	 * carried on.
	 */
	public void selectAnalysis() {
		// Ask if user wants to analyze a file or experiment
		Object[] options = { "Image", "Video" };
		String question = "What do you want to analyze?";
		String title = "Choose one analysis...";
		final int IMAGE = 0;
		int VIDEO = 1;
		Utils utils = new Utils();
		int sourceSelection = utils.analysisSelectionDialog(options, question, title);
		if (sourceSelection < 0) {
			return;
		} else if (sourceSelection == IMAGE) { // Image
			analysis = TypeOfAnalysis.IMAGE;

		} else if (sourceSelection == VIDEO) { // Video
			analysis = TypeOfAnalysis.VIDEO;
		}
	}

	/**
	 * This method returns the class attribute MAX
	 * 
	 * @return
	 */
	public static int getMax() {
		return MAX;
	}

}
