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

package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.ChemotaxisParams;
import data.MotilityParams;
import data.Params;

public class ChemotaxisSettings extends JPanel {

  JTextField dateTF = new JTextField(ChemotaxisParams.date, 8);
  JTextField frameRateTF = new JTextField("" + ChemotaxisParams.frameRate, 4);
  JTextField firstFrameTF = new JTextField("", 4);
  JTextField lastFrameTF = new JTextField("", 4);  
  JTextField genericTF = new JTextField(ChemotaxisParams.genericField, 8);
  JTextField maleTF = new JTextField(ChemotaxisParams.male, 8);
  JTextField maxDisplacementTF = new JTextField("" + ChemotaxisParams.maxDisplacement, 4);
  JTextField maxSizeTF = new JTextField("" + ChemotaxisParams.maxSize, 4);
  JTextField micronPerPixelTF = new JTextField("" + ChemotaxisParams.micronPerPixel, 4);
  JTextField minSizeTF = new JTextField("" + ChemotaxisParams.minSize, 4);
  JTextField minTrackLengthTF = new JTextField("" + ChemotaxisParams.minTrackLength, 4);
  JCheckBox printXYCB = new JCheckBox();
  JTextField vclMinTF = new JTextField("" + ChemotaxisParams.vclMin, 4);
  JTextField windowSizeTF = new JTextField("" + ChemotaxisParams.wSize, 4);
  JTextField angleAmplitudeTF = new JTextField("" + ChemotaxisParams.angleAmplitude, 4);
  JTextField angleDeltaTF = new JTextField("" + ChemotaxisParams.angleDelta, 4);
  JTextField angleDirectionTF = new JTextField("" + ChemotaxisParams.angleDirection, 4);
  JCheckBox compareOppositeDirCB = new JCheckBox();
  JTextField numSamplesBootsTF = new JTextField("" + ChemotaxisParams.NUMSAMPLES, 4);

  
  public ChemotaxisSettings() {
    super();
    firstFrameTF.setText(""+ChemotaxisParams.firstFrame);
    if(ChemotaxisParams.lastFrame<0)
      lastFrameTF.setText("-1");
    else
      lastFrameTF.setText(""+ChemotaxisParams.lastFrame);
  }
  
  public JPanel createGUI() {
    this.setLayout(new GridBagLayout());
    // this.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
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
    label = new JLabel("Minimum vcl (um/s): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(vclMinTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Frame Rate (frames/s): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(frameRateTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Minimum Track Length(frames): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(minTrackLengthTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Maximum displacement between frames (um): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(maxDisplacementTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Window Size (frames): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(windowSizeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Print XY coords: ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    printXYCB.setSelected(ChemotaxisParams.printXY);
    this.add(printXYCB, c);
    ///////////////
    c.gridy += 1;
    c.gridx = 1;
    label = new JLabel("Chemotactic direction (degrees): ");
    this.add(label, c);
    c.gridx = 2;
    this.add(angleDirectionTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Chemotactic cone's amplitude (Degrees): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(angleAmplitudeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Number of bootstrapping resamples: ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(numSamplesBootsTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Angle Delta (frames): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(angleDeltaTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Compare opposite directions: ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    compareOppositeDirCB.setSelected(Params.compareOppositeDirections);
    this.add(compareOppositeDirCB, c);    
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

    ChemotaxisParams.frameRate = Float.parseFloat(frameRateTF.getText());
    ChemotaxisParams.micronPerPixel = Double.parseDouble(micronPerPixelTF.getText());
    ChemotaxisParams.male = maleTF.getText();
    ChemotaxisParams.date = dateTF.getText();
    ChemotaxisParams.genericField = genericTF.getText();
    ChemotaxisParams.minSize = Float.parseFloat(minSizeTF.getText());
    ChemotaxisParams.maxSize = Float.parseFloat(maxSizeTF.getText());
    ChemotaxisParams.minTrackLength = Integer.parseInt(minTrackLengthTF.getText());
    ChemotaxisParams.maxDisplacement = Float.parseFloat(maxDisplacementTF.getText()); // um => pixels
    ChemotaxisParams.wSize = Integer.parseInt(windowSizeTF.getText());
    ChemotaxisParams.vclMin = Float.parseFloat(vclMinTF.getText());
    ChemotaxisParams.printXY = printXYCB.isSelected();
    ChemotaxisParams.angleDelta = Integer.parseInt(angleDeltaTF.getText());
    ChemotaxisParams.angleDirection = Float.parseFloat(angleDirectionTF.getText());
    ChemotaxisParams.angleAmplitude = Float.parseFloat(angleAmplitudeTF.getText());
    ChemotaxisParams.NUMSAMPLES = Integer.parseInt(numSamplesBootsTF.getText());
    ChemotaxisParams.compareOppositeDirections = compareOppositeDirCB.isSelected();
    ChemotaxisParams.firstFrame=Float.parseFloat(firstFrameTF.getText());
    ChemotaxisParams.lastFrame=Float.parseFloat(lastFrameTF.getText());
    ChemotaxisParams.saveParams();
  }

}
