/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquezar
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

Part of this code (draw and doOffset methods in particular) is a modification of a previous code written 
by Jonas Wilson-Leedy and Rolf Ingermann and publish in CASA_ plugin for ImageJ.
Copyright © 2003 The Regents of the University of California and the Howard Hughes Medical Institute.

All Rights Reserved.

Permission to use, copy, modify, and distribute this software and its documentation for educational, research and 
non-profit purposes, without fee, and without a written agreement is hereby granted, provided that the above copyright 
notice, this paragraph and the following three paragraphs appear in all copies.

Permission to incorporate this software into commercial products may be obtained by contacting the Office of 
Technology Management at the University of California San Francisco [Sunita Rajdev, Ph.D., Licensing Officer, 
UCSF Office of Technology Management. 185 Berry St, Suite 4603, San Francisco, CA 94107].

This software program and documentation are copyrighted by The Regents of the University of California 
acting on behalf of the University of California San Francisco via its Office of Technology Management and the 
Howard Hughes Medical Institute (collectively, the Institution).  The software program and documentation are 
supplied "as is", without any accompanying services from the Institution. The Institution does not warrant that the 
operation of the program will be uninterrupted or error-free. The end-user understands that the program was developed 
for research purposes and is advised not to rely exclusively on the program for any reason.

IN NO EVENT SHALL THE INSTITUTION BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL 
DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE INSTITUTION 
HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE  INSTITUTION SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE 
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE  INSTITUTION HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, 
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

*/

package functions;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import data.Cell;
import data.Params;
import data.SerializableList;
import data.Trial;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * @author Carlos Alquezar
 *
 */
public class Paint {

  

  private Color getColor(String color){
    if(color.equalsIgnoreCase("red"))
      return Color.red;
    else if(color.equalsIgnoreCase("yellow"))
      return Color.yellow;
    else if (color.equalsIgnoreCase("green"))
      return Color.green;
    else if (color.equalsIgnoreCase("blue"))
      return Color.blue;
    else //if (color.equalsIgnoreCase("white"))
      return Color.white;
  }
  /******************************************************/
  /**
   * @param ip
   * @param numTracks
   * @param chIdx
   * @param slIdx
   * @param sampleID
   */
  public void chemotaxisTemplate(ColorProcessor ip, int numTracks, float chIdx, float slIdx, String sampleID) {
    // Alpha version of this method
    ip.setLineWidth(4);
    // center coords. of the cone used to clasify chemotactic trajectories
    int xCenter = ip.getWidth() / 2;
    int yCenter = ip.getHeight() / 2;
    float upperAngle = (float) (Params.angleDirection + Params.angleAmplitude / 2 + 360) % 360;
    upperAngle = upperAngle * (float) Math.PI / 180; // calculate and convert to
                                                     // radians
    float lowerAngle = (float) (Params.angleDirection - Params.angleAmplitude / 2 + 360) % 360;
    lowerAngle = lowerAngle * (float) Math.PI / 180; // convert to radians
    // Upper Line
    int upperLineX = xCenter + (int) (1000 * Math.cos(upperAngle));
    int upperLineY = yCenter - (int) (1000 * Math.sin(upperAngle));
    // Lower Line
    int lowerLineX = xCenter + (int) (1000 * Math.cos(lowerAngle));
    int lowerLineY = yCenter - (int) (1000 * Math.sin(lowerAngle));
    // Draw Chemotaxis Cone
    ip.moveTo((int) xCenter, (int) yCenter);
    ip.lineTo((int) upperLineX, (int) upperLineY);
    ip.moveTo((int) xCenter, (int) yCenter);
    ip.lineTo((int) lowerLineX, (int) lowerLineY);
    if (Params.compareOppositeDirections) {
      // Draw opposite cone
      upperAngle = (float) (Params.angleDirection + 180 + Params.angleAmplitude / 2 + 360) % 360;
      upperAngle = upperAngle * (float) Math.PI / 180; // calculate and convert
                                                       // to radians
      lowerAngle = (float) (Params.angleDirection + 180 - Params.angleAmplitude / 2 + 360) % 360;
      lowerAngle = lowerAngle * (float) Math.PI / 180; // convert to radians
      // Upper Line
      upperLineX = xCenter + (int) (1000 * Math.cos(upperAngle));
      upperLineY = yCenter - (int) (1000 * Math.sin(upperAngle));
      // Lower Line
      lowerLineX = xCenter + (int) (1000 * Math.cos(lowerAngle));
      lowerLineY = yCenter - (int) (1000 * Math.sin(lowerAngle));
      // Draw Chemotaxis Cone
      ip.moveTo((int) xCenter, (int) yCenter);
      ip.lineTo((int) upperLineX, (int) upperLineY);
      ip.moveTo((int) xCenter, (int) yCenter);
      ip.lineTo((int) lowerLineX, (int) lowerLineY);
    }
    // Reset line width
    ip.setLineWidth(1);
    ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
    ip.moveTo(10, 30);
    ip.setColor(Color.blue);
    ip.drawString("Sample: ");
    ip.moveTo(70, 30);
    ip.setColor(Color.black);
    ip.drawString(sampleID);
    ip.moveTo(10, 50);
    ip.setColor(Color.blue);
    ip.drawString("Number of tracks: ");
    ip.moveTo(135, 50);
    ip.setColor(Color.black);
    ip.drawString("" + numTracks);
    // ip.moveTo(10, 70);
    // ip.setColor(Color.red);
    // ip.drawString("Ch-Index: ");
    // ip.moveTo(70, 70);
    // ip.setColor(Color.black);
    // ip.drawString("" + chIdx);

     ip.moveTo(10, 70);
     ip.setColor(new Color(34, 146, 234));
     ip.drawString("SL-Index: ");
     ip.moveTo(80, 70);
     ip.setColor(Color.black);
     ip.drawString("" + slIdx);
  }

  /**
   * @param center
   * @param maxSize
   * @param displacement
   * @return
   */
  int doOffset(int center, int maxSize, int displacement) {
    if ((center - displacement) < 2 * displacement) {
      return (center + 4 * displacement);
    } else {
      return (center - displacement);
    }
  }

  /******************************************************/
  /**
   * @param imp
   * @param theTracks
   *          2D-ArrayList with all the tracks
   */
  public void draw(ImagePlus imp, SerializableList theTracks) {

    ComputerVision cv = new ComputerVision();
    cv.convertToRGB(imp);
    int nFrames = imp.getStackSize();
    ImageStack stack = imp.getStack();
    if (imp.getCalibration().scaled()) {
      IJ.showMessage("MultiTracker", "Cannot display paths if image is spatially calibrated");
      return;
    }
    int upRes = 1;
    Kinematics kinematics = new Kinematics();
    // Draw on each frame
    for (int iFrame = 1; iFrame <= nFrames; iFrame++) {
      IJ.showProgress((double) iFrame / nFrames);
      IJ.showStatus("Drawing Tracks (frame " + iFrame + "/" + nFrames + ")...");
      int yWidth = stack.getWidth();
      ImageProcessor ip = stack.getProcessor(iFrame);
      ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
      for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
        List aTrack = (ArrayList) iT.next();
        ListIterator jT = aTrack.listIterator();
        Cell oldCell = (Cell) jT.next();
        boolean isMotile = kinematics.motilityTest(aTrack);
        if (isMotile) {
          Cell newCell = null;
          for (; jT.hasNext();) {
            newCell = (Cell) jT.next();
            if (kinematics.getVelocityTrackType(aTrack) == "Slow")
              ip.setColor(getColor(Params.vclSlowColor));
            else if (kinematics.getVelocityTrackType(aTrack) == "Normal")
              ip.setColor(getColor(Params.vclNormalColor));
            else if (kinematics.getVelocityTrackType(aTrack) == "Fast")
              ip.setColor(getColor(Params.vclFastColor));
            // ip.setValue(color);
            ip.moveTo((int) oldCell.x * upRes, (int) oldCell.y * upRes);
            ip.lineTo((int) newCell.x * upRes, (int) newCell.y * upRes);
            // Draw track numbers
            if (oldCell.z == iFrame) {
              // we could do some boundary testing here to place the labels
              // better when we are close to the edge
              ip.moveTo((int) (oldCell.x / Params.pixelWidth + 0),
                  doOffset((int) (oldCell.y / Params.pixelHeight), yWidth, 5));
              ip.setColor(Color.white);
              ip.drawString("" + oldCell.trackNr);
            }
            oldCell = newCell;
          }
          if ((newCell!=null)&(newCell.z == iFrame)) {
            // we could do some boundary testing here to place the labels
            // better when we are close to the edge
            ip.moveTo((int) (newCell.x / Params.pixelWidth + 0),
                doOffset((int) (newCell.y / Params.pixelHeight), yWidth, 5));
            ip.setColor(Color.white);
            ip.drawString("" + newCell.trackNr);
          }
        } else {
          for (; jT.hasNext();) {
            Cell newCell = (Cell) jT.next();
            if (newCell.z == iFrame) {
              //ip.moveTo((int) ((oldCell.x+oldCell.width/2) / Params.pixelWidth + 0),
               //   doOffset((int) ((oldCell.y+oldCell.height/2) / Params.pixelHeight), yWidth, 5));
              ip.moveTo((int) (oldCell.x / Params.pixelWidth + 0),
                  (int) ((oldCell.y+oldCell.height) / Params.pixelHeight));              
              ip.setColor(Color.white);
              ip.drawString("**");
              //ip.drawString("" + newCell.trackNr);
            }
          }
        }

      }
    }
    IJ.showProgress(2); // To remove progresBar
    imp.updateAndRepaintWindow();
  }

  /******************************************************/
  /**
   * @param imp
   * @param spermatozoa
   */
  public void drawBoundaries(ImagePlus imp, List spermatozoa) {
    int yWidth = imp.getWidth();
    IJ.showStatus("Drawing boundaries...");
    ImageProcessor ip = imp.getProcessor();
    // ip.setColor(Color.white);
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Cell sperm = (Cell) j.next();
      ip.setLineWidth(2);
      if (sperm.selected)
        ip.drawRect((int) sperm.bx, (int) sperm.by, (int) sperm.width, (int) sperm.height);
      ip.setLineWidth(1);
      // Draw numbers
      ip.setFont(new Font("SansSerif", Font.PLAIN, 32));
      // we could do someboundary testing here to place the labels better
      // when we are close to the edge
      ip.moveTo((int) (sperm.x), doOffset((int) (sperm.y), yWidth, 5));
      try {
        ip.drawString("" + sperm.id);
      } catch (Exception e) {
        // IJ.handleException(e);
        e.printStackTrace();
        // ip.drawString throws eventually an exception.
        // Possibly it is a bug in the ImageProcessor implementation of this
        // ImageJ version
      }
    }
  }

  /******************************************************/
  /**
   * @param trial.tracks
   *          2D-ArrayList with all the tracks
   * @param chIdx
   * @param slIdx
   * @param trial.fieldWidth
   * @param trial.fieldHeight
   * @param trial.ID
   */
  public void drawChemotaxis(Trial trial, float chIdx, float slIdx) {

    SignalProcessing sp = new SignalProcessing();
    SerializableList avgTracks = sp.averageTracks(trial.tracks);
    // We create another ImageProcesor to draw chemotactic cone and relative
    // trajectories
    ColorProcessor ipRelTraj = new ColorProcessor(trial.fieldWidth, trial.fieldHeight);
    ipRelTraj.setColor(Color.white);
    ipRelTraj.fill();
    int xCenter = trial.fieldWidth / 2;
    int yCenter = trial.fieldHeight / 2;
    // Draw cone used to clasify chemotactic trajectories
    ipRelTraj.setColor(Color.green);
    chemotaxisTemplate(ipRelTraj, avgTracks.size(), chIdx, slIdx, trial.ID);
    ipRelTraj.setColor(Color.red);
    ipRelTraj.setLineWidth(4);
    ipRelTraj.moveTo(xCenter, yCenter);
    int rx = (int) (1000 * Math.cos(Params.angleDirection * Math.PI / 180));
    int ry = (int) (1000 * Math.sin(Params.angleDirection * Math.PI / 180));
    ipRelTraj.lineTo(xCenter + rx, yCenter - ry);
    ipRelTraj.setLineWidth(1);
    // Draw average paths
    IJ.showStatus("Drawing Tracks...");
    for (ListIterator iT = avgTracks.listIterator(); iT.hasNext();) {
      List zTrack = (ArrayList) iT.next();
      ListIterator jT = zTrack.listIterator();
      Cell oldCell = (Cell) jT.next();
      // Variables used to
      Cell firstCell = new Cell();
      firstCell.copy(oldCell);
      int xLast = xCenter;
      int yLast = yCenter;
      for (; jT.hasNext();) {
        Cell newCell = (Cell) jT.next();
        ipRelTraj.setColor(Color.black);
        ipRelTraj.moveTo(xLast, yLast);
        xLast = (int) (xCenter + (newCell.x - firstCell.x)); // Be careful with
                                                             // the java
                                                             // coordinate
                                                             // system and the
                                                             // user CS
        yLast = (int) (yCenter + (newCell.y - firstCell.y)); // Be careful with
                                                             // the java
                                                             // coordinate
                                                             // system and the
                                                             // user CS
        ipRelTraj.lineTo(xLast, yLast);
        oldCell = newCell;
      }
      ipRelTraj.drawOval(xLast - 3, yLast, 6, 6);
    }
    new ImagePlus("Relative trajectories", ipRelTraj).show();
  }

  /******************************************************/
  /**
   * @param impOrig
   * @param impTh
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
   * @param imp
   * @param impTh
   */  
  public void drawType(ImagePlus imp,List spermatozoa){
    int xHeight=imp.getHeight();
    int yWidth=imp.getWidth();  
    IJ.showStatus("Drawing types...");
    ImageProcessor ip = imp.getProcessor();
    ip.setColor(Color.white);
    for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
      Cell cell=(Cell) j.next(); 
      //ip.drawRect((int)aParticle.bx,(int)aParticle.by,(int)aParticle.width,(int)aParticle.height);
      //Draw numbers
      ip.setFont(new Font("SansSerif", Font.PLAIN, 25));
      // we could do someboundary testing here to place the labels better when we are close to the edge
      ip.moveTo((int)(cell.x/Params.pixelWidth+0),doOffset((int)(cell.y/Params.pixelHeight),yWidth,5) );
      ip.drawString(cell.type);
    }
  }

  /******************************************************/
  /**
   * @param histogram
   * @param radius
   * @param chIdx
   * @param sampleID
   */
  public void drawRoseDiagram(int[] histogram, int radius, float chIdx, String sampleID) {

    // Calculate maximum value of the histogram
    // to use it later for normalization
    double max = 0;
    for (int i = 0; i < histogram.length; i++)
      if (histogram[i] > max)
        max = histogram[i];
    double normFactor = radius / max;
    int xCenter = radius;
    int yCenter = radius;
    ColorProcessor roseDiagram = new ColorProcessor(2 * radius, 2 * radius);
    roseDiagram.setColor(Color.white);
    roseDiagram.fill();
    roseDiagram.setColor(new Color((int) 0, 0, 255, 10));
    roseDiagram.setLineWidth(1);
    int NBINS = histogram.length;
    double angleBin = 2 * Math.PI / (double) NBINS;
    // Draw on triangle for each bin
    for (double i = 0; i < NBINS; i++) {
      int value = histogram[(int) i];
      int r = (int) (value * normFactor);
      Polygon p = new Polygon();
      p.addPoint(xCenter, yCenter); // First vertex
      int x = (int) (r * Math.cos(i * angleBin));
      int y = (int) (r * Math.sin(i * angleBin));
      p.addPoint(xCenter + x, yCenter - y); // Second vertex
      x = (int) (r * Math.cos((i + 1) * angleBin));
      y = (int) (r * Math.sin((i + 1) * angleBin));
      p.addPoint(xCenter + x, yCenter - y); // Third vertex
      roseDiagram.fillPolygon(p);
    }
    roseDiagram.setColor(Color.gray);
    roseDiagram.setFont(new Font("SansSerif", Font.PLAIN, 22));
    // Draw line at each 30º
    for (double i = 0; i < 12; i++) {
      int x = (int) (radius * Math.cos(i * (2 * Math.PI / 12)));
      int y = (int) (radius * Math.sin(i * (2 * Math.PI / 12)));
      roseDiagram.moveTo(xCenter, yCenter);
      roseDiagram.lineTo(xCenter + x, yCenter - y);
      roseDiagram.moveTo(xCenter + x, yCenter - y);
      roseDiagram.drawString("" + i * 30);
    }
    roseDiagram.setColor(Color.gray);
    // Draw three concentric circles as reference values
    roseDiagram.drawOval(0, 0, 2 * radius, 2 * radius); // First circle
    roseDiagram.setColor(Color.black);
    int rx = (int) (radius * Math.cos(Math.PI / 3));
    int ry = (int) (radius * Math.sin(Math.PI / 3));
    roseDiagram.moveTo(xCenter + rx, yCenter - ry);
    roseDiagram.setFont(new Font("SansSerif", Font.PLAIN, 30));
    roseDiagram.drawString("" + (int) max);// Draw reference value
    roseDiagram.setColor(Color.gray);
    roseDiagram.drawOval(radius - 2 * radius / 3, radius - 2 * radius / 3, 4 * radius / 3, 4 * radius / 3); // Second
                                                                                                            // circle
    roseDiagram.setColor(Color.black);
    int r = radius - radius / 3;
    rx = (int) (r * Math.cos(Math.PI / 3));
    ry = (int) (r * Math.sin(Math.PI / 3));
    roseDiagram.moveTo(xCenter + rx, yCenter - ry);
    roseDiagram.drawString("" + (int) (2 * max / 3));// Draw reference value
    roseDiagram.setColor(Color.gray);
    roseDiagram.drawOval(radius - radius / 3, radius - radius / 3, 2 * radius / 3, 2 * radius / 3); // Third
                                                                                                    // circle
    roseDiagram.setColor(Color.black);
    r = radius - 2 * radius / 3;
    rx = (int) (r * Math.cos(Math.PI / 3));
    ry = (int) (r * Math.sin(Math.PI / 3));
    roseDiagram.moveTo(xCenter + rx, yCenter - ry);
    roseDiagram.drawString("" + (int) (max / 3));// Draw reference value
    // Draw gradiend direction
    roseDiagram.setColor(Color.red);
    roseDiagram.setLineWidth(4);
    roseDiagram.moveTo(xCenter, yCenter);
    rx = (int) (radius * Math.cos(Params.angleDirection * Math.PI / 180));
    ry = (int) (radius * Math.sin(Params.angleDirection * Math.PI / 180));
    roseDiagram.lineTo(xCenter + rx, yCenter - ry);
    roseDiagram.setColor(new Color(0, 0, 255, 0));
    // Draw chemotaxis cone
    roseDiagram.setColor(Color.green);
    roseDiagram.setLineWidth(8);
    double upperAngle = (360 + Params.angleDirection + Params.angleAmplitude / 2) % 360;
    upperAngle *= Math.PI / 180;
    int x = (int) (radius * Math.cos(upperAngle));
    int y = (int) (radius * Math.sin(upperAngle));
    roseDiagram.moveTo(xCenter, yCenter);
    roseDiagram.lineTo(xCenter + x, yCenter - y);
    double lowerAngle = (360 + Params.angleDirection - Params.angleAmplitude / 2) % 360;
    lowerAngle *= Math.PI / 180;
    x = (int) (radius * Math.cos(lowerAngle));
    y = (int) (radius * Math.sin(lowerAngle));
    roseDiagram.moveTo(xCenter, yCenter);
    roseDiagram.lineTo(xCenter + x, yCenter - y);
    if (Params.compareOppositeDirections) {
      upperAngle = (360 + Params.angleDirection + 180 + Params.angleAmplitude / 2) % 360;
      upperAngle *= Math.PI / 180;
      x = (int) (radius * Math.cos(upperAngle));
      y = (int) (radius * Math.sin(upperAngle));
      roseDiagram.moveTo(xCenter, yCenter);
      roseDiagram.lineTo(xCenter + x, yCenter - y);
      lowerAngle = (360 + Params.angleDirection + 180 - Params.angleAmplitude / 2) % 360;
      lowerAngle *= Math.PI / 180;
      x = (int) (radius * Math.cos(lowerAngle));
      y = (int) (radius * Math.sin(lowerAngle));
      roseDiagram.moveTo(xCenter, yCenter);
      roseDiagram.lineTo(xCenter + x, yCenter - y);
    }
    // Draw sample info
    roseDiagram.setLineWidth(1);
    roseDiagram.setFont(new Font("SansSerif", Font.PLAIN, 30));
    roseDiagram.moveTo(10, 30);
    roseDiagram.setColor(Color.blue);
    roseDiagram.drawString("Sample: " + sampleID);
    roseDiagram.moveTo(10, 70);
    roseDiagram.drawString("Ch-Index: " + chIdx);

    new ImagePlus("Chemotactic Ratios", roseDiagram).show();
  }
  
  /*public void drawScatterPlot(List<Double> x, List<Double> y, String title,String xLabel, String yLabel) {
    
    double nSteps = 10;
   
    double minX = Collections.min(x);
    double maxX = Collections.max(x);
    double stepX = 1.1*maxX/nSteps;
    double minY = Collections.min(y);
    double maxY = Collections.max(y);
    double stepY = 1.1*maxY/nSteps;
    
    ByteProcessor plot = new ByteProcessor(800,800);
    plot.setColor(Color.white);
    plot.fill();
    plot.setLineWidth(4);
    plot.setColor(Color.black);
    //draw axes
    int maxAxis = 700;
    int minAxis = 150;
    int origX = minAxis;
    int origY = maxAxis;
    int shift = 60;
    plot.moveTo(minAxis, maxAxis);
    plot.lineTo(maxAxis, maxAxis);
    plot.moveTo(minAxis, minAxis);
    plot.lineTo(minAxis, maxAxis);

    plot.setFont(new Font("SansSerif", Font.PLAIN, 20));
    for(int i=1;i<10;i++){
      int pos = origX+i*shift;
      plot.moveTo(pos,origY);
      plot.lineTo(pos, origY+20);
      plot.moveTo(pos-10, origY+50);
      plot.drawString("" + i*(int)stepX);
    }
    for(int i=1;i<10;i++){
      int pos = origY-i*shift;
      plot.moveTo(origX,pos);
      plot.lineTo(origX-20, pos);
      plot.moveTo(origX-70,pos+10);
      plot.drawString("" + i*(int)stepY);
    }
    plot.moveTo(440,790);
    plot.drawString("" + xLabel);
    plot = (ByteProcessor) plot.rotateRight();
    plot.setFont(new Font("SansSerif", Font.PLAIN, 20));
    plot.setLineWidth(4);
    plot.setColor(Color.black);
    plot.moveTo(400,50);
    plot.drawString("" + yLabel);
    plot = (ByteProcessor) plot.rotateLeft();
    plot.setFont(new Font("SansSerif", Font.PLAIN, 20));
    plot.setLineWidth(4);
    plot.setColor(Color.black);
    plot.setLineWidth(5);
    int L = x.size();
    for(int i=0;i<L;i++){
      //double[] x = {10,60};
      //double[] y = {50,500};
      int posX = origX+(int)((x.get(i)/stepX)*shift);
      int posY = origY-(int)((y.get(i)/stepY)*shift);
      plot.drawOval(posX,posY, 5,5);
      System.out.println(x.get(i)/stepX+" "+y.get(i)/stepY);
    }
    new ImagePlus(title,plot).show();
  }*/

}
