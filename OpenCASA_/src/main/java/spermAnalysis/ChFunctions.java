package spermAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import data.Params;
import data.SList;
import data.Spermatozoon;
import data.Trial;
import ij.IJ;
import ij.measure.ResultsTable;
import utils.TrackFilters;

public class ChFunctions {

	/******************************************************/
	/** Fuction to calculate the Ratio-Q
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return the Ratio-Q
	 */
	public static float calculateRatioQ(List theTracks){
		
		float trackNr=0; //Used to count total number of tracks
		float tracksQ=0; //Number of tracks that satisfy condition Q
		float tracksNQ=1; //Number of tracks that not satisfy condition Q
		List angles = new ArrayList();
		float ratioQ = 0;
		int nTracks = theTracks.size();
		
		//This operation is util when half of the chemotaxis' cone amplitude plus angle direction is greater than 360 degrees (270+100 for example)
		float upperAngle = (float)(Params.angleDirection + Params.angleChemotaxis/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		// This operation is util when half of the chemotaxis' cone amplitude is greater than angle direction
		float lowerAngle = (float)(Params.angleDirection - Params.angleChemotaxis/2 + 360)%360;
		lowerAngle = lowerAngle*(float)Math.PI/180; //convert to radians
		
		//float upperAngleNegativeDirection = ((float)Math.PI-upperAngle)%360;
		//float lowerAngleNegativeDirection = ((float)Math.PI+lowerAngle)%360;
		
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			trackNr++;
			IJ.showProgress((double)trackNr/nTracks);
			IJ.showStatus("Calculating RatioQ...");
			
			List track=(ArrayList) iT.next();
			int nPoints = track.size();
			for (int j = 0; j < (nPoints-Params.decimationFactor); j++) {
				Spermatozoon oldSpermatozoon=(Spermatozoon)track.get(j);
				Spermatozoon newSpermatozoon = (Spermatozoon)track.get(j+Params.decimationFactor);
				float diffX = newSpermatozoon.x-oldSpermatozoon.x;
				float diffY = newSpermatozoon.y-oldSpermatozoon.y;
				double angle = (4*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI);
				angles.add(angle);
				//IJ.log("angle: "+angle*180/Math.PI);
				if(lowerAngle>upperAngle){
					//Special case: for example, when chemotaxis cone is between first and fourth quadrant
					if((angle<upperAngle)||(lowerAngle<angle))
						tracksQ++;
					else
						tracksNQ++;
				}
				else{ 
					if((angle<upperAngle)&&(lowerAngle<angle))
						tracksQ++;
					 else //if((angle>(upperAngle+120*Math.PI/180))&&(angle<(lowerAngle-120120*Math.PI/180)))
						tracksNQ++;
				}
				
			}
		}
		
		ratioQ = (tracksQ/(tracksQ+tracksNQ)); // (tracksQ+tracksNQ) = Total number of shifts
		return ratioQ;
	}
	

	/******************************************************/
	/** Fuction to calculate the Ratio-Q
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return the Ratio-Q
	 */
/*	public float calculateRatioQ(List theTracks){
		
		float trackNr=0; //Used to count total number of tracks
		float tracksQ=0; //Number of tracks that satisfy condition Q
		float tracksNQ=1; //Number of tracks that not satisfy condition Q
		List angles = new ArrayList();
		float ratioQ = 0;
		int nTracks = theTracks.size();
		
		//This operation is util when half of the chemotaxis' cone amplitude plus angle direction is greater than 360 degrees (270+100 for example)
		float upperAngle = (float)(angleDirection + angleChemotaxis/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		// This operation is util when half of the chemotaxis' cone amplitude is greater than angle direction
		float lowerAngle = (float)(angleDirection - angleChemotaxis/2 + 360)%360;
		lowerAngle = lowerAngle*(float)Math.PI/180; //convert to radians
		
		//float upperAngleNegativeDirection = ((float)Math.PI-upperAngle)%360;
		//float lowerAngleNegativeDirection = ((float)Math.PI+lowerAngle)%360;
		
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			trackNr++;
			IJ.showProgress((double)trackNr/nTracks);
			IJ.showStatus("Calculating RatioQ...");
			List bTrack=(ArrayList) iT.next();
			ListIterator jT=bTrack.listIterator();
			Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
			Spermatozoon newSpermatozoon = (Spermatozoon) jT.next();
			float diffX = newSpermatozoon.x-oldSpermatozoon.x;
			float diffY = newSpermatozoon.y-oldSpermatozoon.y;
			double angle = (2*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI);
			angles.add(angle);
			IJ.log("angle: "+angle*180/Math.PI);
			//IJ.log("diffX: "+diffX+"; diffY: "+diffY+"; angle: "+angle*180/Math.PI);
			if(lowerAngle>upperAngle){
				//Special case: for example, when chemotaxis cone is between first and fourth quadrant
				if((angle<upperAngle)||(lowerAngle<angle))
					tracksQ++;
				else
					tracksNQ++;
			}
			else{ 
				if((angle<upperAngle)&&(lowerAngle<angle))
					tracksQ++;
				 else //if((angle>(upperAngle+120*Math.PI/180))&&(angle<(lowerAngle-120120*Math.PI/180)))
					tracksNQ++;
			}
			
			// if(lowerAngleNegativeDirection>upperAngleNegativeDirection){
				// if((angle<upperAngleNegativeDirection)||(angle>lowerAngleNegativeDirection))
					// tracksNQ++;
			// }else{
				
				// if((angle<upperAngleNegativeDirection)&&(angle>lowerAngleNegativeDirection))
					// tracksNQ++;
			// }
				
			
			//For each instant (Spermatozoon) in the track
			for (;jT.hasNext();){ 
				oldSpermatozoon = newSpermatozoon;
				newSpermatozoon = (Spermatozoon) jT.next();
				diffX = newSpermatozoon.x-oldSpermatozoon.x;
				diffY = newSpermatozoon.y-oldSpermatozoon.y;
				angle = (2*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI);
				angles.add(angle);
				if(lowerAngle>upperAngle){
					//Special case: for example, when chemotaxis cone is between first and fourth quadrant
					if((angle<upperAngle)||(lowerAngle<angle))
						tracksQ++;
					else
						tracksNQ++;
				}
				else{ if((angle<upperAngle)&&(lowerAngle<angle))
						tracksQ++;
					 else
						tracksNQ++;
				}
				// if(lowerAngleNegativeDirection>upperAngleNegativeDirection){
					// if((angle<upperAngleNegativeDirection)||(angle>lowerAngleNegativeDirection))
						// tracksNQ++;
				// }else{
					
					// if((angle<upperAngleNegativeDirection)&&(angle>lowerAngleNegativeDirection))
						// tracksNQ++;
				// }
			}
		}
		
		//if(tracksNQ>0)
		//	ratioQ = (tracksQ/tracksNQ);
		ratioQ = (tracksQ/(tracksQ+tracksNQ)); // (tracksQ+tracksNQ) = Total number of shifts
		return ratioQ;
	}
*/
	/******************************************************/
	/**
	 * @param theTracks 2D-ArrayList that stores all the tracks
	 * @return RatioSL
	 */
	public static float calculateRatioSL(List theTracks){
		
		float tracksQ=0; //Number of tracks that satisfy condition Q
		float tracksNQ=0; //Number of tracks that not satisfy condition Q
		float ratioSL = 0;
		int tracksCount=0;
		
		//This operation is util when half of the chemotaxis' cone amplitude plus angle direction is greater than 360 degrees (270+100 for example)
		float upperAngle = (float)(Params.angleDirection + Params.angleChemotaxis/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		// This operation is util when half of the chemotaxis' cone amplitude is greater than angle direction
		float lowerAngle = (float)(Params.angleDirection - Params.angleChemotaxis/2 + 360)%360;
		lowerAngle = lowerAngle*(float)Math.PI/180; //convert to radians
		
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			tracksCount++;
			List aTrack=(ArrayList) iT.next();
			Spermatozoon first = (Spermatozoon)aTrack.get(1);
			Spermatozoon last = (Spermatozoon)aTrack.get(aTrack.size() - 1);
			float diffX = last.x-first.x;
			float diffY = last.y-first.y;
			double angle = (2*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI);
			if(lowerAngle>upperAngle){
				//Special case: for example, when chemotaxis cone is between first and fourth quadrant
				if((angle<upperAngle)||(lowerAngle<angle))
					tracksQ++;
				else
					tracksNQ++;
			}
			else{ if((angle<upperAngle)&&(lowerAngle<angle))
					tracksQ++;
				 else
					tracksNQ++;
			}
		}
		ratioSL = (tracksQ/tracksCount);
		return ratioSL;

	}
	
	/******************************************************/
	/**
	 * @param String filename
	 * @return int - 0-Control; 1-chemotaxis(10pM);2-chemotaxis(100pM);3-chemotaxis(10nM)
	 */	
	public static int getTrialType(String filename){
		//Format YYYY-MM-DD-ID-C-numVideo-Medium (for control)
		//Format YYYY-MM-DD-ID-Q-Hormone-Concentration-numVideo-Medium (with hormone)
		String[] parts = filename.split("-");
		if(parts[4].equals("Q")){
			if(parts[6].equals("10pM"))
				return 1;
			else if(parts[6].equals("100pM"))
				return 2;
			else if(parts[6].equals("10nM"))
				return 3;
			else return -1;
		}else{
			return 0;
		}
		
	}
	
	public static String getID(String filename){
		//Format YYYY-MM-DD-ID-C-numVideo-Medium (for control)
		//Format YYYY-MM-DD-ID-Q-Hormone-Concentration-numVideo-Medium (with hormone)
		String[] parts = filename.split("-");
		return parts[3]+parts[0]+parts[1]+parts[2];
	}
	
	public static void setQtResults(String filename,float ratioQ, float ratioSL, int nTracks){
		
		//Format 2000-11-19-1234-Q-P-100pM-0-1
		String[] parts = filename.split("-");

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
//				System.out.println("AngleDirection: "+angleDirection*180/Math.PI+"; AngleChemotaxis: "+angleChemotaxis*180/Math.PI+"; Positive: "+angle*180/Math.PI);
			}
			else if(Math.abs(angle)>(Math.PI-angleChemotaxis)){
				nNeg++;
//				System.out.println("AngleDirection: "+angleDirection*180/Math.PI+"; AngleChemotaxis: "+angleChemotaxis*180/Math.PI+"; Negative: "+angle*180/Math.PI);
			}
		}
		int[] results = new int[3];
		results[0] = nPos;
		results[1] = nNeg;
		return results;
	}
	
	public static double calculateORControlThreshold(Map<String, Trial> trials){		
		
		List<Double> ORs = new ArrayList<Double>();
		final int MAXINSTANGLES = 20000;//Params.controlTracks.size();
		final int NUMSAMPLES = 1000;
		
//		Set keySet = trials.keySet();
//		List keys = new ArrayList();
//		keys.addAll(keySet);
//		
//		List cNumerator = keys.subList(0, keys.size()/2);
//		List cDenominator = keys.subList(keys.size()/2+1, keys.size()-1);
		
		for(int i=0;i<NUMSAMPLES;i++){
			
			double[] numeratorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
			double[] denominatorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
			
			System.out.println("Calculating Control Threshold. Shuffle "+i);
//			System.out.println("Params.controlTracks.size(): "+Params.controlTracks.size());
//			System.out.println("Size of ORs: "+ORs.size());
			
//			java.util.Collections.shuffle(cNumerator);
//			String k0 = (String) cNumerator.get(0);
//			Trial t = (Trial)trials.get(k0);
//			SList controlTracks = t.control;
			
			java.util.Collections.shuffle(Params.controlTracks);
			//Calculate numerator's odds value
			int count=0,index=0;
			while((count<MAXINSTANGLES)&&(index<Params.controlTracks.size())){
				int[] countInstDirections = countInstantDirections((List)Params.controlTracks.get(index));
				count+=countInstDirections[0]+countInstDirections[1];
				numeratorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
				numeratorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]);			        
				index++;
//				System.out.println("count Angles: "+count);
//				System.out.println("index: "+index);
			}
//			System.out.println("numAngles Numerator: "+count);
			
//			java.util.Collections.shuffle(cDenominator);
//			String k1 = (String) cDenominator.get(0);
//			t = (Trial)trials.get(k1);
//			controlTracks = t.control;
			
			java.util.Collections.shuffle(Params.controlTracks);			
			//Calculate denominator's odds value
			count=0;index=0;
			while((count<MAXINSTANGLES)&&(index<Params.controlTracks.size())){
				int[] countInstDirections = countInstantDirections((List)Params.controlTracks.get(index));
				denominatorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
				denominatorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]); //number of instantaneous angles in the opposite direction			        
				count+=countInstDirections[0]+countInstDirections[1];
				index++;
			}
//			System.out.println("numAngles Denominator: "+count);
//			System.out.println("Numerator Positive: "+numeratorValues[0] +"; Denominator Positive : "+ denominatorValues[0]+"; Numerator Negative: "+numeratorValues[1] +"; Denominator Negative : "+ denominatorValues[1]);
			double numeratorRatio = numeratorValues[0]/numeratorValues[1];
			double denominatorRatio = denominatorValues[0]/denominatorValues[1];
			double OddsRatio = numeratorRatio/denominatorRatio;
			ORs.add(OddsRatio);
			IJ.log(""+OddsRatio);
//			System.out.println("OddsRatio: "+OddsRatio);
		}
		
		Collections.sort(ORs);
//		System.out.println(ORs.toString());
//		System.out.println("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
		System.out.println("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
		System.out.println("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
		System.out.println("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
		System.out.println("p97: "+ORs.get((int) (NUMSAMPLES*0.97)));
		System.out.println("p99: "+ORs.get((int) (NUMSAMPLES*0.99)));
//		IJ.log("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
//		IJ.log("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
//		IJ.log("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
//		IJ.log("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
		return ORs.get((int) (NUMSAMPLES*0.95));
	}
	
	public static double OR(Trial trial,String condition){
		
		SList controlTracks = trial.control;
		SList conditionTracks = new SList();
		if(condition.equals("p10pM"))
			conditionTracks = trial.p10pM;
		else if(condition.equals("p100pM"))
			conditionTracks = trial.p100pM;
		else if(condition.equals("p10nM"))
			conditionTracks = trial.p10nM;
		
//		java.util.Collections.shuffle(controlTracks);
		
		double[] numeratorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
		double[] denominatorValues = new double[]{0.0,0.0}; //[0] - positive directions; [1] - negative directions
		final int MAXINSTANGLES = 20000;		
//		int MAXINSTANGLES = Math.min(controlTracks.size(),conditionTracks.size());
		
//		System.out.println("controlTracks.size(): "+controlTracks.size());
		int count=0,index=0;
		//Control Ratio
		while((count<MAXINSTANGLES)&&(index<controlTracks.size())){
//		while(index<controlTracks.size()){
			int[] countInstDirections = countInstantDirections((List)controlTracks.get(index));
			denominatorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
			denominatorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]); //number of instantaneous angles in the opposite direction			        
			count+=countInstDirections[0]+countInstDirections[1];
			index++;
		}
	
		System.out.println("Count denominator angles: "+denominatorValues[1]);
//		System.out.println("conditionTracks.size(): "+conditionTracks.size());

//		java.util.Collections.shuffle(conditionTracks);

		//Condition Ratio
		count=0;index=0;
		while((count<MAXINSTANGLES)&&(index<conditionTracks.size())){
//		while(index<conditionTracks.size()){
			int[] countInstDirections = countInstantDirections((List)conditionTracks.get(index));
			numeratorValues[0]+=(double)countInstDirections[0]; //number of instantaneous angles in the positive direction
			numeratorValues[1]+=(double)(countInstDirections[0]+countInstDirections[1]); //number of instantaneous angles in the opposite direction			        
			count+=countInstDirections[0]+countInstDirections[1];
			index++;
		}
		System.out.println("Count numerator angles: "+numeratorValues[1]);

		double numeratorRatio = numeratorValues[0]/numeratorValues[1];
		double denominatorRatio = denominatorValues[0]/denominatorValues[1];
		double OddsRatio = numeratorRatio/denominatorRatio;
		
//		System.out.println("OR: "+OddsRatio+" ;nAngles: "+(numeratorValues[0]+numeratorValues[1]));
		return OddsRatio;
	}
	
}
