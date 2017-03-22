package utils;

import java.util.List;
import java.util.ListIterator;

import analysis.MotFunctions__;
import data.Params;
import data.Spermatozoon;
import ij.IJ;
import ij.measure.ResultsTable;

public class Output {

	/******************************************************/
	/**
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return String with the results in tsv format (tab separated values)
	 */
	public static String printXYCoords(List theTracks){
		int nTracks = theTracks.size();
		//strings to print out all of the data gathered, point by point
		String xyPts = " ";		
		//initialize variables
		double x1, y1, x2, y2;
		int trackNr=0;
		int displayTrackNr=0;
		int line=1;
		String output = "Line" + "\tTrack" + "\tRelative_Frame" + "\tX" + "\tY";
		//loop through all sperm tracks
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			int frame = 0;
			trackNr++;
			IJ.showProgress((double)trackNr/nTracks);
			IJ.showStatus("Analyzing Tracks...");
			List bTrack=(List) iT.next();
			//keeps track of the current track
			displayTrackNr++;
			ListIterator jT=bTrack.listIterator();
			Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
			Spermatozoon firstSpermatozoon=new Spermatozoon();
			firstSpermatozoon.copy(oldSpermatozoon);
			
			//For each instant (Spermatozoon) in the track
			String outputline = "";
			for (;jT.hasNext();){ 
				Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
				xyPts = "\t"+displayTrackNr + "\t"+ frame + "\t" + (int)newSpermatozoon.x + "\t" + (int)newSpermatozoon.y;
				frame++;
				oldSpermatozoon=newSpermatozoon;
				outputline += "\n" + line + xyPts;
				line++;
			}
			output+=outputline;				
		}
		return output;
	}
}
