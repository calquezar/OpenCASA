package analysis;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Params;
import data.Trial;
import gui.MainWindow;
import ij.IJ;
import ij.measure.ResultsTable;
import utils.SignalProcessing;
import utils.Utils;
import utils.Paint;

public class Motility {

	//Motility variables
	private float total_sperm = 0;
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
	private float total_motile = 0;
	private float total_nonMotile = 0;
	private float countProgressiveSperm = 0;
	
	public Motility() {}

	public void resetParams(){
		total_sperm = 0;
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
		total_motile = 0;
		total_nonMotile = 0;
		countProgressiveSperm = 0;
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
		
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0)
			return;
		String analysis = "Motility-Directory";
		ResultsTable rtIndividual = new ResultsTable();
		ResultsTable rtAverage = new ResultsTable();
		ResultsTable rtTotal = new ResultsTable();		
		for (int i = 0; i < listOfFiles.length; i++) {
			Map<String,Trial> trials = null;
			String absoluteFilePath = listOfFiles[i];
			trials = VideoAnalyzer.extractTrials(absoluteFilePath,analysis);
			if(trials==null)
				return;
			Set keySet = trials.keySet();
			for (Iterator k=keySet.iterator();k.hasNext();) {
				String key= (String)k.next();
				Trial trial = (Trial)trials.get(key);
				// Motility results
				calculateMotility(rtIndividual,trial);
				calculateAverageMotility(rtAverage,trial);
//				resetParams();
			}
			calculateTotalMotility(rtTotal,absoluteFilePath);
			resetParams();
		}
//		rtAverage.show("Average Motility");
		rtTotal.show("Total Motility");
	}
	
	public void analyzeFile(){
		Trial trial = VideoAnalyzer.extractTrial("Motility-File");
		if(trial==null)
			return;
		ResultsTable rtIndividual = new ResultsTable();
		calculateMotility(rtIndividual,trial);
		ResultsTable rtAverage = new ResultsTable();
		calculateAverageMotility(rtAverage,trial);
		//Draw trajectories
		trial.imp.show();
		Paint.draw(trial.imp, trial.tracks);
		rtIndividual.show("Individual Motility");
		rtAverage.show("Average Motility");
//		Params.rTable.show("Results");
	}
	
	/******************************************************/
	/**
	 * @param  
	 * @return 
	 */	
	public void calculateTotalMotility(ResultsTable rt,String filename){
		
		
		float vsl_mean = total_vsl/total_sperm;
		float vcl_mean = total_vcl/total_sperm;
		float vap_mean = total_vap/total_sperm;
		float lin_mean = total_lin/total_sperm;
		float wob_mean = total_wob/total_sperm;
		float str_mean = total_str/total_sperm;
		float alhMean_mean = total_alhMean/total_sperm;
		float alhMax_mean = total_alhMax/total_sperm;
		float bcf_mean = total_bcf/total_sperm;
		float dance_mean = total_dance/total_sperm;
		float mad_mean = total_mad/total_sperm;
		// % progressive Motile sperm
		float progressiveMotPercent = countProgressiveSperm/(float)total_sperm;			
		// % motility
		float motility_value = (float)total_motile/((float)(total_motile+total_nonMotile));
		
		rt.incrementCounter();
		rt.addValue("Motile trajectories",total_sperm);
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
		rt.addValue("Filename",filename);
	}	
	/******************************************************/
	/**
	 * @param nTracks - 
	 * @return 
	 */	
	public void calculateAverageMotility(ResultsTable rt,Trial trial){
		
		
		float nTracks = trial.tracks.size();		
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
		int countMotileSperm = trial.motileSperm[0];
		total_motile += countMotileSperm;
		int countNonMotileSperm = trial.motileSperm[1];
		total_nonMotile += countNonMotileSperm;
		float motility_value = (float)countMotileSperm/((float)(countMotileSperm+countNonMotileSperm));
		total_sperm+=nTracks;
		
		rt.incrementCounter();
		rt.addValue("Motile trajectories",nTracks);
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
		rt.addValue("SampleID", trial.source);
	}
	
	/******************************************************/
	/**
	 * @param theTracks 2d-Array with all tracks
	 * @return 
	 */	
	public void calculateMotility(ResultsTable rt,Trial trial){
		
		//Calculate values for each track
		for (ListIterator iT=trial.tracks.listIterator(); iT.hasNext();) {
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
			float dance_value =  vcl_value*alh_values[0];//vcl*alh_mean
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
			rt.addValue("sampleID", trial.source);
		}
	}
	public void run(MainWindow mw){
		mw.setVisible(false);
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
