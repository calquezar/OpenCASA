package spermAnalysis;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import data.Params;
import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;
import plugins.AVI_Reader;
import utils.ImageProcessing;
import utils.Output;
import utils.TrackFilters;

public class ChemotaxisAnalysis {
	
	private File directory;
	
	/**
	 * Constructor
	 */
	public ChemotaxisAnalysis(){}
	
	/**
	 * 
	 * @return File[]
	 */
	public File[] getFileNames(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("F:\\VIDEOS QUIMIOTAXIS"));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		  //System.out.println("Directory: "+chooser.getSelectedFile());
		  File folder = chooser.getSelectedFile();
		  File[] listOfFiles = folder.listFiles();
		  directory = chooser.getSelectedFile();
		  return listOfFiles;	
		}
		return null;
	}		
	/**
	 * 
	 * @param ImagePlus imp
	 */
	public void analyze(ImagePlus imp){
		
 		ImageProcessing.convertToGrayscale(imp);
		//************************************************************ 
		// * Auto-Threshold Video
		//************************************************************
		ImageProcessing.thresholdStack(imp);
		//************************************************************ 
		// * Record particle positions for each frame in an ArrayList
		//************************************************************
		List[] theParticles = ImageProcessing.detectSpermatozoa(imp);
		//************************************************************ 
		// * Now assemble tracks out of the particle lists
		// * Also record to which track a particle belongs in ArrayLists
		//************************************************************
		List theTracks = ImageProcessing.idenfityTracks(theParticles,imp.getStackSize());
		//************************************************************ 
		// * Filter the tracks list
		// * (We have to filter the tracks list because not all of them are valid)
		//************************************************************
		theTracks = TrackFilters.filterTracks(theTracks);
		//************************************************************
		// * Average the tracks 
		//************************************************************
		List avgTracks = TrackFilters.averageTracks(theTracks);	
		//************************************************************ 
		// * Calculate output
		//************************************************************
		// XY Coordinates
		String xyCoordsOutput = ""; 
		if(Params.printXY){
			xyCoordsOutput = Output.printXYCoords(theTracks);
			IJ.saveString(xyCoordsOutput,"");		
		}
//		// Motility results
//		ResultsTable motResults = Output.calculateMotility(theTracks);
//		if(Params.calcMotilityParameters){
//			motResults.show("Motility Resume");
//		}
//		if(Params.calcMeanMotilityParameters){
//			ResultsTable avgMotility = Output.calculateAverageMotility(theTracks.size());
//			avgMotility.show("Motility Average");
//		}
		//To calculate Ratio-Q we have to decimate all tracks
		List decimatedTracks = TrackFilters.decimateTracks(avgTracks,Params.decimationFactor);
		// Chemotaxis ratios
		float ratioQ = ChFunctions.calculateRatioQ(avgTracks);
		//IJ.log("RatioQ: "+ratioQ);
		float ratioSL = ChFunctions.calculateRatioSL(avgTracks);
		
//		setQtResults(ratioQ,ratioSL,theTracks.size());
//		rTable.show("Ratios Quimiotaxis");
		//************************************************************ 
		// * Draw tracks at each frame
		//************************************************************
		ImageProcessing.draw(imp,theTracks,avgTracks,ratioQ,ratioSL);
	}
	/**
	 * 
	 * @param MainWindow mw
	 */
	public void run(MainWindow mw){
		mw.setVisible(false);
		File[] listOfFiles = getFileNames();
		for (int i = 0; i < listOfFiles.length; i++) {
		    if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
				AVI_Reader ar = new  AVI_Reader();
				ar.run(directory+"\\"+listOfFiles[i].getName());
				final ImagePlus imp = ar.getImagePlus();
				new Thread(new Runnable() {public void run() {imp.show();analyze(imp);}}).start();
		    } else if (listOfFiles[i].isDirectory()) {
		    	System.out.println("Directory " + listOfFiles[i].getName());
		    }
	   }
	}
}
