package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;

/**
 * 
 * @author Carlos Alquezar
 *
 */
public class Chemotaxis extends SwingWorker<Boolean, String> {

  private enum TypeOfAnalysis {
    INDEXESFILE, INDEXESDIRECTORY, BOOTSTRAPPING, INDEXESSIMULATIONS, BOOTSTRAPPINGSIMULATIONS, NONE
  }

  /** */
  private Trial              trial;
  /** */
  private TypeOfAnalysis     analysis = TypeOfAnalysis.NONE;

  private ResultsTable analyseCondition(Map<String, Trial> controls, Map<String, Trial> tests) {
    ResultsTable rt = new ResultsTable();
    switch (analysis) {
      case INDEXESDIRECTORY:
      case INDEXESSIMULATIONS:
        rt = indexesAnalysis(controls, tests);
        break;
      case BOOTSTRAPPING:
      case BOOTSTRAPPINGSIMULATIONS:
        rt = bootstrappingAnalysis(controls, tests);
        break;
      default:
    }
    return rt;
  }

  private void analyseDirectory() {
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    Map<String, Trial> cTrials = getControlTrials(folder);
    List<String> testFolders = getTestFolders(folder);
    for (String f : testFolders) {
      List<String> tests = fm.getFiles(f);
      Map<String, Trial> tTrials = getTestTrials(tests);
      ResultsTable rt = analyseCondition(cTrials, tTrials);
      String condition = fm.getFilename(f);
      rt.show(condition);
    }
  }

  private void analyseFile() {
    FileManager fm = new FileManager();
    String file = fm.selectFile();
    TrialManager tm = new TrialManager();
    trial = tm.getTrialFromAVI(file);
  }

  private void analyseSimulations() {
    GenericDialog gd = new GenericDialog("Set Simulation parameters");
    gd.addNumericField("Beta", 0, 2);
    gd.addNumericField("Responsiveness (%)", 50, 2);
    gd.addNumericField("Number of simulations", 50, 0);
    gd.showDialog();
    if (gd.wasCanceled())
      return;
    final double BETA = gd.getNextNumber();
    final double RESPONSIVENESS = gd.getNextNumber() / 100;// value must be between [0,1]
    final int TOTALSIMULATIONS = (int) gd.getNextNumber();
    TrialManager tm = new TrialManager();
    Map<String, Trial> controls = tm.simulateTrials(0, 0, TOTALSIMULATIONS);
    Map<String, Trial> tests = tm.simulateTrials(BETA, RESPONSIVENESS, TOTALSIMULATIONS);
    ResultsTable rt = analyseCondition(controls, tests);
    rt.show("Results from Simulation (Beta: " + BETA + ",Responsiveness: " + RESPONSIVENESS + ")");
  }

  /**
   * @param trials
   * @return
   */
  private ResultsTable bootstrappingAnalysis(Map<String, Trial> controls, Map<String, Trial> tests) {

    final int cMin = minSampleSize(controls);
    final int tMin = minSampleSize(tests);
    Params.MAXINSTANGLES = Math.min(cMin, tMin);
    // Calculating OR threshold via subsampling
    double thControl = orThreshold(controls);
    ResultsTable rt = new ResultsTable();
    for (String cKey : controls.keySet()) {
      Trial cTrial = (Trial) controls.get(cKey);
      Trial tTrial = getTestTrial(cTrial.ID, tests);
      if (tTrial != null) {
        double or = or(cTrial, tTrial);
        setBootstrappingResults(rt, or, thControl, tTrial);
      }
    }
    return rt;
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
  private float calculateChIndex(List<List<Spermatozoon>> theTracks) {
    int trackNr = 0; // Number of tracks
    int nTracks = theTracks.size();
    int[] displacements = {0,0};
    for (List<Spermatozoon> track : theTracks) {
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Calculating Ch-Index...");
      trackNr++;
      int[] instD = countInstantDisplacements(track);
      displacements[0]+=instD[0];
      displacements[1]+=instD[1];
    }
    float nUpGradient = displacements[0]; // Number of displacements in the gradient direction
    float nOtherDirs = displacements[1]; // Number of displacements in other direction
    IJ.log("nUpGradient: "+nUpGradient+"; nOtherDirs: "+nOtherDirs);
    float chIdx = 0;
    if ((nUpGradient + nOtherDirs) > 0) {
      chIdx = nUpGradient / (nUpGradient + nOtherDirs); // (nUpGradient+nOtherDirs) = Total number of shifts
    } else {
      chIdx = -1;
    }
    return chIdx; //return index between [0,1]
  }

  /******************************************************/
  /**
   * @param theTracks
   *          2D-ArrayList that stores all the tracks
   * @return SL-Index
   */
  private float calculateSLIndex(List<List<Spermatozoon>> theTracks) {

    float nUpGradient = 0; // Number of shifts in the chemoattractant direction
    float nOtherDirs = 0; // Number of shifts in other direction
    int trackNr = 0;
    int nTracks = theTracks.size();
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
    float ratioSL = 0;
    for (List<Spermatozoon> aTrack : theTracks) {
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Calculating SL-Index...");
      trackNr++;
      Spermatozoon first = (Spermatozoon) aTrack.get(1);
      Spermatozoon last = (Spermatozoon) aTrack.get(aTrack.size() - 1);
      double angle = relativeAngle(first,last); //Between [-PI,PI]
      //Check if the angle is upGradient or not
      if (Math.abs(angle) < angleChemotaxis) {
        nUpGradient++;
      } else {
        nOtherDirs++;
      }
    }
    if ((nUpGradient + nOtherDirs) > 0) {
      ratioSL = nUpGradient / (nUpGradient + nOtherDirs);
    } else {
      ratioSL = -1;
    }
    return ratioSL;
  }

  /**
   * @param angles
   * @param n
   * @return
   */
  private int[] circularHistogram(List<Double> angles, int n) {

    int[] histogram = new int[n];
    for (int i = 0; i < n; i++) {
      histogram[i] = 0;
    }
    final int BINSIZE = 360 / n;
    for (int i = 0; i < angles.size(); i++) {
      int bin = angles.get(i).intValue() / BINSIZE;
      histogram[bin]++;
    }
    return histogram;
  }

  /**
   * @param theTracks
   * @return
   */
  private int[] countAngles(SList theTracks) {
    int[] angles = { 0, 0 };
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      int[] instantAngles = countInstantDisplacements(aTrack);
      angles[0] += instantAngles[0];
      angles[1] += instantAngles[1];
    }
    return angles;
  }
  
  /**
   * @param track
   * @return
   */
  private int[] countInstantDisplacements(List<Spermatozoon> track) {
    int nUpGradient = 0;
    int nOtherDir = 0;
    int nPoints = track.size();
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
    for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
      Spermatozoon oldSpermatozoon = (Spermatozoon) track.get(j);
      Spermatozoon newSpermatozoon = (Spermatozoon) track.get(j + Params.angleDelta);
      double angle = relativeAngle(oldSpermatozoon,newSpermatozoon);//Between interval [-PI,PI]
      if(Params.compareOppositeDirections){//We only take into account angles in the gradient and opposite direction
        if (Math.abs(angle) < angleChemotaxis) {
          nUpGradient++;
        } else if (Math.abs(angle) > (Math.PI - angleChemotaxis)) {
          nOtherDir++;
        }
      }else{//We take into account all angles in all directions
        if (Math.abs(angle) < angleChemotaxis) {
          nUpGradient++;
        } else {
          nOtherDir++;
        }
        
      }
    }
    int[] results = new int[2];
    results[0] = nUpGradient;
    results[1] = nOtherDir;
    return results;
  }

  @Override
  public Boolean doInBackground() {

    switch (analysis) {
      case INDEXESFILE:
        analyseFile();
        break;
      case INDEXESDIRECTORY:
      case BOOTSTRAPPING:
        analyseDirectory();
        break;
      case INDEXESSIMULATIONS:
      case BOOTSTRAPPINGSIMULATIONS:
        analyseSimulations();
        break;
    }

    return null;
  }

  @Override
  protected void done() {
    // This method is executed in the EDT
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

  private void drawResults() {
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

  private Map<String, Trial> getControlTrials(String folder) {
    FileManager fm = new FileManager();
    List<String> subFolders = fm.getSubfolders(folder);
    String controlFolder = "";
    for (int i = 0; i < subFolders.size(); i++) {
      String tempName = subFolders.get(i).toLowerCase();
      tempName = fm.getFilename(tempName);
      if (tempName.equals("control")) {
        controlFolder = subFolders.get(i);
        break;
      }
    }
    List<String> controlFiles = fm.getFiles(controlFolder);
    Map<String, Trial> cTrials = new HashMap<String, Trial>();
    TrialManager tm = new TrialManager();
    for (int i = 0; i < controlFiles.size(); i++) {
      String file = controlFiles.get(i);
      trial = tm.getTrialFromAVI(file);
      cTrials.put(trial.type + "-_-" + trial.ID, trial);
    }
    return cTrials;
  }

  /**
   * @param theTracks
   * @return
   */
  private List<Double> getListOfAngles(SList theTracks) {
    List<Double> instAngles = new ArrayList<Double>();
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List track = (ArrayList) iT.next();
      int nPoints = track.size();
      for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
        Spermatozoon oldSpermatozoon = (Spermatozoon) track.get(j);
        Spermatozoon newSpermatozoon = (Spermatozoon) track.get(j + Params.angleDelta);
        float diffX = newSpermatozoon.x - oldSpermatozoon.x;
        float diffY = newSpermatozoon.y - oldSpermatozoon.y;
        double angle = (360 + Math.atan2(diffY, diffX) * 180 / Math.PI) % (360); // Absolute angle
        instAngles.add(angle);
      }
    }
    return instAngles;
  }

  private double[] getOddsValues(SList tracks){
    
    double[] values = new double[] { 0.0, 0.0 };//[0]-upgradient displacements;[1]-displacements to other directionality
    int count = 0;
    int index = 0;
    while ((count < Params.MAXINSTANGLES) && (index < tracks.size())) {
      int[] countInstDirections = countInstantDisplacements((List) tracks.get(index));
      count += countInstDirections[0] + countInstDirections[1];
      values[0] += (double) countInstDirections[0]; // number of  instantaneous angles in the positive direction
      values[1] += (double) (countInstDirections[0] + countInstDirections[1]);
      index++;
    }
    return values;
  }

  private List<String> getTestFolders(String folder) {
    FileManager fm = new FileManager();
    List<String> testFolders = fm.getSubfolders(folder);
    for (int i = 0; i < testFolders.size(); i++) {
      String tempName = testFolders.get(i).toLowerCase();
      tempName = fm.getFilename(tempName);
      if (tempName.equals("control")) {
        testFolders.remove(i);
      }
    }
    return testFolders;
  }

  private Trial getTestTrial(String id, Map<String, Trial> tests) {
    for (String k : tests.keySet()) {
      Trial trial = (Trial) tests.get(k);
      if (trial.ID.equalsIgnoreCase(id))
        return trial;
    }
    return null;
  }

  private Map<String, Trial> getTestTrials(List<String> tests) {
    // Extract Trials
    TrialManager tm = new TrialManager();
    Map<String, Trial> tTrials = new HashMap<String, Trial>();
    for (int i = 0; i < tests.size(); i++) {
      String file = tests.get(i);
      trial = tm.getTrialFromAVI(file);
      tTrials.put(trial.type + "-_-" + trial.ID, trial);
    }
    return tTrials;
  }

  private ResultsTable indexesAnalysis(Map<String, Trial> controls, Map<String, Trial> tests) {

    Set<String> ckeySet = controls.keySet();
    Set<String> tkeySet = tests.keySet();
    ResultsTable rt = new ResultsTable();
    for (String k : ckeySet) {
      Trial trial = (Trial) controls.get(k);
      float chIdx = calculateChIndex(trial.tracks);
      float slIdx = calculateSLIndex(trial.tracks);
      setIndexesResults(rt, trial, chIdx, slIdx);
    }
    for (String k : tkeySet) {
      Trial trial = (Trial) tests.get(k);
      float chIdx = calculateChIndex(trial.tracks);
      float slIdx = calculateSLIndex(trial.tracks);
      setIndexesResults(rt, trial, chIdx, slIdx);
    }
    return rt;
  }

  /**
   * @param controlKeys
   * @param trials
   * @return
   */
  private SList mergeControlTracks(Map<String, Trial> controls) {

    SList tracks = new SList();
    for (String k : controls.keySet()) {
      Trial trial = (Trial) controls.get(k);
      tracks.addAll(trial.tracks);
    }
    return tracks;
  }

  /**
   * @param trials
   */
  private int minSampleSize(Map<String, Trial> trials) {

    int minimum = 999999999;
    for (String k : trials.keySet()) {
      Trial t = (Trial) trials.get(k);
      int[] instantAngles = countAngles(t.tracks);
      int sampleSize = instantAngles[0] + instantAngles[1];
      if (sampleSize < minimum) {
        minimum = sampleSize;
      }
    }
    return minimum;
  }

  /**
   * @param control
   * @param condition
   * @param trials
   * @return
   */
  private double or(Trial control, Trial test) {

    SList controlTracks = control.tracks;
    SList conditionTracks = test.tracks;    
    double[] numeratorValues = getOddsValues(conditionTracks);// Calculate numerator's odds value
    double[] denominatorValues = getOddsValues(controlTracks);// Calculate denominator's odds value
    double numeratorRatio = numeratorValues[0] / numeratorValues[1];
    double denominatorRatio = denominatorValues[0] / denominatorValues[1];
    double oddsRatio = numeratorRatio / denominatorRatio;
   
    return oddsRatio;
  }
  
  /**
   * @param controlTracks
   * @return
   */
  private double orThreshold(Map<String, Trial> controls) {

    SList controlTracks = mergeControlTracks(controls);
    List<Double> oRs = new ArrayList<Double>();
    for (int i = 0; i < Params.NUMSAMPLES; i++) {
      IJ.showProgress((double) i / Params.NUMSAMPLES);
      IJ.showStatus("Calculating Control Threshold. Shuffle " + i);
      Collections.shuffle(controlTracks);
      double[] numeratorValues = getOddsValues(controlTracks);// Calculate numerator's odds value
      Collections.shuffle(controlTracks);
      double[] denominatorValues = getOddsValues(controlTracks);// Calculate denominator's odds value
      double numeratorRatio = numeratorValues[0] / numeratorValues[1];
      double denominatorRatio = denominatorValues[0] / denominatorValues[1];
      double oddsRatio = numeratorRatio / denominatorRatio;
      oRs.add(oddsRatio);
//      IJ.log(""+oddsRatio);
    }
    Collections.sort(oRs);
    return oRs.get((int) (Params.NUMSAMPLES * 0.95));
  }

  private double relativeAngle(Spermatozoon oldSpermatozoon, Spermatozoon newSpermatozoon){ //With gradient direction
    double angleDirection = (2 * Math.PI + Params.angleDirection * Math.PI / 180) % (2 * Math.PI);
    float diffX = newSpermatozoon.x - oldSpermatozoon.x;
    float diffY = newSpermatozoon.y - oldSpermatozoon.y;
    double angle = (2 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute angle
    angle = (2 * Math.PI + angle - angleDirection) % (2 * Math.PI); // Relative angle between interval [0,2*Pi]
    if (angle > Math.PI) {
      angle = -(2 * Math.PI - angle);
    }
    return angle; //Between [-PI,PI]
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
        if (analysisSelection == CHINDEX) {
          analysis = TypeOfAnalysis.INDEXESSIMULATIONS;
        } else if (analysisSelection == BOOTSTRAPPING) {
          analysis = TypeOfAnalysis.BOOTSTRAPPINGSIMULATIONS;
        }
      }
    }
  }

  /**
   * @param rt
   * @param or
   * @param th
   * @param ID
   * @param source
   */
  private void setBootstrappingResults(ResultsTable rt, double or, double th, Trial trial) {
    rt.incrementCounter();
    rt.addValue("ID", trial.ID);
    rt.addValue("OR", or);
    rt.addValue("Threshold", th);
    if (or > (th)) {
      rt.addValue("Result", "POSITIVE");
    } else {
      rt.addValue("Result", "-");
    }
    rt.addValue("Type", trial.type);
    rt.addValue("Source", trial.source);
  }

  /**
   * @param rt
   * @param filename
   * @param chIdx
   * @param slIdx
   * @param nTracks
   */
  private void setIndexesResults(ResultsTable rt, Trial trial, float chIdx, float slIdx) {
    int nTracks = trial.tracks.size();
    rt.incrementCounter();
    rt.addValue("nTracks", nTracks);
    rt.addValue("Ch-Index", chIdx);
    rt.addValue("Sl-Index", slIdx);
    rt.addValue("Type", trial.type);
    rt.addValue("Direction (Degrees)", Params.angleDirection);
    rt.addValue("ArcChemotaxis (Degrees)", Params.angleAmplitude);
    rt.addValue("ID", trial.ID);
    rt.addValue("Source", trial.source);
  }
}
