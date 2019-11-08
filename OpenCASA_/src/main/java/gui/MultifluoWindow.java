/*
 *   OpenCASA software v2.0 for video and image analysis
 *   Copyright (C) 2019  Carlos Alquézar
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
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * This class implements all the functions related to multifluo analysis.
 * 
 * @author Carlos Alquezar
 */
public class MultifluoWindow extends ImageAnalysisWindow implements ChangeListener, MouseListener {

  private boolean isThresholding = false;
  private boolean isProcessing = false;
  /** Resultstable used to show results */
  private ResultsTable multifluometrics = new ResultsTable();

  /**
   * Constructor. The main graphical user interface is created.
   */
  public MultifluoWindow() throws HeadlessException {
    super();
    sldThreshold.setVisible(true);
    setChangeListener(this, sldThreshold);
    setMouseListener(this);
    multifluometrics.showRowNumbers(false);
    resizeFactor=0.8;
  }

  /******************************************************/
  /**
   * This method checks if a click has been done over a cell. In that case, the
   * method select/deselect the cell and add the multifluometrics to resultsTable
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
          //paint.drawBoundaries(impDraw, spermatozoa);
          paint.drawType(impDraw, spermatozoa);
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
   * 
   * @param cell
   */
  private void generateResults(Cell cell) {


    multifluometrics.show("Multifluo results");
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
    setRawImage();
  }

  public void mouseReleased(MouseEvent e) {
    if (!isThresholding){
      drawImage();
    }
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
        typeSpermatozoa(impOrig.duplicate(),spermatozoa);
        // Calculate outlines
        impOutline = impTh.duplicate();
        cv.outlineThresholdImage(impOutline);
        idenfitySperm();
        selectAll(); // By default, all cells are selected
      }
      impDraw = impOrig.duplicate();
      Paint paint = new Paint();
      paint.drawOutline(impDraw, impOutline);
      //paint.drawBoundaries(impDraw, spermatozoa);
      paint.drawType(impDraw, spermatozoa);
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
  
  /******************************************************/
  /**
   * @param
   * @return 
   */
  public void typeSpermatozoa(ImagePlus imp,List spermatozoa){
    for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
      Cell cell=(Cell) j.next();
      typeSpermatozoon(imp,cell);
    }
  }
  /******************************************************/
  /**
   * @param
   * @return 
   */
  public void typeSpermatozoon(ImagePlus imp,Cell p){
    
    ColorProcessor cp = (ColorProcessor)imp.getProcessor();
    ImageStack hsbStack = cp.getHSBStack();
    ImageProcessor hueIp = hsbStack.getProcessor(1);
    ImageProcessor saturationIp = hsbStack.getProcessor(2);
    ImageProcessor brightnessIp = hsbStack.getProcessor(3);
    int red = 0;
    int green = 0;
    int blue = 0;
    for(int x=(int)p.bx;x<(int)(p.bx+p.width);x++){
      for(int y=(int)p.by;y<(int)(p.by+p.height);y++){
        int pixel = hueIp.get(x,y);
        if(pixel>200 || pixel<20)
          red++;
        else if(pixel>50 && pixel<125)
          green++;
        else if(pixel>130 && pixel<185)
          blue++;
      }
    }
    
    //Check type
    int total = red+green+blue;
    float redRate = 100*(float)red/(float)total;
    float greenRate = 100*(float)green/(float)total;
    float blueRate = 100*(float)blue/(float)total;
    
    
    if(redRate<10)
      if(greenRate<10)
        p.type="DAIM";//"Blue"
      else if(blueRate<10)
        p.type="Green";//"Green"
      else
        p.type="IAIM";//"Green-Blue"
    else if(blueRate<10)
      if(greenRate>40)
        p.type="IADM";//"Green-Red"
      else
        p.type="DADM";//"Red"
  } 
  
  protected void saveAction(){
    generateResults();
  }
  
  public void generateResults(){  
 
    int iaimCounter = 0;
    int iadmCounter = 0;
    int daimCounter = 0;
    int dadmCounter = 0;
    int greenCounter = 0;
    int unknownCounter = 0;
    int total = 0;
    for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
      Cell cell=(Cell) j.next();
      
      if(cell.type=="IAIM")
        iaimCounter++;
      else if(cell.type=="IADM")
        iadmCounter++;
      else if(cell.type=="DAIM")
        daimCounter++;
      else if(cell.type=="DADM")
        dadmCounter++;
      else if(cell.type=="Green")
        greenCounter++;
      else
        unknownCounter++;
    }
    total = iaimCounter+iadmCounter+daimCounter+dadmCounter+greenCounter+unknownCounter;
    
    multifluometrics.incrementCounter();
    FileManager fm = new FileManager();
    multifluometrics.addValue("Sample", fm.getParentDirectory(impOrig.getTitle()));
    multifluometrics.addValue("Filename", fm.getFilename(impOrig.getTitle()));
    if (!Params.male.isEmpty())
      multifluometrics.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      multifluometrics.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      multifluometrics.addValue("Generic Field", Params.genericField);
    multifluometrics.addValue("IAIM",iaimCounter);
    multifluometrics.addValue("IADM",iadmCounter);
    multifluometrics.addValue("DAIM",daimCounter);
    multifluometrics.addValue("DADM",dadmCounter);
    multifluometrics.addValue("Green",greenCounter);
    multifluometrics.addValue("Unknown",unknownCounter);
    multifluometrics.addValue("Total",total);
    float percIAIM = ((float)iaimCounter/(float)total)*100;
    float percIADM = ((float)iadmCounter/(float)total)*100;
    float percDAIM = ((float)daimCounter/(float)total)*100;
    float percDADM = ((float)dadmCounter/(float)total)*100;
    float percGreen = ((float)greenCounter/(float)total)*100;
    multifluometrics.addValue("% IAIM",percIAIM);
    multifluometrics.addValue("% IADM",percIADM);
    multifluometrics.addValue("% DAIM",percDAIM);
    multifluometrics.addValue("% DADM",percDADM);
    multifluometrics.addValue("% Green",percGreen);
    
    float percIM = ((float)(iaimCounter+daimCounter+greenCounter)/(float)total)*100;
    float percDM = ((float)(iadmCounter+dadmCounter)/(float)total)*100;
    float percIA = ((float)(iaimCounter+iadmCounter)/(float)total)*100;
    float percDA = ((float)(daimCounter+dadmCounter)/(float)total)*100;
    multifluometrics.addValue("% IM",percIM);
    multifluometrics.addValue("% DM",percDM);
    multifluometrics.addValue("% IA",percIA);
    multifluometrics.addValue("% DA",percDA);

    multifluometrics.show("results");
  }
}