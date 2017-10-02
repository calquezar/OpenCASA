package functions;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SList;
import data.Spermatozoon;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * @author Carlos Alquezar
 *
 */
public abstract class Paint {

   /******************************************************/
   /**
    * @param ip
    * @param numTracks
    * @param chIdx
    * @param slIdx
    * @param sampleID
    */
   public static void chemotaxisTemplate(ColorProcessor ip, int numTracks, float chIdx, float slIdx, String sampleID) {
      // Alpha version of this method
      ip.setLineWidth(4);
      // center coords. of the cone used to clasify chemotactic trajectories
      int xCenter = ip.getWidth() / 2;
      int yCenter = ip.getHeight() / 2;
      float upperAngle = (float) (Params.angleDirection + Params.angleAmplitude / 2 + 360) % 360;
      upperAngle = upperAngle * (float) Math.PI / 180; // calculate and convert
                                                       // to radians
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
      // Reses line width
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
      ip.moveTo(10, 70);
      ip.setColor(Color.red);
      ip.drawString("Ch-Index: ");
      ip.moveTo(70, 70);
      ip.setColor(Color.black);
      ip.drawString("" + chIdx * 100 + "%");
      ip.moveTo(10, 90);
      ip.setColor(new Color(34, 146, 234));
      ip.drawString("SL-Index: ");
      ip.moveTo(80, 90);
      ip.setColor(Color.black);
      ip.drawString("" + slIdx * 100 + "%");
   }

   /**
    * @param center
    * @param maxSize
    * @param displacement
    * @return
    */
   static int doOffset(int center, int maxSize, int displacement) {
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
    *           2D-ArrayList with all the tracks
    */
   public static void draw(ImagePlus imp, SList theTracks) {

      ComputerVision.convertToRGB(imp);
      int nFrames = imp.getStackSize();
      ImageStack stack = imp.getStack();
      if (imp.getCalibration().scaled()) {
         IJ.showMessage("MultiTracker", "Cannot display paths if image is spatially calibrated");
         return;
      }
      int upRes = 1;
      String strPart;
      // Variables used to draw chemotactic cone
      int trackNr = 0;
      int displayTrackNr = 0;
      SList avgTracks = SignalProcessing.averageTracks(theTracks);
      // Draw on each frame
      for (int iFrame = 1; iFrame <= nFrames; iFrame++) {
         IJ.showProgress((double) iFrame / nFrames);
         IJ.showStatus("Drawing Tracks...");
         int trackCount2 = 0;
         int trackCount3 = 0;
         int color;
         int xHeight = stack.getHeight();
         int yWidth = stack.getWidth();
         ImageProcessor ip = stack.getProcessor(iFrame);
         ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
         trackNr = 0;
         displayTrackNr = 0;
         for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
            trackNr++;
            trackCount2++;
            List zTrack = (ArrayList) iT.next();
            displayTrackNr++;
            ListIterator jT = zTrack.listIterator();
            Spermatozoon oldSpermatozoon = (Spermatozoon) jT.next();
            color = 150;
            trackCount3++;
            for (; jT.hasNext();) {
               Spermatozoon newSpermatozoon = (Spermatozoon) jT.next();
               if (Kinematics.getVelocityTrackType(zTrack) == "Slow")
                  ip.setColor(Color.white);
               else if (Kinematics.getVelocityTrackType(zTrack) == "Normal")
                  ip.setColor(Color.yellow);
               else if (Kinematics.getVelocityTrackType(zTrack) == "Fast")
                  ip.setColor(Color.red);
               // ip.setValue(color);
               if (Params.drawOrigTrajectories) {
                  ip.moveTo((int) oldSpermatozoon.x * upRes, (int) oldSpermatozoon.y * upRes);
                  ip.lineTo((int) newSpermatozoon.x * upRes, (int) newSpermatozoon.y * upRes);
               }
               oldSpermatozoon = newSpermatozoon;
               // Draw track numbers
               if (newSpermatozoon.z == iFrame) {
                  strPart = "" + displayTrackNr;
                  ip.setColor(Color.black);
                  // we could do someboundary testing here to place the labels
                  // better when we are close to the edge
                  ip.moveTo((int) (oldSpermatozoon.x / Params.pixelWidth + 0),
                        doOffset((int) (oldSpermatozoon.y / Params.pixelHeight), yWidth, 5));
                  ip.setColor(Color.white);
                  ip.drawString(strPart);
               }
            }
         }
         // //Draw average paths
         // color=0;
         // for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
         // List zTrack=(ArrayList) iT.next();
         // ListIterator jT=zTrack.listIterator();
         // Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
         // //Variables used to
         // Spermatozoon firstSpermatozoon = new Spermatozoon();
         // firstSpermatozoon.copy(oldSpermatozoon);
         // for (;jT.hasNext();) {
         // Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
         // ip.setValue(color);
         // if(Params.drawAvgTrajectories){
         // ip.moveTo((int)oldSpermatozoon.x*upRes,
         // (int)oldSpermatozoon.y*upRes);
         // ip.lineTo((int)newSpermatozoon.x*upRes,
         // (int)newSpermatozoon.y*upRes);
         // }
         // oldSpermatozoon=newSpermatozoon;
         // }
         // }
         System.out.println("Drawind frame: " + iFrame);
      }
      imp.updateAndRepaintWindow();
   }
   // /******************************************************/
   // /**
   // * @param imp
   // * @param theTracks 2D-ArrayList with all the tracks
   // * @param avgTracks 2D-ArrayList with the averaged tracks
   // * @param chIdx
   // * @param slIdx
   // */
   // public static void draw(ImagePlus imp,List theTracks,List avgTracks,float
   // chIdx,float slIdx){
   // int nFrames = imp.getStackSize();
   // ImageStack stack = imp.getStack();
   // if (imp.getCalibration().scaled()) {
   // IJ.showMessage("MultiTracker", "Cannot display paths if image is spatially
   // calibrated");
   // return;
   // }
   // int upRes = 1;
   // String strPart;
   // //Variables used to draw chemotactic cone
   // int trackNr=0;
   // int displayTrackNr=0;
   // //We create another ImageProcesor to draw chemotactic cone and relative
   // trajectories
   // ColorProcessor ipRelTraj = new ColorProcessor(imp.getWidth()*upRes,
   // imp.getHeight()*upRes);
   // ipRelTraj.setColor(Color.white);
   // ipRelTraj.fill();
   // if(Params.drawRelTrajectories){
   // //Draw cone used to clasify chemotactic trajectories
   // ipRelTraj.setColor(Color.green);
   // chemotaxisTemplate(ipRelTraj,upRes,avgTracks.size(),chIdx,slIdx);
   // }
   // //Draw on each frame
   // for (int iFrame=1; iFrame<=nFrames; iFrame++) {
   // IJ.showProgress((double)iFrame/nFrames);
   // IJ.showStatus("Drawing Tracks...");
   // int trackCount2=0;
   // int trackCount3=0;
   // int color;
   // int xHeight=stack.getHeight();
   // int yWidth=stack.getWidth();
   //
   // ImageProcessor ip = stack.getProcessor(iFrame);
   // ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
   // trackNr=0;
   // displayTrackNr=0;
   // for (ListIterator iT=theTracks.listIterator();iT.hasNext();) {
   // trackNr++;
   // trackCount2++;
   // List zTrack=(ArrayList) iT.next();
   // displayTrackNr++;
   // ListIterator jT=zTrack.listIterator();
   // Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
   // color = 150;
   // trackCount3++;
   // for (;jT.hasNext();) {
   // Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
   // ip.setValue(color);
   // if(Params.drawOrigTrajectories){
   // ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
   // ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
   // }
   // oldSpermatozoon=newSpermatozoon;
   // //Draw track numbers
   // if(newSpermatozoon.z==iFrame){
   // strPart=""+displayTrackNr;
   // ip.setColor(Color.black);
   // // we could do someboundary testing here to place the labels better when
   // we are close to the edge
   // ip.moveTo((int)(oldSpermatozoon.x/Params.pixelWidth+0),doOffset((int)(oldSpermatozoon.y/Params.pixelHeight),yWidth,5)
   // );
   // ip.drawString(strPart);
   // }
   // }
   // }
   // //Draw average paths
   // color=0;
   // for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
   // List zTrack=(ArrayList) iT.next();
   // ListIterator jT=zTrack.listIterator();
   // Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
   // //Variables used to
   // Spermatozoon firstSpermatozoon = new Spermatozoon();
   // firstSpermatozoon.copy(oldSpermatozoon);
   // int xCenter = ip.getWidth()/2;
   // int yCenter = ip.getHeight()/2;
   // int xLast = xCenter;
   // int yLast = yCenter;
   //
   // for (;jT.hasNext();) {
   // Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
   // ip.setValue(color);
   // if(Params.drawAvgTrajectories){
   // ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
   // ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
   // }
   // if(Params.drawRelTrajectories){
   // ipRelTraj.setColor(Color.black);
   // ipRelTraj.moveTo(xLast,yLast);
   // xLast = (int)(newSpermatozoon.x-firstSpermatozoon.x+xCenter);
   // yLast = (int)(newSpermatozoon.y-firstSpermatozoon.y+yCenter);
   // ipRelTraj.lineTo(xLast*upRes, yLast*upRes);
   // }
   // oldSpermatozoon=newSpermatozoon;
   // }
   // ipRelTraj.drawOval(xLast-3,yLast,6,6);
   // }
   // }
   // imp.updateAndRepaintWindow();
   // if(Params.drawRelTrajectories)
   // new ImagePlus("Chemotactic Ratios", ipRelTraj).show();
   // }

   /******************************************************/
   /**
    * @param imp
    * @param spermatozoa
    */
   public static void drawBoundaries(ImagePlus imp, List spermatozoa) {
      int xHeight = imp.getHeight();
      int yWidth = imp.getWidth();
      IJ.showStatus("Drawing boundaries...");
      ImageProcessor ip = imp.getProcessor();
      ip.setColor(Color.white);
      for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
         Spermatozoon sperm = (Spermatozoon) j.next();
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
            e.printStackTrace();
            // ip.drawString throws eventually an exception.
            // Possibly it is a bug in the ImageProcessor implementation of this
            // ImageJ version
         }
      }
   }

   /******************************************************/
   /**
    * @param theTracks
    *           2D-ArrayList with all the tracks
    * @param chIdx
    * @param slIdx
    * @param width
    * @param height
    * @param sampleID
    */
   public static void drawChemotaxis(SList theTracks, float chIdx, float slIdx, int width, int height,
         String sampleID) {

      SList avgTracks = SignalProcessing.averageTracks(theTracks);
      String strPart;
      // Variables used to draw chemotactic cone
      int displayTrackNr = 0;
      // We create another ImageProcesor to draw chemotactic cone and relative
      // trajectories
      ColorProcessor ipRelTraj = new ColorProcessor(width, height);
      ipRelTraj.setColor(Color.white);
      ipRelTraj.fill();
      int xCenter = width / 2;
      int yCenter = height / 2;
      // Draw cone used to clasify chemotactic trajectories
      ipRelTraj.setColor(Color.green);
      chemotaxisTemplate(ipRelTraj, avgTracks.size(), chIdx, slIdx, sampleID);
      ipRelTraj.setColor(Color.red);
      ipRelTraj.setLineWidth(4);
      ipRelTraj.moveTo(xCenter, yCenter);
      int rx = (int) (1000 * Math.cos(Params.angleDirection * Math.PI / 180));
      int ry = (int) (1000 * Math.sin(Params.angleDirection * Math.PI / 180));
      ipRelTraj.lineTo(xCenter + rx, yCenter - ry);
      ipRelTraj.setLineWidth(1);
      // Draw average paths
      IJ.showStatus("Drawing Tracks...");
      int color = 0;
      for (ListIterator iT = avgTracks.listIterator(); iT.hasNext();) {
         List zTrack = (ArrayList) iT.next();
         ListIterator jT = zTrack.listIterator();
         Spermatozoon oldSpermatozoon = (Spermatozoon) jT.next();
         // Variables used to
         Spermatozoon firstSpermatozoon = new Spermatozoon();
         firstSpermatozoon.copy(oldSpermatozoon);
         int xLast = xCenter;
         int yLast = yCenter;
         for (; jT.hasNext();) {
            Spermatozoon newSpermatozoon = (Spermatozoon) jT.next();
            ipRelTraj.setColor(Color.black);
            ipRelTraj.moveTo(xLast, yLast);
            xLast = (int) (xCenter + (newSpermatozoon.x - firstSpermatozoon.x));
            yLast = (int) (yCenter - (newSpermatozoon.y - firstSpermatozoon.y));
            ipRelTraj.lineTo(xLast, yLast);
            oldSpermatozoon = newSpermatozoon;
         }
         ipRelTraj.drawOval(xLast - 3, yLast, 6, 6);
      }
      new ImagePlus("Chemotactic Ratios", ipRelTraj).show();
   }

   /******************************************************/
   /**
    * @param impOrig
    * @param impTh
    */
   public static void drawOutline(ImagePlus impOrig, ImagePlus impTh) {

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
    * @param histogram
    * @param radius
    * @param chIdx
    * @param sampleID
    */
   public static void drawRoseDiagram(int[] histogram, int radius, float chIdx, String sampleID) {

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

}
