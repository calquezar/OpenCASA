package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Params;
import data.Spermatozoon;
import functions.ComputerVision;
import functions.FileManager;
import functions.Paint;
import functions.Utils;
import functions.VideoRecognition;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * This class implements all the functions related to morphometry analysis.
 * @author Carlos Alquezar
 */
public class MorphWindow extends JFrame implements ChangeListener, MouseListener {

  /** ImagePlus used to store the original images */
  private ImagePlus impOrig = null;
  /** ImagePlus used to draw over them */
  private ImagePlus impDraw = null;
  /** ImagePlus used to calculate mean gray values */
  private ImagePlus impGray = null;
  /** ImagePlus used to identify spermatozoa */
  private ImagePlus impTh   = null;
  /** ImagePlus used to store outlines */
  private ImagePlus       impOutline      = null;
  private double          threshold       = -1.0;
  private String          thresholdMethod = "Otsu";
  private JLabel          imgLabel;
  private JLabel          title;
  private boolean         isThresholding  = false;
  /** */
  private JSlider         sldThreshold;
  /** */
  private List<ImagePlus> images;
  /** */
  private int             imgIndex;
  // Resize parameters
  /** */
  private double             resizeFactor;
  /** */
  private double             xFactor;
  /** */
  private double             yFactor;
  /** Variable used to store spermatozoa */
  private List<Spermatozoon> spermatozoa   = new ArrayList<Spermatozoon>();
  /** Resultstable used to show results */
  private ResultsTable       morphometrics = new ResultsTable();

  /**
   * Constructor. The main graphical user interface is created.
   */
  public MorphWindow() throws HeadlessException {
    super();
    // Setting image
    imgLabel = new JLabel();
    imgLabel.addMouseListener(this);
    imgIndex = 0;
    resizeFactor = 0.6; // The size of the showed image will be set to 60% of the screen size
  }

  /******************************************************/
  /**
   * This method checks if a click has been done over a cell. In that case, the method select/deselect 
   * the cell and add the morphometrics to resultsTable if it has been selected.
   * @param x
   * @param y
   */
  public void checkSelection(int x, int y) {
    Point click = new Point(x, y);
    Utils utils = new Utils();
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      if (isClickInside(sperm, click)) {
        sperm.selected = !sperm.selected;
        if (sperm.selected) {
          Spermatozoon spermatozoon = utils.getSpermatozoon(sperm.id, spermatozoa);
          generateResults(spermatozoon);
        }
        break;
      }
    }
  }

  /**
   * This method closes all ImagePlus.
   */
  public void close() {
    impOrig.changes = false; //This is necessary to avoid Save changes? dialog when closing
    impDraw.changes = false; //This is necessary to avoid Save changes? dialog when closing
    impOrig.close();
    impDraw.close();
  }

  /******************************************************/
  /**
   * This method refreshes the showed image after a mouse click event
   */
  private void doMouseRefresh() {

    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          impDraw = impOrig.duplicate();
          Paint paint = new Paint();
          paint.drawOutline(impDraw, impOutline);
          paint.drawBoundaries(impDraw, spermatozoa);
          setImage();
          isThresholding = false;
        }
      });
      t1.start();
    }
  }

  /**
   * This method refreshes the showed image after changing the threshold with the sliderbar 
   */
  private void doSliderRefresh() {
    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          resetSelections();
          processImage(true);
          isThresholding = false;
        }
      });
      t1.start();
    }
  }

  /******************************************************/
  /**
   * This method adds the morphometric values of the given spermatozoon to the results table
   * @param spermatozoon
   */
  public void generateResults(Spermatozoon spermatozoon) {

    ComputerVision cv = new ComputerVision();
    double total_meanGray = (double) cv.getMeanGrayValue(spermatozoon, impGray, impTh);
    double total_area = spermatozoon.total_area * Math.pow(Params.micronPerPixel, 2);
    double total_perimeter = spermatozoon.total_perimeter * Params.micronPerPixel;
    double total_feret = spermatozoon.total_feret * Params.micronPerPixel;
    double total_minFeret = spermatozoon.total_minFeret * Params.micronPerPixel;
    double total_ellipticity = total_feret / total_minFeret;
    double total_roughness = 4 * Math.PI * total_area / (Math.pow(total_perimeter, 2));
    double total_elongation = (total_feret - total_minFeret) / (total_feret + total_minFeret);
    double total_regularity = (Math.PI * total_feret * total_minFeret) / (4 * total_area);

    morphometrics.incrementCounter();
    morphometrics.addValue("ID", spermatozoon.id);
    morphometrics.addValue("Threshold", threshold);
    morphometrics.addValue("total_meanGray", total_meanGray);
    morphometrics.addValue("total_area(um^2)", total_area);
    morphometrics.addValue("total_perimeter(um)", total_perimeter);
    morphometrics.addValue("total_length(um)", total_feret);
    morphometrics.addValue("total_width(um)", total_minFeret);
    morphometrics.addValue("total_ellipticity", total_ellipticity);
    morphometrics.addValue("total_roughness", total_roughness);
    morphometrics.addValue("total_elongation", total_elongation);
    morphometrics.addValue("total_regularity", total_regularity);
    FileManager fm = new FileManager();
    morphometrics.addValue("Sample", fm.getParentDirectory(impOrig.getTitle()));
    morphometrics.addValue("Filename", fm.getFilename(impOrig.getTitle()));
    if(!Params.male.isEmpty())
      morphometrics.addValue("Male", Params.male);
    if(!Params.date.isEmpty())
      morphometrics.addValue("Date", Params.date);
    if(!Params.genericField.isEmpty())
      morphometrics.addValue("Generic Field", Params.genericField);    
    morphometrics.show("Morphometrics");
  }

  /**
   * This method set a unique identifier for each spermatozoon in the spermatozoa list
   */
  public void idenfitySperm() {
    int SpermNr = 0;
    for (ListIterator<Spermatozoon> j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      SpermNr++;
      sperm.id = "" + SpermNr;
    }
  }

  /******************************************************/
  /**
   * This method sets the initial image to be showed.
   */
  public void initImage() {
    setImage(0); // Initialization with the first image available
    processImage(false);
  }

  /******************************************************/
  /**
   * This method returns true if the given point is inside the boundaries of the given spermatozoon
   * @param sperm - Spermatozoon
   * @param click -  Point
   * @return True if the point is inside the boundaries of the spermatozoon. Otherwise, it returns false
   */
  public boolean isClickInside(Spermatozoon sperm, Point click) {
    // Get boundaries
    double offsetX = (double) sperm.bx;
    double offsetY = (double) sperm.by;
    int w = (int) sperm.width;
    int h = (int) sperm.height;
    // correct offset
    int pX = (int) (click.getX() - offsetX);
    int pY = (int) (click.getY() - offsetY);
    // IJ.log("offsetX: "+offsetX+" ; offsetY: "+offsetY+" ;w: "+w+"; h:
    // "+h+"px: "+pX+"; py: "+pY);
    Rectangle r = new Rectangle(w, h);
    return r.contains(new Point(pX, pY));
  }

  /******************************************************
   * MOUSE LISTENER
   ******************************************************/
  /**
   * This method manage a mouse click event.
   */
  public void mouseClicked(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    // System.out.println("X: "+ x+"; Y: "+ y);
    int realX = (int) (x * xFactor);
    int realY = (int) (y * yFactor);
    // System.out.println("realX: "+ realX+"; realY: "+ realY);
    checkSelection(realX, realY);
    doMouseRefresh();
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {
  }

  /******************************************************/
  /**
   * This method updates the showed image depending of the type of event ocurred.
   * @param eventType This parameter is used to differentiate between a slider event (true) or a click event (false)
   */
  public void processImage(boolean eventType) {
    if (eventType || threshold == -1 ) {//If true, the threshold has changed or it needs to be calculated
      ComputerVision cv = new ComputerVision();
      impTh = impOrig.duplicate();
      cv.convertToGrayscale(impTh);
      impGray = impTh.duplicate();
      thresholdImagePlus(impTh);
      // Update sliderbar with the new threshold
      sldThreshold.setValue((int) threshold);
      VideoRecognition vr = new VideoRecognition();
      List<Spermatozoon>[] sperm = vr.detectSpermatozoa(impTh);
      if (sperm != null)
        spermatozoa = sperm[0];
      // Calculate outlines
      impOutline = impTh.duplicate();
      cv.outlineThresholdImage(impOutline);
      idenfitySperm();
    }
    impDraw = impOrig.duplicate();
    Paint paint = new Paint();
    paint.drawOutline(impDraw, impOutline);
    paint.drawBoundaries(impDraw, spermatozoa);
    setImage();
  }

  /**
   * 
   */
  public void reset() {
//    impOrig.changes=false;
//    impDraw.changes=false;
//    impGray.changes=false;
//    impTh.changes=false;
//    impOutline.changes=false;
    impOrig.close();
    impDraw.close();
    impGray.close();
    impTh.close();
    impOutline.close();
    threshold = -1.0;
    spermatozoa.clear();
  }

  /**
   * This method deselect all spermatozoa.
   */
  public void resetSelections() {
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon spermatozoon = (Spermatozoon) j.next();
      spermatozoon.selected = false;
    }
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
   * This method sets the image at corresponding index on the list and show it on screen.
   * @param index - index of the image on the list of loaded images.
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

  /******************************************************/
  /**
   * This method sets the images attribute with the given list of ImagePlus.
   * @param i
   */
  public void setImages(List<ImagePlus> i) {
    images = i;
  }

  /******************************************************/
  /**
   * This method calculates the resize factor due to the original image size and the showed image size.
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
   * This method creates and shows the MorphWindow.
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
    sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
    sldThreshold.setMinorTickSpacing(2);
    sldThreshold.setMajorTickSpacing(10);
    sldThreshold.setPaintTicks(true);
    sldThreshold.setPaintLabels(true);
    // We'll just use the standard numeric labels for now...
    sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
    sldThreshold.addChangeListener(this);
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

    // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // double width = screenSize.getWidth();
    // double height = screenSize.getHeight();
    // frame.setPreferredSize(new Dimension((int)width, 250));
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setContentPane(panel);
    this.pack();
    // frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH
    // );
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    this.setVisible(true);
  }

  /******************************************************/
  /** Listen events from slider */
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.
   * ChangeEvent)
   */
  public void stateChanged(ChangeEvent inEvent) {
    Object auxWho = inEvent.getSource();
    if ((auxWho == sldThreshold)) {
      // Updating threshold value from slider
      threshold = sldThreshold.getValue();
      doSliderRefresh();
    }
  }

  /******************************************************/
  /**
   * This method choose between autoThreshold or apply a particular threshold to the given ImagePlus
   * depending if this value has been set before or not.
   * @param imp ImagePlus to be thresholded.
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