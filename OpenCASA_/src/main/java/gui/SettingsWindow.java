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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * @author Carlos Alquezar
 *
 */
public class SettingsWindow extends JFrame {

  JButton cancelBtn;
  JButton saveBtn;
  SettingsWindow sw; // Self reference used in action listeners
  MotilitySettings ms;
  ChemotaxisSettings cs;
  MorphometrySettings mphs;
  ViabilitySettings vs;
  MultifluoSettings mtfs;

  public SettingsWindow(String title) throws HeadlessException {
    super(title);
    sw = this;
    ms = new MotilitySettings();
    cs = new ChemotaxisSettings();
    mphs = new MorphometrySettings();
    vs = new ViabilitySettings();
    mtfs = new MultifluoSettings();
    createGUI();
    this.setVisible(true);
    //this.setLocationRelativeTo(null);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = (int) screenSize.getWidth();
    int h = (int) screenSize.getHeight();
    this.setMinimumSize(new Dimension(w / 2, h));
  }

  private JTabbedPane addTabPane() {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Motility Module", ms.createGUI());
    tabbedPane.addTab("Chemotaxis Module", cs.createGUI());
    tabbedPane.addTab("Morphometry Module",mphs.createGUI());
    tabbedPane.addTab("Viability Module", vs.createGUI());
    tabbedPane.addTab("Multifluo Module", mtfs.createGUI());
    return tabbedPane;
  }

  private void createButtons() {
    saveBtn = new JButton("Save");
    // Add action listener
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setParameters();
        sw.dispatchEvent(new WindowEvent(sw, WindowEvent.WINDOW_CLOSING));
      }
    });
    cancelBtn = new JButton("Cancel");
    // Add action listener
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sw.dispatchEvent(new WindowEvent(sw, WindowEvent.WINDOW_CLOSING));
      }
    });
  }

  private void createGUI() {
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    //////////////////////
    c.gridx = 1;
    c.gridy = 0;
    c.ipadx = 2;
    c.gridheight = 10;
    c.gridwidth = 10;
    JTabbedPane tabbedPane = addTabPane();
    this.add(tabbedPane, c);
    //////////////////////
    c.gridheight = 1;
    c.gridwidth = 1;
    createButtons();
    c.gridx = 0;
    c.gridy = 10;
    this.add(cancelBtn, c);
    c.gridx = 11;
    this.add(saveBtn, c);
  }

  public void setParameters() {
    ms.setParameters();
    cs.setParameters();
    mphs.setParameters();
    vs.setParameters();
    mtfs.setParameters();
  }
}
