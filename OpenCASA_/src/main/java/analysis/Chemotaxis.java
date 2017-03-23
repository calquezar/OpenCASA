package analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Params;
import data.SList;
import data.Spermatozoon;
import data.Trial;
import gui.MainWindow;
import ij.IJ;
import ij.measure.ResultsTable;
import utils.Paint;

public class Chemotaxis {

//	private Map<String, Trial> trials = new HashMap<String, Trial>();
	
	public Chemotaxis() {}
	
	public int analysisSelectionDialog(Object[] options,String question,String title){
		int n = JOptionPane.showOptionDialog(null,
				question,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button title
		return n;
	}
	
	public void analyzeFile(){
		Trial trial = Analyzer.extractTrial("Chemotaxis-File");
		if(trial==null)
			return;
		//Draw trajectories
		float ratioQ = calculateRatioQ(trial.tracks);
		float ratioSL = calculateRatioSL(trial.tracks);
		Paint.drawChemotaxis(trial.tracks,ratioQ,ratioSL,trial.width,trial.height,trial.source);
	}
	public void bootstrapping(Map<String,Trial> trials){
		Set keys = trials.keySet();
		List controlKeys = getControlKeys(keys);
		SList controlTracks = mergeControlTracks(controlKeys,trials);
		double thControl = ORThreshold(controlTracks)
		
	}
	
	public SList mergeControlTracks(List controlKeys, Map<String, Trial> trials){
		
	  SList tracks = new SList();
	  for (Iterator k=controlKeys.iterator();k.hasNext();) {
		  String key= (String)k.next();
		  Trial trial = (Trial)trials.get(key);
		  tracks.addAll(trial.tracks);
	  }		
	  return tracks;
	}
	
	public List getControlKeys(Set keySet){
		List controlList = new ArrayList();
		for (Iterator k=keySet.iterator();k.hasNext();) {
			String id = (String)k.next();
			//Key is in format:
			//	for chemotaxis: YYYYMMDDQ[hormone+concentration][ID]
			//	for control: YYYYMMDDC[ID]
			char type = id.charAt(8);
			if(type=='C') //Control identifier
				controlList.add(id);
		}
		return controlList;
	}
	/******************************************************/
	/** Fuction to calculate the Ratio-Q
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return the Ratio-Q
	 */
	public float calculateRatioQ(List theTracks){
		
		float nPos=0; //Number of shifts in the chemoattractant direction
		float nNeg=0; //Number of shifts in other direction
		int trackNr = 0; //Number of tracks
		List angles = new ArrayList();
		int nTracks = theTracks.size();
		double angleDirection = (2*Math.PI + Params.angleDirection*Math.PI/180)%(2*Math.PI);
		double angleChemotaxis = (2*Math.PI + (Params.angleChemotaxis/2)*Math.PI/180)%(2*Math.PI);		
		float ratioQ = 0;
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			IJ.showProgress((double)trackNr/nTracks);
			IJ.showStatus("Calculating RatioQ...");
			trackNr++;
			List track=(ArrayList) iT.next();
			int nPoints = track.size();
			for (int j = 0; j < (nPoints-Params.decimationFactor); j++) {
				Spermatozoon oldSpermatozoon=(Spermatozoon)track.get(j);
				Spermatozoon newSpermatozoon = (Spermatozoon)track.get(j+Params.decimationFactor);
				float diffX = newSpermatozoon.x-oldSpermatozoon.x;
				float diffY = newSpermatozoon.y-oldSpermatozoon.y;
				double angle = (4*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI); //Absolute angle
				angle = (2*Math.PI+angle-angleDirection)%(2*Math.PI); //Relative angle between interval [0,2*Pi]
				if(angle>Math.PI) //expressing angle between interval [-Pi,Pi]
					angle = -(2*Math.PI-angle);			
				if(Math.abs(angle)<angleChemotaxis){
					nPos++;
				}
				else //if(Math.abs(angle)>(Math.PI-angleChemotaxis)){
					nNeg++;
				}
		}
		if((nPos+nNeg)>0)
			ratioQ = (nPos/(nPos+nNeg)); // (nPos+nNeg) = Total number of shifts
		else
			ratioQ=-1;
		return ratioQ;
	}
	
	/******************************************************/
	/**
	 * @param theTracks 2D-ArrayList that stores all the tracks
	 * @return RatioSL
	 */
	public float calculateRatioSL(List theTracks){
		
		float nPos=0; //Number of shifts in the chemoattractant direction
		float nNeg=0; //Number of shifts in other direction
		int trackNr=0;
		int nTracks = theTracks.size();
		double angleDirection = (2*Math.PI + Params.angleDirection*Math.PI/180)%(2*Math.PI);
		double angleChemotaxis = (2*Math.PI + (Params.angleChemotaxis/2)*Math.PI/180)%(2*Math.PI);		
		float ratioSL = 0;
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			IJ.showProgress((double)trackNr/nTracks);
			IJ.showStatus("Calculating RatioSL...");
			trackNr++;
			List aTrack=(ArrayList) iT.next();
			Spermatozoon first = (Spermatozoon)aTrack.get(1);
			Spermatozoon last = (Spermatozoon)aTrack.get(aTrack.size() - 1);
			float diffX = last.x-first.x;
			float diffY = last.y-first.y;
			double angle = (4*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI); //Absolute angle
			angle = (2*Math.PI+angle-angleDirection)%(2*Math.PI); //Relative angle between interval [0,2*Pi]
			if(angle>Math.PI) //expressing angle between interval [-Pi,Pi]
				angle = -(2*Math.PI-angle);			
			if(Math.abs(angle)<angleChemotaxis)
				nPos++;
			else
				nNeg++;			
		}
		if((nPos+nNeg)>0)
			ratioSL = (nPos/(nPos+nNeg));
		else
			ratioSL=-1;
		return ratioSL;
	}
	
	public void ratioQ(Map<String,Trial> trials){
		
		if(trials==null)
			return;
		Set keySet = trials.keySet();	
		getControlKeys(keySet);
//		ResultsTable rtRatios = new ResultsTable();
//		for (Iterator k=keySet.iterator();k.hasNext();) {
//			String key= (String)k.next();
//			Trial trial = (Trial)trials.get(key);
//		  	System.out.println("key: "+key);
//		  	float ratioQ = calculateRatioQ(trial.tracks);
//		  	float ratioSL = calculateRatioSL(trial.tracks);
//		  	setQResults(rtRatios,trial.source,ratioQ,ratioSL,trial.tracks.size());
//		}
//		rtRatios.show("Chemotaxis results");
	}
	
	public void run(MainWindow mw) throws IOException, ClassNotFoundException{
		mw.setVisible(false);
		//Reset Parameters
		Params.resetParams();
		//Ask if user wants to analyze a file or directory
		Object[] options = {"File", "Directory"};
		String question = "What do you want to analyze?";
		String title = "Choose one analysis...";
		int userSelection1 = analysisSelectionDialog(options,question,title);
		if( userSelection1<0){
			mw.setVisible(true);
			return;
		}
		else if( userSelection1==0){//File
			analyzeFile();
		}else if( userSelection1==1){//Directory
			//Ask user which analysis wants to apply
			Object[] options2 = {"RatioQ", "Bootstrapping"};
			question = "Which analysis do you want to apply to the data?";
			title = "Choose one analysis...";
			int  userSelection2 = analysisSelectionDialog(options2,question,title);
			if(userSelection2<0){
				mw.setVisible(true);
				return;	
			}
			//Create trials dictionary
			Map<String,Trial> trials = Analyzer.extractTrials("Chemotaxis-Directory");
			if(userSelection2==0)
				ratioQ(trials);
			else if(userSelection2==1)
				bootstrapping(trials);			
		}
		mw.setVisible(true);
	}
	
	public void setQResults(ResultsTable rt,String filename,float ratioQ, float ratioSL, int nTracks){
		
//		System.out.println("filename: "+filename);
		String[] parts = filename.split("-");//it's necessary to remove the '.avi' extension
//		System.out.println("parts[0]: "+parts[0]);
//		parts = parts[0].split("-");//Format 2000-11-19-1234-Q-P-100pM-0-1
		
		rt.incrementCounter();	
		rt.addValue("nTracks",nTracks);
		rt.addValue("RatioQ",ratioQ);
		rt.addValue("RatioSL",ratioSL);		
		rt.addValue("Type",parts[4]);
		if(parts[4].equals("Q")){
			rt.addValue("Hormone",parts[5]);
			rt.addValue("Concentration",parts[6]);
		}else{
			rt.addValue("Hormone","-");
			rt.addValue("Concentration","-");
		}
		rt.addValue("Direction (Degrees)",Params.angleDirection);
		rt.addValue("ArcChemotaxis (Degrees)",Params.angleChemotaxis);
		rt.addValue("ID",parts[3]);
		rt.addValue("Date",parts[0]+"-"+parts[1]+"-"+parts[2]);
		rt.addValue("Filename",filename);
	}
	
	public static double ORThreshold(SList controlTracks){		
	
		List<Double> ORs = new ArrayList<Double>();
		final int NUMSAMPLES = 10000;
		
		for(int i=0;i<NUMSAMPLES;i++){
			double[] numeratorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
			double[] denominatorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
			
			System.out.println("Calculating Control Threshold. Shuffle "+i);
			System.out.println("MAX INSTANT ANGLES: "+Params.MAXINSTANGLES);
			
			java.util.Collections.shuffle(controlTracks);
			//Calculate numerator's odds value
			int count=0,index=0;
			while((count<Params.MAXINSTANGLES)&&(index<controlTracks.size())){
				int[] countInstDirections = countInstantDirections((List)controlTracks.get(index));
				count+=countInstDirections[0]+countInstDirections[1];
				numeratorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
				numeratorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]);			        
				index++;
			}
			java.util.Collections.shuffle(controlTracks);			
			//Calculate denominator's odds value
			count=0;index=0;
			while((count<Params.MAXINSTANGLES)&&(index<controlTracks.size())){
				int[] countInstDirections = countInstantDirections((List)controlTracks.get(index));
				denominatorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
				denominatorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]); //number of instantaneous angles in the opposite direction			        
				count+=countInstDirections[0]+countInstDirections[1];
				index++;
			}
			double numeratorRatio = numeratorValues[0]/numeratorValues[1];
			double denominatorRatio = denominatorValues[0]/denominatorValues[1];
			double OddsRatio = numeratorRatio/denominatorRatio;
			ORs.add(OddsRatio);
			IJ.log(""+OddsRatio);
//			System.out.println("OddsRatio: "+OddsRatio);
		}
		
		Collections.sort(ORs);
	//	System.out.println(ORs.toString());
	//	System.out.println("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
		System.out.println("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
		System.out.println("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
		System.out.println("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
		System.out.println("p97: "+ORs.get((int) (NUMSAMPLES*0.97)));
		System.out.println("p99: "+ORs.get((int) (NUMSAMPLES*0.99)));
	//	IJ.log("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
	//	IJ.log("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
	//	IJ.log("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
	//	IJ.log("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
		return ORs.get((int) (NUMSAMPLES*0.95));
	}
	
	public static int[] countInstantDirections(List track){
		int nPos = 0;
		int nNeg = 0;
		double angleDirection = (2*Math.PI + Params.angleDirection*Math.PI/180)%(2*Math.PI);
		double angleChemotaxis = (2*Math.PI + (Params.angleChemotaxis/2)*Math.PI/180)%(2*Math.PI);
		int nPoints = track.size();
		for (int j = 0; j < (nPoints-Params.decimationFactor); j++) {
			Spermatozoon oldSpermatozoon=(Spermatozoon)track.get(j);
			Spermatozoon newSpermatozoon = (Spermatozoon)track.get(j+Params.decimationFactor);
			float diffX = newSpermatozoon.x-oldSpermatozoon.x;
			float diffY = newSpermatozoon.y-oldSpermatozoon.y;
			double angle = (4*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI); //Absolute angle
			angle = (2*Math.PI+angle-angleDirection)%(2*Math.PI); //Relative angle between interval [0,2*Pi]
			if(angle>Math.PI) //expressing angle between interval [-Pi,Pi]
				angle = -(2*Math.PI-angle);			
			if(Math.abs(angle)<angleChemotaxis){
				nPos++;
	//			System.out.println("AngleDirection: "+angleDirection*180/Math.PI+"; AngleChemotaxis: "+angleChemotaxis*180/Math.PI+"; Positive: "+angle*180/Math.PI);
			}
			else if(Math.abs(angle)>(Math.PI-angleChemotaxis)){
				nNeg++;
	//			System.out.println("AngleDirection: "+angleDirection*180/Math.PI+"; AngleChemotaxis: "+angleChemotaxis*180/Math.PI+"; Negative: "+angle*180/Math.PI);
			}
		}
		int[] results = new int[3];
		results[0] = nPos;
		results[1] = nNeg;
		return results;
	}
}
