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
import utils.ComputerVision;
import utils.SignalProcessing;
import utils.Utils;

public abstract class VideoAnalyzer {

	/**
	 * @param ImagePlus imp
	 */
	public static SList analyze(ImagePlus imp){
		
		System.out.println("converToGrayScale...");
 		ComputerVision.convertToGrayscale(imp);
		//************************************************************ 
		// * Automatic Thresholding
		//************************************************************
 		System.out.println("thresholdStack...");
		ComputerVision.thresholdStack(imp);
		//************************************************************ 
		// * Record particle positions for each frame in an ArrayList
		//************************************************************
		System.out.println("detectSpermatozoa...");
		List[] theParticles = ComputerVision.detectSpermatozoa(imp);
		//************************************************************ 
		// * Now assemble tracks out of the spermatozoa lists
		// * Also record to which track a particle belongs in ArrayLists
		//************************************************************
		System.out.println("identifyTracks...");
		SList theTracks = ComputerVision.idenfityTracks(theParticles,imp.getStackSize());
		//Filtering tracks by length
		theTracks = SignalProcessing.filterTracksByLength(theTracks);
		
		return theTracks;
	}
	
	public static Trial extractTrial(String analysis){
		String absoluteFilePath = Utils.selectFile();
		if(absoluteFilePath==null)
			return null;
		if(!Utils.isAVI(absoluteFilePath))
			return  new Trial();
		return getTrialFromAVI(analysis,absoluteFilePath);
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
			    	Trial tr = getTrialFromAVI(analysis,absoluteFilePath);
					trials.put(tr.ID, tr);
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
		// ID's format:
		//	for chemotaxis: YYYYMMDD-[ID]-Q[hormone+concentration]
		//	for control: YYYYMMDD-[ID]-C
		return parts[0]+parts[1]+parts[2]+'-'+parts[3]+'-'+type;
	}
	
	public static Trial getTrialFromAVI(String analysis,String absoluteFilePath){
		
		String[] parts = absoluteFilePath.split("\\\\");
		String filename = parts[parts.length-1];
		String trialType = "";
		String trialID = "";
		if(analysis.equals("Chemotaxis-File")||analysis.equals("Chemotaxis-Directory")){
			trialType = getTrialType(filename);
			trialID = getID(filename);
		}else if(analysis.equals("Motility-File")||analysis.equals("Motility-Directory")){
			trialID = filename;
		}
		//Load video
		AVI_Reader ar = new  AVI_Reader();
		ar.run(absoluteFilePath);
		ImagePlus imp = ar.getImagePlus();
		//Analyze the video
		SList t = analyze(imp);
		int[] motileSperm = SignalProcessing.motilityTest(t);
		//Only pass from here tracks with a minimum level of motility
		t = SignalProcessing.filterTracksByMotility(t);
		Trial tr = null;
		if(analysis.equals("Chemotaxis-File")||analysis.equals("Chemotaxis-Directory"))
			tr = new Trial(trialID,trialType,filename,t,imp.getWidth(),imp.getHeight());
		else if(analysis.equals("Motility-File"))
			tr = new Trial(trialID,trialType,filename,t,imp,motileSperm);
		else if(analysis.equals("Motility-Directory"))
			tr = new Trial(trialID,trialType,filename,t,null,motileSperm);
		//new Thread(new Runnable() {public void run() {analyze(imp,filename);}}).start();
		return tr;
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
			return "Q"+hormone+concentration;
		}else{
			return "C"; //If It's not chemotaxis, then it's control
		}
	}	
}
