package data;

public class Params {
	
	/** @brief minimum sperm size */
	public static float	minSize = 20;
	//maximum sperm size
	public static float	maxSize = 100;
	//minimum length of sperm track in frames
	public static float 	minTrackLength = 50;
	//maximum velocity that is allowable between frames (this is the search radius for the next sperm location in a track... it will only look w/in this distance)
	public static float 	maxVelocity = 10;
	//Window size for moving average method
	public static int wSize = 9;
	//Motility filter for motile and non motile sperm
	public static int vclMin = 70;
	//Decimation factor
	public static int decimationFactor = 10;
	//Angles used to clasify chemotactic trajectories
	public static float angleDirection = 0; 
	public static float angleChemotaxis = 90;
	// frame rate
	public static float frameRate = 200;
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
	public static boolean calcMotilityParameters = false;
	public static boolean calcMeanMotilityParameters = false;
	//Draw original trajectories
	public static boolean drawOrigTrajectories = true;
	//Draw original trajectories
	public static boolean drawAvgTrajectories = true;
	//Draw relative trajectories
	public static boolean drawRelTrajectories = true;
	
	static void resetParams(){
		minSize = 20;
		maxSize = 100;
		minTrackLength = 50;
		maxVelocity = 10;
		wSize = 9;
		vclMin = 70;
		decimationFactor = 10;
		angleDirection = 0; 
		angleChemotaxis = 90;
		frameRate = 200;
		bcf_shift = 0;
		progressMotility = 80;
		microPerPixel = 0.58;
		printXY = false;
		calcMotilityParameters = false;
		calcMeanMotilityParameters = false;
		drawOrigTrajectories = true;
		drawAvgTrajectories = true;
		drawRelTrajectories = true;
	}
}
