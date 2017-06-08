package analysis;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import analysis.RandomPersistentWalkers.Cell;
import analysis.RandomPersistentWalkers.Obstacle;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class OscillatoryWalker extends Simulation {


	int width = 800;
	int height = 800;
	int cellCount = 1;
	Cell[] sperm = new Cell[cellCount];
	int SIMLENGTH = 700;
	Point[][] tracks = new Point[cellCount][SIMLENGTH];
	
	public OscillatoryWalker() {
		  for (int x = cellCount-1; x >= 0; x--) { 
			    sperm[x] = new Cell();
			  }
	}

	class Cell {
		  
	  int sizex;
	  int sizey;
	  float t;
	  float y;
	  float amplitude;
	  double w;
	  double f;
	  double phi;
	  double T;
	  float dist;
	  
	  Cell(){
	    sizex= 10;
	    sizey=8;
	    t = 0;
	    y = height/2;
	    amplitude= 100;
	    T=350;
	    f=1/T;//0.01;
	    w=2*Math.PI*f;
	    phi=0;
	  }
	  
	  void update(ImageProcessor ip){

		float prevT = t;
		float prevY = y;
	    //Update variables
	    t += 1;//T/width;
	    y = (float) (amplitude*Math.cos(w*t+phi))+height/2; 
	    dist += distance(prevT,prevY,t,y);
	    //Draw Cell
	    ip.fillOval((int)t, (int)y, sizex, sizey);
	  }
	  
	  double distance(float x1,float y1, float x2, float y2){
		  return Math.sqrt(Math.pow(x2-x1, 2)+Math.pow(y2-y1, 2));
	  }
	}
		
	void draw(ImageProcessor ip){
		  ip.setColor(Color.black);
		  ip.fill();
		  ip.setColor(Color.white);
	      for (int x = cellCount-1; x >= 0; x--) { 
	        sperm[x].update(ip);
	      }
	}
	
	public ImagePlus createSimulation(){
		ImageStack imStack = new ImageStack(width,height);
		for(int i=0;i<SIMLENGTH;i++){
			ImageProcessor ip = new ByteProcessor(width,height);
			draw(ip);
			imStack.addSlice(ip);
		}
		for (int x = cellCount-1; x >= 0; x--) { 
			System.out.println("Distance: "+sperm[x].dist);
//			System.out.println("Time: "+sperm[x].t);
			double meanVel = sperm[x].dist/sperm[x].t;
//			System.out.println("mean Velocity: "+meanVel);
	      }
		
		return new ImagePlus("Simulation", imStack);
	}
	
	public void run(){createSimulation().show();}

}
