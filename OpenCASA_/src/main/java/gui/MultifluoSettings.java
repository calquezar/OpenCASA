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

package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.MultifluoParams;

public class MultifluoSettings extends JPanel {

  JTextField maxSizeTF = new JTextField("" + MultifluoParams.maxSize, 4);
  JTextField micronPerPixelTF = new JTextField("" + MultifluoParams.micronPerPixel, 4);
  JTextField minSizeTF = new JTextField("" + MultifluoParams.minSize, 4);
  JTextField dateTF = new JTextField(MultifluoParams.date, 8);
  JTextField genericTF = new JTextField(MultifluoParams.genericField, 8);
  JTextField maleTF = new JTextField(MultifluoParams.male, 8);

  public MultifluoSettings() {
    super();
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
    MultifluoParams.micronPerPixel = Double.parseDouble(micronPerPixelTF.getText());
    MultifluoParams.male = maleTF.getText();
    MultifluoParams.date = dateTF.getText();
    MultifluoParams.genericField = genericTF.getText();
    MultifluoParams.minSize = Float.parseFloat(minSizeTF.getText());
    MultifluoParams.maxSize = Float.parseFloat(maxSizeTF.getText());
    MultifluoParams.saveParams();
  }

}
