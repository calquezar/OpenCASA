package utils;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import analysis.Kinematics;
import data.Params;
import data.SList;
import data.Spermatozoon;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public abstract class Paint {

	/******************************************************/
	/**
	 * @param imp 
	 * @param theTracks 2D-ArrayList with all the tracks
	 * @param avgTracks 2D-ArrayList with the averaged tracks
	 */
	public static void draw(ImagePlus imp,SList theTracks){
		
		ComputerVision.convertToRGB(imp);
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
		SList avgTracks = SignalProcessing.averageTracks(theTracks);
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
					if(Kinematics.getVelocityTrackType(zTrack)=="Slow")
						ip.setColor(Color.white);
					else if(Kinematics.getVelocityTrackType(zTrack)=="Normal")
						ip.setColor(Color.yellow);
					else if(Kinematics.getVelocityTrackType(zTrack)=="Fast")
						ip.setColor(Color.red);
//					ip.setValue(color);
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
						ip.setColor(Color.white);
						ip.drawString(strPart);
					}
				}					
			}
//			//Draw average paths
//			color=0;
//			for (ListIterator iT=avgTracks.listIterator();iT.hasNext();) {
//				List zTrack=(ArrayList) iT.next();
//				ListIterator jT=zTrack.listIterator();
//				Spermatozoon oldSpermatozoon=(Spermatozoon) jT.next();
//				//Variables used to 
//				Spermatozoon firstSpermatozoon = new Spermatozoon();
//				firstSpermatozoon.copy(oldSpermatozoon);
//				for (;jT.hasNext();) {
//					Spermatozoon newSpermatozoon=(Spermatozoon) jT.next();
//					ip.setValue(color);
//					if(Params.drawAvgTrajectories){
//						ip.moveTo((int)oldSpermatozoon.x*upRes, (int)oldSpermatozoon.y*upRes);
//						ip.lineTo((int)newSpermatozoon.x*upRes, (int)newSpermatozoon.y*upRes);
//					}
//					oldSpermatozoon=newSpermatozoon;
//				}
//			}							
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
	public static void drawChemotaxis(SList theTracks,float ratioQ,float ratioSL,int width,int height,String sampleID){

		SList avgTracks = SignalProcessing.averageTracks(theTracks);
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
		chemotaxisTemplate(ipRelTraj,upRes,avgTracks.size(),ratioQ,ratioSL,sampleID);	
		
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
	public static void chemotaxisTemplate(ColorProcessor ip,int upRes,int numTracks,float ratioQ,float ratioSL,String sampleID){
		// Alpha version of this method
		ip.setLineWidth(4);
		//center coords. of the cone used to clasify chemotactic trajectories
		int xCenter = ip.getWidth()/2;
		int yCenter = ip.getHeight()/2;
		float upperAngle = (float)(Params.angleDirection + Params.angleAmplitude/2 + 360)%360;
		upperAngle = upperAngle*(float)Math.PI/180; //calculate and convert to radians		
		float lowerAngle = (float)(Params.angleDirection - Params.angleAmplitude/2 + 360)%360;
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
		ip.drawString("Sample: ");		
		ip.moveTo(70, 30);
		ip.setColor(Color.black);
		ip.drawString(sampleID);		
		ip.moveTo(10, 50);
		ip.setColor(Color.blue);
		ip.drawString("Number of tracks: ");
		ip.moveTo(135, 50);
		ip.setColor(Color.black);
		ip.drawString(""+numTracks);
		ip.moveTo(10, 70);
		ip.setColor(Color.red);
		ip.drawString("Ratio-Q: ");
		ip.moveTo(70, 70);
		ip.setColor(Color.black);
		ip.drawString(""+ratioQ*100+"%");
		ip.moveTo(10, 90);
		ip.setColor(new Color(34,146,234));
		ip.drawString("Ratio-SL: ");
		ip.moveTo(80, 90);
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
