package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import ij.IJ;
import ij.ImagePlus;

public class ViabilityWindow {

   /**
   	 * 
   	 */
   MainWindow mainW;
   /**
   	 * 
   	 */
   List<ImagePlus> images;

   /**
   	 */
   public ViabilityWindow(MainWindow mw) {
      mainW = mw;
   }

}
