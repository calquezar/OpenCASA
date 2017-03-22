package analysis;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Params;
import data.Trial;
import gui.MainWindow;
import ij.measure.ResultsTable;
import utils.SignalProcessing;
import utils.Paint;

public class Motility {

	//Motility variables
	private float total_vsl = 0;
	private float total_vcl = 0;
	private float total_vap = 0;
	private float total_lin = 0;
	private float total_wob = 0;
	private float total_str = 0;
	private float total_alhMean = 0;
	private float total_alhMax = 0;
	private float total_bcf = 0;
	private float total_dance = 0;
	private float total_mad = 0;
	private float countProgressiveSperm = 0;
	private int countMotileSperm = 0;
	private int countNonMotileSperm = 0;
	
	public Motility() {}

	public void resetParams(){
	    total_vsl = 0;
		total_vcl = 0;
		total_vap = 0;
		total_lin = 0;
		total_wob = 0;
		total_str = 0;
		total_alhMean = 0;
		total_alhMax = 0;
		total_bcf = 0;
		total_dance = 0;
		total_mad = 0;
		countProgressiveSperm = 0;
		countMotileSperm = 0;
		countNonMotileSperm = 0;
	}
	public int analysisSelectionDialog(){
		Object[] options = {"File", "Directory"};
		int n = JOptionPane.showOptionDialog(null,
				"What do you want to analyze?",
				"Choose one analysis...",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button title
		return n;
	}
	public void analyzeDirectory(){
		//Create trials dictionary
		Map<String,Trial> trials = CommonAnalysis.extractTrials("Motility");
		if(trials==null)
			return;
		Set keySet = trials.keySet();
		ResultsTable rtIndividual = new ResultsTable();
		ResultsTable rtAverage = new ResultsTable();
		for (Iterator k=keySet.iterator();k.hasNext();) {
			String key= (String)k.next();
			Trial trial = (Trial)trials.get(key);
			// Motility results
			calculateMotility(rtIndividual,trial.tracks,trial.source);
			calculateAverageMotility(rtAverage,trial.tracks.size(),trial.source);
		}
		rtIndividual.show("Individual Motility");
		rtAverage.show("Average Motility");
	}
	
	public void analyzeFile(){
		Trial trial = CommonAnalysis.extractTrial("Motility");
		if(trial==null)
			return;
		ResultsTable rtIndividual = new ResultsTable();
		calculateMotility(rtIndividual,trial.tracks,trial.source);
		ResultsTable rtAverage = new ResultsTable();
		calculateAverageMotility(rtAverage,trial.tracks.size(),trial.source);
		//Draw trajectories
		trial.imp.show();
		Paint.draw(trial.imp, trial.tracks);
		rtIndividual.show("Individual Motility");
		rtAverage.show("Average Motility");
//		Params.rTable.show("Results");
	}
	
	/******************************************************/
	/**
	 * @param nTracks - 
	 * @return 
	 */	
	public void calculateAverageMotility(ResultsTable rt,int nTracks,String sampleID){
		
		rt.incrementCounter();
		float vsl_mean = total_vsl/nTracks;
		float vcl_mean = total_vcl/nTracks;
		float vap_mean = total_vap/nTracks;
		float lin_mean = total_lin/nTracks;
		float wob_mean = total_wob/nTracks;
		float str_mean = total_str/nTracks;
		float alhMean_mean = total_alhMean/nTracks;
		float alhMax_mean = total_alhMax/nTracks;
		float bcf_mean = total_bcf/nTracks;
		float dance_mean = total_dance/nTracks;
		float mad_mean = total_mad/nTracks;
		// % progressive Motile sperm
		float progressiveMotPercent = countProgressiveSperm/(float)nTracks;			
		// % motility
		float motility_value = (float)countMotileSperm/((float)(countMotileSperm+countNonMotileSperm));
		
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
		rt.addValue("SampleID", sampleID);
	}
	
	/******************************************************/
	/**
	 * @param theTracks 2d-Array with all tracks
	 * @return 
	 */	
	public void calculateMotility(ResultsTable rt,List theTracks,String sampleID){
		
		//Calculate values for each track
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			List aTrack=(List)iT.next();
			List avgTrack = SignalProcessing.movingAverage(aTrack);
			float length = (float)aTrack.size();
			// VSL
			float vsl_value = Kinematics.vsl(aTrack);
			total_vsl+=vsl_value;
			// VCL
			float vcl_value =  Kinematics.vcl(aTrack);
			total_vcl+=vcl_value;
			// VAP is equivalent to calculate vcl from averaged track
			float vap_value =  Kinematics.vcl(avgTrack);
			total_vap+=vap_value;
			// Linearity
			float lin_value = (vsl_value/vcl_value)*100;
			total_lin+=lin_value;
			// Wobble
			float wob_value = (vap_value/vcl_value)*100;
			total_wob+=wob_value;
			// Straightness
			float str_value = (vsl_value/vap_value)*100;
			total_str+=str_value;
			// Amplitude of lateral head
			float alh_values[] =  Kinematics.alh(aTrack,avgTrack);
			total_alhMean+=alh_values[0];
			total_alhMax+=alh_values[1];
			// Beat-cross frequency
			float bcf_value =  Kinematics.bcf(aTrack,avgTrack);
			total_bcf+=bcf_value;
			//Progressive motility
			String progressMotility_value = "NO";
			if(str_value> Params.progressMotility){
				progressMotility_value = "YES";	
				countProgressiveSperm++;
			}	
			// DANCE
			float dance_value =  vcl_value*alh_values[0];
			total_dance+=dance_value;
			//MAD
			float mad_value = Kinematics.mad(aTrack);
			total_mad+=mad_value;

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
			rt.addValue("sampleID", sampleID);
		}
	}
	public void run(MainWindow mw){
		mw.setVisible(false);
		//Reset Parameters
		Params.resetParams();
		//Ask user which analysis wants to apply
		int userSelection = analysisSelectionDialog();
		if(userSelection<0){
			mw.setVisible(true);
			return;			
		}
		if(userSelection==0)
			analyzeFile();
		else if(userSelection==1)
			analyzeDirectory();
		mw.setVisible(true);
	}
}
