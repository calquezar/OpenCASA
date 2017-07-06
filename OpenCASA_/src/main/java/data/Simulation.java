package data;

import gui.MainWindow;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public abstract class Simulation {

	abstract void draw(ImageProcessor ip);
	abstract public ImagePlus createSimulation();
	abstract public void run();
}