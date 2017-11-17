/*
 *   OpenCASA software v0.8 for video and image analysis
 *   Copyright (C) 2017  Carlos Alquézar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/    

package data;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author Carlos Alquezar
 *
 */
public class PersistentRandomWalker extends Simulation {

  /**
   * @author Carlos Alquezar
   *
   */
  class SimulatedCell {

    /**   */
    int sizex;
    /**   */
    int sizey;
    /**   */
    float x;
    /**   */
    float y;
    /**   */
    double angle;
    /**   */
    float speed;
    /**   */
    double Drot;
    /**   */
    double beta;
    /**   */
    double ro;
    /**   */
    SimulatedCell() {
      Random rand = new Random();
      sizex = 10;
      sizey = 8;
      x = rand.nextInt(w);
      y = rand.nextInt(h);
      angle = 0;// random(-PI,PI);
      speed = 3;// 4;
      Drot = 0.1;
      beta = 0;
      ro = 1 / Drot;
    }

    /**
     * @param b
     * @param responsiveCells
     */
    SimulatedCell(double b, double responsiveCells) {
      Random rand = new Random();
      sizex = 10;
      sizey = 8;
      x = rand.nextInt(w);
      y = rand.nextInt(h);
      angle = 0;// random(-PI,PI);
      speed = 3;// 4;
      Drot = 0.1;
      // beta=0;//Control
      // Chemotaxis
      if (rand.nextFloat() < responsiveCells) // Only x% of the population is
                                              // chemoattracted
        beta = b;
      else
        beta = 0;
      ro = 1 / Drot;
    }

    /**
     * @param ip
     */
    void update(ImageProcessor ip) {
      Random rand = new Random();
      double epsilon = rand.nextGaussian();
      // Persistent random walker's differential equation
      double da = -(beta / ro) * Math.sin(angle) + epsilon * Math.sqrt(2 * Drot);
      // Update variables
      angle += da;
      angle = angle % (2 * Math.PI);
      float dx = (float) (speed * Math.cos(angle));
      float dy = (float) (speed * Math.sin(angle));
      x += dx;
      y += dy;
      // Draw SimulatedCell
      ip.fillOval((int) x, (int) y, sizex, sizey);

    }
  }

  /**
   * @author Carlos Alquezar
   *
   */
  class Obstacle {

    int x;
    int y;
    int radius;

    Obstacle() {
      Random rand = new Random();
      x = rand.nextInt(w);
      y = rand.nextInt(h);
      radius = rand.nextInt(100);
    }

    void update(ImageProcessor ip) {
      ip.fillOval(x, y, radius, radius);
    }
  }

  /**   */
  int w = 800;
  /**   */
  int h = 800;
  /**   */
  int cellCount = 100;
  /**   */
  int obstaclesCount = 0;
  /**   */
  SimulatedCell[] sperm = new SimulatedCell[cellCount];
  /**   */
  Obstacle[] obstacles = new Obstacle[obstaclesCount];
  /**   */
  int SIMLENGTH = 500;
  /**   */
  Point[][] tracks = new Point[cellCount][SIMLENGTH];
  /**   */
  public PersistentRandomWalker() {

    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x] = new SimulatedCell();
    }
    for (int x = obstaclesCount - 1; x >= 0; x--) {
      obstacles[x] = new Obstacle();
    }
  }

  /**
   * @param b
   * @param responsiveCells
   */
  public PersistentRandomWalker(double b, double responsiveCells) {
    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x] = new SimulatedCell(b, responsiveCells);
    }
    for (int x = obstaclesCount - 1; x >= 0; x--) {
      obstacles[x] = new Obstacle();
    }
  }

  /**
   * @param b
   * @param responsiveCells
   * @param simlength
   */
  public PersistentRandomWalker(double b, double responsiveCells, int simlength) {
    SIMLENGTH = simlength;
    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x] = new SimulatedCell(b, responsiveCells);
    }
    for (int x = obstaclesCount - 1; x >= 0; x--) {
      obstacles[x] = new Obstacle();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see data.Simulation#createSimulation()
   */
  public ImagePlus createSimulation() {
    ImageStack imStack = new ImageStack(w, h);
    for (int i = 0; i < SIMLENGTH; i++) {
      ImageProcessor ip = new ByteProcessor(w, h);
      draw(ip);
      imStack.addSlice(ip);
    }
    return new ImagePlus("PersistentRandomWalker", imStack);
  }

  /*
   * (non-Javadoc)
   * 
   * @see data.Simulation#draw(ij.process.ImageProcessor)
   */
  void draw(ImageProcessor ip) {
    ip.setColor(Color.black);
    ip.fill();
    ip.setColor(Color.white);

    for (int x = obstaclesCount - 1; x >= 0; x--) {
      obstacles[x].update(ip);
    }
    for (int x = cellCount - 1; x >= 0; x--) {
      sperm[x].update(ip);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see data.Simulation#run()
   */
  public void run() {
    createSimulation().show();
  }
}
