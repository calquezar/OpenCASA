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


	int w = 800;
	int h = 800;
	int cellCount = 1;
	Cell[] sperm = new Cell[cellCount];
	int SIMLENGTH = 800;
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
	  
	  Cell(){
	    sizex= 10;
	    sizey=8;
	    t = 0;
	    y = h/2;
	    amplitude= 100;
	    T=800;
	    f=1/T;//0.01;
	    w=2*Math.PI*f;
	    phi=0;
	  }
	  
	  void update(ImageProcessor ip){

	    //Update variables
	    t += T/1600;
	    y = (float) (amplitude*Math.cos(w*t+phi))+h/2; 
	    //Draw Cell
	    ip.fillOval((int)t, (int)y, sizex, sizey);
	    
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
		ImageStack imStack = new ImageStack(w,h);
		for(int i=0;i<SIMLENGTH;i++){
			ImageProcessor ip = new ByteProcessor(w,h);
			draw(ip);
			imStack.addSlice(ip);
		}
		return new ImagePlus("RandomPersistentWalkers", imStack);
	}
	
	public void run(){createSimulation().show();}

}
