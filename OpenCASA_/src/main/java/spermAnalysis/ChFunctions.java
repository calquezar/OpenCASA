package spermAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.Spermatozoon;
import ij.IJ;
import ij.measure.ResultsTable;

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
		//Format 2000-11-19-1234-Q-P-100pM-0-1
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
		
		//This operation is util when half of the chemotaxis' cone amplitude plus angle direction is greater than 360 degrees (270+100 for example)
		float upperAngle = (float)(Params.angleDirection + Params.angleChemotaxis/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		// This operation is util when half of the chemotaxis' cone amplitude is greater than angle direction
		float lowerAngle = (float)(Params.angleDirection - Params.angleChemotaxis/2 + 360)%360;
		lowerAngle = lowerAngle*(float)Math.PI/180; //convert to radians
		
		int nPoints = track.size();
		for (int j = 0; j < (nPoints-Params.decimationFactor); j++) {
			Spermatozoon oldSpermatozoon=(Spermatozoon)track.get(j);
			Spermatozoon newSpermatozoon = (Spermatozoon)track.get(j+Params.decimationFactor);
			float diffX = newSpermatozoon.x-oldSpermatozoon.x;
			float diffY = newSpermatozoon.y-oldSpermatozoon.y;
			double angle = (4*Math.PI+Math.atan2(diffY,diffX))%(2*Math.PI);
			//IJ.log("angle: "+angle*180/Math.PI);
			if(lowerAngle>upperAngle){
				//Special case: for example, when chemotaxis cone is between first and fourth quadrant
				if((angle<upperAngle)||(lowerAngle<angle))
					nPos++;
				else
					nNeg++;
			}
			else{ 
				if((angle<upperAngle)&&(lowerAngle<angle))
					nPos++;
				 else //if((angle>(upperAngle+120*Math.PI/180))&&(angle<(lowerAngle-120120*Math.PI/180)))
					nNeg++;
			}
		}
		
		int[] results = new int[3];
		results[0] = nPos;
		results[1] = nNeg;
		IJ.log("nPos: "+results[0]+"; nNeg: "+results[1]);
		return results;
	}
	
	
//	public static float OddsRatio(){
//		
//		
//		
//	}
	
}
