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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import analysis.Accumulation;
import data.Params;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import net.sf.javaml.core.kdtree.KDTree;

/**
 * This class implements all the functions related to accumulation analysis on
 * image.
 * 
 * @author Jorge Yagüe
 *
 */
public class AccumulationWindow extends JFrame implements MouseListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	private JLabel imgLabel;
	/** Radius slider */
	private JSlider sldRadius;
	/** Opacity slider */
	private JSlider sldOpac;
	/** K-d tree which contains spermatozoa */
	private KDTree tree;
	private boolean isProcessing = false;
	/** Label where maximun accumulation is shown */
	private JLabel acText;
	private JButton saveBtn;
	private JLabel label1;
	private JLabel label2;
	/** Processed image */
	private ImagePlus imp;
	/** Original image */
	private ImagePlus impOrig;
	/** Image's width and height on JFrame */
	private int width;
	private int height;
	/** Total spermatozoa in image */
	private int total;

	/**
	 * Constructor
	 * 
	 * @param tree
	 * @param orig
	 * @param total
	 */
	public AccumulationWindow(KDTree tree, ImagePlus orig, int total) {
		super(orig.getTitle() + "-HeatMap");
		this.total = total;
		acText = new JLabel();
		saveBtn = new JButton("Save image");
		this.tree = tree;
		this.impOrig = orig;
		this.imp = orig.duplicate();
		imp.setTitle(this.getTitle());
		imgLabel = new JLabel();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		double h = screenSize.getHeight();
		this.width = (int) (w * 0.6);
		this.height = (int) (h * 0.6);

		sldRadius = new JSlider(JSlider.HORIZONTAL, 25, 250, 100);
		sldRadius.setMinorTickSpacing(5);
		sldRadius.setMajorTickSpacing(25);
		sldRadius.setPaintTicks(true);
		sldRadius.setPaintLabels(true);
		sldRadius.setLabelTable(sldRadius.createStandardLabels(25));
		sldRadius.addMouseListener(this);
		sldRadius.setVisible(true);

		sldOpac = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		sldOpac.setMinorTickSpacing(5);
		sldOpac.setMajorTickSpacing(25);
		sldOpac.setPaintTicks(true);
		sldOpac.setPaintLabels(true);
		sldOpac.setLabelTable(sldRadius.createStandardLabels(25));
		sldOpac.setVisible(true);
		sldOpac.addChangeListener(this);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.CENTER;
		JLabel label = new JLabel("Radius");
		panel.add(label, c);

		c.gridy = 1;
		label = new JLabel("x " + Params.micronPerPixel + " um/pixel");
		panel.add(label, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sldRadius, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		label = new JLabel("Opacity");
		panel.add(label, c);

		c.gridx = 1;
		c.gridwidth = 4;
		c.gridheight = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sldOpac, c);

		c.gridx = 0;
		c.gridy += 2;
		c.gridwidth = 4;
		c.gridheight = 1;
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridx = 1;
		c.gridy += 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		panel.add(acText, c);

		c.gridx = 1;
		c.gridy += 1;
		c.gridwidth = 1;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = c.weighty = 1;
		panel.add(imgLabel, c);

		ImagePlus scale = null;
		try {
			Image img = ImageIO.read(getClass().getResource("/Jet.png"));
			scale = new ImagePlus("Scale", img);
		} catch (IOException e1) {
			IJ.handleException(e1);
		}
		int scalewidth = scale.getWidth() * this.height / scale.getHeight();
		ImageProcessor ip = scale.getProcessor();
		ip.setInterpolationMethod(ImageProcessor.BILINEAR);
		ip = ip.resize(scalewidth, this.height);
		scale.setProcessor(ip);
		c.gridx = 2;
		c.gridwidth = 1;
		c.gridheight = 3;
		c.weightx = 0;
		c.insets = new Insets(0, 12, 0, 0);
		JLabel scaLabel = new JLabel();
		scaLabel.setIcon(new ImageIcon(scale.getImage()));
		scaLabel.repaint();
		panel.add(scaLabel, c);

		label1 = new JLabel();
		c.gridx = 3;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 0, 0);
		panel.add(label1, c);

		label2 = new JLabel();
		c.gridy += 1;
		panel.add(label2, c);

		c.gridx = 0;
		c.gridy += 2;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridx = 3;
		c.gridy += 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		panel.add(saveBtn, c);

		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImageProcessor ip = impOrig.getProcessor();
				ImageRoi roi = new ImageRoi(0, 0, ip);
				roi.setOpacity(sldOpac.getValue() / 100.0);
				imp.getProcessor().drawRoi(roi);
				FileSaver f = new FileSaver(imp);
				FileSaver.setJpegQuality(100);
				if (f.saveAsJpeg())
					saveBtn.setEnabled(false);
			}
		});

		processImage();

		this.setContentPane(panel);
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * When radius slider changes, image is processed
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Object auxWho = e.getSource();
		if (auxWho == sldRadius) {
			refreshImage();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Refresh image
	 */
	private void refreshImage() {
		if (!isProcessing) {
			isProcessing = true;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					processImage();
					isProcessing = false;
				}
			});
			t1.start();
		}
	}

	/**
	 * Process image and update imp attribute
	 */
	private void processImage() {
		int AC[][] = Accumulation.getAccumulation(impOrig.getWidth(), impOrig.getHeight(), sldRadius.getValue(), 1,
				tree, true);
		Color conc[] = Accumulation.getScale(-1);
		Accumulation.drawImage(imp.getProcessor(), AC, conc);
		label1.setText(100 * Accumulation.getMax() / total + "%");
		label2.setText((100 * Accumulation.getMax() / 2) / total + "%");
		showImage();
	}

	/**
	 * Paint image on screen
	 */
	private void showImage() {
		ImagePlus impDraw = imp.duplicate();

		ImageProcessor ip = impDraw.getProcessor();
		ip.setInterpolationMethod(ImageProcessor.BILINEAR);
		ip = ip.resize(this.width, this.height);
		impDraw.setProcessor(ip);

		ImagePlus impOrig2 = impOrig.duplicate();
		ip = impOrig2.getProcessor();
		ip.setInterpolationMethod(ImageProcessor.BILINEAR);
		ip = ip.resize(this.width, this.height);
		impOrig2.setProcessor(ip);

		ImageRoi roi = new ImageRoi(0, 0, ip);
		roi.setOpacity(sldOpac.getValue() / 100.0);
		impDraw.getProcessor().drawRoi(roi);

		imgLabel.setIcon(new ImageIcon(impDraw.getImage()));
		imgLabel.repaint();

		acText.setText("Max. Accumulation: " + Accumulation.getMax() + " spermatozoa in a "
				+ sldRadius.getValue() * Params.micronPerPixel + " um radius");
	}

	/**
	 * When opacity slider changes, image is updated automatically
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object auxWho = e.getSource();
		if (auxWho == sldOpac) {
			showImage();
		}

	}

}