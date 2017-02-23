package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import spermAnalysis.MotFunctions;
import data.Params;
import data.SList;
import data.Spermatozoon;

public class TrackFilters {

	static int countMotileSperm = 0;
	static int countNonMotileSperm = 0;
	
	
	/******************************************************/
	/** Fuction to calculate the average path of all tracks using a moving average filter
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return 2D-ArrayList with the averaged tracks
	 */
	public static SList averageTracks (SList theTracks){
		
		SList avgTracks = new SList();
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			List aTrack=(ArrayList)iT.next();
			List avgTrack = movingAverage(aTrack);
			avgTracks.add(avgTrack);
		}
		return avgTracks;
	}
	/******************************************************/
	/** Fuction to decimate a track 
	 * @param track - a track
	 * @param factor - decimation factor
	 * @return Decimated track
	 */
	public static List decimateTrack (List track,int factor){
		List decimatedTrack = new ArrayList();
		for (ListIterator iT=track.listIterator(); iT.hasNext();) {
			Spermatozoon p = (Spermatozoon) iT.next();
			decimatedTrack.add(p);
			for(int i=1;i<factor;i++){
				if(iT.hasNext())
					p = (Spermatozoon) iT.next();
			}
		}
		return decimatedTrack;
	}
	
	/******************************************************/
	/** Function to decimate all tracks 
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @param factor - decimation factor
	 * @return 2D-ArrayList with all the tracks decimated
	 */
	public static List decimateTracks (List theTracks,int factor){
		List decimatedTracks = new ArrayList();
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			List aTrack=(ArrayList)iT.next();
			decimatedTracks.add(decimateTrack(aTrack,factor));
		}
		return decimatedTracks;
	}
	
	/******************************************************/
	/**
	 * @param track ArrayList that stores a track
	 * @return true if the track passes the filter. Otherwise returns false.
	 */
	public static boolean filterOneTrack (List track){
		//Length filter
		
		int nPoints = track.size();
		Spermatozoon firstSpermatozoon = (Spermatozoon)track.get(0);
		Spermatozoon lastSpermatozoon = (Spermatozoon)track.get(nPoints-1);
		float distance = lastSpermatozoon.distance(firstSpermatozoon);
		
		if (track.size() >= Params.minTrackLength) {		
			List avgTrack = movingAverage(track);
			float vap = MotFunctions.vcl(avgTrack);
			//MotFunctions filter
			if(MotFunctions.vcl(track)>Params.vclMin && (vap>0) && (distance>20)){
				//Update motile sperm count
				countMotileSperm++;
				return true;
			}
			else{
				//Update non motile sperm count
				countNonMotileSperm++;
				return false;	
			}
		}
		else
			return false;
	}
	
	/******************************************************/
	/**
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @return 2D-ArrayList with all the tracks that have passed the filter
	 */
	public static SList filterTracks (SList theTracks){
		
		SList filteredTracks = new SList();
		for (ListIterator iT=theTracks.listIterator(); iT.hasNext();) {
			List aTrack=(ArrayList)iT.next();
			if(filterOneTrack(aTrack))
				filteredTracks.add(aTrack);
		}
		return filteredTracks;
	}
	
	/******************************************************/
	/** Fuction to calculate the average path of a track using a moving average filter
	 * @param track Array list that stores one track 
	 * @return ArrayList with the averaged track
	 */
	public static List movingAverage (List track){
		int nPoints = track.size();
		List avgTrack = new ArrayList();
		for (int j = Params.wSize-1; j < nPoints; j++) {
			int avgX = 0;
			int avgY = 0;
			for (int k=Params.wSize-1;k>=0;k--){
				Spermatozoon aSpermatozoon = (Spermatozoon)track.get(j-k);
				avgX += (int)aSpermatozoon.x;
				avgY += (int)aSpermatozoon.y;
			}
			avgX = avgX/Params.wSize;
			avgY = avgY/Params.wSize;
			Spermatozoon newSpermatozoon = new Spermatozoon();
			newSpermatozoon.x=(float)avgX;
			newSpermatozoon.y=(float)avgY;
			avgTrack.add(newSpermatozoon);
		}
		return avgTrack;
	}
}