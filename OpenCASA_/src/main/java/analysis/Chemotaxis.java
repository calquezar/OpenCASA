package analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Params;
import data.PersistentRandomWalker;
import data.SList;
import data.Spermatozoon;
import data.Trial;
import functions.Paint;
import gui.MainWindow;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;

/**
 * 
 * @author Carlos Alquezar
 *
 */
public class Chemotaxis {

  /**
   * 
   */
  private static final Float FLOAT = (Float) null;

  // private Map<String, Trial> trials = new HashMap<String, Trial>();
  /**
   * 
   */
  public Chemotaxis() {
  }

  /**
   * @param trials
   * @return
   */
  public float analyseChDirectory(Map<String, Trial> trials) {

    float maxChIndex = 0;
    float maxSLIndex = 0;
    if (trials == null) {
      return FLOAT;
    }
    Set<String> keySet = trials.keySet();
    List<String> controlList = getKeys(keySet, 'C'); // 'C' = Control
    List<String> chemoList = getKeys(keySet, 'Q'); // 'Q' = Chemotaxis

    ResultsTable rtRatios = new ResultsTable();
    for (Iterator<String> k = controlList.iterator(); k.hasNext();) {
      String key = (String) k.next();
      Trial trial = (Trial) trials.get(key);
      System.out.println("key: " + key);
      float chIdx = calculateChIndex(trial.tracks);
      IJ.log("" + chIdx);
      if (chIdx > maxChIndex) {
        maxChIndex = chIdx;
      }
      float ratioSL = calculateSLIndex(trial.tracks);
      if (ratioSL > maxSLIndex) {
        maxSLIndex = ratioSL;
      }
      setChResults(rtRatios, trial.source, chIdx, ratioSL, trial.tracks.size());
    }

    for (Iterator<String> k = chemoList.iterator(); k.hasNext();) {
      String key = (String) k.next();
      Trial trial = (Trial) trials.get(key);
      System.out.println("key: " + key);
      float chIdx = calculateChIndex(trial.tracks);
      IJ.log("" + chIdx);
      if (chIdx > maxChIndex) {
        maxChIndex = chIdx;
      }
      float ratioSL = calculateSLIndex(trial.tracks);
      if (ratioSL > maxSLIndex) {
        maxSLIndex = ratioSL;
      }
      setChResults(rtRatios, trial.source, chIdx, ratioSL, trial.tracks.size());
    }

    rtRatios.show("Chemotaxis results");
    return maxChIndex;
  }

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
   * 
   */
  public void analyzeFile() {
    Trial trial = VideoAnalyzer.extractTrial("Chemotaxis-File");
    if (trial == null) {
      return;
    }
    // Draw trajectories
    float chIdx = calculateChIndex(trial.tracks);
    float slIdx = calculateSLIndex(trial.tracks);
    Paint.drawChemotaxis(trial.tracks, chIdx, slIdx, trial.width, trial.height, trial.source);
    int[] hist = circularHistogram(getListOfAngles(trial.tracks), 45);
    int radius = trial.width;
    Paint.drawRoseDiagram(hist, radius, chIdx, trial.source);
  }

  /**
   * @param trials
   * @return
   */
  public float bootstrapping(Map<String, Trial> trials) {

    ResultsTable rtRatios = new ResultsTable();
    float positiveSamples = 0;// Percentage of positive samples
    // Calculate minimum sample size
    minSampleSize(trials);
    Set keys = trials.keySet();
    List controlKeys = getKeys(keys, 'C');
    SList controlTracks = mergeControlTracks(controlKeys, trials);
    // Setting maximum number of subsamples used by bootstrapping method
    // Params.NUMSAMPLES=controlKeys.size();
    // Calculating OR threshold via subsampling
    double thControl = ORThreshold(controlTracks);
    for (Iterator k = controlKeys.iterator(); k.hasNext();) {
      String control = (String) k.next();
      List conditionsKeys = getRelatedConditions(keys, control);
      float TOTALSAMPLES = (float) controlKeys.size();
      for (Iterator cond = conditionsKeys.iterator(); cond.hasNext();) {
        String condition = (String) cond.next();
        double OR = OR(control, condition, trials);
        if (OR > thControl) {
          positiveSamples += 1 / TOTALSAMPLES;
        }
        String filename = trials.get(condition).source;
        String ID = trials.get(condition).ID;
        setBootstrappingResults(rtRatios, OR, thControl, ID, filename);
      }
    }
    rtRatios.show("Bootstrapping Results");
    return positiveSamples;
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
        double angle = (4 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute
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
      double angle = (4 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute
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
      double angle = (4 * Math.PI + Math.atan2(diffY, diffX)) % (2 * Math.PI); // Absolute
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
   * @param keySet
   * @param type
   * @return
   */
  public List<String> getKeys(Set<String> keySet, char type) {
    List<String> keyList = new ArrayList<String>();
    for (Iterator<String> k = keySet.iterator(); k.hasNext();) {
      String id = k.next();
      // Key is in format:
      // for chemotaxis: YYYYMMDD-[ID]-Q[hormone+concentration]
      // for control: YYYYMMDD-[ID]-C
      String[] parts = id.split("-");
      if (parts[parts.length - 1].charAt(0) == type) {
        keyList.add(id);
      }
    }
    return keyList;
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

  /**
   * @param keySet
   * @param controlKey
   * @return
   */
  public List getRelatedConditions(Set keySet, String controlKey) {
    List conditionsList = new ArrayList();
    // Key is in format:
    // for chemotaxis: YYYYMMDD-[ID]-Q[hormone+concentration]
    // for control: YYYYMMDD-[ID]-C
    String id = controlKey.substring(0, controlKey.length() - 2);
    id = id + "-Q";
    for (Iterator k = keySet.iterator(); k.hasNext();) {
      String key = (String) k.next();
      if (key.length() >= id.length()) {
        // prefix: YYYYMMDD-[ID]-Q
        String prefix = key.substring(0, id.length());
        if (id.equals(prefix)) {
          conditionsList.add(key);
        }
      }
    }
    return conditionsList;
  }

  /**
   * @param controlKeys
   * @param trials
   * @return
   */
  public SList mergeControlTracks(List controlKeys, Map<String, Trial> trials) {

    SList tracks = new SList();
    for (Iterator k = controlKeys.iterator(); k.hasNext();) {
      String key = (String) k.next();
      Trial trial = (Trial) trials.get(key);
      tracks.addAll(trial.tracks);
    }
    return tracks;
  }

  /**
   * @param trials
   */
  public void minSampleSize(Map<String, Trial> trials) {

    Set keySet = trials.keySet();
    List keys = new ArrayList();
    keys.addAll(keySet);
    int minimum = 999999999;

    for (ListIterator iT = keys.listIterator(); iT.hasNext();) {
      String k1 = (String) iT.next();
      Trial t = (Trial) trials.get(k1);
      int[] instantAngles = countAngles(t.tracks);
      int sampleSize = instantAngles[0] + instantAngles[1];
      if (sampleSize < minimum) {
        minimum = sampleSize;
      }
    }
    Params.MAXINSTANGLES = minimum;
  }

  /**
   * @param control
   * @param condition
   * @param trials
   * @return
   */
  public double OR(String control, String condition, Map<String, Trial> trials) {

    Trial trialControl = (Trial) trials.get(control);
    SList controlTracks = trialControl.tracks;
    Trial trialCondition = (Trial) trials.get(condition);
    SList conditionTracks = trialCondition.tracks;

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
   * @param controlTracks
   * @return
   */
  public double ORThreshold(SList controlTracks) {

    List<Double> ORs = new ArrayList<Double>();
    // final int NUMSAMPLES = 100;

    for (int i = 0; i < Params.NUMSAMPLES; i++) {
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

      System.out.println("Calculating Control Threshold. Shuffle " + i);
      System.out.println("MAX INSTANT ANGLES: " + Params.MAXINSTANGLES);

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
   * @param mw
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void run(MainWindow mw) throws IOException, ClassNotFoundException {
    mw.setVisible(false);
    // Ask if user wants to analyze a file or directory
    Object[] options = { "File", "Directory", "Simulation" };
    String question = "What do you want to analyze?";
    String title = "Choose one analysis...";
    int userSelection1 = analysisSelectionDialog(options, question, title);
    if (userSelection1 < 0) {
      mw.setVisible(true);
      return;
    } else if (userSelection1 == 0) {// File
      analyzeFile();
    } else if (userSelection1 == 1 || userSelection1 == 2) {// Directory
      // Ask user which analysis wants to apply
      Object[] options2 = { "Ch-Index", "Bootstrapping" };
      question = "Which analysis do you want to apply to the data?";
      title = "Choose one analysis...";
      int userSelection2 = analysisSelectionDialog(options2, question, title);
      if (userSelection2 < 0) {
        mw.setVisible(true);
        return;
      }
      // Create trials dictionary
      if (userSelection1 == 1) {
        Map<String, Trial> trials = VideoAnalyzer.extractTrials("Chemotaxis-Directory");//
        // Utils.saveTrials(trials);
        if (trials == null) {
          mw.setVisible(true);
          return;
        }
        // Utils.saveTrials(trials);
        if (userSelection2 == 0) {
          analyseChDirectory(trials);
        } else if (userSelection2 == 1) {
          bootstrapping(trials);
        }
      } else if (userSelection1 == 2) { // Simulations
        int MAXNBETAS = 10;
        int MAXNRESP = 10;
        int MAXSIMULATIONS = 20;
        double[][] results = new double[MAXNBETAS][MAXNRESP];
        double maxBeta = 2;
        final boolean CHINDEX = true;
        final boolean BOOTSTRAPPING = false;
        boolean analysis = false;
        if (userSelection2 == 0) {
          analysis = CHINDEX;
          GenericDialog gd = new GenericDialog("Set Simulation parameters");
          gd.addNumericField("Beta", 0, 2);
          gd.addNumericField("Responsiveness (%)", 50, 2);
          gd.showDialog();
          if (gd.wasCanceled()) {
            mw.setVisible(true);
            return;
          }
          double beta = gd.getNextNumber();
          double responsiveness = gd.getNextNumber()/100;//value must be between [0,1]
          simulate(beta,responsiveness); // a single ch-index simulation
        } else if (userSelection2 == 1) {
          analysis = BOOTSTRAPPING;
          // results =
          // simulate(analysis,MAXNBETAS,MAXNRESP,maxBeta,MAXSIMULATIONS);
        }
        mw.setVisible(true);
        // Print simulation results throw IJ.log
        // for(int i=0;i<MAXNBETAS;i++){
        // for(int j=0;j<MAXNRESP;j++){
        // IJ.log(""+results[i][j]);
        // }
        // }
      }
    }
    mw.setVisible(true);
  }

  /**
   * @param rt
   * @param OR
   * @param th
   * @param ID
   * @param filename
   */
  public void setBootstrappingResults(ResultsTable rt, double OR, double th, String ID, String filename) {

    // System.out.println("filename: "+filename);
    String[] parts = filename.split("-");// it's necessary to remove the
                                         // '.avi' extension
    // System.out.println("parts[0]: "+parts[0]);
    // parts = parts[0].split("-");//Format 2000-11-19-1234-Q-P-100pM-0-1

    rt.incrementCounter();
    rt.addValue("ID", ID);
    rt.addValue("OR", OR);
    rt.addValue("Threshold", th);
    if (OR > (th)) {
      rt.addValue("Result", "POSITIVE");
    } else {
      rt.addValue("Result", "-");
    }
    rt.addValue("Type", VideoAnalyzer.getTrialType(filename));
    rt.addValue("Filename", filename);
  }

  /**
   * @param rt
   * @param filename
   * @param chIdx
   * @param slIdx
   * @param nTracks
   */
  public void setChResults(ResultsTable rt, String filename, float chIdx, float slIdx, int nTracks) {

    // System.out.println("filename: "+filename);
    String[] parts = filename.split("-");// it's necessary to remove the
                                         // '.avi' extension
    // System.out.println("parts[0]: "+parts[0]);
    // parts = parts[0].split("-");//Format 2000-11-19-1234-Q-P-100pM-0-1

    rt.incrementCounter();
    rt.addValue("nTracks", nTracks);
    rt.addValue("Ch-Index", chIdx);
    rt.addValue("sl-Index", slIdx);
    rt.addValue("Type", parts[4]);
    if (parts[4].equals("Q")) {
      rt.addValue("Hormone", parts[5]);
      rt.addValue("Concentration", parts[6]);
    } else {
      rt.addValue("Hormone", "-");
      rt.addValue("Concentration", "-");
    }
    rt.addValue("Direction (Degrees)", Params.angleDirection);
    rt.addValue("ArcChemotaxis (Degrees)", Params.angleAmplitude);
    rt.addValue("ID", parts[3]);
    rt.addValue("Date", parts[0] + "-" + parts[1] + "-" + parts[2]);
    rt.addValue("Filename", filename);
  }

  /******************************************************/
  /**
   * @param analysis
   *          - true: simulate ch-index; false: bootstrapping
   * @return matrix of chIndexes relative to each pair beta-responsiveness level
   */
  /**
   * @param analysis
   * @param MAXNBETAS
   * @param MAXNRESP
   * @param maxBeta
   * @param MAXSIMULATIONS
   * @return
   */
  public double[][] simulate(boolean analysis, int MAXNBETAS, int MAXNRESP, double maxBeta, int MAXSIMULATIONS) {

    double[] Betas = new double[MAXNBETAS];
    double[] Responsiveness = new double[MAXNRESP];
    double[][] results = new double[MAXNBETAS][MAXNRESP];
    Map<String, Trial> trials = null;
    for (int i = 0; i < MAXNBETAS; i++) {
      double beta = (i / (double) MAXNBETAS) * maxBeta;
      System.out.println("beta: " + beta);
      Betas[i] = beta;
      for (int j = 0; j < MAXNRESP; j++) {
        System.out.println("i: " + i + "; j: " + j);
        double responsiveCells = j / (double) MAXNRESP;
        Responsiveness[j] = responsiveCells;
        System.out.println("responsiveCells: " + responsiveCells);
        trials = VideoAnalyzer.simulateTrials("Chemotaxis-Simulation", beta, responsiveCells, MAXSIMULATIONS);//
        // Utils.saveTrials(trials);
        if (trials == null) {
          return null;
        }
        // Utils.saveTrials(trials);
        if (analysis) {
          results[i][j] = analyseChDirectory(trials);
        } else {
          results[i][j] = bootstrapping(trials);
        }
      }
    }
    return results;
  }

  /**
   * @param beta
   * @param responsiveness
   * @param length 
   */
  public void simulate(double beta, double responsiveness) {
    Trial tr = null;
    tr = VideoAnalyzer.simulateTrial("Chemotaxis-Simulation", beta, responsiveness);//
    if (tr == null) {
      return;
    }
    float chIdx = calculateChIndex(tr.tracks);
    float slIdx = calculateSLIndex(tr.tracks);
    Paint.drawChemotaxis(tr.tracks, chIdx, slIdx, tr.width, tr.height, tr.source);

    int[] hist = circularHistogram(getListOfAngles(tr.tracks), 45);
    int radius = tr.width;
    Paint.drawRoseDiagram(hist, radius, chIdx, tr.source);
  }
}
