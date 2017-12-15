/*
 *   OpenCASA software v0.8 for video and image analysis
 *   Copyright (C) 2017  Carlos Alquézar
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import data.Cell;
import functions.ComputerVision;
import functions.FileManager;
import functions.Utils;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * This class implements all the functions related to viability analysis.
 * 
 * @author Carlos Alquezar
 */
public class ImageAnalysisWindow extends JFrame {

  private enum TypeOfAnalysis {
    DIRECTORY, FILE, NONE
  }

  private TypeOfAnalysis analysis = TypeOfAnalysis.NONE;

  /** */
  private List<ImagePlus>      images;
  private int                  imgIndex;
  private JLabel               imgLabel;
  protected JLabel               genericLabel1;
  protected JLabel               genericLabel2;
  protected JLabel               genericLabel3;
  
  /** ImagePlus used to draw over them */
  protected ImagePlus          impDraw         = null;
  /** ImagePlus used to calculate mean gray values */
  protected ImagePlus          impGray         = null;
  /** ImagePlus used to store the original images */
  protected ImagePlus          impOrig         = null;
  /** ImagePlus used to store outlines */
  protected ImagePlus          impOutline      = null;
  /** ImagePlus used to identify spermatozoa */
  protected ImagePlus          impTh           = null;
  /** */
  private double               resizeFactor;
  protected JSlider            sldThreshold;  
  protected JSlider            sldRedThreshold;  
  protected JSlider            sldGreenThreshold;
  protected JSlider            sldBlueThreshold;
  protected JRadioButton btnOtsu;
  protected JRadioButton btnMinimum;
  protected ButtonGroup btnGroup;
  protected JButton prevBtn;
  protected JButton nextBtn;

  protected List<Cell> spermatozoa     = new ArrayList<Cell>();
  protected double             threshold       = -1.0;
  protected double             redThreshold       = -1.0;
  protected double             greenThreshold       = -1.0;
  protected double             blueThreshold       = -1.0;
  protected String             thresholdMethod = "Otsu";
  private JLabel               title;

  protected double             xFactor;
  protected double             yFactor;
  

  public ImageAnalysisWindow() {
    imgLabel = new JLabel();
    imgIndex = 0;
    //The size of the showed image will be set to 60% of the screen size
    resizeFactor = 0.6; 
    //its necessary to initialize here the slider bar in order to enable 
    //the change listener selection for an inherit class
    sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60); 
    sldRedThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60); 
    sldRedThreshold.setForeground(Color.RED);
    sldGreenThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60); 
    sldGreenThreshold.setForeground(Color.GREEN);
    sldBlueThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
    sldBlueThreshold.setForeground(Color.BLUE);
    sldThreshold.setVisible(false); //By default
    sldRedThreshold.setVisible(false); //By default
    sldGreenThreshold.setVisible(false);//By default
    sldBlueThreshold.setVisible(false);//By default
    imgLabel = new JLabel();// The same as slider bar
    btnOtsu = new JRadioButton("Otsu");
    btnMinimum = new JRadioButton("Minimum");
    btnGroup = new ButtonGroup();
    prevBtn = new JButton("Previous");
    nextBtn = new JButton("Next");
    genericLabel1 = new JLabel();
    genericLabel2 = new JLabel();
    genericLabel3 = new JLabel();
    
  }

  private int analyseDirectory() {
    FileManager fm = new FileManager();
    List<ImagePlus> images = fm.loadImageDirectory();
    if (images != null) {
      setImages(images);
      showWindow();
      return 0;
    } else {
      return -1;
    }
  }

  private int analyseFile() {
    FileManager fm = new FileManager();
    List<ImagePlus> images = fm.loadImageFile();
    if (images != null) {
      setImages(images);
      showWindow();
      return 0;
    } else {
      return -1;
    }
  }

  /**
   * This method deselect all spermatozoa.
   */
  public void deselectAll() {
    if(spermatozoa != null & spermatozoa.size()>0){
      for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
        Cell cell = (Cell) j.next();
        cell.selected = false;
      }
    }
  }
  protected void drawImage() {}
  /**
   * This method set a unique identifier for each spermatozoon in the
   * spermatozoa list
   */
  public void idenfitySperm() {
    if(spermatozoa != null & spermatozoa.size()>0){
      int SpermNr = 0;
      for (ListIterator<Cell> j = spermatozoa.listIterator(); j.hasNext();) {
        Cell sperm = (Cell) j.next();
        SpermNr++;
        sperm.id = "" + SpermNr;
      }
    }
  }

  /**
   * This method sets the initial image to be showed.
   */
  public void initImage() {
    setImage(0); // Initialization with the first image available
    processImage(false);
    drawImage();
  }

  protected void nextAction() {
  }

  protected void previousAction() {
  }

  protected void processImage(boolean eventType) {}

  public void reset() {
    if (impOrig != null)
      impOrig.close();
    if (impDraw != null)
      impDraw.close();
    if (impGray != null)
      impGray.close();
    if (impTh != null)
      impTh.close();
    if (impOutline != null)
      impOutline.close();
    threshold = -1.0;
    spermatozoa.clear();
  }

  public int run() {
    int out = selectAnalysis();
    if (out < 0)
      return out;
    switch (analysis) {
      case FILE:
        out = analyseFile();
        break;
      case DIRECTORY:
        out = analyseDirectory();
        break;
      default:
        out = -2;
        break;
    }
    return out;
  }

  public void selectAll() {
    selectAll(spermatozoa);
  }

  /**
   * This method deselect all spermatozoa.
   */
  public void selectAll(List<Cell> sperm) {
    if(sperm != null & sperm.size()>0){
      for (ListIterator j = sperm.listIterator(); j.hasNext();) {
        Cell cell = (Cell) j.next();
        cell.selected = true;
      }
    }
  }

  /**
   * This method opens a set of dialogs to ask the user which analysis has to be
   * carried on.
   */
  public int selectAnalysis() {
    // Ask if user wants to analyze a file or directory
    Object[] options = { "File", "Directory" };
    String question = "What do you want to analyze?";
    String title = "Choose one analysis...";
    final int FILE = 0;
    final int DIR = 1;
    final int MULTIDIR = 2;
    Utils utils = new Utils();
    int sourceSelection = utils.analysisSelectionDialog(options, question, title);
    if (sourceSelection < 0) {
      analysis = TypeOfAnalysis.NONE;
      return -1;
    } else if (sourceSelection == FILE) {
      analysis = TypeOfAnalysis.FILE;
    } else if (sourceSelection == DIR) {
      analysis = TypeOfAnalysis.DIRECTORY;
    }
    return 0;
  }

  public void setChangeListener(ChangeListener ch,JSlider sld) {
    sld.addChangeListener(ch);
  }

  /******************************************************/
  /**
   * This method sets the first image on the list and show it on screen.
   */
  public void setImage() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    double w = screenSize.getWidth();
    double h = screenSize.getHeight();
    int targetWidth = (int) (w * resizeFactor);
    int targetHeight = (int) (h * resizeFactor);
    ImageProcessor ip = impDraw.getProcessor();
    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
    ip = ip.resize(targetWidth, targetHeight);
    impDraw.setProcessor(ip);
    imgLabel.setIcon(new ImageIcon(impDraw.getImage()));
    imgLabel.repaint();
  }

  /******************************************************/
  /**
   * This method sets the image at corresponding index on the list and show it
   * on screen.
   * 
   * @param index
   *          - index of the image on the list of loaded images.
   */
  public void setImage(int index) {
    if (index < 0 || index >= images.size())
      return;
    impOrig = images.get(index).duplicate();
    impOrig.setTitle(images.get(index).getTitle());
    impDraw = impOrig.duplicate();
    title.setText(impOrig.getTitle());
    setImage();
    setResizeFactor();
  }

  /**
   * This method sets the images attribute with the given list of ImagePlus.
   * 
   * @param i
   */
  public void setImages(List<ImagePlus> i) {
    images = i;
  }

  public void setMouseListener(MouseListener ml) {
    imgLabel.addMouseListener(ml);
  }

  public void setRawImage() {
    setImage(imgIndex);
  }

  /**
   * This method calculates the resize factor due to the original image size and
   * the showed image size.
   */
  public void setResizeFactor() {
    double origW = impOrig.getWidth();
    double origH = impOrig.getHeight();
    double resizeW = impDraw.getWidth();
    double resizeH = impDraw.getHeight();
    xFactor = origW / resizeW;
    yFactor = origH / resizeH;
  }

  protected void genericRadioButtonsAction(){}
  
  private void configureSliderBar(JSlider sld){
    sld.setMinorTickSpacing(2);
    sld.setMajorTickSpacing(10);
    sld.setPaintTicks(true);
    sld.setPaintLabels(true);
    // We'll just use the standard numeric labels for now...
    sld.setLabelTable(sld.createStandardLabels(10));
  }
  /******************************************************/
  /**
   * This method creates and shows the window.
   */
  public void showWindow() {

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    // RADIO BUTTONS
    btnOtsu.setSelected(true);
    btnOtsu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        threshold = -1.0;
        thresholdMethod = "Otsu";
        processImage(false);
        genericRadioButtonsAction();
      }
    });
    
    btnMinimum.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        threshold = -1.0;
        thresholdMethod = "Minimum";
        processImage(false);   
        genericRadioButtonsAction();
      }
    });
    // Group the radio buttons.
   
    btnGroup.add(btnOtsu);
    btnGroup.add(btnMinimum);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    panel.add(btnOtsu, c);
    c.gridy = 1;
    panel.add(btnMinimum, c);
    // THRESHOLD SLIDERBARS
    configureSliderBar(sldThreshold);
    configureSliderBar(sldRedThreshold);
    configureSliderBar(sldGreenThreshold);
    configureSliderBar(sldBlueThreshold);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 10;
    c.gridheight = 2;
    c.ipady = 10;
    panel.add(sldRedThreshold, c);
    panel.add(sldThreshold, c); // this two sliders are mutually exclusives
    c.gridy = 2;
    panel.add(sldGreenThreshold, c);
    c.gridy = 4;
    panel.add(sldBlueThreshold, c);
    
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 10;
    c.gridheight = 1;
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    title = new JLabel();
    c.gridx = 2;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 20;
//    c.ipady = 10;
    panel.add(title, c);

    c.gridx = 3;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 20;
//    c.ipady = 10;
    panel.add(genericLabel1, c);
    
    c.gridx = 4;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 20;
//    c.ipady = 10;
    panel.add(genericLabel2, c);   
    
    c.gridx = 5;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 20;
//    c.ipady = 10;
    panel.add(genericLabel3, c);   
    
    c.gridx = 2;
    c.gridy = 8;
    c.gridwidth = 7;
    c.gridheight = 1;
    c.ipady = 10;
    panel.add(imgLabel, c);
    initImage(); // Initialization with the first image available 
    
    c.gridx = 0;
    c.gridy = 9;
    c.gridwidth = 10;
    c.gridheight = 1;
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    // Add action listener
    prevBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (imgIndex > 0) {
          nextBtn.setEnabled(true);
          previousAction();
          reset();
          setImage(--imgIndex);
          processImage(false);
        }else if(imgIndex==0){
          previousAction();
          prevBtn.setEnabled(false);
        }
      }
    });
    c.gridx = 0;
    c.gridy = 10;
    c.gridwidth = 1;
    c.gridheight = 1;
    panel.add(prevBtn, c);

    // Add action listener
    nextBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (imgIndex < (images.size() - 1)) {
          prevBtn.setEnabled(true);
          nextAction();
          reset();
          setImage(++imgIndex);
          processImage(false);
        }else if(imgIndex==(images.size()-1)){
          nextAction();
          nextBtn.setEnabled(false);
        }
      }
    });
    c.gridx = 9;
    c.gridy = 10;
    panel.add(nextBtn, c);

    this.setContentPane(panel);
    this.pack();
    this.setVisible(true);
  }
  private void setSlidersAutoThreshold(){
    redThreshold = threshold;
    greenThreshold = threshold;
    blueThreshold = threshold;
    sldThreshold.setValue((int) threshold);
    sldRedThreshold.setValue((int) threshold);
    sldGreenThreshold.setValue((int) threshold);
    sldBlueThreshold.setValue((int) threshold);
  }
  /******************************************************/
  /**
   * This method choose between autoThreshold or apply a particular threshold to
   * the given ImagePlus depending if this value has been set before or not.
   * 
   * @param imp
   *          ImagePlus to be thresholded.
   */
  public void thresholdImagePlus(ImagePlus imp) {
    ComputerVision cv = new ComputerVision();
    if (threshold == -1) {
      threshold = cv.autoThresholdImagePlus(imp, thresholdMethod);
      setSlidersAutoThreshold();
    } else {
      cv.thresholdImagePlus(imp, threshold);
    }
  }

}