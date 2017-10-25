package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import analysis.Chemotaxis;
import analysis.Motility;
import data.Params;
import data.PersistentRandomWalker;
import data.Simulation;
import functions.FileManager;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;

/**
 * This window shows all functional modules available.
 * @author Carlos Alquezar
 */
public class MainWindow extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** @brief Self reference used in action listeners to show and hide main window. */
  private MainWindow mw;

  /**
   * @brief Constructor. The main graphical user interface is created.
   * @param title - String that is used as window's title
   */
  public MainWindow(String title) throws HeadlessException {
    super(title);
    createGUI();
    this.setPreferredSize(new Dimension(600, 300));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.pack();
    this.setVisible(true);
    this.setLocationRelativeTo(null);
    mw = this;
    Params.resetParams();
  }
  
  /**
   * @brief This method add to the given JPanel a button with the specified parameters
   * @param label - String that is shown as button's label
   * @param gridx - relative layout's x location
   * @param gridy - relative layout's y location
   * @param background - Background color
   * @param iconPath - Path relative to the icon's image
   * @param panel - panel where the button is going to be added
   */
  private void addButton(final String label, int gridx, int gridy, Color background, String iconPath, JPanel panel) {

    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 0;
    c.gridx = gridx;
    c.gridy = gridy;
    JButton btn = new JButton(label);
    btn.setBackground(background);
    try {
      // Image img =
      // ImageIO.read(getClass().getResource("/resources/motility.png"));
      Image img = ImageIO.read(getClass().getResource(iconPath));
      btn.setIcon(new ImageIcon(img));
    } catch (Exception ex) {
      System.out.println(ex);
    }
    // Add action listener
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (label.equals("Chemotaxis")) {
          Chemotaxis ch = new Chemotaxis();
          try {
            mw.setVisible(false);
            ch.selectAnalysis();
            ch.execute();
            mw.setVisible(true);
          } catch (Exception e1) {
            IJ.handleException(e1);
          }
        } else if (label.equals("Motility")) {
          Motility mot = new Motility();
          try {
            mw.setVisible(false);
            mot.selectAnalysis();
            mot.execute();
            mw.setVisible(true);
          } catch (Exception e1) {
            IJ.handleException(e1);
          }          
        } else if (label.equals("Viability")) {
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
          if(out<0){
            mw.setVisible(true);
          }
        } else if (label.equals("Morphometry")) {
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
          if(out<0){
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
          sw.run(); 
        }
      }
    });
    panel.add(btn, c);
  }

  /**
   * @brief This method creates the main user interface.
   */
  private void createGUI() {
    JPanel panel = new JPanel(new GridBagLayout());
    addButton("Motility", 0, 0, new Color(255, 255, 255), "/motility.png", panel);
    addButton("Chemotaxis", 1, 0, new Color(255, 255, 255), "/chemotaxis.png", panel);
    addButton("Viability", 0, 1, new Color(255, 255, 255), "/viability.png", panel);
    addButton("Morphometry", 1, 1, new Color(255, 255, 255), "/morphometry.png", panel);
    addButton("Simulation", 0, 2, new Color(255, 255, 255), "/Settings.png", panel);
    addButton("Settings", 1, 2, new Color(255, 204, 153), "/Settings.png", panel);
    this.setContentPane(panel);
  }
  
  /**
   * @brief Shows a Generic Dialog to ask user which simulation parameters have to be used for the simulation.
   */  
  private void simulate(){
    GenericDialog gd = new GenericDialog("Set Simulation parameters");
    gd.addNumericField("Beta", 0, 2);
    gd.addNumericField("Responsiveness (%)", 50, 2);
    gd.addNumericField("Length of the simulation (frames)", 500, 0);
    gd.showDialog();
    if (gd.wasCanceled())
      return;
    double beta = gd.getNextNumber();
    double responsiveness = gd.getNextNumber() / 100; // value must be between [0,1]
    int length = (int) gd.getNextNumber();
    Simulation sim = new PersistentRandomWalker(beta, responsiveness, length);
    try {
      sim.run();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }
}
