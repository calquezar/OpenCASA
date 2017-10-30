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
import data.SerializableList;
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
 * This class implements all the functions related to chemotaxis analysis.
 * 
 * @author Carlos Alquezar
 */
public class Chemotaxis extends SwingWorker<Boolean, String> {

  private enum TypeOfAnalysis {
    BOOTSTRAPPING, BOOTSTRAPPINGSIMULATIONS, INDEXESDIRECTORY, INDEXESFILE, INDEXESSIMULATIONS, NONE
  }

  private TypeOfAnalysis analysis = TypeOfAnalysis.NONE;

  /**
   * This method runs the correct analysis looking at the analysis variable set
   * by the user.
   * 
   * @param controls
   *          - control trials
   * @param tests
   *          - test trials that are going to be compare with control trials
   * @return - ResultsTable with the results of the analysis
   */
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

  /**
   * This method asks user for the main folder that contains the data that is
   * going to be analysed. The content of this folder has to be one folder named
   * "control" with the control videos, and one or more folders corresponding
   * with each condition that user wants to compare with reference controls.
   */
  private void analyseDirectory() {
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    Map<String, Trial> cTrials = getControlTrials(folder);
    List<String> testFolders = getTestFolders(folder);
    for (String f : testFolders) {
      List<String> tests = fm.getFiles(f);
      Map<String, Trial> tTrials = getTrials(tests);
      ResultsTable rt = analyseCondition(cTrials, tTrials);
      String condition = fm.getFilename(f);
      rt.show(condition);
    }
  }

  /**
   * This method asks user for the file that is going to be analysed, extract
   * the corresponding trial and show results.
   */
  private void analyseFile() {
    FileManager fm = new FileManager();
    String file = fm.selectFile();
    TrialManager tm = new TrialManager();
    Trial trial = tm.getTrialFromAVI(file);
    drawResults(trial);
  }

  /**
   * This method asks user which simulation parameters have to be used in the
   * simulations, generate the corresponding trials, analyse them and show
   * results
   */
  private void analyseSimulations() {
    GenericDialog gd = new GenericDialog("Set Simulation parameters");
    gd.addNumericField("Beta", 0, 2);
    gd.addNumericField("Responsiveness (%)", 50, 2);
    gd.addNumericField("Number of simulations", 50, 0);
    gd.showDialog();
    if (gd.wasCanceled())
      return;
    final double BETA = gd.getNextNumber();
    final double RESPONSIVENESS = gd.getNextNumber() / 100;// value must be
                                                           // between [0,1]
    final int TOTALSIMULATIONS = (int) gd.getNextNumber();
    TrialManager tm = new TrialManager();
    Map<String, Trial> controls = tm.simulateTrials(0, 0, TOTALSIMULATIONS);
    Map<String, Trial> tests = tm.simulateTrials(BETA, RESPONSIVENESS, TOTALSIMULATIONS);
    ResultsTable rt = analyseCondition(controls, tests);
    rt.show("Results from Simulation (Beta: " + BETA + ",Responsiveness: " + RESPONSIVENESS + ")");
  }

  /**
   * This method applies the bootstrapping analysis for the given set of trials.
   * 
   * @param controls
   *          - control trials
   * @param tests
   *          - test trials that are going to be compare with control trials
   * @return ResultsTable with the bootstrapping analysis
   */
  private ResultsTable bootstrappingAnalysis(Map<String, Trial> controls, Map<String, Trial> tests) {

    final int cMin = minSampleSize(controls);
    final int tMin = minSampleSize(tests);
    Params.MAXINSTANGLES = Math.min(cMin, tMin);
    // Calculating OR threshold via resampling
    double thControl = orThreshold(controls);
    ResultsTable rt = new ResultsTable();
    for (String cKey : controls.keySet()) {
      Trial cTrial = (Trial) controls.get(cKey);
      Trial tTrial = findTrial(cTrial.ID, tests);
      if (tTrial != null) {
        double or = or(cTrial, tTrial);
        setBootstrappingResults(rt, or, thControl, tTrial);
      }
    }
    return rt;
  }

  /******************************************************/
  /**
   * This method calculates the CH-index for the given set of trajectories.
   * 
   * @param theTracks
   *          - 2D-ArrayList with all the tracks
   * @return Ch-Index
   */
  private float calculateChIndex(List<List<Spermatozoon>> theTracks) {
    int trackNr = 0; // Number of tracks
    int nTracks = theTracks.size();
    int[] displacements = { 0, 0 };
    for (List<Spermatozoon> track : theTracks) {
      IJ.showProgress((double) trackNr / nTracks);
      IJ.showStatus("Calculating Ch-Index...");
      trackNr++;
      int[] instD = countInstantDisplacements(track);
      displacements[0] += instD[0];
      displacements[1] += instD[1];
    }
    float nUpGradient = displacements[0]; // Number of displacements in the
                                          // gradient direction
    float nOtherDirs = displacements[1]; // Number of displacements in other
                                         // direction
    // IJ.log("nUpGradient: "+nUpGradient+"; nOtherDirs: "+nOtherDirs);
    float chIdx = 0;
    if ((nUpGradient + nOtherDirs) > 0) {
      chIdx = nUpGradient / (nUpGradient + nOtherDirs); // (nUpGradient+nOtherDirs)
                                                        // = Total number of
                                                        // shifts
    } else {
      chIdx = -1;
    }
    return chIdx; // return index between [0,1]
  }

  /******************************************************/
  /**
   * This method calculates the SL-index for the given set of trajectories.
   * 
   * @param theTracks
   *          - 2D-ArrayList that stores all the tracks
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
      double angle = relativeAngle(first, last); // Between [-PI,PI]
      // Check if the angle is upGradient or not
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
   * This method calculates the histogram for the given set of angles.
   * 
   * @param angles
   *          - The angles (in degrees).
   * @param n
   *          - Total number of bins for the histogram.
   * @return Array of the number of elements for each bin.
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
   * This method counts, for the given set of trajectories, how many angles go
   * in the gradient direction and how many in the opposite (or other)
   * direction, depending on the value of the parameter "Compare Opposite
   * Directions" set by the user on the Settings Window.
   * 
   * @param theTracks
   *          - The array with all trajectories
   * @return An array containing the total count of angles ([0] - upgradient:
   *         [1] - other directions).
   */
  private int[] countAngles(SerializableList theTracks) {
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
   * This method counts, for the given trajectory, how many angles go in the
   * gradient direction and how many in the opposite (or other) direction,
   * depending on the value of the parameter "Compare Opposite Directions" set
   * by the user on the Settings Window.
   * 
   * @param track
   *          - The single trajectory to analyse
   * @return An array containing the total count of angles ([0] - upgradient:
   *         [1] - other directions).
   */
  private int[] countInstantDisplacements(List<Spermatozoon> track) {
    int nUpGradient = 0;
    int nOtherDir = 0;
    int nPoints = track.size();
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
    for (int j = 0; j < (nPoints - Params.angleDelta); j++) {
      Spermatozoon oldSpermatozoon = (Spermatozoon) track.get(j);
      Spermatozoon newSpermatozoon = (Spermatozoon) track.get(j + Params.angleDelta);
      double angle = relativeAngle(oldSpermatozoon, newSpermatozoon);// Between
                                                                     // interval
                                                                     // [-PI,PI]
      if (Params.compareOppositeDirections) {// We only take into account angles
                                             // in the gradient and opposite
                                             // direction
        if (Math.abs(angle) < angleChemotaxis) {
          nUpGradient++;
        } else if (Math.abs(angle) > (Math.PI - angleChemotaxis)) {
          nOtherDir++;
        }
      } else {// We take into account all angles in all directions
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

  /**
   * This method is inherit from SwingWorker class and it is the starting point
   * after the execute() method is called.
   */
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

  /**
   * This method is executed at the end of the worker thread in the Event
   * Dispatch Thread.
   */
  @Override
  protected void done() {
    // switch (analysis) {
    // case INDEXESFILE:
    // break;
    // case INDEXESDIRECTORY:
    // break;
    // case BOOTSTRAPPING:
    // break;
    // case INDEXESSIMULATIONS:
    // break;
    // case BOOTSTRAPPINGSIMULATIONS:
    // break;
    // }
  }

  /**
   * This method draws a chemotactic cone and a rose diagram for a single trial
   * analysis.
   * 
   * @param trial
   */
  private void drawResults(Trial trial) {
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
   * This method search in the given set of trials for the one with the given
   * ID.
   * 
   * @param id
   *          - Identifier of the trial to find.
   * @param trials
   *          - Set of trials
   * @return the trial found or null otherwise
   */
  private Trial findTrial(String id, Map<String, Trial> trials) {
    for (String k : trials.keySet()) {
      Trial trial = (Trial) trials.get(k);
      if (trial.ID.equalsIgnoreCase(id))
        return trial;
    }
    return null;
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
      Trial trial = tm.getTrialFromAVI(file);
      if (trial != null)
        cTrials.put(trial.type + "-_-" + trial.ID, trial);
    }
    return cTrials;
  }

  /**
   * This method calculates all instantaneous displacements for a given set of
   * trajectories.
   * 
   * @param theTracks
   *          - Array with all trajectories.
   * @return List of angles
   */
  private List<Double> getListOfAngles(SerializableList theTracks) {
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

  /**
   * This method counts, for the given set of trajectories, how many angles go
   * in the gradient direction and how many in the opposite (or other)
   * direction, depending on the value of the parameter "Compare Opposite
   * Directions" set by the user on the Settings Window. The method stops when
   * all the trajectories have been analysed or the maximum number of angles to
   * analyse is reached.
   * 
   * @param tracks
   *          - All the trajectories to analyse
   * @return An array containing the total count of angles ([0] - upgradient:
   *         [1] - other directions).
   */
  private double[] getOddsValues(SerializableList tracks) {

    double[] values = new double[] { 0.0, 0.0 };// [0]-upgradient
                                                // displacements;[1]-displacements
                                                // to other directionality
    int count = 0;
    int index = 0;
    while ((count < Params.MAXINSTANGLES) && (index < tracks.size())) {
      int[] countInstDirections = countInstantDisplacements((List) tracks.get(index));
      count += countInstDirections[0] + countInstDirections[1];
      values[0] += (double) countInstDirections[0]; // number of instantaneous
                                                    // angles in the positive
                                                    // direction
      values[1] += (double) (countInstDirections[0] + countInstDirections[1]);
      index++;
    }
    return values;
  }

  /**
   * For a given folder, this method returns a list of all subfolders contained
   * in it, that are not named as "control".
   * 
   * @param folder
   * @return List of subfolders
   */
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

  /**
   * This method returns all trials extracted from the given set of AVI files.
   * 
   * @param filenames
   *          List of avi filenames to be analysed.
   * @return All extracted trials
   */
  private Map<String, Trial> getTrials(List<String> filenames) {
    // Extract Trials
    TrialManager tm = new TrialManager();
    Map<String, Trial> trials = new HashMap<String, Trial>();
    for (int i = 0; i < filenames.size(); i++) {
      String file = filenames.get(i);
      Trial trial = tm.getTrialFromAVI(file);
      if (trial != null)
        trials.put(trial.type + "-_-" + trial.ID, trial); // Expression "-_-" is
                                                          // just a separator
    }
    return trials;
  }

  /**
   * This method calculates Ch-Index and SL-index for the given set of trials
   * 
   * @param controls
   *          - control trials
   * @param tests
   *          - tests trials
   * @return ResultsTable with all indexes
   */
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
   * This method join all tracks of the given set of trials, into one single
   * array.
   * 
   * @param trials
   *          - set of trials
   * @return List with all tracks
   */
  private SerializableList mergeTracks(Map<String, Trial> trials) {

    SerializableList tracks = new SerializableList();
    for (String k : trials.keySet()) {
      Trial trial = (Trial) trials.get(k);
      tracks.addAll(trial.tracks);
    }
    return tracks;
  }

  /**
   * This method looks for which trial has the minimum number of instantaneous
   * displacements and return this number.
   * 
   * @param trials
   *          - set of trials
   * @return the value of the minimum number of instantaneous displacements for
   *         one trial in the given set of trials.
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
   * This method calculates odds ratio for a given pair control-test
   * 
   * @param control
   *          - control trial
   * @param test
   *          - test trial to be analysed
   * @return Odds Ratio
   */
  private double or(Trial control, Trial test) {
    SerializableList controlTracks = control.tracks;
    SerializableList conditionTracks = test.tracks;
    double[] numeratorValues = getOddsValues(conditionTracks);// Calculate
                                                              // numerator's
                                                              // odds value
    double[] denominatorValues = getOddsValues(controlTracks);// Calculate
                                                              // denominator's
                                                              // odds value
    double numeratorRatio = numeratorValues[0] / numeratorValues[1];
    double denominatorRatio = denominatorValues[0] / denominatorValues[1];
    double oddsRatio = numeratorRatio / denominatorRatio;
    return oddsRatio;
  }

  /**
   * This method calculates the control threshold used for bootstrapping
   * analysis.
   * 
   * @param controls
   *          - Set of control trials
   * @return Threshold for bootstrapping analysis
   */
  private double orThreshold(Map<String, Trial> controls) {

    SerializableList controlTracks = mergeTracks(controls);
    List<Double> oRs = new ArrayList<Double>();
    for (int i = 0; i < Params.NUMSAMPLES; i++) {
      IJ.showProgress((double) i / Params.NUMSAMPLES);
      IJ.showStatus("Calculating Control Threshold. Shuffle " + i);
      Collections.shuffle(controlTracks);
      double[] numeratorValues = getOddsValues(controlTracks);// Calculate
                                                              // numerator's
                                                              // odds value
      Collections.shuffle(controlTracks);
      double[] denominatorValues = getOddsValues(controlTracks);// Calculate
                                                                // denominator's
                                                                // odds value
      double numeratorRatio = numeratorValues[0] / numeratorValues[1];
      double denominatorRatio = denominatorValues[0] / denominatorValues[1];
      double oddsRatio = numeratorRatio / denominatorRatio;
      oRs.add(oddsRatio);
      // IJ.log(""+oddsRatio);
    }
    Collections.sort(oRs);
    return oRs.get((int) (Params.NUMSAMPLES * 0.95));
  }

  /**
   * This method calculates the relative angle between the given displacement
   * made by a cell, and the gradient direction.
   * 
   * @param previous
   *          - previous coordinates of the cell before displacement
   * @param next
   *          - next coordinates of the cell after displacement
   * @return relative angle in the interval [-PI,PI]
   */
  private double relativeAngle(Spermatozoon previous, Spermatozoon next) { // With
                                                                           // gradient
                                                                           // direction
    double angleDirection = (2 * Math.PI + Params.angleDirection * Math.PI / 180) % (2 * Math.PI);
    float diffX = next.x - previous.x;
    float diffY = next.y - previous.y;
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
    return angle; // Between [-PI,PI]
  }

  /**
   * This method opens a set of dialogs to ask the user which analysis has to be
   * carried on.
   */
  public void selectAnalysis() {
    // Ask if user wants to analyze a file or directory
    Object[] options = { "File", "Directory", " Multiple Simulations" };
    String question = "What do you want to analyze?";
    String title = "Choose one analysis...";
    final int FILE = 0;
    final int DIR = 1;
    final int SIMULATION = 2;
    Utils utils = new Utils();
    int sourceSelection = utils.analysisSelectionDialog(options, question, title);
    if (sourceSelection < 0) {
      return;
    } else if (sourceSelection == FILE) {// File
      analysis = TypeOfAnalysis.INDEXESFILE; // It's not possible to carry on
                                             // bootstrapping analysis in a
                                             // single file
    } else if (sourceSelection == DIR || sourceSelection == SIMULATION) {// Directory
                                                                         // or
                                                                         // simulations
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
   * This method adds to the given results table, a new row with the given
   * parameters.
   * 
   * @param rt
   *          - ResultsTable to be appended.
   * @param or
   *          - OddsRatio.
   * @param th
   *          - Threshold used to calculate the given oddsRatio.
   * @param trial
   *          - Trial analysed.
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
    if (Params.compareOppositeDirections)
      rt.addValue("COD", "YES");
    else
      rt.addValue("COD", "NO");
    if (!Params.male.isEmpty())
      rt.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      rt.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      rt.addValue("Generic Field", Params.genericField);
  }

  /**
   * This method adds to the given results table, a new row with the given
   * parameters.
   * 
   * @param rt
   *          - ResultsTable to be appended.
   * @param trial
   *          - Trial analysed.
   * @param chIdx
   *          - Ch-Index of the given trial.
   * @param slIdx
   *          - SL-Index of the given trial.
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
    if (Params.compareOppositeDirections)
      rt.addValue("COD", "YES");
    else
      rt.addValue("COD", "NO");
    if (!Params.male.isEmpty())
      rt.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      rt.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      rt.addValue("Generic Field", Params.genericField);
  }
}
