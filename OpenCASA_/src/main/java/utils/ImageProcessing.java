package utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class ImageProcessing {


	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * 
	 * This functions converts imp to grayscale.
	 */	
	public static void convertToGrayscale (ImagePlus imp){
		
		ImageConverter ic = new ImageConverter(imp);
		ic.convertToGray8();
	}
	
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * This function makes binary 'imp' applying an statistical threshold
	 */	
	public static void thresholdStack(ImagePlus imp){
		
		ImageStack stack = imp.getStack();
		ImageProcessor ip = stack.getProcessor(1);
		ImageStatistics st = ip.getStatistics();
		double mean = st.mean;
		double std = st.stdDev;
		//Set threshold as mean + 2 x standard deviation
		double lowerThreshold = mean+2*std; // std factor: candidate to be a parameter of the plugin
		double upperThreshold = 255;
		//Make binary
		int[] lut = new int[256];
		for (int j=0; j<256; j++) {
			if (j>=lowerThreshold && j<=upperThreshold)
				lut[j] = 0;
			else
				lut[j] = (byte)255;
		}
		int nFrames = imp.getStackSize();
		for (int iFrame=1; iFrame<=nFrames; iFrame++) {
			ip = stack.getProcessor(iFrame);
            ip.applyTable(lut);
		}
	}
	

}
