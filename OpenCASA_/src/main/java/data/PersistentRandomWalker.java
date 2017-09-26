package data;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;
import java.util.Random;

import gui.MainWindow;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class PersistentRandomWalker extends Simulation {

	int w = 800;
	int h = 800;
	int cellCount = 100;
	int obstaclesCount = 0;
	Cell[] sperm = new Cell[cellCount];
	Obstacle[]  obstacles = new Obstacle[obstaclesCount];
	int SIMLENGTH = 500;
	Point[][] tracks = new Point[cellCount][SIMLENGTH];
	
	class Cell {
		  
	  int sizex;
	  int sizey;
	  float x;
	  float y;
	  double angle;
	  float speed;
	  double Drot;
	  double beta;
	  double ro;
	  
	  Cell(double b,double responsiveCells){
		Random rand = new Random();
	    sizex= 10;
	    sizey=8;
	    x = rand.nextInt(w);
	    y = rand.nextInt(h);
	    angle = 0;//random(-PI,PI);
	    speed=3;//4;
	    Drot =0.1;
	    //beta=0;//Control
	    //Chemotaxis
	    if(rand.nextFloat()<responsiveCells) //Only 10% of the population is chemoattracted
	      beta = b;
	    else
	      beta=0;
	    ro = 1/Drot;
	  }
	  Cell(){
		Random rand = new Random();
	    sizex= 10;
	    sizey=8;
	    x = rand.nextInt(w);
	    y = rand.nextInt(h);
	    angle = 0;//random(-PI,PI);
	    speed=3;//4;
	    Drot =0.1;
	    beta=0;
	    ro = 1/Drot;
	  }
	  
	  void update(ImageProcessor ip){
		Random rand = new Random();
	    double epsilon = rand.nextGaussian();
	    // Persistent random walker's differential equation
	    double da = -(beta/ro)*Math.sin(angle)+epsilon*Math.sqrt(2*Drot);
	    //Update variables
	    angle += da;
	    angle = angle%(2*Math.PI);
	    float dx = (float) (speed*Math.cos(angle));
	    float dy = (float) (speed*Math.sin(angle));
	    x+=dx;
	    y+=dy;    
	    //Draw Cell
	    ip.fillOval((int)x, (int)y, sizex, sizey);
	    
	  }
	}

	class Obstacle {
		 
	  int x;
	  int y;
	  int radius;
	  
	  Obstacle(){
		Random rand = new Random();
	    x = rand.nextInt(w);
	    y = rand.nextInt(h);
	    radius = rand.nextInt(100);
	  }
	  
	  void update(ImageProcessor ip){
		ip.fillOval(x,y,radius,radius);
	  }
	}
	

	public PersistentRandomWalker(double b, double responsiveCells,int simlength) {
		SIMLENGTH = simlength;
		for (int x = cellCount-1; x >= 0; x--) {
			sperm[x] = new Cell(b,responsiveCells);
		}
		for (int x = obstaclesCount-1; x >= 0; x--) { 
			obstacles[x] = new Obstacle();
		}
	}
	public PersistentRandomWalker(double b, double responsiveCells) {
		SIMLENGTH = 500;
		for (int x = cellCount-1; x >= 0; x--) {
		   sperm[x] = new Cell(b,responsiveCells);
		}
		for (int x = obstaclesCount-1; x >= 0; x--) { 
		   obstacles[x] = new Obstacle();
		}	
	}
	public PersistentRandomWalker() {
		
	  for (int x = cellCount-1; x >= 0; x--) { 
		    sperm[x] = new Cell();
		  }
	  for (int x = obstaclesCount-1; x >= 0; x--) { 
	    obstacles[x] = new Obstacle();
	  }
	}
	
	void draw(ImageProcessor ip){
		  ip.setColor(Color.black);
		  ip.fill();
		  ip.setColor(Color.white);

	      for (int x = obstaclesCount-1; x >= 0; x--) { 
	        obstacles[x].update(ip);
	      }
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
		return new ImagePlus("PersistentRandomWalker", imStack);
	}
	
	public void run(){createSimulation().show();}
}
