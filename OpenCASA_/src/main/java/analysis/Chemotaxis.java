package analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import data.Params;
import data.SList;
import data.Spermatozoon;
import data.Trial;
import functions.FileManager;
import functions.Paint;
import functions.TrialManager;
import functions.Utils;
import ij.IJ;

/**
 * 
 * @author Carlos Alquezar
 *
 */
public class Chemotaxis extends SwingWorker<Boolean, String> {

  private enum TypeOfAnalysis {
    INDEXESFILE, INDEXESDIRECTORY, BOOTSTRAPPING, INDEXESSIMULATIONS, BOOTSTRAPPINGSIMULATIONS,NONE
  }
  /** */
  private static final Float FLOAT = (Float) null;
  private Trial trial;
  /** */
  private TypeOfAnalysis analysis = TypeOfAnalysis.NONE;

  private void analyseFile(){
    FileManager fm = new FileManager();
    String file = fm.selectFile();
    TrialManager tm = new TrialManager();
    trial = tm.getTrialFromAVI(file);
  }
  
  private void analyseDirectory(){
    //Get list of files
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    String controlFolder = folder+"\\control";
    String testFolder = folder+"\\test";
    String[] controlFiles = fm.getListOfFiles(controlFolder);
    String[] testFiles = fm.getListOfFiles(testFolder);
    //Extract Trials 
    TrialManager tm = new TrialManager();
    Map<String, Trial> controls = new HashMap<String, Trial>();
    for (int i = 0; i<controlFiles.length;i++){
      String file = controlFiles[i];
      trial = tm.getTrialFromAVI(file);
      controls.put(trial.type+"-"+trial.ID, trial);
      System.out.println("TrialID: "+trial.ID+"; TrialType: "+trial.type);
    }
    Map<String, Trial> tests = new HashMap<String, Trial>();
    for (int i = 0; i<testFiles.length;i++){
      String file = testFiles[i];
      trial = tm.getTrialFromAVI(file);
      tests.put(trial.type+"-"+trial.ID, trial);
      System.out.println("TrialID: "+trial.ID+"; TrialType: "+trial.type);
    }
    
//AQUI VA EL ANTIGUO 
    CHEMOTAXIS.analyseChDirectory
    
  }
  
  /******************************************************/
  /**
   * Fuction to calculate the Ratio-Q
   * 
   * @param theTracks
   *          2D-ArrayList with all the tracks
   * @return the Ratio-Q
   */
  /**
   * @param theTracks
   * @return
   */
  public float calculateChIndex(List theTracks) {

    float nPos = 0; // Number of shifts in the chemoattractant direction
    float nNeg = 0; // Number of shifts in other direction
    int trackNr = 0; // Number of tracks
    List angles = new ArrayList();
    int nTracks = theTracks.size();
    double angleDirection = (2 * Math.PI + Params.angleDirection * Math.PI / 180) % (2 * Math.PI);
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
    float chIdx = 0;
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Calculating Ch-Index...");
      trackNr++;
      List track = (ArrayList) iT.next();
      int nPoints = track.size();
      for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
        Spermatozoon oldSpermatozoon = (Spermatozoon) track.get(j);
        Spermatozoon newSpermatozoon = (Spermatozoon) track.get(j + Params.angleDelta);
        float diffX = newSpermatozoon.x - oldSpermatozoon.x;
        float diffY = newSpermatozoon.y - oldSpermatozoon.y;
        double angle = (2 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute
                                                                                 // angle
        angle = (2 * Math.PI + angle - angleDirection) % (2 * Math.PI); // Relative
                                                                        // angle
                                                                        // between
                                                                        // interval
                                                                        // [0,2*Pi]
        // IJ.log(""+angle);
        if (angle > Math.PI) {
          angle = -(2 * Math.PI - angle);
        }
        if (Math.abs(angle) < angleChemotaxis) {
          nPos++;
        } else {
          nNeg++;
        }
      }

    }
    if ((nPos + nNeg) > 0) {
      chIdx = (nPos / (nPos + nNeg)); // (nPos+nNeg) = Total number of shifts
    } else {
      chIdx = -1;
    }
    return chIdx;
  }
  
  /******************************************************/
  /**
   * @param theTracks
   *          2D-ArrayList that stores all the tracks
   * @return RatioSL
   */
  /**
   * @param theTracks
   * @return
   */
  public float calculateSLIndex(List theTracks) {

    float nPos = 0; // Number of shifts in the chemoattractant direction
    float nNeg = 0; // Number of shifts in other direction
    int trackNr = 0;
    int nTracks = theTracks.size();
    double angleDirection = (2 * Math.PI + Params.angleDirection * Math.PI / 180) % (2 * Math.PI);
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
    float ratioSL = 0;
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Calculating RatioSL...");
      trackNr++;
      List aTrack = (ArrayList) iT.next();
      Spermatozoon first = (Spermatozoon) aTrack.get(1);
      Spermatozoon last = (Spermatozoon) aTrack.get(aTrack.size() - 1);
      float diffX = last.x - first.x;
      float diffY = last.y - first.y;
      double angle = (2 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute
                                                                               // angle
      angle = (2 * Math.PI + angle - angleDirection) % (2 * Math.PI); // Relative
                                                                      // angle
                                                                      // between
                                                                      // interval
                                                                      // [0,2*Pi]
      if (angle > Math.PI) {
        angle = -(2 * Math.PI - angle);
      }
      if (Math.abs(angle) < angleChemotaxis) {
        nPos++;
      } else {
        nNeg++;
      }
    }
    if ((nPos + nNeg) > 0) {
      ratioSL = (nPos / (nPos + nNeg));
    } else {
      ratioSL = -1;
    }
    return ratioSL;
  }

  /**
   * @param angles
   * @param N
   * @return
   */
  public int[] circularHistogram(List<Double> angles, int N) {

    int[] histogram = new int[N];
    for (int i = 0; i < N; i++) {
      histogram[i] = 0;
    }
    int BINSIZE = 360 / N;
    for (int i = 0; i < angles.size(); i++) {
      int bin = angles.get(i).intValue() / BINSIZE;
      histogram[bin]++;
    }
    return histogram;
  }

  @Override
  public Boolean doInBackground() throws Exception {
    
    switch (analysis) {
      case INDEXESFILE:
        analyseFile();
        break;
      case INDEXESDIRECTORY:
      case BOOTSTRAPPING:
        analyseDirectory();
        break;
      case INDEXESSIMULATIONS:
        break;
      case BOOTSTRAPPINGSIMULATIONS:
        break;
    }

    return null;
  }  
  
  @Override
  protected void done() {
    //This method is executed in the EDT
    switch (analysis) {
      case INDEXESFILE:
        drawResults();
        break;
      case INDEXESDIRECTORY:
        break;
      case BOOTSTRAPPING:
        break;
      case INDEXESSIMULATIONS:
        break;
      case BOOTSTRAPPINGSIMULATIONS:
        break;
    }
  }

  private void drawResults(){
    if (trial == null)
      return;
    float chIdx = calculateChIndex(trial.tracks);
    float slIdx = calculateSLIndex(trial.tracks);
    int[] hist = circularHistogram(getListOfAngles(trial.tracks), 45);
    int radius = trial.fieldWidth;
    Paint paint = new Paint();
    paint.drawChemotaxis(trial, chIdx, slIdx);
    paint.drawRoseDiagram(hist, radius, chIdx, trial.source);
  }
  

  /**
   * @param theTracks
   * @return
   */
  public List<Double> getListOfAngles(SList theTracks) {
    List<Double> instAngles = new ArrayList<Double>();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List track = (ArrayList) iT.next();
      int nPoints = track.size();
      for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
        Spermatozoon oldSpermatozoon = (Spermatozoon) track.get(j);
        Spermatozoon newSpermatozoon = (Spermatozoon) track.get(j + Params.angleDelta);
        float diffX = newSpermatozoon.x - oldSpermatozoon.x;
        float diffY = newSpermatozoon.y - oldSpermatozoon.y;
        double angle = (360 + Math.atan2(diffY, diffX) * 180 / Math.PI) % (360); // Absolute
                                                                                 // angle
        instAngles.add(angle);
      }
    }
    return instAngles;
  }  

  public void selectAnalysis() {
    // Ask if user wants to analyze a file or directory
    Object[] options = { "File", "Directory", " Multiple Simulations" };
    String question = "What do you want to analyze?";
    String title = "Choose one analysis...";
    Utils utils = new Utils();
    final int FILE = 0;
    final int DIR = 1;
    final int SIMULATION = 2;
    int sourceSelection = utils.analysisSelectionDialog(options, question, title);
    if (sourceSelection < 0) {
      return;
    } else if (sourceSelection == FILE) {// File
      analysis = TypeOfAnalysis.INDEXESFILE; // It's not possible to carry on
                                             // bootstrapping analysis in a
                                             // single file
    } else if (sourceSelection == DIR || sourceSelection == SIMULATION) {// Directory or simulations
      // Ask user which analysis wants to apply
      Object[] options2 = { "Ch-Index", "BOOTSTRAPPING" };
      question = "Which analysis do you want to apply to the data?";
      title = "Choose one analysis...";
      int analysisSelection = utils.analysisSelectionDialog(options2, question, title);
      final int CHINDEX = 0;
      final int BOOTSTRAPPING = 1;
      if (analysisSelection < 0)
        return;
      if (sourceSelection == DIR) {
        if (analysisSelection == CHINDEX) {
          analysis = TypeOfAnalysis.INDEXESDIRECTORY;
        } else if (analysisSelection == BOOTSTRAPPING) {
          analysis = TypeOfAnalysis.BOOTSTRAPPING;
        }
      } else if (sourceSelection == SIMULATION) { // Simulations
        if (analysisSelection == CHINDEX)
          analysis = TypeOfAnalysis.INDEXESSIMULATIONS;
      } else if (analysisSelection == BOOTSTRAPPING) {
        analysis = TypeOfAnalysis.BOOTSTRAPPINGSIMULATIONS;
      }
    }
  }
}
