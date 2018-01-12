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

package analysis;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.SwingWorker;

import data.Params;
import data.Cell;
import data.Trial;
import functions.FileManager;
import functions.Kinematics;
import functions.Paint;
import functions.SignalProcessing;
import functions.TrialManager;
import functions.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * This class implements all the functions related to motility analysis.
 * 
 * @author Carlos Alquezar
 */
public class Motility extends SwingWorker<Boolean, String> {

  private enum TypeOfAnalysis {
    DIRECTORIES, DIRECTORY, FILE, NONE
  }

  private TypeOfAnalysis analysis              = TypeOfAnalysis.NONE;
  private float          countProgressiveSperm = 0;
  private double         total_alhMax          = 0;
  private double         total_alhMean         = 0;
  private float          total_bcf             = 0;
  private float          total_dance           = 0;
  private float          total_lin             = 0;
  private float          total_mad             = 0;
  private float          total_motile          = 0;
  private float          total_nonMotile       = 0;
  private float          total_sperm           = 0;
  private float          total_str             = 0;
  private float          total_vap             = 0;
  private float          total_vcl             = 0;
  private float          total_vsl             = 0;
  private float          total_wob             = 0;

  public Motility() {
  }

  /**
   * This method asks user for the directory that contains all subfolders that
   * are going to be analysed. For each subfolder, the average motility
   * parameters will be calculated. The method finish showing a ResultsTable
   * with the motility information.
   */
  private void analyseDirectories() {
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    List<String> subfolders = fm.getSubfolders(folder);
    ResultsTable rtTotal = new ResultsTable();
    int i = 0;
    for (String s : subfolders) {
      IJ.showProgress((double) i / subfolders.size());
      IJ.showStatus("Analizing folder " + i++ + "...");
      List<String> files = fm.getFiles(s);
      Map<String, Trial> trials = getTrials(files);
      for (String key : trials.keySet()) {
        Trial trial = (Trial) trials.get(key);
        // Motility results
        calculateMotility(new ResultsTable(), trial);
        calculateAverageMotility(new ResultsTable(), trial);
      }
      calculateTotalMotility(rtTotal, s);
      resetParams();
    }
    rtTotal.showRowNumbers(false);
    rtTotal.show("Total Motility");
  }

  /**
   * This method asks user for the directory that contains all AVI files that
   * are going to be analysed. For each file, the individual and average
   * motility parameters will be calculated. The method finish showing the
   * corresponding results tables with the motility information.
   */
  private void analyseDirectory() {
    FileManager fm = new FileManager();
    String folder = fm.selectFolder();
    List<String> files = fm.getFiles(folder);
    Map<String, Trial> trials = getTrials(files);
    ResultsTable rtIndividual = new ResultsTable();
    ResultsTable rtAverage = new ResultsTable();
    for (String key : trials.keySet()) {
      Trial trial = (Trial) trials.get(key);
      // Motility results
      calculateMotility(rtIndividual, trial);
      calculateAverageMotility(rtAverage, trial);
      resetParams();
    }
    rtIndividual.showRowNumbers(false);
    rtIndividual.show("Individual motility");
    rtAverage.showRowNumbers(false);
    rtAverage.show("Average motility");
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
    // Calculate motility
    ResultsTable rtIndividual = new ResultsTable();
    ResultsTable rtAverage = new ResultsTable();
    calculateMotility(rtIndividual, trial);
    calculateAverageMotility(rtAverage, trial);
    // Show results
    rtIndividual.showRowNumbers(false);
    rtIndividual.show("Individual Motility");
    rtAverage.showRowNumbers(false);
    rtAverage.show("Average Motility");
    // Draw trajectories
    ImagePlus imp = fm.getAVI(file);
    Paint paint = new Paint();
    paint.draw(imp, trial.tracks);
    imp.show();
    if(Params.printXY){
      Utils utils = new Utils();
      IJ.saveString(utils.printXYCoords(trial.tracks),"");
    }
  }

  /**
   * This method calculates the average motility values for the given trial.
   * 
   * @param rt
   *          - ResultsTable where the motility information will be added.
   * @param trial
   *          - Trial with all trajectories that will be analysed.
   */
  private void calculateAverageMotility(ResultsTable rt, Trial trial) {

    SignalProcessing sp = new SignalProcessing();
    List<List<Cell>> filteredTracks = sp.filterTracksByMotility(trial.tracks);
    float nTracks = filteredTracks.size(); // Only take into account those who passed the motility test
    float vsl_mean = total_vsl / nTracks;
    float vcl_mean = total_vcl / nTracks;
    float vap_mean = total_vap / nTracks;
    float lin_mean = total_lin / nTracks;
    float wob_mean = total_wob / nTracks;
    float str_mean = total_str / nTracks;
    double alhMean_mean = total_alhMean / nTracks;
    double alhMax_mean = total_alhMax / nTracks;
    float bcf_mean = total_bcf / nTracks;
    float dance_mean = total_dance / nTracks;
    float mad_mean = total_mad / nTracks;
    // % progressive Motile sperm
    float progressiveMotPercent = countProgressiveSperm / (float) nTracks;
    // % motility
    Kinematics K = new Kinematics();
    int[] motileSperm = K.motilityTest(trial.tracks);
    int countMotileSperm = motileSperm[0];
    total_motile += countMotileSperm;
    int countNonMotileSperm = motileSperm[1];
    total_nonMotile += countNonMotileSperm;
    float motility_value = (float) countMotileSperm / ((float) (countMotileSperm + countNonMotileSperm));
    total_sperm += nTracks;

    rt.incrementCounter();
    rt.addValue("Motile trajectories", nTracks);
    rt.addValue("VSL Mean (um/s)", vsl_mean);
    rt.addValue("VCL Mean (um/s)", vcl_mean);
    rt.addValue("VAP Mean (um/s)", vap_mean);
    rt.addValue("LIN Mean ", lin_mean);
    rt.addValue("WOB Mean ", wob_mean);
    rt.addValue("STR Mean ", str_mean);
    rt.addValue("ALH_Mean Mean (um)", alhMean_mean);
    rt.addValue("ALH_Max Mean (um)", alhMax_mean);
    rt.addValue("BCF Mean (Hz)", bcf_mean);
    rt.addValue("DANCE Mean (um^2/s)", dance_mean);
    rt.addValue("MAD Mean (degrees)", mad_mean);
    rt.addValue("Progressive Motility (%)", progressiveMotPercent * 100);
    rt.addValue("Motility (%)", motility_value * 100);
    rt.addValue("Sample", trial.type);
    rt.addValue("ID", trial.ID);
    rt.addValue("Source", trial.source);
    if (!Params.male.isEmpty())
      rt.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      rt.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      rt.addValue("Generic Field", Params.genericField);
  }

  /**
   * This method calculates the individual motility values for the given trial.
   * 
   * @param rt
   *          - ResultsTable where the motility information will be added.
   * @param trial
   *          - Trial with all trajectories that will be analysed.
   */
  private void calculateMotility(ResultsTable rt, Trial trial) {

    SignalProcessing sp = new SignalProcessing();
    Kinematics K = new Kinematics();
    Utils util = new Utils();
    // Only pass from here tracks with a minimum level of motility
    List<List<Cell>> filteredTracks = sp.filterTracksByMotility(trial.tracks);
    // Calculate values for each track
    for (ListIterator iT = filteredTracks.listIterator(); iT.hasNext();) {
      List aTrack = (List) iT.next();
      List avgTrack = sp.movingAverage(aTrack);
      int trackNr = util.getTrackNr(aTrack);
      float length = (float) aTrack.size();
      // VSL
      float vsl_value = K.vsl(aTrack);
      total_vsl += vsl_value;
      // VCL
      float vcl_value = K.vcl(aTrack);
      total_vcl += vcl_value;
      // VAP is equivalent to calculate vcl from averaged track
      float vap_value = K.vcl(avgTrack);
      total_vap += vap_value;
      // Linearity
      float lin_value = (vsl_value / vcl_value) * 100;
      total_lin += lin_value;
      // Wobble
      float wob_value = (vap_value / vcl_value) * 100;
      total_wob += wob_value;
      // Straightness
      float str_value = (vsl_value / vap_value) * 100;
      total_str += str_value;
      // Amplitude of lateral head
      double[] alh_values = K.alh(aTrack, avgTrack);
      total_alhMean += alh_values[0];
      total_alhMax += alh_values[1];
      // Beat-cross frequency
      float bcf_value = K.bcf(aTrack, avgTrack);
      total_bcf += bcf_value;
      // Progressive motility
      String progressMotility_value = "NO";
      if (str_value > Params.progressMotility) {
        progressMotility_value = "YES";
        countProgressiveSperm++;
      }
      // DANCE
      double dance_value = vcl_value * alh_values[0];// vcl*alh_mean
      total_dance += dance_value;
      // MAD
      float mad_value = K.mad(aTrack);
      total_mad += mad_value;

      rt.incrementCounter();
      rt.addValue("Track Number", trackNr);
      rt.addValue("Length (frames)", length);
      rt.addValue("VSL (um/s)", vsl_value);
      rt.addValue("VCL (um/s)", vcl_value);
      rt.addValue("VAP (um/s)", vap_value);
      rt.addValue("LIN", lin_value);
      rt.addValue("WOB", wob_value);
      rt.addValue("STR", str_value);
      rt.addValue("ALH_Mean (um)", alh_values[0]);
      rt.addValue("ALH_Max (um)", alh_values[1]);
      rt.addValue("BCF (Hz)", bcf_value);
      rt.addValue("DANCE (um^2/s)", dance_value);
      rt.addValue("MAD (degrees)", mad_value);
      rt.addValue("Progress Motility", progressMotility_value);
      rt.addValue("Sample", trial.type);
      rt.addValue("ID", trial.ID);
      rt.addValue("Source", trial.source);
      if (!Params.male.isEmpty())
        rt.addValue("Male", Params.male);
      if (!Params.date.isEmpty())
        rt.addValue("Date", Params.date);
      if (!Params.genericField.isEmpty())
        rt.addValue("Generic Field", Params.genericField);
    }
  }

  /**
   * This method calculates the total average motility values for a folder.
   * 
   * @param rt
   *          - ResultsTable where the motility information will be added.
   * @param filename
   *          - the folder name. This information will be added to the results
   *          table.
   */
  private void calculateTotalMotility(ResultsTable rt, String filename) {

    float vsl_mean = total_vsl / total_sperm;
    float vcl_mean = total_vcl / total_sperm;
    float vap_mean = total_vap / total_sperm;
    float lin_mean = total_lin / total_sperm;
    float wob_mean = total_wob / total_sperm;
    float str_mean = total_str / total_sperm;
    double alhMean_mean = total_alhMean / total_sperm;
    double alhMax_mean = total_alhMax / total_sperm;
    float bcf_mean = total_bcf / total_sperm;
    float dance_mean = total_dance / total_sperm;
    float mad_mean = total_mad / total_sperm;
    // % progressive Motile sperm
    float progressiveMotPercent = countProgressiveSperm / (float) total_sperm;
    // % motility
    float motility_value = (float) total_motile / ((float) (total_motile + total_nonMotile));

    rt.incrementCounter();
    rt.addValue("Motile trajectories", total_sperm);
    rt.addValue("VSL Mean (um/s)", vsl_mean);
    rt.addValue("VCL Mean (um/s)", vcl_mean);
    rt.addValue("VAP Mean (um/s)", vap_mean);
    rt.addValue("LIN Mean ", lin_mean);
    rt.addValue("WOB Mean ", wob_mean);
    rt.addValue("STR Mean ", str_mean);
    rt.addValue("ALH_Mean Mean (um)", alhMean_mean);
    rt.addValue("ALH_Max Mean (um)", alhMax_mean);
    rt.addValue("BCF Mean (Hz)", bcf_mean);
    rt.addValue("DANCE Mean (um^2/s)", dance_mean);
    rt.addValue("MAD Mean (degrees)", mad_mean);
    rt.addValue("Progressive Motility (%)", progressiveMotPercent * 100);
    rt.addValue("Motility (%)", motility_value * 100);
    rt.addValue("Filename", filename);
    if (!Params.male.isEmpty())
      rt.addValue("Male", Params.male);
    if (!Params.date.isEmpty())
      rt.addValue("Date", Params.date);
    if (!Params.genericField.isEmpty())
      rt.addValue("Generic Field", Params.genericField);
  }

  /**
   * This method is inherit from SwingWorker class and it is the starting point
   * after the execute() method is called.
   */
  @Override
  protected Boolean doInBackground() throws Exception {
    switch (analysis) {
      case FILE:
        analyseFile();
        break;
      case DIRECTORY:
        analyseDirectory();
        break;
      case DIRECTORIES:
        analyseDirectories();
        break;
    }
    return null;
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
   * This method resets all motility parameters of the motility analysis
   */
  private void resetParams() {
    total_sperm = 0;
    total_vsl = 0;
    total_vcl = 0;
    total_vap = 0;
    total_lin = 0;
    total_wob = 0;
    total_str = 0;
    total_alhMean = 0;
    total_alhMax = 0;
    total_bcf = 0;
    total_dance = 0;
    total_mad = 0;
    total_motile = 0;
    total_nonMotile = 0;
    countProgressiveSperm = 0;
  }

  /**
   * This method opens a set of dialogs to ask the user which analysis has to be
   * carried on.
   */
  public void selectAnalysis() {
    // Ask if user wants to analyze a file or directory
    Object[] options = { "File", "Directory", "Multiple directories" };
    String question = "What do you want to analyze?";
    String title = "Choose one analysis...";
    final int FILE = 0;
    final int DIR = 1;
    final int MULTIDIR = 2;
    Utils utils = new Utils();
    int sourceSelection = utils.analysisSelectionDialog(options, question, title);
    if (sourceSelection < 0) {
      return;
    } else if (sourceSelection == FILE) {
      analysis = TypeOfAnalysis.FILE;
    } else if (sourceSelection == DIR) {
      analysis = TypeOfAnalysis.DIRECTORY;
    } else if (sourceSelection == MULTIDIR) {
      analysis = TypeOfAnalysis.DIRECTORIES;
    }
  }
}
