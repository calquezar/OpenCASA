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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import data.Params;

/**
 * @author Carlos Alquezar
 *
 */
public class SettingsWindow extends JFrame {

  JTextField     angleAmplitudeTF      = new JTextField("" + Params.angleAmplitude, 4);
  JTextField     angleDeltaTF          = new JTextField("" + Params.angleDelta, 4);
  JTextField     angleDirectionTF      = new JTextField("" + Params.angleDirection, 4);
  //JTextField     bcfShiftTF            = new JTextField("" + Params.bcf_shift, 4);
  JButton        cancelBtn;
  JCheckBox      compareOppositeDirCB  = new JCheckBox();
  JTextField     dateTF                = new JTextField(Params.date, 8);
  JTextField     frameRateTF           = new JTextField("" + Params.frameRate, 4);
  JTextField     genericTF             = new JTextField(Params.genericField, 8);
  JTextField     maleTF                = new JTextField(Params.male, 8);
  JTextField     maxDisplacementTF     = new JTextField("" + Params.maxDisplacement, 4);
  JTextField     maxSizeTF             = new JTextField("" + Params.maxSize, 4);
  JTextField     micronPerPixelTF      = new JTextField("" + Params.micronPerPixel, 4);
  JTextField     minSizeTF             = new JTextField("" + Params.minSize, 4);
  JTextField     minTrackLengthTF      = new JTextField("" + Params.minTrackLength, 4);
  JTextField     numSamplesBootsTF     = new JTextField("" + Params.NUMSAMPLES, 4);
  JCheckBox      printXYCB             = new JCheckBox();
  JTextField     progressiveMotilityTF = new JTextField("" + Params.progressMotility, 4);
  JButton        saveBtn;
  SettingsWindow sw; // Self reference used in action listeners
  JTextField     vclMinTF              = new JTextField("" + Params.vclMin, 4);
  JTextField     windowSizeTF          = new JTextField("" + Params.wSize, 4);

  /**
   * @param title
   *          - Window's title
   */
  public SettingsWindow(String title) throws HeadlessException {
    super(title);
    sw = this;
    createGUI();
    this.setVisible(true);
    // this.setLocationRelativeTo(null);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = (int) screenSize.getWidth();
    int h = (int) screenSize.getHeight();
    this.setMinimumSize(new Dimension(w/3, h/3));
  }

  private JTabbedPane addTabPane() {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("General", createGeneralBox());
    tabbedPane.addTab("Video", createVideoBox());
    tabbedPane.addTab("Chemotaxis", createChemotaxisBox());
    tabbedPane.addTab("Motility", createMotilityBox());
    return tabbedPane;

  }

  private void createButtons() {
    saveBtn = new JButton("Save");
    // Add action listener
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setParameters();
        Params.saveParams();
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

  /**
   * @return JPanel with all elements
   */
  public JPanel createChemotaxisBox() {
    JPanel box = new JPanel();
    box.setLayout(new GridBagLayout());
    // box.setBackground(new Color(204, 229, 255));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Chemotactic direction (degrees): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(angleDirectionTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Chemotactic cone's amplitude (Degrees): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(angleAmplitudeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Number of bootstrapping resamples: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(numSamplesBootsTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Angle Delta (frames): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(angleDeltaTF, c);
    ///////////////    
    c.gridy += 1;
    label = new JLabel("Compare opposite directions: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    compareOppositeDirCB.setSelected(Params.compareOppositeDirections);
    box.add(compareOppositeDirCB, c);
    ///////////////
    // box.setBorder(BorderFactory.createTitledBorder("Chemotaxis"));

    return box;
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createGeneralBox() {
    JPanel box = new JPanel();
    box.setLayout(new GridBagLayout());
    // box.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Microns per Pixel: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(micronPerPixelTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Minimum cell size (um^2): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(minSizeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Maximum cell size (um^2): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(maxSizeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Male: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(maleTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Date: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(dateTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Generic: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(genericTF, c);
    ///////////////
    // box.setBorder(BorderFactory.createTitledBorder("General"));

    return box;
  }

  private void createGUI() {
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    //////////////////////
    c.gridx = 1;
    c.gridy = 0;
    c.ipadx = 2;
    c.gridheight = 8;
    c.gridwidth = 8;
    // c.gridwidth = 6;
    JTabbedPane tabbedPane = addTabPane();
    this.add(tabbedPane, c);
    //////////////////////
    c.gridheight = 1;
    c.gridwidth = 1;
    createButtons();
    c.gridx = 0;
    c.gridy = 8;
    this.add(cancelBtn, c);
    c.gridx = 9;
    c.gridy = 8;
    this.add(saveBtn, c);

  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createMotilityBox() {
    JPanel box = new JPanel();
    box.setLayout(new GridBagLayout());
    // box.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
//    JLabel label = new JLabel("Minimum shift for BCF (frames): ");
//    box.add(label, c);
//    c.gridx = 1;
//    box.add(bcfShiftTF, c);
    ///////////////
    c.gridy += 1;
    JLabel label = new JLabel("Progressive motility (STR>%): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(progressiveMotilityTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Minimum vcl (um/s): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(vclMinTF, c);
    ///////////////
    // box.setBorder(BorderFactory.createTitledBorder("Motility"));

    return box;
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createVideoBox() {
    JPanel box = new JPanel();
    box.setLayout(new GridBagLayout());
    // box.setBackground(new Color(204, 229, 255));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Frame Rate (frames/s): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(frameRateTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Minimum Track Length(frames): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(minTrackLengthTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Maximum displacement between frames (um): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(maxDisplacementTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Window Size (frames): ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(windowSizeTF, c);
    ///////////////
    c.gridy += 1;
    label = new JLabel("Print XY coords: ");
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    printXYCB.setSelected(Params.printXY);
    box.add(printXYCB, c);
    ///////////////    
    // box.setBorder(BorderFactory.createTitledBorder("Recognition"));

    return box;
  }

  /**
   * @brief Set Params static fields with the values introduced by the user.
   */
  public void setParameters() {
    // General
    Params.frameRate = Float.parseFloat(frameRateTF.getText());
    Params.micronPerPixel = Double.parseDouble(micronPerPixelTF.getText());
    Params.male = maleTF.getText();
    Params.date = dateTF.getText();
    Params.genericField = genericTF.getText();
    Params.minSize = Float.parseFloat(minSizeTF.getText());
    Params.maxSize = Float.parseFloat(maxSizeTF.getText());
    Params.minTrackLength = Integer.parseInt(minTrackLengthTF.getText());
    Params.maxDisplacement = Float.parseFloat(maxDisplacementTF.getText()); // um => pixels
    Params.wSize = Integer.parseInt(windowSizeTF.getText());
    Params.vclMin = Float.parseFloat(vclMinTF.getText());
    Params.angleDelta = Integer.parseInt(angleDeltaTF.getText());
    Params.angleDirection = Float.parseFloat(angleDirectionTF.getText());
    Params.angleAmplitude = Float.parseFloat(angleAmplitudeTF.getText());
    Params.NUMSAMPLES = Integer.parseInt(numSamplesBootsTF.getText());
    Params.compareOppositeDirections = compareOppositeDirCB.isSelected();
//    Params.bcf_shift = Integer.parseInt(bcfShiftTF.getText());
    Params.progressMotility = Float.parseFloat(progressiveMotilityTF.getText());
    Params.printXY = printXYCB.isSelected();
  }
}
