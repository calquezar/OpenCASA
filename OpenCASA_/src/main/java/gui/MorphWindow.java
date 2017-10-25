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
 * 
 * @author Carlos Alquezar
 */
public class MorphWindow extends ImageAnalysisWindow implements ChangeListener, MouseListener {

  
  private boolean         isThresholding  = false;
  /** Resultstable used to show results */
  private ResultsTable       morphometrics = new ResultsTable();

  /**
   * Constructor. The main graphical user interface is created.
   */
  public MorphWindow() throws HeadlessException {
    super();
    setChangeListener(this);
    setMouseListener(this);
  }

  /******************************************************/
  /**
   * This method checks if a click has been done over a cell. In that case, the
   * method select/deselect the cell and add the morphometrics to resultsTable
   * if it has been selected.
   * 
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
    impOrig.changes = false; // This is necessary to avoid Save changes? dialog when closing
    impDraw.changes = false; // This is necessary to avoid Save changes? dialog when closing
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
   * This method refreshes the showed image after changing the threshold with
   * the sliderbar
   */
  private void doSliderRefresh() {
    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          deselectAll();
          processImage(true);
          isThresholding = false;
        }
      });
      t1.start();
    }
  }

  /******************************************************/
  /**
   * This method adds the morphometric values of the given spermatozoon to the
   * results table
   * 
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
    if (!Params.male.isEmpty())
      morphometrics.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      morphometrics.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      morphometrics.addValue("Generic Field", Params.genericField);
    morphometrics.show("Morphometrics");
  }

  /******************************************************/
  /**
   * This method returns true if the given point is inside the boundaries of the
   * given spermatozoon
   * 
   * @param sperm
   *          - Spermatozoon
   * @param click
   *          - Point
   * @return True if the point is inside the boundaries of the spermatozoon.
   *         Otherwise, it returns false
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
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}

  /******************************************************/
  /**
   * This method updates the showed image depending of the type of event
   * ocurred.
   * 
   * @param eventType
   *          This parameter is used to differentiate between a slider event
   *          (true) or a click event (false)
   */
  public void processImage(boolean eventType) {
    if (eventType || threshold == -1) {// If true, the threshold has changed or it needs to be calculated
      ComputerVision cv = new ComputerVision();
      impTh = impOrig.duplicate();
      cv.convertToGrayscale(impTh);
      impGray = impTh.duplicate();
      thresholdImagePlus(impTh);
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

  /** Listen events from slider */
  public void stateChanged(ChangeEvent inEvent) {
    Object auxWho = inEvent.getSource();
    if ((auxWho == sldThreshold)) {
      // Updating threshold value from slider
      threshold = sldThreshold.getValue();
      doSliderRefresh();
    }
  }
}