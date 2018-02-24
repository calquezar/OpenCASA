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

package functions;

import data.Cell;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ChannelSplitter;
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
public class ComputerVision {

  /******************************************************/

  /**
   * @param imp
   * @return
   */
  public double autoThresholdImagePlus(ImagePlus imp) {
    return autoThresholdImagePlus(imp, "Otsu"); // Otsu as a default
                                                // thresholding method
  }

  /******************************************************/
  /**
   * @param imp
   * @param thresholdMethod
   * @return
   */
  public double autoThresholdImagePlus(ImagePlus imp, String thresholdMethod) {
    ImageProcessor ip = imp.getProcessor();
    double lowerThreshold = 0;
    ImageStatistics st = ip.getStatistics();
    long[] histlong = st.getHistogram();
    Utils utils = new Utils();
    int histogram[] = utils.convertLongArrayToInt(histlong);
    AutoThresholder at = new AutoThresholder();
    lowerThreshold = (double) at.getThreshold(thresholdMethod, histogram);
    // Upper threshold set to maximum
    double upperThreshold = 255;
    // Threshold image processor
    thresholdImageProcessor(ip, lowerThreshold, upperThreshold);
    return lowerThreshold;
  }

  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   * 
   *          This functions converts imp to grayscale.
   */
  public void convertToGrayscale(ImagePlus imp) {
    ImageConverter ic = new ImageConverter(imp);
    ic.convertToGray8();
  }

  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   * 
   *          This functions converts imp to grayscale.
   */
  public void convertToRGB(ImagePlus imp) {
    ImageConverter ic = new ImageConverter(imp);
    ic.convertToRGB();
  }

  /******************************************************/
  /**
   * @param impColor
   * @return
   */
  public ImagePlus getBlueChannel(ImagePlus impColor) {
    ImagePlus[] images = ChannelSplitter.split(impColor);
    return images[2];
  }

  /******************************************************/
  /**
   * @param impColor
   * @return
   */
  public ImagePlus getGreenChannel(ImagePlus impColor) {
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
  public float getMeanGrayValue(Cell part, ImagePlus impGray, ImagePlus impTh) {

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
  public ImagePlus getRedChannel(ImagePlus impColor) {
    ImagePlus[] images = ChannelSplitter.split(impColor);
    return images[0];
  }

  /******************************************************
   * /**
   * 
   * @param imp
   */
  public void outlineThresholdImage(ImagePlus imp) {
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
  public void thresholdImagePlus(ImagePlus imp, double lowerThreshold) {
    ImageProcessor ip = imp.getProcessor();
    // Upper threshold set to maximum
    double upperThreshold = 255;
    // Threshold image processor
    thresholdImageProcessor(ip, lowerThreshold, upperThreshold);
  }

  /******************************************************/
  /**
   * @param ip
   * @param lowerThreshold
   * @param upperThreshold
   */
  public void thresholdImageProcessor(ImageProcessor ip, double lowerThreshold, double upperThreshold) {
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
   * @param ImagePlus
   *          This function makes binary 'imp' applying an statistical threshold
   */
  public void thresholdStack(ImagePlus imp) {

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
