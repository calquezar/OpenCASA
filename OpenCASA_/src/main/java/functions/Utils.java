/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
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

package functions;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import javax.swing.JOptionPane;

import data.Cell;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.process.LUT;
import net.sf.javaml.core.kdtree.KDTree;


/**
 * @author Carlos Alquezar
 *
 */
public class Utils {

  /**
   * 
   * @param options
   * @param question
   * @param title
   * @return
   */
  public int analysisSelectionDialog(Object[] options, String question, String title) {
    int n = JOptionPane.showOptionDialog(null, question, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, // do not use a custom Icon
        options, // the titles of buttons
        options[0]); // default button title
    return n;
  }

  /**
   * @param orig
   * @return
   */
  public int[] convertLongArrayToInt(long[] orig) {
    int[] arrayInt = new int[orig.length];
    for (int i = 0; i < orig.length; i++)
      arrayInt[i] = (int) orig[i];
    return arrayInt;
  }

  /******************************************************/
  /**
   * @param id
   * @param spermatozoa
   * @return
   */
  public Cell getCell(String id, List spermatozoa) {
    Cell cell = null;
    for (ListIterator j = spermatozoa.listIterator(); j.hasNext();) {
      Cell candidate = (Cell) j.next();
      if (candidate.id.equals(id) && id != "***") {
        cell = candidate;
        break;
      }
    }
    return cell;
  }
  
  /**
   * 
   * @param url
   * @return
   */
  public static LUT getLut(URL url) {
    byte r[] = new byte[256], g[] = new byte[256], b[] = new byte[256];
    try {
      Scanner sc = new Scanner(url.openStream());

      while (sc.hasNextInt()) {
        int i = sc.nextInt();
        r[i] = (byte) sc.nextInt();
        g[i] = (byte) sc.nextInt();
        b[i] = (byte) sc.nextInt();
      }

      sc.close();
    } catch (Exception e) {
      IJ.handleException(e);
    }
    return new LUT(r, g, b);
  }
  
  public static double getMinDouble(double[] x){
    Double min = null;
    for (double val : x) {
      if (min==null)
        min = val;
      else if (min>val)
        min = val;
    }
    return min.doubleValue();
  }
  
  public static double getMaxDouble(double[] x){
    Double max = null;
    for (double val : x) {
      if (max==null)
        max = val;
      else if (max<val)
        max = val;
    }
    return max.doubleValue();
  }

  public int getTrackNr(List track) {
    Cell cell = (Cell) track.get(0);
    return cell.trackNr;
  }

  /******************************************************/
  /**
   * @param theTracks
   *          2D-ArrayList with all the tracks
   * @return String with the results in tsv format (tab separated values)
   */
  public String printXYCoords(List theTracks) {
    int nTracks = theTracks.size();
    // strings to print out all of the data gathered, point by point
    String xyPts = " ";
    // initialize variables
    int trackNr = 0;
    int displayTrackNr = 0;
    int line = 1;
    String output = "Line" + "\tTrack" + "\tRelative_Frame" + "\tX" + "\tY";
    // loop through all sperm tracks
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      int frame = 0;
      trackNr++;
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Analyzing Tracks...");
      List bTrack = (List) iT.next();
      // keeps track of the current track
      displayTrackNr++;
      ListIterator jT = bTrack.listIterator();
      Cell oldCell = (Cell) jT.next();
      Cell firstCell = new Cell();
      firstCell.copy(oldCell);
      // For each instant (Cell) in the track
      String outputline = "";
      for (; jT.hasNext();) {
        Cell newCell = (Cell) jT.next();
        xyPts = "\t" + displayTrackNr + "\t" + frame + "\t" + (int) newCell.x + "\t" + (int) newCell.y;
        frame++;
        oldCell = newCell;
        outputline += "\n" + line + xyPts;
        line++;
      }
      output += outputline;
    }
    IJ.showProgress(2); // To remove progresBar
    return output;
  }

public static void scatter(){
    
    FileManager fm = new FileManager();
    ResultsTable rt = fm.loadResultsTable();
    if(rt==null)
      return;
    
    String[] headings = rt.getHeadings();
    GenericDialog gd = new GenericDialog("Set Simulation parameters");
    gd.addChoice("Select X data", headings, "");
    gd.addChoice("Select Y data", headings, "");
    gd.showDialog();
    if (gd.wasCanceled())
      return;
    
    String xColumn = gd.getNextChoice();
    String yColumn = gd.getNextChoice();
    
    int xColInt = rt.getColumnIndex(xColumn);
    int yColInt = rt.getColumnIndex(yColumn);
    
    //Get data for columns
    double[] x = rt.getColumnAsDoubles(xColInt);
    double[] y = rt.getColumnAsDoubles(yColInt);

    double xmin = Utils.getMinDouble(x);
    double ymin = Utils.getMinDouble(y);
    double xmax = Utils.getMaxDouble(x);
    double ymax = Utils.getMaxDouble(y);

    //PlotWindow.noGridLines = false; // draw grid lines
    Plot plot = new Plot("Scatter Plot",xColumn, yColumn);
    plot.setLimits(xmin,xmax,ymin,ymax);
    plot.setColor(Color.black);
    plot.setLineWidth(4);
    plot.addPoints(x, y, Plot.DOT);
    // add label
    //plot.setColor(Color.black);
    //plot.changeFont(new Font("Helvetica", Font.PLAIN, 24));
    //plot.addLabel(0.15, 0.95, "This is a label");

    plot.changeFont(new Font("Helvetica", Font.PLAIN, 16));
    //plot.setColor(Color.blue);
    plot.show();
  }

  /**
   * 
   * @param cells
   * @return KDTree array containing each cells component
   */
  public KDTree[] getKDTree(List[] cells) {
    KDTree res[] = new KDTree[cells.length];
    for (int i = 0; i < cells.length; i++) {
      res[i] = new KDTree(2);
      if (cells[i] != null) {
        for (ListIterator j = cells[i].listIterator(); j.hasNext();) {
          Cell c = (Cell) j.next();
          res[i].insert(new double[] { (int) c.x, (int) c.y }, c);
        }
      }
    }
    return res;
  }

}
