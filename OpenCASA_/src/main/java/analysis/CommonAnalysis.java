package analysis;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.SList;
import data.Trial;
import gui.MessageWindow;
import ij.ImagePlus;
import plugins.AVI_Reader;
import utils.ImageProcessing;
import utils.TrackFilters;
import utils.Utils;

public abstract class CommonAnalysis {

	/**
	 * @param ImagePlus imp
	 */
	public static SList analyze(ImagePlus imp){
		
		System.out.println("converToGrayScale...");
 		ImageProcessing.convertToGrayscale(imp);
		//************************************************************ 
		// * Automatic Thresholding
		//************************************************************
 		System.out.println("thresholdStack...");
		ImageProcessing.thresholdStack(imp);
		//************************************************************ 
		// * Record particle positions for each frame in an ArrayList
		//************************************************************
		System.out.println("detectSpermatozoa...");
		List[] theParticles = ImageProcessing.detectSpermatozoa(imp);
		//************************************************************ 
		// * Now assemble tracks out of the spermatozoa lists
		// * Also record to which track a particle belongs in ArrayLists
		//************************************************************
		System.out.println("identifyTracks...");
		SList theTracks = ImageProcessing.idenfityTracks(theParticles,imp.getStackSize());
		//************************************************************ 
		// * Filter the tracks list
		// * (We have to filter the tracks list because not all of them are valid)
		//************************************************************
		System.out.println("filterTracks...");
		theTracks = TrackFilters.filterTracks(theTracks);	
		return theTracks;
	}
	
	public static Trial extractTrial(String analysis){
		
		String absoluteFilePath = Utils.selectFile();
		if(absoluteFilePath==null)
			return null;
		String[] parts = absoluteFilePath.split("\\\\");
		String filename = parts[parts.length-1];
		String trialType = "";
		String trialID = "";
		if(analysis.equals("Chemotaxis")){
			trialType = getTrialType(filename);
			trialID = getID(filename);
		}else if(analysis.equals("Motility"))
			trialID = filename;
		AVI_Reader ar = new  AVI_Reader();
		ar.run(absoluteFilePath);
		ImagePlus imp = ar.getImagePlus();
		//Analyze the video
		SList t = analyze(imp);
		Trial tr = null;
		if(analysis.equals("Chemotaxis"))
			tr = new Trial(trialID,trialType,filename,t);
		else if(analysis.equals("Motility"))
			tr = new Trial(trialID,trialType,filename,t,imp);
		return tr;
	}
	public static Map<String,Trial> extractTrials(String analysis){
		
		Map<String,Trial> trials = new HashMap<String,Trial>();
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0)
			return null;
		for (int i = 0; i < listOfFiles.length; i++) {
		    if (new File(listOfFiles[i]).isFile()) {
		    	String absoluteFilePath = listOfFiles[i];
				if(Utils.isAVI(absoluteFilePath)){
			    	String[] parts = absoluteFilePath.split("\\\\");
					String filename = parts[parts.length-1];
					String trialType = "";
					String trialID = "";
					if(analysis.equals("Chemotaxis")){
						trialType = getTrialType(filename);
						trialID = getID(filename);
					}else if(analysis.equals("Motility")){
						trialID = filename;
					}
					//Load video
					AVI_Reader ar = new  AVI_Reader();
					ar.run(absoluteFilePath);
					ImagePlus imp = ar.getImagePlus();
					//Analyze the video
					SList t = analyze(imp);
					Trial tr = new Trial(trialID,trialType,filename,t);
					trials.put(trialID, tr);
					//new Thread(new Runnable() {public void run() {analyze(imp,filename);}}).start();							
				}
		    } //else if (new File(listOfFiles[i]).isDirectory()) {}		    
		}
		return trials;
	}
	
	public static String getID(String filename){
		//Format YYYY-MM-DD-ID-C-numVideo-Medium (for control)
		//Format YYYY-MM-DD-ID-Q-Hormone-Concentration-numVideo-Medium (with hormone)
		String[] parts = filename.split("-");
		String type = getTrialType(filename);
		return parts[0]+parts[1]+parts[2]+parts[3]+type;
	}
	/******************************************************/
	/**
	 * @param String filename
	 * @return String type
	 */	
	public static String getTrialType(String filename){
		//Format YYYY-MM-DD-ID-C-numVideo-Medium (for control)
		//Format YYYY-MM-DD-ID-Q-Hormone-Concentration-numVideo-Medium (with hormone)
		String[] parts = filename.split("-");
		if(parts[4].equals("Q")){
			String hormone = parts[5];
			String concentration = parts[6];
			return hormone+concentration;
		}else{
			return "C"; //If It's not chemotaxis, then it's control
		}
	}	
}
