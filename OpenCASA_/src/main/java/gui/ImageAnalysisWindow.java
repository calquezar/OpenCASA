package gui;

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

import data.Spermatozoon;
import functions.ComputerVision;
import functions.FileManager;
import functions.Utils;
import ij.IJ;
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
  private List<ImagePlus>    images;
  private int                imgIndex;
  private JLabel             imgLabel;
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
  private double             resizeFactor;
  protected JSlider            sldThreshold;
  protected List<Spermatozoon> spermatozoa     = new ArrayList<Spermatozoon>();
  protected double             threshold       = -1.0;
  protected String             thresholdMethod = "Otsu";
  private JLabel             title;
  /** */
  protected double             xFactor;

  /** */
  protected double yFactor;

  public ImageAnalysisWindow() {
    imgLabel = new JLabel();
    imgIndex = 0;
    resizeFactor = 0.6; // The size of the showed image will be set to 60% of the screen size
    sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60); //its necessary to initialize here the slider bar 
                                                                // in order to enable the change listener selection
                                                                // for an inherit class
    imgLabel = new JLabel();//The same as slider bar
    
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
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon spermatozoon = (Spermatozoon) j.next();
      spermatozoon.selected = false;
    }
  }
  /**
   * This method set a unique identifier for each spermatozoon in the
   * spermatozoa list
   */
  public void idenfitySperm() {
    int SpermNr = 0;
    for (ListIterator<Spermatozoon> j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      SpermNr++;
      sperm.id = "" + SpermNr;
    }
  }   
  /**
   * This method sets the initial image to be showed.
   */
  public void initImage() {
    setImage(0); // Initialization with the first image available
  }  
  protected void processImage(boolean eventType) {}  

  public void reset() {
    if(impOrig!=null)
      impOrig.close();
    if(impDraw!=null)
      impDraw.close();
    if(impGray!=null)
      impGray.close();
    if(impTh!=null)
      impTh.close();
    if(impOutline!=null)
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

  /**
   * This method deselect all spermatozoa.
   */
  public void selectAll() {
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon spermatozoon = (Spermatozoon) j.next();
      spermatozoon.selected = true;
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

  public void setChangeListener(ChangeListener ch){
    sldThreshold.addChangeListener(ch);
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

  public void setMouseListener(MouseListener ml){
    imgLabel.addMouseListener(ml);
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

  /******************************************************/
  /**
   * This method creates and shows the window.
   */
  public void showWindow() {

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    // RADIO BUTTONS
    JRadioButton btnOtsu = new JRadioButton("Otsu");
    btnOtsu.setSelected(true);
    btnOtsu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        threshold = -1.0;
        thresholdMethod = "Otsu";
        processImage(false);
      }
    });
    JRadioButton btnMinimum = new JRadioButton("Minimum");
    btnMinimum.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        threshold = -1.0;
        thresholdMethod = "Minimum";
        processImage(false);
      }
    });
    // Group the radio buttons.
    ButtonGroup btnGroup = new ButtonGroup();
    btnGroup.add(btnOtsu);
    btnGroup.add(btnMinimum);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    panel.add(btnOtsu, c);
    c.gridy = 1;
    panel.add(btnMinimum, c);
    // THRESHOLD SLIDERBAR
    sldThreshold.setMinorTickSpacing(2);
    sldThreshold.setMajorTickSpacing(10);
    sldThreshold.setPaintTicks(true);
    sldThreshold.setPaintLabels(true);
    // We'll just use the standard numeric labels for now...
    sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 10;
    c.gridheight = 2;
    c.ipady = 10;
    panel.add(sldThreshold, c);

    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 10;
    c.gridheight = 1;
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    title = new JLabel();
    c.gridx = 2;
    c.gridy = 3;
    c.gridwidth = 6;
    c.gridheight = 1;
    c.ipady = 10;
    panel.add(title, c);

    c.gridx = 2;
    c.gridy = 4;
    c.gridwidth = 6;
    c.gridheight = 1;
    c.ipady = 10;
    panel.add(imgLabel, c);
    initImage(); // Initialization with the first image available

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 10;
    c.gridheight = 1;
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    JButton btn1 = new JButton("Previous");
    // Add action listener
    btn1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (imgIndex > 0) {
          reset();
          setImage(--imgIndex);
          processImage(false);
        }
      }
    });
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    panel.add(btn1, c);

    JButton btn2 = new JButton("Next");
    // Add action listener
    btn2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (imgIndex < (images.size() - 1)) {
          reset();
          setImage(++imgIndex);
          processImage(false);
        }
      }
    });
    c.gridx = 9;
    c.gridy = 6;
    panel.add(btn2, c);

    this.setContentPane(panel);
    this.pack();
    this.setVisible(true);
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
    } else {
      cv.thresholdImagePlus(imp, threshold);
    }
  }  

}