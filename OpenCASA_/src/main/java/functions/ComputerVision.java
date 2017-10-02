package functions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SList;
import data.Spermatozoon;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * @author Carlos Alquezar
 *
 */
public abstract class ComputerVision implements Measurements {

   /******************************************************/
   /**
    * @param imp
    * @return
    */
   public static double autoThresholdImagePlus(ImagePlus imp) {
      return autoThresholdImagePlus(imp, "Otsu"); // Otsu as a default
                                                  // thresholding method
   }

   /******************************************************/
   /**
    * @param imp
    * @param thresholdMethod
    * @return
    */
   public static double autoThresholdImagePlus(ImagePlus imp, String thresholdMethod) {
      ImageProcessor ip = imp.getProcessor();
      double lowerThreshold = 0;
      ImageStatistics st = ip.getStatistics();
      long[] histlong = st.getHistogram();
      int histogram[] = Utils.convertLongArrayToInt(histlong);
      AutoThresholder at = new AutoThresholder();
      lowerThreshold = (double) at.getThreshold(thresholdMethod, histogram);
      // Upper threshold set to maximum
      double upperThreshold = 255;
      // Threshold image processor
      ComputerVision.thresholdImageProcessor(ip, lowerThreshold, upperThreshold);
      return lowerThreshold;
   }

   /******************************************************/
   /**
    * @param imp
    *           ImagePlus
    * 
    *           This functions converts imp to grayscale.
    */
   public static void convertToGrayscale(ImagePlus imp) {

      ImageConverter ic = new ImageConverter(imp);
      ic.convertToGray8();
   }

   /******************************************************/
   /**
    * @param imp
    *           ImagePlus
    * 
    *           This functions converts imp to grayscale.
    */
   public static void convertToRGB(ImagePlus imp) {

      ImageConverter ic = new ImageConverter(imp);
      ic.convertToRGB();
   }

   /******************************************************/
   /**
    * @param imp
    *           ImagePlus
    * @return 2D-ArrayList with all spermatozoa detected for each frame
    */
   public static List[] detectSpermatozoa(ImagePlus imp) {

      int nFrames = imp.getStackSize();
      ImageStack stack = imp.getStack();
      int options = 0; // set all PA options false
      int measurements = MEAN + CENTROID + RECT + AREA + PERIMETER + FERET;
      // Initialize results table
      ResultsTable rt = new ResultsTable();
      rt.reset();

      int minSize = (int) (Params.minSize * Math.pow((1 / Params.micronPerPixel), 2));
      int maxSize = (int) (Params.maxSize * Math.pow((1 / Params.micronPerPixel), 2));

      // create storage for Spermatozoa positions
      List[] spermatozoa = new ArrayList[nFrames];

      // *************************************************************
      // * Record spermatozoa positions for each frame in an ArrayList
      // *************************************************************/

      for (int iFrame = 1; iFrame <= nFrames; iFrame++) {
         spermatozoa[iFrame - 1] = new ArrayList();
         rt.reset();
         ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
         pa.analyze(imp, stack.getProcessor(iFrame));
         float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);
         float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
         float[] bxRes = rt.getColumn(ResultsTable.ROI_X);
         float[] byRes = rt.getColumn(ResultsTable.ROI_Y);
         float[] widthRes = rt.getColumn(ResultsTable.ROI_WIDTH);
         float[] heightRes = rt.getColumn(ResultsTable.ROI_HEIGHT);
         float[] areaRes = rt.getColumn(ResultsTable.AREA);
         float[] perimeterRes = rt.getColumn(ResultsTable.PERIMETER);
         float[] feretRes = rt.getColumn(ResultsTable.FERET);
         float[] minFeretRes = rt.getColumn(ResultsTable.MIN_FERET);
         if (sxRes == null)
            return null;
         for (int iPart = 0; iPart < sxRes.length; iPart++) {
            Spermatozoon aSpermatozoon = new Spermatozoon();
            aSpermatozoon.id = "***";
            aSpermatozoon.x = sxRes[iPart];
            aSpermatozoon.y = syRes[iPart];
            aSpermatozoon.z = iFrame - 1;
            aSpermatozoon.bx = bxRes[iPart];
            aSpermatozoon.by = byRes[iPart];
            aSpermatozoon.width = widthRes[iPart];
            aSpermatozoon.height = heightRes[iPart];
            aSpermatozoon.total_area = areaRes[iPart];
            aSpermatozoon.total_perimeter = perimeterRes[iPart];
            aSpermatozoon.total_feret = feretRes[iPart];
            aSpermatozoon.total_minFeret = minFeretRes[iPart];
            spermatozoa[iFrame - 1].add(aSpermatozoon);
         }
         IJ.showProgress((double) iFrame / nFrames);
         IJ.showStatus("Identifying spermatozoa per frame...");

      }
      return spermatozoa;
   }

   /******************************************************/
   /**
    * @param impColor
    * @return
    */
   public static ImagePlus getBlueChannel(ImagePlus impColor) {
      ImagePlus[] images = ChannelSplitter.split(impColor);
      return images[2];
   }

   /******************************************************/
   /**
    * @param impColor
    * @return
    */
   public static ImagePlus getGreenChannel(ImagePlus impColor) {
      ImagePlus[] images = ChannelSplitter.split(impColor);
      return images[1];
   }

   /******************************************************/
   /**
    * @param part
    * @param impGray
    * @param impTh
    * @return
    */
   public static float getMeanGrayValue(Spermatozoon part, ImagePlus impGray, ImagePlus impTh) {

      ImageProcessor ipTh = impTh.getProcessor();
      ImageProcessor ipGray = impGray.getProcessor();
      int bx = (int) part.bx;
      int by = (int) part.by;
      int width = (int) part.width;
      int height = (int) part.height;
      float totalGray = 0;
      float totalPixels = 0;
      for (int x = bx; x < (width + bx); x++) {
         IJ.showStatus("scanning pixels...");
         for (int y = by; y < (height + by); y++) {
            int pixel = ipTh.get(x, y);
            if (pixel == 0) {
               totalGray += (float) ipGray.get(x, y);
               totalPixels++;
            }
         }
      }
      return totalGray / totalPixels;
   }

   /******************************************************/
   /**
    * @param impColor
    * @return
    */
   public static ImagePlus getRedChannel(ImagePlus impColor) {
      ImagePlus[] images = ChannelSplitter.split(impColor);
      return images[0];
   }

   /******************************************************/
   /**
    * @param spermatozoa
    *           2D-ArrayList with all spermatozoa detected for each frame
    * @param nFrames
    * @return 2D-ArrayList with all tracks detected
    */
   public static SList idenfityTracks(List[] spermatozoa, int nFrames) {

      // int nFrames = imp.getStackSize();
      SList theTracks = new SList();
      int trackCount = 0;

      for (int i = 0; i <= (nFrames - 1); i++) {

         IJ.showProgress((double) i / nFrames);
         IJ.showStatus("Calculating Tracks...");
         for (ListIterator j = spermatozoa[i].listIterator(); j.hasNext();) {
            Spermatozoon aSpermatozoon = (Spermatozoon) j.next();
            if (!aSpermatozoon.inTrack) {
               // This must be the beginning of a new track
               List aTrack = new ArrayList();
               trackCount++;
               aSpermatozoon.inTrack = true;
               aSpermatozoon.trackNr = trackCount;
               aTrack.add(aSpermatozoon);
               // *************************************************************
               // search in next frames for more Spermatozoa to be added to
               // track
               // *************************************************************
               boolean searchOn = true;
               Spermatozoon oldSpermatozoon = new Spermatozoon();
               Spermatozoon tmpSpermatozoon = new Spermatozoon();
               oldSpermatozoon.copy(aSpermatozoon);
               // *
               // * For each frame
               // *
               for (int iF = i + 1; iF <= (nFrames - 1); iF++) {
                  boolean foundOne = false;
                  Spermatozoon newSpermatozoon = new Spermatozoon();
                  // *
                  // * For each Spermatozoon in this frame
                  // *
                  for (ListIterator jF = spermatozoa[iF].listIterator(); jF.hasNext() && searchOn;) {
                     Spermatozoon testSpermatozoon = (Spermatozoon) jF.next();
                     float distance = testSpermatozoon.distance(oldSpermatozoon);
                     // record a Spermatozoon when it is within the search
                     // radius, and when it had not yet been claimed by another
                     // track
                     if ((distance < (Params.maxDisplacement / Params.micronPerPixel)) && !testSpermatozoon.inTrack) {
                        // if we had not found a Spermatozoon before, it is easy
                        if (!foundOne) {
                           tmpSpermatozoon = testSpermatozoon;
                           testSpermatozoon.inTrack = true;
                           testSpermatozoon.trackNr = trackCount;
                           newSpermatozoon.copy(testSpermatozoon);
                           foundOne = true;
                        } else {
                           // if we had one before, we'll take this one if it is
                           // closer. In any case, flag these Spermatozoa
                           testSpermatozoon.flag = true;
                           if (distance < newSpermatozoon.distance(oldSpermatozoon)) {
                              testSpermatozoon.inTrack = true;
                              testSpermatozoon.trackNr = trackCount;
                              newSpermatozoon.copy(testSpermatozoon);
                              tmpSpermatozoon.inTrack = false;
                              tmpSpermatozoon.trackNr = 0;
                              tmpSpermatozoon = testSpermatozoon;
                           } else {
                              newSpermatozoon.flag = true;
                           }
                        }
                     } else if (distance < (Params.maxDisplacement / Params.micronPerPixel)) {
                        // this Spermatozoon is already in another track but
                        // could have been part of this one
                        // We have a number of choices here:
                        // 1. Sort out to which track this Spermatozoon really
                        // belongs (but how?)
                        // 2. Stop this track
                        // 3. Stop this track, and also delete the remainder of
                        // the other one
                        // 4. Stop this track and flag this Spermatozoon:
                        testSpermatozoon.flag = true;
                     }
                  }
                  if (foundOne)
                     aTrack.add(newSpermatozoon);
                  else
                     searchOn = false;
                  oldSpermatozoon.copy(newSpermatozoon);
               }
               theTracks.add(aTrack);
            }
         }
      }
      return theTracks;
   }

   /******************************************************
    * /**
    * 
    * @param imp
    */
   public static void outlineThresholdImage(ImagePlus imp) {
      convertToGrayscale(imp);
      ImageProcessor ip = imp.getProcessor();
      BinaryProcessor bp = new BinaryProcessor((ByteProcessor) ip);
      bp.outline();
   }

   /******************************************************/
   /**
    * @param imp
    * @param lowerThreshold
    */
   public static void thresholdImagePlus(ImagePlus imp, double lowerThreshold) {
      ImageProcessor ip = imp.getProcessor();
      // Upper threshold set to maximum
      double upperThreshold = 255;
      // Threshold image processor
      ComputerVision.thresholdImageProcessor(ip, lowerThreshold, upperThreshold);
   }

   /******************************************************/
   /**
    * @param ip
    * @param lowerThreshold
    * @param upperThreshold
    */
   public static void thresholdImageProcessor(ImageProcessor ip, double lowerThreshold, double upperThreshold) {
      // Make binary
      int[] lut = new int[256];
      for (int j = 0; j < 256; j++) {
         if (j >= lowerThreshold && j <= upperThreshold)
            lut[j] = (byte) 0;
         else
            lut[j] = (byte) 255;
      }
      ip.applyTable(lut);
   }

   /******************************************************/
   /**
    * @param imp
    *           ImagePlus This function makes binary 'imp' applying an
    *           statistical threshold
    */
   public static void thresholdStack(ImagePlus imp) {

      ImageStack stack = imp.getStack();
      ImageProcessor ip = stack.getProcessor(1);
      ImageStatistics st = ip.getStatistics();
      double mean = st.mean;
      double std = st.stdDev;
      // Set threshold as mean + 2 x standard deviation
      double lowerThreshold = mean + 2 * std; // std factor: candidate to be a
                                              // parameter of the plugin
      double upperThreshold = 255;
      // Make binary
      int[] lut = new int[256];
      for (int j = 0; j < 256; j++) {
         if (j >= lowerThreshold && j <= upperThreshold)
            lut[j] = 0;
         else
            lut[j] = (byte) 255;
      }
      int nFrames = imp.getStackSize();
      for (int iFrame = 1; iFrame <= nFrames; iFrame++) {
         ip = stack.getProcessor(iFrame);
         ip.applyTable(lut);
      }
   }
}
