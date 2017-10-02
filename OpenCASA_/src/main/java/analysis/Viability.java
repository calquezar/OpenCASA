package analysis;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Params;
import data.Spermatozoon;
import functions.ComputerVision;
import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Viability implements Measurements, ChangeListener, MouseListener {

  MainWindow mainW;
  // GENERIC DIALOG PARAMETERS
  public double pixelsPerUm = 1 / Params.micronPerPixel;
  float minSize = (float) (Params.minSize * pixelsPerUm);// minimum sperm size
                                                         // (um^2)
  float maxSize = (float) (Params.maxSize * pixelsPerUm);// maximum sperm size
                                                         // (um^2)
  float borderSize = 20 * (float) Math.pow(pixelsPerUm, 2);
  String selectedFilter = "";
  String male = "";
  String date = "";

  ImagePlus imp = new ImagePlus();
  // Here we'll store the original images
  ImagePlus aliveImpOrig = null;
  ImagePlus deadsImpOrig = null;
  // These ImagePlus will be used to draw over them
  ImagePlus aliveImpDraw = null;
  ImagePlus deadsImpDraw = null;
  // These ImagePlus will be used to calculate mean gray values
  ImagePlus aliveImpGray = null;
  ImagePlus deadsImpGray = null;
  // These ImagePlus will be used to identify spermatozoa
  ImagePlus aliveImpTh = null;
  ImagePlus deadsImpTh = null;
  // These ImagePlus will be used to store outlines
  ImagePlus aliveImpOutline = null;
  ImagePlus deadsImpOutline = null;

  // Thresholds
  double aliveThreshold = -1.0;
  double deadsThreshold = -1.0;
  String aliveThresholdMethod = "Otsu";
  String deadsThresholdMethod = "Otsu";
  boolean isThresholding = false;

  // GUI elements and variables
  int activeImage = 1; // 1-Complete;2-Alive;3-NucleusA;4-Deads
  JSlider sldThreshold;
  boolean hasCanvas = false;
  ImageCanvas canvas;
  boolean hideSliderEvent = false;// Used to make transparent Slider event
  JLabel JLAlives;
  JLabel JLDeads;
  // Variables used to identify spermatozoa
  List aliveSpermatozoa = new ArrayList();
  List deadsSpermatozoa = new ArrayList();

  // Counters
  int alives = 0; // Temporal variable, reset at each image
  int deads = 0;
  ResultsTable results = new ResultsTable();

  public Viability() {
  }

  /******************************************************/
  /**
   * @param
   * @return
   */
  public void changeSelectedStatus(String id, List spermatozoa) {
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon candidate = (Spermatozoon) j.next();
      // IJ.log("candidate.id: "+candidate.id+"; id: "+id);
      if (candidate.id.equals(id) && id != "***") {
        // IJ.log("son iguales");
        candidate.selected = !candidate.selected;
        // break;
      }
    }
  }

  /******************************************************/
  /**
   * @param
   * @return
   */
  public void checkSelection(int x, int y) {

    Point click = new Point(x, y);
    List thespermatozoa = new ArrayList();
    switch (activeImage) {
    case 1:
      thespermatozoa = aliveSpermatozoa;
      break;
    case 2:
      thespermatozoa = deadsSpermatozoa;
      break;
    }
    for (ListIterator j = thespermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      if (isClickInside(sperm, click)) {
        sperm.selected = !sperm.selected;
      }
    }
  }

  public int[] convertLongArrayToInt(long[] orig) {
    int[] arrayInt = new int[orig.length];
    for (int i = 0; i < orig.length; i++)
      arrayInt[i] = (int) orig[i];
    return arrayInt;
  }

  /******************************************************/
  public int countSelectedSpermatozoa(List spermatozoa) {
    int count = 0;
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      if (sperm.selected)
        count++;
    }
    return count;
  }

  /******************************************************/
  /**
   * 
   */
  public void createGUI() {

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    // natural height, maximum width
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;

    JButton btnAlive = new JButton("Alives");
    if (selectedFilter == "Green")
      btnAlive.setBackground(Color.GREEN);
    else if (selectedFilter == "Blue")
      btnAlive.setBackground(new Color(51, 153, 255));
    btnAlive.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (aliveImpOrig == null) {
          aliveImpOrig = IJ.openImage();
          deadsImpOrig = aliveImpOrig.duplicate();
        }
        if (aliveImpOrig != null) {// Usefull when the user cancel before
                                   // load an image
          activeImage = 1;
          aliveImpDraw = aliveImpOrig.duplicate();
          imp.setProcessor(aliveImpDraw.getProcessor());
          imp.setTitle("Alive");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setAliveImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) aliveThreshold);
        }
      }
    });
    c.gridx = 1;
    c.gridy = 0;
    panel.add(btnAlive, c);

    JButton btnDeads = new JButton("Deads");
    btnDeads.setBackground(Color.MAGENTA);
    btnDeads.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (deadsImpOrig == null) {
          deadsImpOrig = IJ.openImage();
          aliveImpOrig = deadsImpOrig.duplicate();
        }
        if (deadsImpOrig != null) {// Usefull when the user cancel before
                                   // load an image
          activeImage = 2;
          deadsImpDraw = deadsImpOrig.duplicate();
          imp.setProcessor(deadsImpDraw.getProcessor());
          imp.setTitle("Deads");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setDeadsImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) deadsThreshold);

        }
      }
    });
    c.gridx = 3;
    c.gridy = 0;
    panel.add(btnDeads, c);

    // RADIO BUTTONS
    JRadioButton aliveOtsuButton = new JRadioButton("Otsu");
    aliveOtsuButton.setSelected(true);
    aliveOtsuButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aliveThresholdMethod = "Otsu";
        aliveThreshold = -1.0;
        if ((aliveImpOrig != null) && (activeImage == 1)) {// Usefull when
                                                           // the user
                                                           // cancel before
                                                           // load an image
          aliveImpDraw = aliveImpOrig.duplicate();
          imp.setProcessor(aliveImpDraw.getProcessor());
          imp.setTitle("Alive");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setAliveImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) aliveThreshold);
        }
      }
    });
    JRadioButton aliveMinimumButton = new JRadioButton("Minimum");
    aliveMinimumButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aliveThresholdMethod = "Minimum";
        aliveThreshold = -1.0;
        if ((aliveImpOrig != null) && (activeImage == 1)) {// Usefull when
                                                           // the user
                                                           // cancel before
                                                           // load an image
          aliveImpDraw = aliveImpOrig.duplicate();
          imp.setProcessor(aliveImpDraw.getProcessor());
          imp.setTitle("Alive");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setAliveImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) aliveThreshold);
        }
      }
    });
    // Group the radio buttons.
    ButtonGroup aliveGroup = new ButtonGroup();
    aliveGroup.add(aliveOtsuButton);
    aliveGroup.add(aliveMinimumButton);
    c.gridx = 1;
    c.gridy = 1;
    panel.add(aliveOtsuButton, c);
    c.gridy = 2;
    panel.add(aliveMinimumButton, c);

    JRadioButton deadsOtsuButton = new JRadioButton("Otsu");
    deadsOtsuButton.setSelected(true);
    deadsOtsuButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deadsThresholdMethod = "Otsu";
        deadsThreshold = -1.0;
        if ((deadsImpOrig != null) && (activeImage == 2)) {// Usefull when
                                                           // the user
                                                           // cancel before
                                                           // load an image
          deadsImpDraw = deadsImpOrig.duplicate();
          imp.setProcessor(deadsImpDraw.getProcessor());
          imp.setTitle("Deads");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setDeadsImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) deadsThreshold);
        }
      }
    });
    JRadioButton deadsMinimumButton = new JRadioButton("Minimum");
    deadsMinimumButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deadsThresholdMethod = "Minimum";
        deadsThreshold = -1.0;
        if ((deadsImpOrig != null) && (activeImage == 2)) {// Usefull when
                                                           // the user
                                                           // cancel before
                                                           // load an image
          deadsImpDraw = deadsImpOrig.duplicate();
          imp.setProcessor(deadsImpDraw.getProcessor());
          imp.setTitle("Deads");
          imp.show();
          if (!hasCanvas) {
            setCanvas();
            hasCanvas = true;
          }
          setDeadsImage(false);
          hideSliderEvent = true;
          sldThreshold.setValue((int) deadsThreshold);
        }
      }
    });
    // Group the radio buttons.
    ButtonGroup deadsGroup = new ButtonGroup();
    deadsGroup.add(deadsOtsuButton);
    deadsGroup.add(deadsMinimumButton);
    c.gridx = 3;
    c.gridy = 1;
    panel.add(deadsOtsuButton, c);
    c.gridy = 2;
    panel.add(deadsMinimumButton, c);

    JButton btnSaveResults = new JButton("Save Results");
    btnSaveResults.setBackground(Color.CYAN);
    // Add action listener
    btnSaveResults.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        results.incrementCounter();
        results.addValue("Male", male);
        results.addValue("Date", date);
        results.addValue("Alives", alives);
        results.addValue("Deads", deads);
        int total = alives + deads;
        results.addValue("Total", total);
        float percAlives = ((float) alives) / ((float) total) * 100;
        results.addValue("% Alives", percAlives);
        results.show("Resultados");
      }
    });
    c.weightx = 0.5;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.ipady = 5;
    c.gridx = 1;
    c.gridy = 4;
    panel.add(btnSaveResults, c);

    // THRESHOLD SLIDERBAR
    sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
    sldThreshold.setMinorTickSpacing(2);
    sldThreshold.setMajorTickSpacing(10);
    sldThreshold.setPaintTicks(true);
    sldThreshold.setPaintLabels(true);
    // We'll just use the standard numeric labels for now...
    sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
    sldThreshold.addChangeListener(this);
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 40;
    c.gridwidth = 4;
    c.gridx = 0;
    c.gridy = 3;
    panel.add(sldThreshold, c);

    JButton btnResetImages = new JButton("Reset Images");
    btnResetImages.setBackground(Color.YELLOW);
    // Add action listener
    btnResetImages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        imp.close();
        // Here we'll store the original images
        aliveImpOrig = null;
        deadsImpOrig = null;
        // These ImagePlus will be used to draw over them
        aliveImpDraw = null;
        deadsImpDraw = null;
        // These ImagePlus will be used to calculate mean gray values
        aliveImpGray = null;
        deadsImpGray = null;
        // These ImagePlus will be used to identify spermatozoa
        aliveImpTh = null;
        deadsImpTh = null;
        // These ImagePlus will be used to store outlines
        aliveImpOutline = null;
        deadsImpOutline = null;

        // Thresholds
        aliveThreshold = -1.0;
        deadsThreshold = -1.0;

        // Variables used to identify spermatozoa
        aliveSpermatozoa = new ArrayList();
        deadsSpermatozoa = new ArrayList();

        JLAlives.setText("Alives: -");
        JLDeads.setText("Deads: -");
        // GUI elements and variables
        hasCanvas = false;
      }
    });
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipady = 0;
    c.gridx = 3;
    c.gridy = 4;
    panel.add(btnResetImages, c);

    JLAlives = new JLabel("Alives: -");// ,JLabel.CENTER);
    JLAlives.setFont(new Font("Serif", Font.PLAIN, 22));
    JLDeads = new JLabel("Deads: -");
    JLDeads.setFont(new Font("Serif", Font.PLAIN, 22));

    c.weightx = 0.5;
    c.ipady = 0;
    c.gridx = 0;
    c.gridy = 5;
    panel.add(JLAlives, c);
    c.gridx = 0;
    c.gridy = 6;
    panel.add(JLDeads, c);

    JFrame frame = new JFrame("Adjust Threshold");
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    double width = screenSize.getWidth();
    double height = screenSize.getHeight();
    frame.setPreferredSize(new Dimension((int) width, 300));
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(panel);
    frame.pack();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        imp.close();
        mainW.setVisible(true);
      }
    });
    frame.setVisible(true);
  }

  /******************************************************/
  /**
   * @param imageP
   *          ImagePlus
   * @return 2D-ArrayList with all spermatozoa detected for each frame
   */
  public List detectSpermatozoa(ImagePlus imageP) {

    int options = 0;// ParticleAnalyzer.DISPLAY_SUMMARY; // set all PA options
                    // false
    int measurements = MEAN + CENTROID + RECT + AREA + PERIMETER + FERET;
    // Initialize results table
    ResultsTable rt = new ResultsTable();
    rt.reset();
    // create storage for Spermatozoon positions
    List thespermatozoa = new ArrayList();
    ////////////////////////////////////////////////////////////////
    // Record Spermatozoon positions in an ArrayList
    ////////////////////////////////////////////////////////////////
    ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
    pa.analyze(imageP, imageP.getProcessor());
    // rt.show("resultados");
    // if(iFrame==1)
    // rt.show("Resultados");
    float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);
    float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
    float[] bxRes = rt.getColumn(ResultsTable.ROI_X);
    float[] byRes = rt.getColumn(ResultsTable.ROI_Y);
    float[] widthRes = rt.getColumn(ResultsTable.ROI_WIDTH);
    float[] heightRes = rt.getColumn(ResultsTable.ROI_HEIGHT);
    if (sxRes != null) {// There are some spermatozo
      for (int iPart = 0; iPart < sxRes.length; iPart++) {
        // System.out.println("heightRes: "+heightRes[iPart]);
        Spermatozoon sperm = new Spermatozoon();
        sperm.id = "*";
        sperm.x = sxRes[iPart];
        sperm.y = syRes[iPart];
        sperm.z = activeImage;
        sperm.bx = bxRes[iPart];
        sperm.by = byRes[iPart];
        sperm.width = widthRes[iPart];
        sperm.height = heightRes[iPart];
        if (!isOutsideBorder(sperm))
          thespermatozoa.add(sperm);
        IJ.showStatus("Identifying spermatozoa...");
      }
    }
    return thespermatozoa;
  }

  /******************************************************/
  /**
   *
   */
  private void doMouseRefresh() {
    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          switch (activeImage) {
          case 1:
            aliveImpDraw = aliveImpOrig.duplicate();
            imp.setProcessor(aliveImpDraw.getProcessor());
            setAliveImage(false);
            break;
          case 2:
            deadsImpDraw = deadsImpOrig.duplicate();
            imp.setProcessor(deadsImpDraw.getProcessor());
            setDeadsImage(false);
            break;
          }
          isThresholding = false;
        }
      });
      t1.start();
    }
  }

  int doOffset(int center, int maxSize, int displacement) {
    if ((center - displacement) < 2 * displacement) {
      return (center + 2 * displacement);
    } else {
      return (center - displacement);
    }
  }

  private void doSliderRefresh() {
    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          switch (activeImage) {
          case 1:
            aliveImpDraw = aliveImpOrig.duplicate();
            imp.setProcessor(aliveImpDraw.getProcessor());
            setAliveImage(true);
            break;
          case 2:
            deadsImpDraw = deadsImpOrig.duplicate();
            imp.setProcessor(deadsImpDraw.getProcessor());
            setDeadsImage(true);
            break;
          }
          isThresholding = false;
        }
      });
      t1.start();
    }
  }

  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   * @return 2D-ArrayList with all spermatozoa detected for each frame
   */
  public void drawBoundaries(ImagePlus imp, List thespermatozoa) {
    int xHeight = imp.getHeight();
    int yWidth = imp.getWidth();
    IJ.showStatus("Drawing boundaries...");
    ImageProcessor ip = imp.getProcessor();

    switch (activeImage) {
    case 1:
      ip.setColor(Color.green);
      break;
    case 2:
      ip.setColor(Color.MAGENTA);
      break;
    }
    ip.setLineWidth(5);
    for (ListIterator j = thespermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon sperm = (Spermatozoon) j.next();
      if (sperm.selected)
        ip.drawRect((int) sperm.bx, (int) sperm.by, (int) sperm.width, (int) sperm.height);
      // Draw numbers
      // ip.setFont(new Font("SansSerif", Font.PLAIN, 32));
      // we could do someboundary testing here to place the labels better
      // when we are close to the edge
      // ip.moveTo((int)(sperm.x/pixelWidth+0),doOffset((int)(sperm.y/pixelHeight),yWidth,5)
      // );
      // ip.moveTo((int)(sperm.bx/pixelWidth),(int)(sperm.by/pixelHeight) );
      // ip.drawString(sperm.id);
    }
  }

  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   */
  public void drawOutline(ImagePlus impOrig, ImagePlus impTh) {

    IJ.showStatus("Changing background...");
    ColorProcessor ipOrig = (ColorProcessor) impOrig.getProcessor();
    ipOrig.setColor(Color.yellow);
    ImageProcessor ipTh = impTh.getProcessor();
    int ipWidth = ipOrig.getWidth();
    int ipHeight = ipOrig.getHeight();
    for (int x = 0; x < ipWidth; x++) {
      IJ.showStatus("scanning pixels...");
      for (int y = 0; y < ipHeight; y++) {
        int pixel = ipTh.get(x, y);
        if (pixel == 0)// It's background
          ipOrig.drawPixel(x, y);
      }
    }
  }

  /******************************************************/
  /**
   * @param
   * @return
   */
  public Spermatozoon getSpermatozoon(String id, List spermatozoa) {
    Spermatozoon spermatozoon = null;
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Spermatozoon candidate = (Spermatozoon) j.next();
      if (candidate.id.equals(id) && id != "*") {
        spermatozoon = candidate;
        break;
      }
    }
    return spermatozoon;
  }

  /**
  * 
  * 
  */
  public void getThresholdFromSlider() {
    if (activeImage == 1)
      aliveThreshold = sldThreshold.getValue();
    else if (activeImage == 2)
      deadsThreshold = sldThreshold.getValue();
  }

  /******************************************************/
  /**
   * @param
   * @return
   */
  public boolean isClickInside(Spermatozoon part, Point click) {
    // Get boundaries
    double offsetX = (double) part.bx;
    double offsetY = (double) part.by;
    int w = (int) part.width;
    int h = (int) part.height;
    // correct offset
    int pX = (int) (click.getX() - offsetX);
    int pY = (int) (click.getY() - offsetY);
    // IJ.log("offsetX: "+offsetX+" ; offsetY: "+offsetY+" ;w: "+w+"; h:
    // "+h+"px: "+pX+"; py: "+pY);
    Rectangle r = new Rectangle(w, h);

    return r.contains(new Point(pX, pY));
  }

  /******************************************************/
  /**
   * @param
   * @return
   */
  public boolean isOutsideBorder(Spermatozoon part) {

    float x = part.x;
    float y = part.y;
    ImageProcessor ip = imp.getProcessor();
    int ipWidth = ip.getWidth();
    int ipHeight = ip.getHeight();

    // Check left-margin
    if (x < borderSize)
      return true;
    // Check right-margin
    if (x > (ipWidth - borderSize))
      return true;
    // Check top-margin
    if (y < borderSize)
      return true;
    // Check bottom-margin
    if (y > (ipHeight - borderSize))
      return true;

    return false; // It's not outside border
  }

  /******************************************************
   * MOUSE LISTENER
   ******************************************************/
  public void mouseClicked(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    int offscreenX = canvas.offScreenX(x);
    int offscreenY = canvas.offScreenY(y);
    checkSelection(offscreenX, offscreenY);
    doMouseRefresh();
    imp.updateAndRepaintWindow();
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  /******************************************************/
  /**
   * @param
   */
  public void outlineThresholdImage(ImagePlus imp) {
    ImageProcessor ip = imp.getProcessor();
    BinaryProcessor bp = new BinaryProcessor((ByteProcessor) ip);
    bp.outline();
  }
  /******************************************************/

  /**
   * @param arg
   *          String
   * @param imp
   *          ImagePlus
   * @return integer
   */
  /*
   * public int setup(String arg, ImagePlus imp) { if(imp==null) return
   * DOES_RGB; this.aliveImpOrig = imp.duplicate(); this.deadsImpOrig =
   * imp.duplicate(); this.imp=imp; if (IJ.versionLessThan("1.17y")) return
   * DONE; else return DOES_RGB+NO_CHANGES; }
   */
  /******************************************************/
  /**
   * @param arg
   *          String
   */
  public void run(MainWindow mw) {

    mainW = mw;
    mw.setVisible(false);

    // the stuff below is the box that pops up to ask for pertinant values -
    // why doesn't it remember the values entered????
    GenericDialog gd = new GenericDialog("Sperm Analyzer");
    String[] items = { "Green", "Blue" };
    gd.addRadioButtonGroup("Filter Type", items, 1, 2, "Green");
    gd.addMessage("PARAMETERS USED TO IDENTIFY SAMPLES");
    gd.addStringField("Male", "");
    gd.addStringField("Date", "");

    gd.showDialog();
    if (gd.wasCanceled()) {
      mainW.setVisible(true);
      return;
    }

    // PARAMETERS USED TO CALCULATE MORPHOMETRICS
    Vector<CheckboxGroup> radiobtns = gd.getRadioButtonGroups();
    CheckboxGroup chkBoxG = (CheckboxGroup) radiobtns.lastElement();
    Checkbox selectedRadioBtn = chkBoxG.getSelectedCheckbox();
    selectedFilter = selectedRadioBtn.getLabel();

    // PARAMETERS USED TO IDENTIFY SAMPLES
    male = gd.getNextString();
    date = gd.getNextString();

    results.reset();
    results.show("Resultados");
    createGUI();
  }

  double s2d(String s) {
    Double d;
    try {
      d = new Double(s);
    } catch (NumberFormatException e) {
      d = null;
    }
    if (d != null)
      return (d.doubleValue());
    else
      return (0.0);
  }

  /******************************************************/
  /**
   * 
   */
  public void setAliveImage(boolean isEvent) {
    if (aliveThreshold == -1 || isEvent) {// First time
      if (selectedFilter == "Green")
        aliveImpGray = ComputerVision.getGreenChannel(aliveImpOrig);
      else if (selectedFilter == "Blue")
        aliveImpGray = ComputerVision.getBlueChannel(aliveImpOrig);
      ComputerVision.convertToGrayscale(aliveImpGray);
      aliveImpTh = aliveImpGray.duplicate();
      thresholdImagePlus(aliveImpTh);
      aliveSpermatozoa = detectSpermatozoa(aliveImpTh);
      // Calculate outlines
      aliveImpOutline = aliveImpTh.duplicate();
      outlineThresholdImage(aliveImpOutline);
    }
    // Refresh counter
    alives = countSelectedSpermatozoa(aliveSpermatozoa);
    JLAlives.setText("Alives: " + alives);
    drawOutline(imp, aliveImpOutline);
    drawBoundaries(imp, aliveSpermatozoa);
    imp.updateAndRepaintWindow();
  }

  /******************************************************/
  /**
   * 
   */
  public void setCanvas() {
    ImageWindow win = imp.getWindow();
    canvas = win.getCanvas();
    canvas.addMouseListener(this);
  }

  /******************************************************/
  /**
   * 
   */
  public void setDeadsImage(boolean isEvent) {
    if (deadsThreshold == -1 || isEvent) {// First time
      deadsImpGray = ComputerVision.getRedChannel(deadsImpOrig);
      ComputerVision.convertToGrayscale(deadsImpGray);
      deadsImpTh = deadsImpGray.duplicate();
      thresholdImagePlus(deadsImpTh);
      deadsSpermatozoa = detectSpermatozoa(deadsImpTh);
      // Calculate outlines
      deadsImpOutline = deadsImpTh.duplicate();
      outlineThresholdImage(deadsImpOutline);
    }
    // Refresh counter
    deads = countSelectedSpermatozoa(deadsSpermatozoa);
    JLDeads.setText("Deads: " + deads);
    drawOutline(imp, deadsImpOutline);
    drawBoundaries(imp, deadsSpermatozoa);
    imp.updateAndRepaintWindow();
  }

  /******************************************************/
  // Utility functions
  double sqr(double n) {
    return n * n;
  }

  /******************************************************/
  /** Listen events from checkboxes and slider */
  public void stateChanged(ChangeEvent inEvent) {
    Object auxWho = inEvent.getSource();
    if ((auxWho == sldThreshold)) {
      if (!hideSliderEvent) {
        getThresholdFromSlider();
        doSliderRefresh();
      } else {
        hideSliderEvent = false;
      }
    }
  }

  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   */
  public void thresholdImagePlus(ImagePlus imp) {

    double lowerThreshold = 0;
    String thresholdMethod = "";
    if (activeImage == 1) {
      lowerThreshold = aliveThreshold;
      thresholdMethod = aliveThresholdMethod;
    } else if (activeImage == 2) {
      lowerThreshold = deadsThreshold;
      thresholdMethod = deadsThresholdMethod;
    }
    // First we look at if threshold has been set before
    // Else we have to calculate it
    ImageProcessor ip = imp.getProcessor();
    if (lowerThreshold == -1) {
      ImageStatistics st = ip.getStatistics();
      long[] histlong = st.getHistogram();
      int histogram[] = convertLongArrayToInt(histlong);
      AutoThresholder at = new AutoThresholder();
      lowerThreshold = (double) at.getThreshold(thresholdMethod, histogram);
      hideSliderEvent = true;
      sldThreshold.setValue((int) lowerThreshold);
      if (activeImage == 1)
        aliveThreshold = lowerThreshold;
      else if (activeImage == 2)
        deadsThreshold = lowerThreshold;
    }
    // Upper threshold set to maximum
    double upperThreshold = 255;
    // Threshold image processor
    ComputerVision.thresholdImageProcessor(ip, lowerThreshold, upperThreshold);
  }
}
