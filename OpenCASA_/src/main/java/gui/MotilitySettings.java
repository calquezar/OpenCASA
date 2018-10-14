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

import data.MotilityParams;

public class MotilitySettings extends JPanel {

  JTextField dateTF = new JTextField(MotilityParams.date, 8);
  JTextField frameRateTF = new JTextField("" + MotilityParams.frameRate, 4);
  JTextField firstFrameTF = new JTextField("", 4);
  JTextField lastFrameTF = new JTextField("", 4);
  JTextField genericTF = new JTextField(MotilityParams.genericField, 8);
  JTextField maleTF = new JTextField(MotilityParams.male, 8);
  JTextField maxDisplacementTF = new JTextField("" + MotilityParams.maxDisplacement, 4);
  JTextField maxSizeTF = new JTextField("" + MotilityParams.maxSize, 4);
  JTextField micronPerPixelTF = new JTextField("" + MotilityParams.micronPerPixel, 4);
  JTextField minSizeTF = new JTextField("" + MotilityParams.minSize, 4);
  JTextField minTrackLengthTF = new JTextField("" + MotilityParams.minTrackLength, 4);
  JCheckBox printXYCB = new JCheckBox();
  JTextField progressiveMotilityTF = new JTextField("" + MotilityParams.progressMotility, 4);
  JTextField vclLowerThTF = new JTextField("" + MotilityParams.vclLowerTh, 4);
  JTextField vclMinTF = new JTextField("" + MotilityParams.vclMin, 4);
  JTextField vclUpperThTF = new JTextField("" + MotilityParams.vclUpperTh, 4);
  JTextField windowSizeTF = new JTextField("" + MotilityParams.wSize, 4);
  
  public MotilitySettings() {
    super();
    firstFrameTF.setText(""+(MotilityParams.firstFrame-1)/MotilityParams.frameRate);
    if(MotilityParams.lastFrame<0)
      lastFrameTF.setText("-1");
    else
      lastFrameTF.setText(""+MotilityParams.lastFrame/MotilityParams.frameRate);
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
    ///////////////
    c.gridy += 1;
    label = new JLabel("Progressive motility (STR>%): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(progressiveMotilityTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Minimum vcl (um/s): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(vclMinTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("vcl lower threshold (um/s): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(vclLowerThTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("vcl upper threshold (um/s): ");
    c.gridx = 1;
    this.add(label, c);
    c.gridx = 2;
    this.add(vclUpperThTF, c);    
    ///////////////
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
    printXYCB.setSelected(MotilityParams.printXY);
    this.add(printXYCB, c);
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

    MotilityParams.frameRate = Float.parseFloat(frameRateTF.getText());
    MotilityParams.micronPerPixel = Double.parseDouble(micronPerPixelTF.getText());
    MotilityParams.male = maleTF.getText();
    MotilityParams.date = dateTF.getText();
    MotilityParams.genericField = genericTF.getText();
    MotilityParams.minSize = Float.parseFloat(minSizeTF.getText());
    MotilityParams.maxSize = Float.parseFloat(maxSizeTF.getText());
    MotilityParams.minTrackLength = Integer.parseInt(minTrackLengthTF.getText());
    MotilityParams.maxDisplacement = Float.parseFloat(maxDisplacementTF.getText()); // um => pixels
    MotilityParams.wSize = Integer.parseInt(windowSizeTF.getText());
    MotilityParams.vclMin = Float.parseFloat(vclMinTF.getText());
    MotilityParams.vclLowerTh = Float.parseFloat(vclLowerThTF.getText());
    MotilityParams.vclUpperTh = Float.parseFloat(vclUpperThTF.getText());
    MotilityParams.progressMotility = Float.parseFloat(progressiveMotilityTF.getText());
    MotilityParams.printXY = printXYCB.isSelected();
    float first = Float.parseFloat(firstFrameTF.getText());
    float last = Float.parseFloat(lastFrameTF.getText());
    MotilityParams.firstFrame=(int) (first*MotilityParams.frameRate+1);
    if(last==-1)
      MotilityParams.lastFrame=-1;
    else if(last>0)
      MotilityParams.lastFrame=(int) (last*MotilityParams.frameRate);
    
    MotilityParams.saveParams();
  }

}
