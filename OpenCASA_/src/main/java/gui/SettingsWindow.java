package gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.Params;
import ij.IJ;

/**
 * @author Carlos Alquezar
 *
 */
public class SettingsWindow extends JFrame {


  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  JTextField angleAmplitudeTF = new JTextField("" + Params.angleAmplitude, 4);
  JTextField angleDeltaTF = new JTextField("" + Params.angleDelta, 4);
  // Chemotaxis
  JTextField angleDirectionTF = new JTextField("" + Params.angleDirection, 4);
  JCheckBox compareOppositeDirCB = new JCheckBox();
  // Motility
  JTextField bcfShiftTF = new JTextField("" + Params.bcf_shift, 4);
  // General
  JTextField frameRateTF = new JTextField("" + Params.frameRate, 4);
  JTextField maxDisplacementTF = new JTextField("" + Params.maxDisplacement, 4);
  JTextField maxSizeTF = new JTextField("" + Params.maxSize, 4);
  JTextField micronPerPixelTF = new JTextField("" + Params.micronPerPixel, 4);
  JTextField maleTF = new JTextField(Params.male,8);
  JTextField dateTF = new JTextField(Params.date,8);
  JTextField genericTF = new JTextField(Params.genericField,8);
  // Recognition
  JTextField minSizeTF = new JTextField("" + Params.minSize, 4);
  JTextField minTrackLengthTF = new JTextField("" + Params.minTrackLength, 4);
  JTextField numSamplesBootsTF = new JTextField("" + Params.NUMSAMPLES, 4);
  JTextField progressiveMotilityTF = new JTextField("" + Params.progressMotility, 4);
  JTextField vclMinTF = new JTextField("" + Params.vclMin, 4);
  // Filtering
  JTextField windowSizeTF = new JTextField("" + Params.wSize, 4);
  SettingsWindow sw; // Self reference used in action listeners

  /**
   * @param title - Window's title
   */
  public SettingsWindow(String title) throws HeadlessException {
    super(title);
    sw = this;
    this.setVisible(true);
//    setLocationRelativeTo(null);
//    this.setPreferredSize(new Dimension(600, 300));
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createChemotaxisBox() {
    JPanel box = new JPanel();
    box.setBackground(new Color(204, 229, 255));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Chemotactic direction (degrees): ");
    box.add(label, c);
    c.gridx += 1;
    box.add(angleDirectionTF, c);
    ///////////////
    label = new JLabel("Chemotactic cone's amplitude (Degrees): ");
    c.gridx += 1;
    box.add(label, c);
    c.gridx += 1;
    box.add(angleAmplitudeTF, c);
    ///////////////
    label = new JLabel("Number of repetitions for bootstrapping: ");
    c.gridx += 1;
    box.add(label, c);
    c.gridx += 1;
    box.add(numSamplesBootsTF, c);
    ///////////////
    label = new JLabel("Compare opposite directions: ");
    c.gridx += 1;
    box.add(label, c);
    c.gridx += 1;
    compareOppositeDirCB.setSelected(Params.compareOppositeDirections);
    box.add(compareOppositeDirCB, c);
    
    ///////////////
    box.setBorder(BorderFactory.createTitledBorder("Chemotaxis"));

    return box;
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createFilterBox() {
    JPanel box = new JPanel();
    // box.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Window Size (frames): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(windowSizeTF, c);
    ///////////////
    label = new JLabel("Minimum vcl (um/s): ");
    c.gridx = 2;
    box.add(label, c);
    c.gridx = 3;
    box.add(vclMinTF, c);
    ///////////////
    label = new JLabel("Angle Delta (frames): ");
    c.gridx = 4;
    box.add(label, c);
    c.gridx = 5;
    box.add(angleDeltaTF, c);
    ///////////////
    box.setBorder(BorderFactory.createTitledBorder("Filtering"));

    return box;
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createGeneralBox() {
    JPanel box = new JPanel();
    // box.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Frame Rate (frames/s): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(frameRateTF, c);
    ///////////////
    label = new JLabel("Micron per Pixel: ");
    c.gridx = 2;
    box.add(label, c);
    c.gridx = 3;
    box.add(micronPerPixelTF, c);
    ///////////////
     label = new JLabel("Male: ");
     c.gridx=4;
     box.add(label,c);
     c.gridx=5;
     box.add(maleTF,c);
     ///////////////
     label = new JLabel("Date: ");
     c.gridx=6;
     box.add(label,c);
     c.gridx=7;
     box.add(dateTF,c);
     ///////////////
     label = new JLabel("Generic: ");
     c.gridx=6;
     box.add(label,c);
     c.gridx=7;
     box.add(genericTF,c);     
    ///////////////
    box.setBorder(BorderFactory.createTitledBorder("General"));

    return box;
  }

  /**
   * @return JPanel with all elements
   */
  public JPanel createMotilityBox() {
    JPanel box = new JPanel();
    // box.setBackground(new Color(229,255,204));
    GridBagConstraints c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Minimum shift for BCF (frames): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(bcfShiftTF, c);
    ///////////////
    label = new JLabel("Progressive motility (STR>%): ");
    c.gridx = 2;
    box.add(label, c);
    c.gridx = 3;
    box.add(progressiveMotilityTF, c);
    ///////////////
    box.setBorder(BorderFactory.createTitledBorder("Motility"));

    return box;
  }

  /** 
   * @return JPanel with all elements
   */
  public JPanel createRecognitionBox() {
    JPanel box = new JPanel();
    box.setBackground(new Color(204, 229, 255));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    ///////////////
    JLabel label = new JLabel("Minimum Size (um^2): ");
    box.add(label, c);
    c.gridx = 1;
    box.add(minSizeTF, c);
    ///////////////
    label = new JLabel("Maximum Size (um^2): ");
    c.gridx = 2;
    box.add(label, c);
    c.gridx = 3;
    box.add(maxSizeTF, c);
    ///////////////
    label = new JLabel("Minimum Track Length(frames): ");
    c.gridy = 1;
    c.gridx = 0;
    box.add(label, c);
    c.gridx = 1;
    box.add(minTrackLengthTF, c);
    ///////////////
    label = new JLabel("Maximum displacement between frames (um): ");
    c.gridx = 2;
    box.add(label, c);
    c.gridx = 3;
    box.add(maxDisplacementTF, c);
    ///////////////
    box.setBorder(BorderFactory.createTitledBorder("Recognition"));

    return box;
  }
  
  /**
   * @brief Build Settings window with all parameters.
   */
  public void run() {

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 0;
    c.gridx = 0;
    c.gridy = 0;
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(createGeneralBox(), c);
    c.gridy += 1;
    panel.add(createRecognitionBox(), c);
    c.gridy += 1;
    panel.add(createFilterBox(), c);
    c.gridy += 1;
    panel.add(createChemotaxisBox(), c);
    c.gridy += 1;
    panel.add(createMotilityBox(), c);
    JButton saveBtn = new JButton("Save");
    // Add action listener
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setParameters();
        Params.saveParams();
        sw.dispatchEvent(new WindowEvent(sw, WindowEvent.WINDOW_CLOSING));
      }
    });
    JButton cancelBtn = new JButton("Cancel");
    // Add action listener
    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sw.dispatchEvent(new WindowEvent(sw, WindowEvent.WINDOW_CLOSING));
      }
    });
    c.gridx = 1;
    c.gridy += 1;
    panel.add(saveBtn, c);
    c.gridx = 2;
    panel.add(cancelBtn, c);
    // panel.setBackground(new Color(255,204,153));
    this.setContentPane(panel);
    this.pack();

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
    // Recognition
    Params.minSize = Float.parseFloat(minSizeTF.getText());
    Params.maxSize = Float.parseFloat(maxSizeTF.getText());
    Params.minTrackLength = Integer.parseInt(minTrackLengthTF.getText());
    Params.maxDisplacement = Float.parseFloat(maxDisplacementTF.getText()); // um => pixels
    // Filtering
    Params.wSize = Integer.parseInt(windowSizeTF.getText());
    Params.vclMin = Float.parseFloat(vclMinTF.getText());
    Params.angleDelta = Integer.parseInt(angleDeltaTF.getText());
    // Chemotaxis
    Params.angleDirection = Float.parseFloat(angleDirectionTF.getText());
    Params.angleAmplitude = Float.parseFloat(angleAmplitudeTF.getText());
    Params.NUMSAMPLES = Integer.parseInt(numSamplesBootsTF.getText());
    Params.compareOppositeDirections = compareOppositeDirCB.isSelected();
    // Motility
    Params.bcf_shift = Integer.parseInt(bcfShiftTF.getText());
    Params.progressMotility = Float.parseFloat(progressiveMotilityTF.getText());

  }
}
