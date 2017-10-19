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
import ij.measure.ResultsTable;

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

  private ResultsTable analyseCondition (Map<String, Trial> controls, Map<String, Trial> tests){

    ResultsTable rt = new ResultsTable();
    switch (analysis) {
      case INDEXESDIRECTORY:
      case INDEXESSIMULATIONS:
        rt = indexesAnalysis(controls,tests);
        break;
      case BOOTSTRAPPING:
      case BOOTSTRAPPINGSIMULATIONS:
        rt = bootstrappingAnalysis(controls,tests);
        break;
    }
    return rt;
  }
  
  /**
   * @param trials
   * @return
   */
  public ResultsTable bootstrappingAnalysis(Map<String, Trial> controls, Map<String, Trial> tests) {

    int cMin = minSampleSize(controls);
    int tMin = minSampleSize(tests);
    Params.MAXINSTANGLES = Math.min(cMin, tMin);    
    // Calculating OR threshold via subsampling
    double thControl = ORThreshold(controls);
    ResultsTable rt = new ResultsTable();    
    for (String cKey : controls.keySet()) {
      Trial cTrial = (Trial) controls.get(cKey);
      Trial tTrial = getTestTrial(cTrial.ID,tests);
      if(tTrial!=null){
        double OR = OR(cTrial, tTrial);
        setBootstrappingResults(rt, OR, thControl, tTrial);
      }
    }
    return rt;
  }  
  
  private void analyseDirectory(){
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    Map<String, Trial> cTrials = getControlTrials(folder);    
    List<String> testFolders = getTestFolders(folder);
    for( String f : testFolders){
      List<String> tests = fm.getFiles(f);
      Map<String, Trial> tTrials = getTestTrials(tests);
      ResultsTable rt = analyseCondition(cTrials,tTrials);
      String condition = fm.getFilename(f);
      rt.show(condition);
    }
  }
  
  private void analyseFile(){
    FileManager fm = new FileManager();
    String file = fm.selectFile();
    TrialManager tm = new TrialManager();
    trial = tm.getTrialFromAVI(file);
  }
  

  private void analyseSimulations(){
    //generateTrials
//    for each condition?
    //analyseCondition(cTrials,tests)
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
      case BOOTSTRAPPINGSIMULATIONS:
        analyseSimulations();
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
  
  private Map<String, Trial> getControlTrials(String folder){
    FileManager fm = new FileManager();
    List<String> subFolders = fm.getSubfolders(folder);
    String controlFolder = "";
    for ( int i = 0;  i < subFolders.size(); i++){
      String tempName = subFolders.get(i).toLowerCase();
      tempName = fm.getFilename(tempName);
      if(tempName.equals("control")){
        controlFolder = subFolders.get(i);
        break;
      }
    }
    List<String> controlFiles = fm.getFiles(controlFolder);
    Map<String, Trial> cTrials = new HashMap<String, Trial>();
    TrialManager tm = new TrialManager();
    for (int i = 0; i<controlFiles.size();i++){
      String file = controlFiles.get(i);
      trial = tm.getTrialFromAVI(file);
      cTrials.put(trial.type+"-_-"+trial.ID, trial);
    }
    return cTrials;
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
  private List<String> getTestFolders(String folder){
    FileManager fm = new FileManager();
    List<String> testFolders = fm.getSubfolders(folder);
    for ( int i = 0;  i < testFolders.size(); i++){
      String tempName = testFolders.get(i).toLowerCase();
      tempName = fm.getFilename(tempName);
      if(tempName.equals("control")){
        testFolders.remove(i);
      }
    }
    return testFolders;
  }
  
  private Map<String, Trial> getTestTrials( List<String> tests){
    //Extract Trials 
    TrialManager tm = new TrialManager();
    Map<String, Trial> tTrials = new HashMap<String, Trial>();
    for (int i = 0; i<tests.size();i++){
      String file = tests.get(i);
      trial = tm.getTrialFromAVI(file);
      tTrials.put(trial.type+"-_-"+trial.ID, trial);
    }
    return tTrials;
  }

  private ResultsTable indexesAnalysis(Map<String, Trial> controls, Map<String, Trial> tests){
    
    Set<String> ckeySet = controls.keySet();
    Set<String> tkeySet = tests.keySet();   
    ResultsTable rt = new ResultsTable();
    for(String k: ckeySet){
      Trial trial = (Trial) controls.get(k);
      float chIdx = calculateChIndex(trial.tracks);
      float slIdx = calculateSLIndex(trial.tracks);
      setIndexesResults(rt,trial,chIdx,slIdx);
    }
    for(String k: tkeySet){
      Trial trial = (Trial) tests.get(k);
      float chIdx = calculateChIndex(trial.tracks);
      float slIdx = calculateSLIndex(trial.tracks);
      setIndexesResults(rt,trial,chIdx,slIdx);
    }
    return rt;
  }
  /**
   * @param trials
   */
  public int minSampleSize(Map<String, Trial> trials) {

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
  
  private Trial getTestTrial(String ID, Map<String, Trial> tests){
    for (String k : tests.keySet()) {
      Trial trial = (Trial)tests.get(k);
      if(trial.ID.equalsIgnoreCase(ID))
        return trial;
    }
    return null;
  }
  /**
   * @param control
   * @param condition
   * @param trials
   * @return
   */
  public double OR(Trial control, Trial test) {

    SList controlTracks = control.tracks;
    SList conditionTracks = test.tracks;

    double[] numeratorValues = new double[] { 0.0, 0.0 }; // [0] - positive
                                                          // directions; [1] -
                                                          // negative
                                                          // directions
    double[] denominatorValues = new double[] { 0.0, 0.0 }; // [0] - positive
                                                            // directions; [1]
                                                            // - negative
                                                            // directions

    int count = 0, index = 0;
    // Control Ratio
    while ((count < Params.MAXINSTANGLES) && (index < controlTracks.size())) {
      // while(index<controlTracks.size()){
      int[] countInstDirections = countInstantDirections((List) controlTracks.get(index));
      denominatorValues[0] += (double) countInstDirections[0]; // number of
                                                               // instantaneous
                                                               // angles in
                                                               // the
                                                               // positive
                                                               // direction
      denominatorValues[1] += (countInstDirections[0] + countInstDirections[1]);
      count += countInstDirections[0] + countInstDirections[1];
      index++;
    }

    // System.out.println("Count denominator angles: "+denominatorValues[1]);
    // System.out.println("conditionTracks.size(): "+conditionTracks.size());

    // java.util.Collections.shuffle(conditionTracks);

    // Condition Ratio
    count = 0;
    index = 0;
    while ((count < Params.MAXINSTANGLES) && (index < conditionTracks.size())) {
      // while(index<conditionTracks.size()){
      int[] countInstDirections = countInstantDirections((List) conditionTracks.get(index));
      numeratorValues[0] += (double) countInstDirections[0]; // number of
                                                             // instantaneous
                                                             // angles in the
                                                             // positive
                                                             // direction
      numeratorValues[1] += (double) (countInstDirections[0] + countInstDirections[1]);
      count += countInstDirections[0] + countInstDirections[1];
      index++;
    }
    // System.out.println("Count numerator angles: "+numeratorValues[1]);

    double numeratorRatio = numeratorValues[0] / numeratorValues[1];
    double denominatorRatio = denominatorValues[0] / denominatorValues[1];
    double OddsRatio = numeratorRatio / denominatorRatio;

    // System.out.println("OR: "+OddsRatio+" ;nAngles:
    // "+(numeratorValues[0]+numeratorValues[1]));
    return OddsRatio;
  }  
  
  /**
   * @param rt
   * @param OR
   * @param th
   * @param ID
   * @param source
   */
  public void setBootstrappingResults(ResultsTable rt, double OR, double th, Trial trial) {
    rt.incrementCounter();
    rt.addValue("ID", trial.ID);
    rt.addValue("OR", OR);
    rt.addValue("Threshold", th);
    if (OR > (th)) {
      rt.addValue("Result", "POSITIVE");
    } else {
      rt.addValue("Result", "-");
    }
    rt.addValue("Type", trial.type);
    rt.addValue("Filename", trial.source);
  }
  
  /**
   * @param controlTracks
   * @return
   */
  private double ORThreshold(Map<String, Trial> controls) {

    SList controlTracks = mergeControlTracks(controls);
    List<Double> ORs = new ArrayList<Double>();

    for (int i = 0; i < Params.NUMSAMPLES; i++) {
      IJ.showProgress((double) i / Params.NUMSAMPLES);
      IJ.showStatus("Calculating Control Threshold. Shuffle " + i);
      double[] numeratorValues = new double[] { 0.0, 0.0 }; // [0] - positive
                                                            // directions;
                                                            // [1] - negative
                                                            // directions
      double[] denominatorValues = new double[] { 0.0, 0.0 }; // [0] -
                                                              // positive
                                                              // directions;
                                                              // [1] -
                                                              // negative
                                                              // directions
      
      java.util.Collections.shuffle(controlTracks);
      // Calculate numerator's odds value
      int count = 0, index = 0;
      while ((count < Params.MAXINSTANGLES) && (index < controlTracks.size())) {
        int[] countInstDirections = countInstantDirections((List) controlTracks.get(index));
        count += countInstDirections[0] + countInstDirections[1];
        numeratorValues[0] += (double) countInstDirections[0]; // number of
                                                               // instantaneous
                                                               // angles in
                                                               // the
                                                               // positive
                                                               // direction
        numeratorValues[1] += (double) (countInstDirections[0] + countInstDirections[1]);
        index++;
      }
      java.util.Collections.shuffle(controlTracks);
      // Calculate denominator's odds value
      count = 0;
      index = 0;
      while ((count < Params.MAXINSTANGLES) && (index < controlTracks.size())) {
        int[] countInstDirections = countInstantDirections((List) controlTracks.get(index));
        denominatorValues[0] += (double) countInstDirections[0]; // number
                                                                 // of
                                                                 // instantaneous
                                                                 // angles
                                                                 // in the
                                                                 // positive
                                                                 // direction
        denominatorValues[1] += (double) (countInstDirections[0] + countInstDirections[1]); // number
                                                                                            // of
                                                                                            // instantaneous
                                                                                            // angles
                                                                                            // in
                                                                                            // the
                                                                                            // opposite
                                                                                            // direction
        count += countInstDirections[0] + countInstDirections[1];
        index++;
      }
      double numeratorRatio = numeratorValues[0] / numeratorValues[1];
      double denominatorRatio = denominatorValues[0] / denominatorValues[1];
      double OddsRatio = numeratorRatio / denominatorRatio;
      ORs.add(OddsRatio);
      // IJ.log(""+OddsRatio);
      // System.out.println("OddsRatio: "+OddsRatio);
    }
    Collections.sort(ORs);
    // System.out.println(ORs.toString());
    // System.out.println("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
    // System.out.println("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
    // System.out.println("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
    // System.out.println("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
    // System.out.println("p97: "+ORs.get((int) (NUMSAMPLES*0.97)));
    // System.out.println("p99: "+ORs.get((int) (NUMSAMPLES*0.99)));
    // IJ.log("p25: "+ORs.get((int) (NUMSAMPLES*0.25)));
    // IJ.log("p50: "+ORs.get((int) (NUMSAMPLES*0.5)));
    // IJ.log("p75: "+ORs.get((int) (NUMSAMPLES*0.75)));
    // IJ.log("p95: "+ORs.get((int) (NUMSAMPLES*0.95)));
    return ORs.get((int) (Params.NUMSAMPLES * 0.95));
  }    
  /**
   * @param theTracks
   * @return
   */
  public int[] countAngles(SList theTracks) {
    int[] angles = { 0, 0 };
    for (ListIterator iT = theTracks.listIterator(); iT.hasNext();) {
      List aTrack = (ArrayList) iT.next();
      int[] instantAngles = countInstantDirections(aTrack);
      angles[0] += instantAngles[0];
      angles[1] += instantAngles[1];
    }
    return angles;
  }
  /**
   * @param track
   * @return
   */
  public int[] countInstantDirections(List track) {
    int nPos = 0;
    int nNeg = 0;
    double angleDirection = (2 * Math.PI + Params.angleDirection * Math.PI / 180) % (2 * Math.PI);
    double angleChemotaxis = (2 * Math.PI + (Params.angleAmplitude / 2) * Math.PI / 180) % (2 * Math.PI);
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
      if (angle > Math.PI) {
        angle = -(2 * Math.PI - angle);
      }
      if (Math.abs(angle) < angleChemotaxis) {
        nPos++;
      } else if (Math.abs(angle) > (Math.PI - angleChemotaxis)) {
        nNeg++;
      }
    }
    int[] results = new int[3];
    results[0] = nPos;
    results[1] = nNeg;
    return results;
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
    rt.addValue("Filename", trial.source);
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
