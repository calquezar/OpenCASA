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

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Cell;
import data.Params;
import functions.ComputerVision;
import functions.FileManager;
import functions.Paint;
import functions.Utils;
import functions.VideoRecognition;
import ij.measure.ResultsTable;

/**
 * This class implements all the functions related to morphometry analysis.
 * 
 * @author Carlos Alquezar
 */
public class MorphWindow extends ImageAnalysisWindow implements ChangeListener, MouseListener {

  private boolean isThresholding = false;
  private boolean isProcessing = false;
  /** Resultstable used to show results */
  private ResultsTable morphometrics = new ResultsTable();

  /**
   * Constructor. The main graphical user interface is created.
   */
  public MorphWindow() throws HeadlessException {
    super();
    sldThreshold.setVisible(true);
    setChangeListener(this, sldThreshold);
    setMouseListener(this);
    morphometrics.showRowNumbers(false);
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
      Cell sperm = (Cell) j.next();
      if (isClickInside(sperm, click)) {
        sperm.selected = !sperm.selected;
        if (sperm.selected) {
          Cell cell = utils.getCell(sperm.id, spermatozoa);
          generateResults(cell);
        }
        break;
      }
    }
  }

  /**
   * This method closes all ImagePlus.
   */
  public void close() {
    impOrig.changes = false; // This is necessary to avoid Save changes? dialog
                             // when closing
    impDraw.changes = false; // This is necessary to avoid Save changes? dialog
                             // when closing
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
   * @param cell
   */
  private void generateResults(Cell cell) {

    ComputerVision cv = new ComputerVision();
    double total_meanGray = (double) cv.getMeanGrayValue(cell, impGray, impTh);
    double total_area = cell.total_area * Math.pow(Params.micronPerPixel, 2);
    double total_perimeter = cell.total_perimeter * Params.micronPerPixel;
    double total_feret = cell.total_feret * Params.micronPerPixel;
    double total_minFeret = cell.total_minFeret * Params.micronPerPixel;
    double total_ellipticity = total_feret / total_minFeret;
    double total_roughness = 4 * Math.PI * total_area / (Math.pow(total_perimeter, 2));
    double total_elongation = (total_feret - total_minFeret) / (total_feret + total_minFeret);
    double total_regularity = (Math.PI * total_feret * total_minFeret) / (4 * total_area);

    morphometrics.incrementCounter();
    morphometrics.addValue("ID", cell.id);
    morphometrics.addValue("Threshold", threshold);
    morphometrics.addValue("MeanGray", total_meanGray);
    morphometrics.addValue("Area(um^2)", total_area);
    morphometrics.addValue("Perimeter(um)", total_perimeter);
    morphometrics.addValue("Length(um)", total_feret);
    morphometrics.addValue("Width(um)", total_minFeret);
    morphometrics.addValue("Ellipticity", total_ellipticity);
    morphometrics.addValue("Roughness", total_roughness);
    morphometrics.addValue("Elongation", total_elongation);
    morphometrics.addValue("Regularity", total_regularity);
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
   *          - Cell
   * @param click
   *          - Point
   * @return True if the point is inside the boundaries of the spermatozoon.
   *         Otherwise, it returns false
   */
  public boolean isClickInside(Cell sperm, Point click) {
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
   * This method updates the showed image depending of the type of event
   * ocurred.
   * 
   * @param eventType
   *          This parameter is used to differentiate between a slider event
   *          (true) or a click event (false)
   */
  public void processImage(boolean eventType) {
    if (!isProcessing) {// else do not disturb
      isProcessing = true;
      if (eventType || threshold == -1) {// If true, the threshold has changed
                                         // or it needs to be calculated
        ComputerVision cv = new ComputerVision();
        impTh = impOrig.duplicate();
        cv.convertToGrayscale(impTh);
        impGray = impTh.duplicate();
        thresholdImagePlus(impTh);
        VideoRecognition vr = new VideoRecognition();
        List<Cell>[] sperm = vr.detectCells(impTh);
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
      isProcessing = false;
    }
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