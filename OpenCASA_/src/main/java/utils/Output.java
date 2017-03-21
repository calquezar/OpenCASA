package utils;

import java.util.List;
import java.util.ListIterator;

import analysis.MotFunctions;
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
	/******************************************************/
	/**
	 * @param theTracks 2d-Array with all tracks
	 * @return 
	 */	
	public static ResultsTable calculateMotility(List theTracks){
		
		ResultsTable rt = new ResultsTable();
		
		//Calculate values for each track
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			List aTrack=(List)iT.next();
			List avgTrack = TrackFilters.movingAverage(aTrack);
			float length = (float)aTrack.size();
			// VSL
			float vsl_value = MotFunctions.vsl(aTrack);
			Params.total_vsl+=vsl_value;
			// VCL
			float vcl_value =  MotFunctions.vcl(aTrack);
			Params.total_vcl+=vcl_value;
			// VAP is equivalent to calculate vcl from averaged track
			float vap_value =  MotFunctions.vcl(avgTrack);
			Params.total_vap+=vap_value;
			// Linearity
			float lin_value = (vsl_value/vcl_value)*100;
			Params.total_lin+=lin_value;
			// Wobble
			float wob_value = (vap_value/vcl_value)*100;
			Params.total_wob+=wob_value;
			// Straightness
			float str_value = (vsl_value/vap_value)*100;
			Params.total_str+=str_value;
			// Amplitude of lateral head
			float alh_values[] =  MotFunctions.alh(aTrack,avgTrack);
			Params.total_alhMean+=alh_values[0];
			Params.total_alhMax+=alh_values[1];
			// Beat-cross frequency
			float bcf_value =  MotFunctions.bcf(aTrack,avgTrack);
			Params.total_bcf+=bcf_value;
			//Progressive motility
			String progressMotility_value = "NO";
			if(str_value> Params.progressMotility){
				progressMotility_value = "YES";	
				Params.countProgressiveSperm++;
			}	
			// DANCE
			float dance_value =  vcl_value*alh_values[0];
			Params.total_dance+=dance_value;
			//MAD
			float mad_value = MotFunctions.mad(aTrack);
			Params.total_mad+=mad_value;

			rt.incrementCounter();
			rt.addValue("Length (frames)",length);
			rt.addValue("VSL (um/s)",vsl_value);
			rt.addValue("VCL (um/s)",vcl_value);
			rt.addValue("VAP (um/s)",vap_value);
			rt.addValue("LIN",lin_value);
			rt.addValue("WOB",wob_value);
			rt.addValue("STR",str_value);
			rt.addValue("ALH_Mean (um)",alh_values[0]);
			rt.addValue("ALH_Max (um)",alh_values[1]);
			rt.addValue("BCF (Hz)",bcf_value);
			rt.addValue("DANCE (um^2/s)",dance_value);
			rt.addValue("MAD (degrees)",mad_value);
			rt.addValue("Progress Motility",progressMotility_value);
		}
		return rt;
	}
	/******************************************************/
	/**
	 * @param nTracks - 
	 * @return 
	 */	
	public static ResultsTable calculateAverageMotility(int nTracks){
		
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		float vsl_mean = Params.total_vsl/nTracks;
		float vcl_mean = Params.total_vcl/nTracks;
		float vap_mean = Params.total_vap/nTracks;
		float lin_mean = Params.total_lin/nTracks;
		float wob_mean = Params.total_wob/nTracks;
		float str_mean = Params.total_str/nTracks;
		float alhMean_mean = Params.total_alhMean/nTracks;
		float alhMax_mean = Params.total_alhMax/nTracks;
		float bcf_mean = Params.total_bcf/nTracks;
		float dance_mean = Params.total_dance/nTracks;
		float mad_mean = Params.total_mad/nTracks;
		// % progressive Motile sperm
		float progressiveMotPercent = Params.countProgressiveSperm/(float)nTracks;			
		// % motility
		float motility_value = (float)Params.countMotileSperm/((float)(Params.countMotileSperm+Params.countNonMotileSperm));
		
		rt.addValue("VSL Mean (um/s)",vsl_mean);
		rt.addValue("VCL Mean (um/s)",vcl_mean);
		rt.addValue("VAP Mean (um/s)",vap_mean);
		rt.addValue("LIN Mean ",lin_mean);
		rt.addValue("WOB Mean ",wob_mean);
		rt.addValue("STR Mean ",str_mean);
		rt.addValue("ALH_Mean Mean (um)",alhMean_mean);
		rt.addValue("ALH_Max Mean (um)",alhMax_mean);
		rt.addValue("BCF Mean (Hz)",bcf_mean);
		rt.addValue("DANCE Mean (um^2/s)",dance_mean);
		rt.addValue("MAD Mean (degrees)",mad_mean);
		rt.addValue("Progressive Motility (%)",progressiveMotPercent*100);
		rt.addValue("Motility (%)",motility_value*100);
		
		return rt;
		
	}
}
