package analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Params;
import data.Spermatozoon;
import data.Trial;
import gui.MainWindow;
import ij.IJ;

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
	
	public void bootstrapping(Map<String,Trial> trials){
		
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
		for (Iterator k=keySet.iterator();k.hasNext();) {
			String key= (String)k.next();
			Trial trial = (Trial)trials.get(key);
		  	System.out.println("key: "+key);
		  	float ratioQ = calculateRatioQ(trial.tracks);
		  	float ratioSL = calculateRatioSL(trial.tracks);
		  	setQResults(trial.source,ratioQ,ratioSL,trial.tracks.size());
		}
		Params.rTable.show("Results");
	}
	
	public void run(MainWindow mw) throws IOException, ClassNotFoundException{
		mw.setVisible(false);
		//Reset Parameters
		Params.resetParams();
		//Ask if user wants to analyze a file or directory
		Object[] options = {"File", "Directory"};
		String question = "What do you want to analyze?";
		String title = "Choose one analysis...";
		int sel1 = analysisSelectionDialog(options,question,title);
		if(sel1<0)
			return;
		else if(sel1==0){//File
			
		}else if(sel1==1){//Directory
			//Ask user which analysis wants to apply
			Object[] options2 = {"RatioQ", "Bootstrapping"};
			question = "Which analysis do you want to apply to the data?";
			title = "Choose one analysis";
			int sel2 = analysisSelectionDialog(options2,question,title);
			if(sel2<0){
				mw.setVisible(true);
				return;			
			}
			//Create trials dictionary
			Map<String,Trial> trials = CommonAnalysis.generateTrials("Chemotaxis");
			if(sel2==0)
				ratioQ(trials);
			else if(sel2==1)
				bootstrapping(trials);			
		}
		mw.setVisible(true);
	}
	
	public void setQResults(String filename,float ratioQ, float ratioSL, int nTracks){
		
//		System.out.println("filename: "+filename);
		String[] parts = filename.split("-");//it's necessary to remove the '.avi' extension
//		System.out.println("parts[0]: "+parts[0]);
//		parts = parts[0].split("-");//Format 2000-11-19-1234-Q-P-100pM-0-1
		
		Params.rTable.incrementCounter();	
		Params.rTable.addValue("nTracks",nTracks);
		Params.rTable.addValue("RatioQ",ratioQ);
		Params.rTable.addValue("RatioSL",ratioSL);		
		Params.rTable.addValue("Type",parts[4]);
		if(parts[4].equals("Q")){
			Params.rTable.addValue("Hormone",parts[5]);
			Params.rTable.addValue("Concentration",parts[6]);
		}else{
			Params.rTable.addValue("Hormone","-");
			Params.rTable.addValue("Concentration","-");
		}
		Params.rTable.addValue("Direction (Degrees)",Params.angleDirection);
		Params.rTable.addValue("ArcChemotaxis (Degrees)",Params.angleChemotaxis);
		Params.rTable.addValue("ID",parts[3]);
		Params.rTable.addValue("Date",parts[0]+"-"+parts[1]+"-"+parts[2]);
		Params.rTable.addValue("Filename",filename);
	}	
}
