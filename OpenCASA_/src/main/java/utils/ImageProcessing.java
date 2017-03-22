package utils;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SList;
import data.Spermatozoon;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class ImageProcessing implements Measurements {


	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * 
	 * This functions converts imp to grayscale.
	 */	
	public static void convertToGrayscale (ImagePlus imp){
		
		ImageConverter ic = new ImageConverter(imp);
		ic.convertToGray8();
	}
	
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * This function makes binary 'imp' applying an statistical threshold
	 */	
	public static void thresholdStack(ImagePlus imp){
		
		ImageStack stack = imp.getStack();
		ImageProcessor ip = stack.getProcessor(1);
		ImageStatistics st = ip.getStatistics();
		double mean = st.mean;
		double std = st.stdDev;
		//Set threshold as mean + 2 x standard deviation
		double lowerThreshold = mean+2*std; // std factor: candidate to be a parameter of the plugin
		double upperThreshold = 255;
		//Make binary
		int[] lut = new int[256];
		for (int j=0; j<256; j++) {
			if (j>=lowerThreshold && j<=upperThreshold)
				lut[j] = 0;
			else
				lut[j] = (byte)255;
		}
		int nFrames = imp.getStackSize();
		for (int iFrame=1; iFrame<=nFrames; iFrame++) {
			ip = stack.getProcessor(iFrame);
            ip.applyTable(lut);
		}
	}
	
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * @return 2D-ArrayList with all spermatozoa detected for each frame
	 */
	public static List[] detectSpermatozoa(ImagePlus imp){
		
		int nFrames = imp.getStackSize();
		ImageStack stack = imp.getStack();	
		int options = 0; // set all PA options false
		int measurements = CENTROID;
		// Initialize results table
		ResultsTable rt = new ResultsTable();
		rt.reset();

		// create storage for Spermatozoa positions
		List[] spermatozoa = new ArrayList[nFrames];

		//************************************************************* 
		//* Record spermatozoa positions for each frame in an ArrayList
		//*************************************************************/
		
		for (int iFrame=1; iFrame<=nFrames; iFrame++) {
			spermatozoa[iFrame-1]=new ArrayList();
			rt.reset();
			ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, Params.minSize, Params.maxSize);
			pa.analyze(imp, stack.getProcessor(iFrame));
			float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);
			float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
			if (sxRes==null)
				return null;
			
			for (int iPart=0; iPart<sxRes.length; iPart++) {
				Spermatozoon aSpermatozoon = new Spermatozoon();
				aSpermatozoon.x=sxRes[iPart];
				aSpermatozoon.y=syRes[iPart];
				aSpermatozoon.z=iFrame-1;
				spermatozoa[iFrame-1].add(aSpermatozoon);
			}
			IJ.showProgress((double)iFrame/nFrames);
		    IJ.showStatus("Identifying spermatozoa per frame...");
			
		}
		return spermatozoa;
	}
	
	/******************************************************/
	/**
	 * @param spermatozoa 2D-ArrayList with all spermatozoa detected for each frame
	 * @return 2D-ArrayList with all tracks detected
	 */
	public static SList idenfityTracks(List[] spermatozoa,int nFrames){
		
		//int nFrames = imp.getStackSize();
		SList theTracks = new SList();
		int trackCount=0;
		
		for (int i=0; i<=(nFrames-1); i++) {
			
			IJ.showProgress((double)i/nFrames);
		    IJ.showStatus("Calculating Tracks...");
			for (ListIterator j=spermatozoa[i].listIterator();j.hasNext();) {
				Spermatozoon aSpermatozoon=(Spermatozoon) j.next();
				if (!aSpermatozoon.inTrack) {
					// This must be the beginning of a new track
					List aTrack = new ArrayList();
					trackCount++;
					aSpermatozoon.inTrack=true;
					aSpermatozoon.trackNr=trackCount;
					aTrack.add(aSpermatozoon);
					//************************************************************* 
					// search in next frames for more Spermatozoa to be added to track
					//*************************************************************
					boolean searchOn=true;
					Spermatozoon oldSpermatozoon=new Spermatozoon();
					Spermatozoon tmpSpermatozoon=new Spermatozoon();
					oldSpermatozoon.copy(aSpermatozoon);
					//*
					//* For each frame
					//*
					for (int iF=i+1; iF<=(nFrames-1);iF++) {
						boolean foundOne=false;
						Spermatozoon newSpermatozoon=new Spermatozoon();
						//*
					    //* For each Spermatozoon in this frame
					    //*
						for (ListIterator jF=spermatozoa[iF].listIterator();jF.hasNext() && searchOn;) {
							Spermatozoon testSpermatozoon =(Spermatozoon) jF.next();
							float distance = testSpermatozoon.distance(oldSpermatozoon);
							// record a Spermatozoon when it is within the search radius, and when it had not yet been claimed by another track
							if ( (distance < Params.maxVelocity) && !testSpermatozoon.inTrack) {
								// if we had not found a Spermatozoon before, it is easy
								if (!foundOne) {
									tmpSpermatozoon=testSpermatozoon;
									testSpermatozoon.inTrack=true;
									testSpermatozoon.trackNr=trackCount;
									newSpermatozoon.copy(testSpermatozoon);
									foundOne=true;
								}
								else {
									// if we had one before, we'll take this one if it is closer.  In any case, flag these Spermatozoa
									testSpermatozoon.flag=true;
									if (distance < newSpermatozoon.distance(oldSpermatozoon)) {
										testSpermatozoon.inTrack=true;
										testSpermatozoon.trackNr=trackCount;
										newSpermatozoon.copy(testSpermatozoon);
										tmpSpermatozoon.inTrack=false;
										tmpSpermatozoon.trackNr=0;
										tmpSpermatozoon=testSpermatozoon;
									}
									else {
										newSpermatozoon.flag=true;
									}
								}
							}
							else if (distance < Params.maxVelocity) {
							// this Spermatozoon is already in another track but could have been part of this one
							// We have a number of choices here:
							// 1. Sort out to which track this Spermatozoon really belongs (but how?)
							// 2. Stop this track
							// 3. Stop this track, and also delete the remainder of the other one
							// 4. Stop this track and flag this Spermatozoon:
								testSpermatozoon.flag=true;
							}
						}
						if (foundOne)
							aTrack.add(newSpermatozoon);
						else
							searchOn=false;
						oldSpermatozoon.copy(newSpermatozoon);
					}
					theTracks.add(aTrack);
				}
			}
		}
		return theTracks;
	}
	/******************************************************/
	/**
	 * @param imp 
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @param avgTracks 2D-ArrayList with the averaged tracks
	 */
	public static void draw(ImagePlus imp,SList theTracks){
		int nFrames = imp.getStackSize();
		ImageStack stack = imp.getStack();	
		if (imp.getCalibration().scaled()) {
			IJ.showMessage("MultiTracker", "Cannot display paths if image is spatially calibrated");
			return;
		}
		int upRes = 1;
		String strPart;
		//Variables used to draw chemotactic cone
		int trackNr=0;
		int displayTrackNr=0;	
		SList avgTracks = TrackFilters.averageTracks(theTracks);
		//Draw on each frame
		for (int iFrame=1; iFrame<=nFrames; iFrame++) {
			IJ.showProgress((double)iFrame/nFrames);
			IJ.showStatus("Drawing Tracks...");
			int trackCount2=0;
			int trackCount3=0;
			int color;
		    int xHeight=stack.getHeight();
		    int yWidth=stack.getWidth();
			ImageProcessor ip = stack.getProcessor(iFrame);		
			ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
			trackNr=0;
			displayTrackNr=0;
			for (ListIterator iT=theTracks.listIterator();iT.hasNext();) {
				trackNr++;
				trackCount2++;
				List zTrack=(ArrayList) iT.next();
				displayTrackNr++;
				ListIterator jT=zTrack.listIterator();
				Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
				color = 150;
				trackCount3++;
				for (;jT.hasNext();) {
					Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
					ip.setValue(color);
					if(Params.drawOrigTrajectories){
						ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
						ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
					}
					oldSpermatozoon=newSpermatozoon;
					//Draw track numbers
					if(newSpermatozoon.z==iFrame){
						strPart=""+displayTrackNr;
						ip.setColor(Color.black);
						// we could do someboundary testing here to place the labels better when we are close to the edge
						ip.moveTo((int)(oldSpermatozoon.x/Params.pixelWidth+0),doOffset((int)(oldSpermatozoon.y/Params.pixelHeight),yWidth,5) );
						ip.drawString(strPart);
					}
				}					
			}
			//Draw average paths
			color=0;
			for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
				List zTrack=(ArrayList) iT.next();
				ListIterator jT=zTrack.listIterator();
				Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
				//Variables used to 
				Spermatozoon firstSpermatozoon = new Spermatozoon();
				firstSpermatozoon.copy(oldSpermatozoon);
				for (;jT.hasNext();) {
					Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
					ip.setValue(color);
					if(Params.drawAvgTrajectories){
						ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
						ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
					}
					oldSpermatozoon=newSpermatozoon;
				}
			}							
		}
		imp.updateAndRepaintWindow();
	}	
//	/******************************************************/
//	/**
//	 * @param imp 
//	 * @param theTracks 2D-ArrayList with all the tracks
//	 * @param avgTracks 2D-ArrayList with the averaged tracks
//	 * @param ratioQ
//	 * @param ratioSL
//	 */
//	public static void draw(ImagePlus imp,List theTracks,List avgTracks,float ratioQ,float ratioSL){
//		int nFrames = imp.getStackSize();
//		ImageStack stack = imp.getStack();	
//		if (imp.getCalibration().scaled()) {
//			IJ.showMessage("MultiTracker", "Cannot display paths if image is spatially calibrated");
//			return;
//		}
//		int upRes = 1;
//		String strPart;
//		//Variables used to draw chemotactic cone
//		int trackNr=0;
//		int displayTrackNr=0;
//		//We create another ImageProcesor to draw chemotactic cone and relative trajectories
//		ColorProcessor ipRelTraj = new ColorProcessor(imp.getWidth()*upRes, imp.getHeight()*upRes);
//		ipRelTraj.setColor(Color.white);
//		ipRelTraj.fill();
//		if(Params.drawRelTrajectories){
//			//Draw cone used to clasify chemotactic trajectories
//			ipRelTraj.setColor(Color.green);
//			chemotaxisTemplate(ipRelTraj,upRes,avgTracks.size(),ratioQ,ratioSL);	
//		}	
//		//Draw on each frame
//		for (int iFrame=1; iFrame<=nFrames; iFrame++) {
//			IJ.showProgress((double)iFrame/nFrames);
//			IJ.showStatus("Drawing Tracks...");
//			int trackCount2=0;
//			int trackCount3=0;
//			int color;
//		    int xHeight=stack.getHeight();
//		    int yWidth=stack.getWidth();
//	
//			ImageProcessor ip = stack.getProcessor(iFrame);		
//			ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
//			trackNr=0;
//			displayTrackNr=0;
//			for (ListIterator iT=theTracks.listIterator();iT.hasNext();) {
//				trackNr++;
//				trackCount2++;
//				List zTrack=(ArrayList) iT.next();
//				displayTrackNr++;
//				ListIterator jT=zTrack.listIterator();
//				Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
//				color = 150;
//				trackCount3++;
//				for (;jT.hasNext();) {
//					Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
//					ip.setValue(color);
//					if(Params.drawOrigTrajectories){
//						ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
//						ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
//					}
//					oldSpermatozoon=newSpermatozoon;
//					//Draw track numbers
//					if(newSpermatozoon.z==iFrame){
//						strPart=""+displayTrackNr;
//						ip.setColor(Color.black);
//						// we could do someboundary testing here to place the labels better when we are close to the edge
//						ip.moveTo((int)(oldSpermatozoon.x/Params.pixelWidth+0),doOffset((int)(oldSpermatozoon.y/Params.pixelHeight),yWidth,5) );
//						ip.drawString(strPart);
//					}
//				}					
//			}
//			//Draw average paths
//			color=0;
//			for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
//				List zTrack=(ArrayList) iT.next();
//				ListIterator jT=zTrack.listIterator();
//				Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
//				//Variables used to 
//				Spermatozoon firstSpermatozoon = new Spermatozoon();
//				firstSpermatozoon.copy(oldSpermatozoon);
//				int xCenter = ip.getWidth()/2;
//				int yCenter = ip.getHeight()/2;
//				int xLast = xCenter;
//				int yLast = yCenter;
//				
//				for (;jT.hasNext();) {
//					Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
//					ip.setValue(color);
//					if(Params.drawAvgTrajectories){
//						ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
//						ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
//					}
//					if(Params.drawRelTrajectories){
//						ipRelTraj.setColor(Color.black);
//						ipRelTraj.moveTo(xLast,yLast);
//						xLast = (int)(newSpermatozoon.x-firstSpermatozoon.x+xCenter);
//						yLast = (int)(newSpermatozoon.y-firstSpermatozoon.y+yCenter);
//						ipRelTraj.lineTo(xLast*upRes, yLast*upRes);
//					}
//					oldSpermatozoon=newSpermatozoon;
//				}
//				ipRelTraj.drawOval(xLast-3,yLast,6,6);
//			}							
//		}
//		imp.updateAndRepaintWindow();
//		if(Params.drawRelTrajectories)
//			new ImagePlus("Chemotactic Ratios", ipRelTraj).show();
//	}
	
	/******************************************************/
	/**
	 * @param imp 
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @param avgTracks 2D-ArrayList with the averaged tracks
	 * @param ratioQ
	 * @param ratioSL
	 */
	public static void drawChemotaxis(SList theTracks,float ratioQ,float ratioSL,int width,int height){

		SList avgTracks = TrackFilters.averageTracks(theTracks);
		int upRes = 1;
		String strPart;
		//Variables used to draw chemotactic cone
		int displayTrackNr=0;
		//We create another ImageProcesor to draw chemotactic cone and relative trajectories
		ColorProcessor ipRelTraj = new ColorProcessor(width*upRes, height*upRes);
		ipRelTraj.setColor(Color.white);
		ipRelTraj.fill();
		//Draw cone used to clasify chemotactic trajectories
		ipRelTraj.setColor(Color.green);
		chemotaxisTemplate(ipRelTraj,upRes,avgTracks.size(),ratioQ,ratioSL);	
		
		IJ.showStatus("Drawing Tracks...");
	
		//Draw average paths
		int color = 0;
		for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
			List zTrack=(ArrayList) iT.next();
			ListIterator jT=zTrack.listIterator();
			Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
			//Variables used to 
			Spermatozoon firstSpermatozoon = new Spermatozoon();
			firstSpermatozoon.copy(oldSpermatozoon);
			int xCenter = width/2;
			int yCenter = height/2;
			int xLast = xCenter;
			int yLast = yCenter;
			for (;jT.hasNext();) {
				Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
				ipRelTraj.setColor(Color.black);
				ipRelTraj.moveTo(xLast,yLast);
				xLast = (int)(newSpermatozoon.x-firstSpermatozoon.x+xCenter);
				yLast = (int)(newSpermatozoon.y-firstSpermatozoon.y+yCenter);
				ipRelTraj.lineTo(xLast*upRes, yLast*upRes);
				oldSpermatozoon=newSpermatozoon;
			}
			ipRelTraj.drawOval(xLast-3,yLast,6,6);
		}
		new ImagePlus("Chemotactic Ratios", ipRelTraj).show();
	}
	
	/******************************************************/
	/**
	 * @param ip 
	 * @param upRes 
	 * @param numTracks 
	 * @param ratioQ 
	 * @param ratioSL 
	 */
	public static void chemotaxisTemplate(ColorProcessor ip,int upRes,int numTracks,float ratioQ,float ratioSL){
		// Alpha version of this method
		ip.setLineWidth(4);
		//center coords. of the cone used to clasify chemotactic trajectories
		int xCenter = ip.getWidth()/2;
		int yCenter = ip.getHeight()/2;
		float upperAngle = (float)(Params.angleDirection + Params.angleChemotaxis/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		float lowerAngle = (float)(Params.angleDirection - Params.angleChemotaxis/2 + 360)%360;
		lowerAngle = lowerAngle*(float)Math.PI/180; //convert to radians
		//Upper Line
		int upperLineX = xCenter+(int)(1000*Math.cos(upperAngle));
		int upperLineY = yCenter+(int)(1000*Math.sin(upperAngle));
		//Lower Line
		int lowerLineX = xCenter+(int)(1000*Math.cos(lowerAngle));
		int lowerLineY = yCenter+(int)(1000*Math.sin(lowerAngle));
		//Draw Chemotaxis Cone
		ip.moveTo((int)xCenter*upRes, (int)yCenter*upRes);
		ip.lineTo((int)upperLineX*upRes, (int)upperLineY*upRes);		
		ip.moveTo((int)xCenter*upRes, (int)yCenter*upRes);
		ip.lineTo((int)lowerLineX*upRes, (int)lowerLineY*upRes);
		//Reses line width
		ip.setLineWidth(1);
		ip.setFont(new Font("SansSerif", Font.PLAIN, 16));
		ip.moveTo(10, 30);
		ip.setColor(Color.blue);
		ip.drawString("Number of tracks: ");
		ip.moveTo(135, 30);
		ip.setColor(Color.black);
		ip.drawString(""+numTracks);
		ip.moveTo(10, 50);
		ip.setColor(Color.red);
		ip.drawString("Ratio-Q: ");
		ip.moveTo(70, 50);
		ip.setColor(Color.black);
		ip.drawString(""+ratioQ*100+"%");
		ip.moveTo(10, 70);
		ip.setColor(new Color(34,146,234));
		ip.drawString("Ratio-SL: ");
		ip.moveTo(80, 70);
		ip.setColor(Color.black);
		ip.drawString(""+ratioSL*100+"%");
	}
	
	static int doOffset (int center, int maxSize, int displacement) {
		if ((center - displacement) < 2*displacement) {
			return (center + 4*displacement);
		}
		else {
			return (center - displacement);
		}
	}
}
