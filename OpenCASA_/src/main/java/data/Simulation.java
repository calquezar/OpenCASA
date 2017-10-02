package data;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * @author Carlos Alquezar
 *
 */
public abstract class Simulation {

  /**
   * @return
   */
  abstract public ImagePlus createSimulation();

  /**
   * @param ip
   */
  abstract void draw(ImageProcessor ip);

  /**
  	 * 
  	 */
  abstract public void run();
}