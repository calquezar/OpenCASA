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

package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import data.Cell;
import data.Params;
import data.Square;
import functions.ComputerVision;
import functions.FileManager;
import functions.Utils;
import functions.VideoRecognition;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import net.sf.javaml.core.kdtree.KDTree;

/**
 * This class implements all the functions related to cell count analysis.
 * 
 * @author Jorge Yagüe
 *
 */
public class CellCountWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private enum TypeOfAnalysis {
		DIRECTORY, FILE, NONE
	}

	private TypeOfAnalysis analysis = TypeOfAnalysis.NONE;

	/** Image list to process */
	private List<ImagePlus> images;
	/** Label where the current image is shown */
	private JLabel imgLabel;
	/** Label where the cell count is shown */
	private JLabel concLabel;
	private JButton nextBtn;
	private JButton prevBtn;
	protected JButton saveBtn;
	/** Button to hide/show the squares */
	private JButton cuadricula;
	/** TextField to write the dilution of the current image */
	private JTextField diluc;
	private int imgIndex;
	/** Cell count of each image on list */
	private double[] conc;
	/** Image list which have been processed */
	private ArrayList<ImagePlus> impDraw;
	/** Number of squares detected in each image */
	private int[] squares;
	/** Number of spermatozoa detected in each image */
	private int[] spermatozoa;
	
	private ResultsTable results;

	/**
	 * Constructor
	 */
	public CellCountWindow() {
		imgLabel = new JLabel();
		concLabel = new JLabel();
		nextBtn = new JButton("Next");
		prevBtn = new JButton("Previous");
		saveBtn = new JButton("Save");
		cuadricula = new JButton("Squares");
		diluc = new JTextField("1", 4);
		imgIndex = 0;
		conc = null;
		squares = null;
		results = new ResultsTable();
	}

	/**
	 * This method creates and shows the window.
	 */
	private void showWindow() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 0;
		c.gridx = 0;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.weightx = 1;
		panel.add(concLabel, c);

		JLabel label = new JLabel("Dilution: 1/");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		c.gridy += 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;
		panel.add(label, c);

		c.gridx = 1;
		c.weightx = 1;
		diluc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double dis = Double.parseDouble(((JTextField) e.getSource()).getText());
				setConc(dis);
			}
		});
		panel.add(diluc, c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		cuadricula.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cuadricula.setSelected(!cuadricula.isSelected());
				setImage();
			}
		});
		cuadricula.setSelected(true);
		panel.add(cuadricula, c);

		c.gridy += 1;
		c.ipadx = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridx = 2;
		c.gridy += 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1;
		panel.add(imgLabel, c);
		processImage();

		c.gridy += 1;
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		prevBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imgIndex > 0) {
					nextBtn.setEnabled(true);
					imgIndex--;
					setImage();
				} else if (imgIndex == 0) {
					prevBtn.setEnabled(false);
				}
			}
		});
		c.gridy += 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		prevBtn.setEnabled(false);
		panel.add(prevBtn, c);

    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         ImagePlus impOrig = images.get(imgIndex);
         results.incrementCounter();
         FileManager fm = new FileManager();
         results.addValue("Sample", fm.getParentDirectory(impOrig.getTitle()));
         results.addValue("Filename", fm.getFilename(impOrig.getTitle()));
         results.addValue("Concentration", conc[imgIndex]);
         results.show("Results");
      }
    });
    c.gridx = 2;
    panel.add(saveBtn, c);
    
		nextBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imgIndex < (images.size() - 1)) {
					prevBtn.setEnabled(true);
					if (impDraw.size() > ++imgIndex) {
						// Se evita procesar una imagen dos veces
						setImage();
					} else {
						processImage();
					}
				} else if (imgIndex == (images.size() - 1)) {
					nextBtn.setEnabled(false);
				}
			}
		});
		c.gridx = 3;
		panel.add(nextBtn, c);

		this.setContentPane(panel);
		this.pack();
		this.setVisible(true);
	}

	public int run() {
		int out = selectAnalysis();
		if (out < 0)
			return out;
		switch (analysis) {
		case FILE:
			out = analyseFile();
			break;
		case DIRECTORY:
			out = analyseDirectory();
			break;
		default:
			out = -2;
			break;
		}
		return out;
	}

	/**
	 * This method opens a set of dialogs to ask the user which analysis has to be
	 * carried on.
	 */
	public int selectAnalysis() {
		// Ask if user wants to analyze a file or directory
		Object[] options = { "File", "Directory" };
		String question = "What do you want to analyze?";
		String title = "Choose one analysis...";
		final int FILE = 0;
		final int DIR = 1;
		Utils utils = new Utils();
		int sourceSelection = utils.analysisSelectionDialog(options, question, title);
		if (sourceSelection < 0) {
			analysis = TypeOfAnalysis.NONE;
			return -1;
		} else if (sourceSelection == FILE) {
			analysis = TypeOfAnalysis.FILE;
		} else if (sourceSelection == DIR) {
			analysis = TypeOfAnalysis.DIRECTORY;
		}
		return 0;
	}

	private int analyseFile() {
		FileManager fm = new FileManager();
		List<ImagePlus> images = fm.loadImageFile();
		if (images != null) {
			setImages(images);
			showWindow();
			return 0;
		} else {
			return -1;
		}
	}

	private int analyseDirectory() {
		FileManager fm = new FileManager();
		List<ImagePlus> images = fm.loadImageDirectory();
		if (images != null) {
			setImages(images);
			showWindow();
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * This method sets the images attribute with the given list of ImagePlus.
	 * 
	 * @param i
	 */
	public void setImages(List<ImagePlus> i) {
		images = i;
		impDraw = new ArrayList<ImagePlus>(i.size());
		conc = new double[i.size()];
		squares = new int[i.size()];
		spermatozoa = new int[i.size()];
	}

	/**
	 * Process the first image and show it on screen
	 */
	private void processImage() {
		ComputerVision cv = new ComputerVision();
		VideoRecognition vr = new VideoRecognition();
		ImagePlus impOrig = images.get(imgIndex);
		ImagePlus imp = impOrig.duplicate();
		ImagePlus impDraw = imp.duplicate();
		cv.convertToRGB(impDraw);
		cv.convertToGrayscale(imp);
		cv.equalize(imp);
		imp.getProcessor().findEdges();
		cv.autoThresholdImagePlus(imp, "Otsu");
		imp.getProcessor().invertLut();
		List<Square> squares = vr.detectSquares(imp)[0];
		this.squares[imgIndex] = squares.size();
		imp = impOrig.duplicate();
		cv.convertToGrayscale(imp);
		cv.equalize(imp);
		cv.autoThresholdImagePlus(imp, "RenyiEntropy");
		List[] spermatozoa = squares.size() == 0 ? vr.detectCells(imp) : vr.detectCellsSquares(imp);
		double acum = spermatozoa[0].size();
		if (squares != null && !squares.isEmpty()) {
			if (spermatozoa != null && !spermatozoa[0].isEmpty()) {
				ImageProcessor ip = impDraw.getProcessor();
				Utils u = new Utils();
				KDTree tree = u.getKDTree(spermatozoa)[0];
				List<Cell> inside = new LinkedList<Cell>();
				ip.setLineWidth(4);
				ip.setFont(new Font("SansSerif", Font.PLAIN, 32));
				for (Square s : squares) {
					ip.setColor(Color.red);
					ip.drawRect((int) s.x, (int) s.y, (int) s.width, (int) s.height);
					Object[] insideO = tree.range(new double[] { s.x, s.y },
							new double[] { s.x + s.width, s.y + s.height });
					Cell[] insideAux = Arrays.copyOf(insideO, insideO.length, Cell[].class);
					inside.addAll(Arrays.asList(insideAux));
					ip.setColor(Color.green);
					ip.moveTo((int) (s.x + s.width / 2), (int) (s.y + s.height / 2));
					ip.drawString("" + insideAux.length);
				}
				// Square volume (ml)
				double volume = Params.sideS * Params.sideS * Params.depthC / 1e12;
				// Concentration (Million spermatozoa / ml)
				acum = inside.size() / (squares.size() * volume * 1e6);
			}
		} else {
			this.spermatozoa[imgIndex] = (int) acum;
			double volume = Params.micronPerPixel * impDraw.getWidth() * Params.micronPerPixel * impDraw.getHeight()
					* Params.depthC / 1e12;
			acum = acum / (volume * 1e6);
		}
		// Draw a rectangle for each detected cell
		ImageProcessor ip = impDraw.getProcessor();
		ip.setColor(Color.white);
		ip.setLineWidth(4);
		List cells = spermatozoa[0];
		for (int i = 0; i < cells.size(); i++) {
			Cell c = (Cell) cells.get(i);
			ip.drawRect((int) c.bx, (int) c.by, (int) c.width, (int) c.height);
		}

		conc[imgIndex] = acum;
		this.impDraw.add(impDraw);
		setImage();
	}

	/**
	 * This method sets the first image on the list and show it on screen.
	 */
	public void setImage() {
		setConc(Double.parseDouble(diluc.getText()));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		int targetWidth = (int) (w * 0.75);
		ImagePlus imp = cuadricula.isSelected() ? impDraw.get(imgIndex) : images.get(imgIndex);
		ImageProcessor ip = imp.getProcessor();
		ip.setInterpolationMethod(ImageProcessor.BILINEAR);
		ip = ip.resize(targetWidth);
		imp.setProcessor(ip);
		imgLabel.setIcon(new ImageIcon(imp.getImage()));
		imgLabel.repaint();
    ImagePlus impOrig = images.get(imgIndex);
    this.setTitle(impOrig.getTitle());
	}

	/**
	 * Update cell count with new disolution d
	 * 
	 * @param d
	 */
	private void setConc(double d) {
		double aux = Math.round(100 * conc[imgIndex] * d) / 100.0;
		if (squares[imgIndex] > 0) {
			concLabel.setText(aux + " Mespermatozoa / ml (" + squares[imgIndex] + " squares)");
		} else {
			concLabel.setText(aux + " Mespermatozoa / ml (" + spermatozoa[imgIndex] + " spermatozoa)");
		}
	}

}
