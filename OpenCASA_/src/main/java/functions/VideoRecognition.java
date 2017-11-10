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
 *   

Part of this code is a modification of a previous code written by Jonas Wilson-Leedy and Rolf Ingermann
Copyright © 2003 The Regents of the University of California and the Howard Hughes Medical Institute.

All Rights Reserved.

Permission to use, copy, modify, and distribute this software and its documentation for educational, research and 
non-profit purposes, without fee, and without a written agreement is hereby granted, provided that the above copyright 
notice, this paragraph and the following three paragraphs appear in all copies.

Permission to incorporate this software into commercial products may be obtained by contacting the Office of 
Technology Management at the University of California San Francisco [Sunita Rajdev, Ph.D., Licensing Officer, 
UCSF Office of Technology Management. 185 Berry St, Suite 4603, San Francisco, CA 94107].

This software program and documentation are copyrighted by The Regents of the University of California 
acting on behalf of the University of California San Francisco via its Office of Technology Management and the 
Howard Hughes Medical Institute (collectively, the Institution).  The software program and documentation are 
supplied "as is", without any accompanying services from the Institution. The Institution does not warrant that the 
operation of the program will be uninterrupted or error-free. The end-user understands that the program was developed 
for research purposes and is advised not to rely exclusively on the program for any reason.

IN NO EVENT SHALL THE INSTITUTION BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL 
DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE INSTITUTION 
HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE  INSTITUTION SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE 
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE  INSTITUTION HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, 
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
*/    

package functions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import data.Params;
import data.SerializableList;
import data.Spermatozoon;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

public class VideoRecognition implements Measurements {

  public VideoRecognition() {}
  
  /**
   * @param ImagePlus
   *          imp
   * @return
   */
  public SerializableList analyzeVideo(ImagePlus imp) {
    if (imp == null)
      return new SerializableList();
    System.out.println("converToGrayScale...");
    ComputerVision cv = new ComputerVision();
    cv.convertToGrayscale(imp);
    // ************************************************************
    // * Automatic Thresholding
    // ************************************************************
    System.out.println("thresholdStack...");
    cv.thresholdStack(imp);
    // ************************************************************
    // * Record particle positions for each frame in an ArrayList
    // ************************************************************
    System.out.println("detectSpermatozoa...");
    List[] theParticles =  detectSpermatozoa(imp);
    // ************************************************************
    // * Now assemble tracks out of the spermatozoa lists
    // * Also record to which track a particle belongs in ArrayLists
    // ************************************************************
    System.out.println("identifyTracks...");
    SerializableList theTracks = idenfityTracks(theParticles, imp.getStackSize());
    // Filtering tracks by length
    SignalProcessing sp = new SignalProcessing();
    theTracks = sp.filterTracksByLength(theTracks);
    // IJ.saveString(Utils.printXYCoords(theTracks),"");
    return theTracks;
  }  
  
  /******************************************************/
  /**
   * @param imp
   *          ImagePlus
   * @return 2D-ArrayList with all spermatozoa detected for each frame
   */
  public List[] detectSpermatozoa(ImagePlus imp) {

    int nFrames = imp.getStackSize();
    ImageStack stack = imp.getStack();
    int options = 0; // set all PA options false
    int measurements = MEAN + CENTROID + RECT + AREA + PERIMETER + FERET;
    // Initialize results table
    ResultsTable rt = new ResultsTable();
    rt.reset();
    int minSize = (int) (Params.minSize * Math.pow((1 / Params.micronPerPixel), 2));
    int maxSize = (int) (Params.maxSize * Math.pow((1 / Params.micronPerPixel), 2));
    // create storage for Spermatozoa positions
    List[] spermatozoa = new ArrayList[nFrames];
    // *************************************************************
    // * Record spermatozoa positions for each frame in an ArrayList
    // *************************************************************/
    for (int iFrame = 1; iFrame <= nFrames; iFrame++) {
      IJ.showProgress((double) iFrame / nFrames);
      IJ.showStatus("Identifying spermatozoa per frame...");      
      spermatozoa[iFrame - 1] = new ArrayList();
      rt.reset();
      ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
      pa.analyze(imp, stack.getProcessor(iFrame));
      float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);
      float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
      float[] bxRes = rt.getColumn(ResultsTable.ROI_X);
      float[] byRes = rt.getColumn(ResultsTable.ROI_Y);
      float[] widthRes = rt.getColumn(ResultsTable.ROI_WIDTH);
      float[] heightRes = rt.getColumn(ResultsTable.ROI_HEIGHT);
      float[] areaRes = rt.getColumn(ResultsTable.AREA);
      float[] perimeterRes = rt.getColumn(ResultsTable.PERIMETER);
      float[] feretRes = rt.getColumn(ResultsTable.FERET);
      float[] minFeretRes = rt.getColumn(ResultsTable.MIN_FERET);
      if (sxRes == null) //Nothing detected
        continue;//jump to next frame
      for (int iPart = 0; iPart < sxRes.length; iPart++) {
        Spermatozoon aSpermatozoon = new Spermatozoon();
        aSpermatozoon.id = "***";
        aSpermatozoon.x = sxRes[iPart];
        aSpermatozoon.y = syRes[iPart];
        aSpermatozoon.z = iFrame - 1;
        aSpermatozoon.bx = bxRes[iPart];
        aSpermatozoon.by = byRes[iPart];
        aSpermatozoon.width = widthRes[iPart];
        aSpermatozoon.height = heightRes[iPart];
        aSpermatozoon.total_area = areaRes[iPart];
        aSpermatozoon.total_perimeter = perimeterRes[iPart];
        aSpermatozoon.total_feret = feretRes[iPart];
        aSpermatozoon.total_minFeret = minFeretRes[iPart];
        spermatozoa[iFrame - 1].add(aSpermatozoon);
      }
    }
    return spermatozoa;
  }
  /******************************************************/
  /**
   * @param spermatozoa
   *          2D-ArrayList with all spermatozoa detected for each frame
   * @param nFrames
   * @return 2D-ArrayList with all tracks detected
   */
  public SerializableList idenfityTracks(List[] spermatozoa, int nFrames) {

    // int nFrames = imp.getStackSize();
    SerializableList theTracks = new SerializableList();
    int trackCount = 0;
    if(spermatozoa == null)
      return theTracks;
    for (int i = 0; i <= (nFrames - 1); i++) {
      IJ.showProgress((double) i / nFrames);
      IJ.showStatus("Calculating Tracks...");
      if(spermatozoa[i] == null)//no spermatozoa detected in frame i
        continue; //jump to next frame
      for (ListIterator j = spermatozoa[i].listIterator(); j.hasNext();) {
        Spermatozoon aSpermatozoon = (Spermatozoon) j.next();
        if (!aSpermatozoon.inTrack) {
          // This must be the beginning of a new track
          List aTrack = new ArrayList();
          trackCount++;
          aSpermatozoon.inTrack = true;
          aSpermatozoon.trackNr = trackCount;
          aTrack.add(aSpermatozoon);
          // *************************************************************
          // search in next frames for more Spermatozoa to be added to
          // track
          // *************************************************************
          boolean searchOn = true;
          Spermatozoon oldSpermatozoon = new Spermatozoon();
          Spermatozoon tmpSpermatozoon = new Spermatozoon();
          oldSpermatozoon.copy(aSpermatozoon);
          // *
          // * For each frame
          // *
          for (int iF = i + 1; iF <= (nFrames - 1); iF++) {
            boolean foundOne = false;
            Spermatozoon newSpermatozoon = new Spermatozoon();
            // *
            // * For each Spermatozoon in this frame
            // *
            for (ListIterator jF = spermatozoa[iF].listIterator(); jF.hasNext() && searchOn;) {
              Spermatozoon testSpermatozoon = (Spermatozoon) jF.next();
              float distance = testSpermatozoon.distance(oldSpermatozoon);
              // record a Spermatozoon when it is within the search
              // radius, and when it had not yet been claimed by another
              // track
              if ((distance < (Params.maxDisplacement / Params.micronPerPixel)) && !testSpermatozoon.inTrack) {
                // if we had not found a Spermatozoon before, it is easy
                if (!foundOne) {
                  tmpSpermatozoon = testSpermatozoon;
                  testSpermatozoon.inTrack = true;
                  testSpermatozoon.trackNr = trackCount;
                  newSpermatozoon.copy(testSpermatozoon);
                  foundOne = true;
                } else {
                  // if we had one before, we'll take this one if it is
                  // closer. In any case, flag these Spermatozoa
                  testSpermatozoon.flag = true;
                  if (distance < newSpermatozoon.distance(oldSpermatozoon)) {
                    testSpermatozoon.inTrack = true;
                    testSpermatozoon.trackNr = trackCount;
                    newSpermatozoon.copy(testSpermatozoon);
                    tmpSpermatozoon.inTrack = false;
                    tmpSpermatozoon.trackNr = 0;
                    tmpSpermatozoon = testSpermatozoon;
                  } else {
                    newSpermatozoon.flag = true;
                  }
                }
              } else if (distance < (Params.maxDisplacement / Params.micronPerPixel)) {
                // this Spermatozoon is already in another track but
                // could have been part of this one
                // We have a number of choices here:
                // 1. Sort out to which track this Spermatozoon really
                // belongs (but how?)
                // 2. Stop this track
                // 3. Stop this track, and also delete the remainder of
                // the other one
                // 4. Stop this track and flag this Spermatozoon:
                testSpermatozoon.flag = true;
              }
            }
            if (foundOne)
              aTrack.add(newSpermatozoon);
            else
              searchOn = false;
            oldSpermatozoon.copy(newSpermatozoon);
          }
          theTracks.add(aTrack);
        }
      }
    }
    return theTracks;
  }  

}
