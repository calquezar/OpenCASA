package analysis;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Params;
import data.SList;
import data.Trial;
import ij.IJ;
import ij.ImagePlus;
import plugins.AVI_Reader;
import utils.ImageProcessing;
import utils.Output;
import utils.TrackFilters;
import utils.Utils;

public abstract class CommonAnalysis {

	public static Map<String,Trial> generateTrials(){
		
		Map<String,Trial> trials = new HashMap<String,Trial>();
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0)
			return null;
		for (int i = 0; i < listOfFiles.length; i++) {
		    if (new File(listOfFiles[i]).isFile()) {
		    	String filename = listOfFiles[i];
				if(Utils.isAVI(filename)){
					String trialType = ChFunctions__.getTrialType(filename);
					String trialID = ChFunctions__.getID(filename);
					String[] parts = filename.split("\\\\");
					System.out.println(parts[parts.length-1]);
						
//					//Load video
//					AVI_Reader ar = new  AVI_Reader();
//					ar.run(filename);
//					final ImagePlus imp = ar.getImagePlus();
//					//Analyze the video
//					SList t = analyze(imp,filename);
//					Trial tr = new Trial(trialID,trialType,t);
//					trials.put(trialID, tr);
					//new Thread(new Runnable() {public void run() {analyze(imp,filename);}}).start();							
				}
		    } //else if (new File(listOfFiles[i]).isDirectory()) {}		    
		}
		return trials;
	}
	/**
	 * 
	 * @param ImagePlus imp
	 */
	public static SList analyze(ImagePlus imp,String filename){
		
		//System.out.println("converToGrayScale...");
 		ImageProcessing.convertToGrayscale(imp);
		//************************************************************ 
		// * Automatic Thresholding
		//************************************************************
// 		System.out.println("thresholdStack...");
		ImageProcessing.thresholdStack(imp);
		//************************************************************ 
		// * Record particle positions for each frame in an ArrayList
		//************************************************************
//		System.out.println("detectSpermatozoa...");
		List[] theParticles = ImageProcessing.detectSpermatozoa(imp);
		//************************************************************ 
		// * Now assemble tracks out of the spermatozoa lists
		// * Also record to which track a particle belongs in ArrayLists
		//************************************************************
//		System.out.println("identifyTracks...");
		SList theTracks = ImageProcessing.idenfityTracks(theParticles,imp.getStackSize());
		//************************************************************ 
		// * Filter the tracks list
		// * (We have to filter the tracks list because not all of them are valid)
		//************************************************************
//		System.out.println("filterTracks...");
		theTracks = TrackFilters.filterTracks(theTracks);	
		return theTracks;
	}	
}
