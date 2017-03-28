package data;

import ij.measure.ResultsTable;

public class Params {
	
	public static double	pixelWidth=1.0;
	public static double pixelHeight=1.0;
	/** @brief minimum sperm size */
	public static float	minSize = 20;
	//maximum sperm size
	public static float	maxSize = 200;
	//minimum length of sperm track in frames
	public static float 	minTrackLength = 15;
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
//	public static boolean calcMotilityParameters = true;
//	public static boolean calcMeanMotilityParameters = true;
	//Draw original trajectories
	public static boolean drawOrigTrajectories = true;
	//Draw original trajectories
	public static boolean drawAvgTrajectories = true;
	
	//Used to calculate OR ratios
	public static int MAXINSTANGLES = 20000;
	public static int NUMSAMPLES = 100;
	
	public static void resetParams(){
//		minSize = 20;
//		maxSize = 200;
//		minTrackLength = 20;
//		maxVelocity = 15;
//		wSize = 5;
//		vclMin = 50;
//		decimationFactor = 5;
//		angleDirection = 0; 
//		angleChemotaxis = 60;
//		frameRate = 100;
//		bcf_shift = 0;
//		progressMotility = 80;
//		microPerPixel = 0.58;
//		printXY = false;
//		calcMotilityParameters = false;
//		calcMeanMotilityParameters = false;
//		drawOrigTrajectories = true;
//		drawAvgTrajectories = true;

		// GOAT
		minSize = 100; //um^2
		maxSize = 10000;//um^2
		minTrackLength = 5;
		maxVelocity = 10;
		wSize = 2;
		vclMin = 1;
		decimationFactor = 1;
		angleDirection = 0; 
		angleChemotaxis = 60;
		frameRate = 25;
		bcf_shift = 0;
		progressMotility = 80;
		microPerPixel = 0.58;
		printXY = false;
//		calcMotilityParameters = false;
//		calcMeanMotilityParameters = false;
		drawOrigTrajectories = true;
		drawAvgTrajectories = true;
	}
}
