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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.AccumulationParams;
import data.MotilityParams;

/**
 * This class implements all the settings related to accumulation analysis
 * 
 * @author Jorge Yagüe
 *
 */
public class AccumulationSettings extends JPanel {

	JTextField maxSizeTF = new JTextField("" + AccumulationParams.maxSize, 4);
	JTextField micronPerPixelTF = new JTextField("" + AccumulationParams.micronPerPixel, 4);
	JTextField minSizeTF = new JTextField("" + AccumulationParams.minSize, 4);
	JTextField frameRateTF = new JTextField("" + AccumulationParams.frameRate, 4);
	JTextField frameIntTF = new JTextField("" + AccumulationParams.frameInt, 4);
	JTextField radiusTF = new JTextField("" + AccumulationParams.radius, 4);
	JTextField opacTF = new JTextField("" + AccumulationParams.opacity, 4);
	JTextField windowTF = new JTextField("" + AccumulationParams.window, 4);
	JCheckBox maxConstantCB = new JCheckBox();
	JTextField maxConstantTF = new JTextField("" + AccumulationParams.maxConstantV, 4);
	JTextField firstFrameTF = new JTextField("", 4);
	JTextField lastFrameTF = new JTextField("", 4);
	JTextField dateTF = new JTextField(AccumulationParams.date, 8);
	JTextField genericTF = new JTextField(AccumulationParams.genericField, 8);
	JTextField maleTF = new JTextField(AccumulationParams.male, 8);

	public AccumulationSettings() {
		super();
		firstFrameTF.setText("" + AccumulationParams.firstFrame);
		if (AccumulationParams.lastFrame < 0)
			lastFrameTF.setText("-1");
		else
			lastFrameTF.setText("" + AccumulationParams.lastFrame);
	}

	public JPanel createGUI() {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		///////////////
		JLabel label = new JLabel("-------------------------------- ");
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("-- Persistent parameters -- ");
		c.gridx = 1;
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("-------------------------------- ");
		c.gridx = 1;
		this.add(label, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Microns per Pixel: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(micronPerPixelTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Minimum cell size (um^2): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(minSizeTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Maximum cell size (um^2): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(maxSizeTF, c);
		///////////////
		c.gridy += 1;
		c.gridx = 1;
		label = new JLabel("-------------");
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("Video");
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("-------------");
		this.add(label, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Frame Rate (frames/s): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(frameRateTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Sampling factor: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(frameIntTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Radius to analyze (pixels): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(radiusTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Opacity (%): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(opacTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Window size (pixels): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(windowTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Max Accumulation Value: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(maxConstantTF, c);
		//////////
		c.gridy += 1;
		label = new JLabel("Maximum constant Accumulation: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		maxConstantCB.setSelected(AccumulationParams.maxConstant);
		this.add(maxConstantCB, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("-------------------------------- ");
		c.gridx = 1;
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("-- Not persistent parameters -- ");
		c.gridx = 1;
		this.add(label, c);
		c.gridy += 1;
		label = new JLabel("-------------------------------- ");
		c.gridx = 1;
		this.add(label, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("First frame (seconds): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(firstFrameTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Last frame (seconds): ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(lastFrameTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Male: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(maleTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Date: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(dateTF, c);
		///////////////
		c.gridy += 1;
		label = new JLabel("Generic: ");
		c.gridx = 1;
		this.add(label, c);
		c.gridx = 2;
		this.add(genericTF, c);

		return this;
	}

	public void setParameters() {
		AccumulationParams.micronPerPixel = Double.parseDouble(micronPerPixelTF.getText());
		AccumulationParams.male = maleTF.getText();
		AccumulationParams.date = dateTF.getText();
		AccumulationParams.genericField = genericTF.getText();
		AccumulationParams.minSize = Float.parseFloat(minSizeTF.getText());
		AccumulationParams.maxSize = Float.parseFloat(maxSizeTF.getText());
		AccumulationParams.frameRate = Float.parseFloat(frameRateTF.getText());
		AccumulationParams.frameInt = Integer.parseInt(frameIntTF.getText());
		AccumulationParams.radius = Integer.parseInt(radiusTF.getText());
		AccumulationParams.opacity = Integer.parseInt(opacTF.getText());
		AccumulationParams.window = Integer.parseInt(windowTF.getText());
		AccumulationParams.firstFrame = Float.parseFloat(firstFrameTF.getText());
		AccumulationParams.lastFrame = Float.parseFloat(lastFrameTF.getText());
		AccumulationParams.maxConstant = maxConstantCB.isSelected();
		AccumulationParams.maxConstantV = Integer.parseInt(maxConstantTF.getText());
		AccumulationParams.saveParams();
	}

}
