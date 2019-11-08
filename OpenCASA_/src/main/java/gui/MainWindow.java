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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import analysis.Accumulation;
import analysis.Chemotaxis;
import analysis.Motility;
import data.AccumulationParams;
import data.CellCountParams;
import data.ChemotaxisParams;
import data.MorphometryParams;
import data.MotilityParams;
import data.MultifluoParams;
import data.PersistentRandomWalker;
import data.Simulation;
import data.ViabilityParams;
import functions.Utils;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.process.LUT;

/**
 * This window shows all functional modules available.
 * 
 * @author Carlos Alquezar
 */
public class MainWindow extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * @brief Self reference used in action listeners to show and hide main
   *        window.
   */
  private MainWindow mw;

  /**
   * @brief Constructor. The main graphical user interface is created.
   * @param title
   *          - String that is used as window's title
   */
  public MainWindow(String title) throws HeadlessException {
    super(title);
    createGUI();
    this.setPreferredSize(new Dimension(450, 600));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.pack();
    this.setVisible(true);
    this.setLocationRelativeTo(null);
    mw = this;
    
    MotilityParams.resetParams();
    ChemotaxisParams.resetParams();
    ViabilityParams.resetParams();
    MorphometryParams.resetParams();
    MultifluoParams.resetParams();
    AccumulationParams.resetParams();
    CellCountParams.resetParams();
  }

  /**
   * @brief This method add to the given JPanel a button with the specified
   *        parameters
   * @param label
   *          - String that is shown as button's label
   * @param gridx
   *          - relative layout's x location
   * @param gridy
   *          - relative layout's y location
   * @param background
   *          - Background color
   * @param iconPath
   *          - Path relative to the icon's image
   * @param panel
   *          - panel where the button is going to be added
   */
  private void addButton(final String label, int gridx, int gridy, Color background, String iconPath, JPanel panel) {

    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 0;
    c.gridx = gridx;
    c.gridy = gridy;
    JButton btn = new JButton(label);
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setBackground(background);
    try {
      // Image img =
      // ImageIO.read(getClass().getResource("/resources/motility.png"));
      Image img = ImageIO.read(getClass().getResource(iconPath));
      btn.setIcon(new ImageIcon(img));
    } catch (Exception ex) {
      // IJ.handleException(ex);
      // System.out.println(ex);
    }
    // Add action listener
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (label.equals("Accumulation")) {
          LUT lut = getLut(getClass().getResource("/Jet.lut"));
          Accumulation ac = new Accumulation(lut);
          try {
            AccumulationParams.setGlobalParams();
            mw.setVisible(false);
            ac.selectAnalysis();
            ac.execute();
            mw.setVisible(true);
          } catch (Exception e1) {
            IJ.handleException(e1);
          }
        } else if (label.equals("Concentration")) {
          CellCountParams.setGlobalParams();
          mw.setVisible(false);
          CellCountWindow ccW = new CellCountWindow();
          ccW.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
              if (mw != null) {
                mw.setVisible(true);
              }
            }
          });
          int out = ccW.run();
          if (out < 0) {
            mw.setVisible(true);
          }
        } else if (label.equals("Chemotaxis")) {
          Chemotaxis ch = new Chemotaxis();
          try {
            ChemotaxisParams.setGlobalParams();
            //ChemotaxisParams.printParams();
            //Params.printParams();
            mw.setVisible(false);
            ch.selectAnalysis();// this method has to be run outside the
                                // execute() method because it
                                // is a GUI method and has to be run on the EDT
            ch.execute();
            mw.setVisible(true);
          } catch (Exception e1) {
            IJ.handleException(e1);
          }
        } else if (label.equals("Motility")) {
          Motility mot = new Motility();
          try {
            MotilityParams.setGlobalParams();
            //MotilityParams.printParams();
            //Params.printParams();
            mw.setVisible(false);
            mot.selectAnalysis();
            mot.execute();
            mw.setVisible(true);
          } catch (Exception e1) {
            IJ.handleException(e1);
          }
        } else if (label.equals("Viability")) {
          ViabilityParams.setGlobalParams();
          //ViabilityParams.printParams();
          //Params.printParams();
          mw.setVisible(false);
          ViabilityWindow viabilityW = new ViabilityWindow();
          viabilityW.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
              if (mw != null)
                mw.setVisible(true);
            }
          });
          int out = viabilityW.run();
          if (out < 0) {
            mw.setVisible(true);
          }
        } else if (label.equals("Morphometry")) {
          MorphometryParams.setGlobalParams();
          //MorphometryParams.printParams();
          //Params.printParams();
          mw.setVisible(false);
          MorphWindow morphW = new MorphWindow();
          morphW.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
              if (mw != null)
                mw.setVisible(true);
            }
          });
          int out = morphW.run();
          if (out < 0) {
            mw.setVisible(true);
          }
        }else if (label.equals("Multifluo")) {
          MultifluoParams.setGlobalParams();
          //MultifluoParams.printParams();
          //Params.printParams();
          mw.setVisible(false);
          MultifluoWindow multiFluoW = new MultifluoWindow();
          multiFluoW.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
              if (mw != null)
                mw.setVisible(true);
            }
          });
          int out = multiFluoW.run();
          if (out < 0) {
            mw.setVisible(true);
          }
        } else if (label.equals("Simulation")) {
          simulate();
        } else if (label.equals("Settings")) {
          SettingsWindow sw = new SettingsWindow("Settings");
          sw.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
              if (mw != null)
                mw.setVisible(true);
            }
          });
          mw.setVisible(false);
          // sw.run();
        } else if (label.equals("Scatter Plot")) {
          Utils.scatter();
        } 
      }
    });
    panel.add(btn, c);
  }

  /**
   * @brief This method creates the main user interface.
   */
  private void createGUI() {
    String parentDir = "";
    //String parentDir = "/resources";
    JPanel panel = new JPanel(new GridBagLayout());
    int x = -1;
    int y = -1;
    // accumulation icon made by Freepik from www.flaticon.com
    addButton("Accumulation", ++x%2,++y/2, new Color(255, 204, 153), parentDir + "/accumulation.png", panel);    
    // chemotaxis icon made by Those Icons from www.flaticon.com
    addButton("Chemotaxis", ++x%2,++y/2, new Color(255, 255, 255), parentDir + "/chemotaxis.png", panel);   
    // concentration icon made by xnimrodx from www.flaticon.com
    addButton("Concentration",++x%2,++y/2, new Color(255, 204, 153), parentDir + "/concentration.png", panel);
    // morphometry icon made by Cursor Creative from www.flaticon.com
    addButton("Morphometry", ++x%2,++y/2, new Color(255, 255, 255), parentDir + "/morphometry.png", panel);   
    // motility icon made by Freepik from www.flaticon.com
    addButton("Motility",++x%2,++y/2, new Color(255, 255, 255), parentDir + "/motility.png", panel);   
    // multifluo icon made by Prosymbols from www.flaticon.com
    addButton("Multifluo", ++x%2,++y/2, new Color(255, 204, 153), parentDir + "/multifluo.png", panel);    
    // scatter icon made by Flat Icons from www.flaticon.com
    addButton("Scatter Plot", ++x%2,++y/2, new Color(255, 204, 153), parentDir + "/scatter.png", panel);
    // simulation icon made by Freepik from www.flaticon.com
    addButton("Simulation", ++x%2,++y/2, new Color(255, 255, 255), parentDir + "/simulation.png", panel);
    // viability icon made by Freepik from www.flaticon.com
    addButton("Viability", ++x%2,++y/2, new Color(255, 255, 255), parentDir + "/viability.png", panel);
    // settings icon made by Freepik from www.flaticon.com
    addButton("Settings", ++x%2,++y/2, new Color(255, 204, 153), parentDir + "/settings.png", panel);
    this.setContentPane(panel);
    panel.setBackground(Color.white);
  }

  /**
   * @brief Shows a Generic Dialog to ask user which simulation parameters have
   *        to be used for the simulation.
   */
  private void simulate() {
    GenericDialog gd = new GenericDialog("Set Simulation parameters");
    gd.addNumericField("Beta", 0, 2);
    gd.addNumericField("Responsiveness (%)", 50, 2);
    gd.addNumericField("Length of the simulation (frames)", 500, 0);
    gd.showDialog();
    if (gd.wasCanceled())
      return;
    double beta = gd.getNextNumber();
    double responsiveness = gd.getNextNumber() / 100; // value must be between
                                                      // [0,1]
    int length = (int) gd.getNextNumber();
    Simulation sim = new PersistentRandomWalker(beta, responsiveness, length);
    try {
      sim.run();
    } catch (Exception e1) {
      IJ.handleException(e1);
      // e1.printStackTrace();
    }
  }
  
  /**
   * 
   * @param url
   * @return
   */
  private LUT getLut(URL url) {
    byte r[] = new byte[256], g[] = new byte[256], b[] = new byte[256];
    try {
      Scanner sc = new Scanner(url.openStream());

      while (sc.hasNextInt()) {
        int i = sc.nextInt();
        r[i] = (byte) sc.nextInt();
        g[i] = (byte) sc.nextInt();
        b[i] = (byte) sc.nextInt();
      }

      sc.close();
    } catch (Exception e) {
      IJ.handleException(e);
    }
    return new LUT(r, g, b);
  }
  
}
