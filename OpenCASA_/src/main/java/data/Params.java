package data;

import java.util.ArrayList;
import java.util.List;

import ij.measure.ResultsTable;

public class Params {
	
	public static double	pixelWidth=1.0;
	public static double pixelHeight=1.0;
	/** @brief minimum sperm size */
	public static float	minSize = 10;
	//maximum sperm size
	public static float	maxSize = 1000;
	//minimum length of sperm track in frames
	public static float 	minTrackLength = 50;
	//maximum velocity that is allowable between frames (this is the search radius for the next sperm location in a track... it will only look w/in this distance)
	public static float 	maxVelocity = 10;
	//Window size for moving average method
	public static int wSize = 9;
	//Motility filter for motile and non motile sperm
	public static int vclMin = 70;
	//Decimation factor
	public static int decimationFactor = 4;
	//Angles used to clasify chemotactic trajectories
	public static float angleDirection = 0; 
	public static float angleChemotaxis = 90;
	// frame rate
	public static float frameRate = 100;
	//parameters used to compute BCF (equivalent to decimation factor)
	public static int bcf_shift = 0;
	// Parameter used to determine progressive motility sperm
	public static float progressMotility = 80;
	//Micrometers per pixel
	// 10x ==> 0.58
	// 40x ==> 0.1455
	public static double microPerPixel = 0.58; //10x ISAS
	//print the xy co-ordinates for all tracks?
	public static boolean printXY = false;
	//Calculate motility parameters
	public static boolean calcMotilityParameters = true;
	public static boolean calcMeanMotilityParameters = true;
	//Draw original trajectories
	public static boolean drawOrigTrajectories = true;
	//Draw original trajectories
	public static boolean drawAvgTrajectories = true;
	//Draw relative trajectories
	public static boolean drawRelTrajectories = true;
	
	
	//Motility variables
	public static float total_vsl = 0;
	public static float total_vcl = 0;
	public static float total_vap = 0;
	public static float total_lin = 0;
	public static float total_wob = 0;
	public static float total_str = 0;
	public static float total_alhMean = 0;
	public static float total_alhMax = 0;
	public static float total_bcf = 0;
	public static float total_dance = 0;
	public static float total_mad = 0;
	public static float countProgressiveSperm = 0;
	public static int countMotileSperm = 0;
	public static int countNonMotileSperm = 0;
	
	public static ResultsTable rTable = new ResultsTable();
	
	public static SList controlTracks = new SList();
	public static SList conditionTracks = new SList();
	//Used to calculate OR ratios
	public static int MAXINSTANGLES = 20000;
//	public static int typeTrial = 0; // 0-Control; 1-chemotaxis(10pM);2-chemotaxis(100pM);3-chemotaxis(10nM)
//	public static int nPlusControl = 0;
//	public static int nMinusControl = 0;
//	public static int nPlusCondition= 0;
//	public static int nMinusCondition = 0;
	
	public static void resetParams(){
		minSize = 20;
		maxSize = 100;
		minTrackLength = 50;
		maxVelocity = 10;
		wSize = 9;
		vclMin = 70;
		decimationFactor = 4;
		angleDirection = 0; 
		angleChemotaxis = 90;
		frameRate = 100;
		bcf_shift = 0;
		progressMotility = 80;
		microPerPixel = 0.58;
		printXY = false;
		calcMotilityParameters = false;
		calcMeanMotilityParameters = false;
		drawOrigTrajectories = true;
		drawAvgTrajectories = true;
		drawRelTrajectories = true;
		
		//Motility variables
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
	
		rTable = new ResultsTable();
		
		controlTracks = new SList();
		conditionTracks = new SList();
		
//		typeTrial = 0;
//		nPlusControl = 0;
//		nMinusControl = 0;
//		nPlusCondition= 0;
//		nMinusCondition = 0;		
	}
}
