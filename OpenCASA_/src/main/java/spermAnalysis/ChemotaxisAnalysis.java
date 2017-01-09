package spermAnalysis;

import java.util.List;

import data.Params;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import utils.ImageProcessing;
import utils.Output;
import utils.TrackFilters;

public class ChemotaxisAnalysis {

	public ChemotaxisAnalysis(){
		
	}
	
	public void run(){
		
		final ImagePlus impOrig = IJ.openImage();
		final ImagePlus imp = impOrig.duplicate();
		impOrig.show();
		new Thread(new Runnable() {
		     public void run() {
		    	
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
				// Motility results
				ResultsTable motResults = Output.calculateMotility(theTracks);
				if(Params.calcMotilityParameters){
					motResults.show("Motility Resume");
				}
				if(Params.calcMeanMotilityParameters){
					ResultsTable avgMotility = Output.calculateAverageMotility(theTracks.size());
					avgMotility.show("Motility Average");
				}
				//To calculate Ratio-Q we have to decimate all tracks
				List decimatedTracks = TrackFilters.decimateTracks(avgTracks,Params.decimationFactor);
				// Chemotaxis ratios
				float ratioQ = ChFunctions.calculateRatioQ(avgTracks);
				//IJ.log("RatioQ: "+ratioQ);
				float ratioSL = ChFunctions.calculateRatioSL(avgTracks);
				
//				setQtResults(ratioQ,ratioSL,theTracks.size());
//				rTable.show("Ratios Quimiotaxis");
				//************************************************************ 
				// * Draw tracks at each frame
				//************************************************************
				ImageProcessing.draw(impOrig,theTracks,avgTracks,ratioQ,ratioSL);			    	 
		     }
		}).start();
	}
}
