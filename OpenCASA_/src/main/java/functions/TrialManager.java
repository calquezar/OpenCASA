package functions;

import data.Trial;

/**
 * @author Carlos Alquezar
 *
 */
public class TrialManager {
  
  
  /**
   * @param analysis
   * @return
   */
  public Trial extractTrial(String path) {
    if (path == null)
      return null;
    FileManager fm = new FileManager();
    if (!fm.isAVI(path))
      return new Trial();
    return getTrialFromAVI(path);
  }
  
  /**
   * @param analysis
   * @param absoluteFilePath
   * @return
   */
  public static Trial getTrialFromAVI(String analysis, String absoluteFilePath) {

    String[] parts = absoluteFilePath.split("\\\\");
    String filename = parts[parts.length - 1];
    String trialType = "";
    String trialID = "";
    if (analysis.equals("Chemotaxis-File") || analysis.equals("Chemotaxis-Directory")) {
      trialType = getTrialType(filename);
      trialID = getID(filename);
    } else if (analysis.equals("Motility-File") || analysis.equals("Motility-Directory")) {
      trialID = filename;
    }
    // Load videos
    AVI_Reader ar = new AVI_Reader();
    ar.run(absoluteFilePath);
    ImagePlus imp = ar.getImagePlus();
    return getTrialFromImp(imp, analysis, trialID, trialType, filename);
  }

  /**
   * @param impOrig
   * @param analysis
   * @param trialID
   * @param trialType
   * @param filename
   * @return
   */
  public static Trial getTrialFromImp(ImagePlus impOrig, String analysis, String trialID, String trialType,
      String filename) {
    // Analyze the video
    // It's necessary to duplicate the ImagePlus if
    // we want to draw later sperm trajectories in the original video
    ImagePlus imp = impOrig;
    if (analysis.equals("Motility-File"))
      imp = impOrig.duplicate();
    SList tracks = analyze(imp);
    int[] motileSperm = SignalProcessing.motilityTest(tracks);
    // Only pass from here tracks with a minimum level of motility
    tracks = SignalProcessing.filterTracksByMotility(tracks);
    Trial trial = null;
    if (analysis.equals("Chemotaxis-File") || analysis.equals("Chemotaxis-Directory")
        || analysis.equals("Chemotaxis-Simulation"))
      trial = new Trial(trialID, trialType, filename, tracks, impOrig.getWidth(), impOrig.getHeight());
    else if (analysis.equals("Motility-File"))
      trial = new Trial(trialID, trialType, filename, tracks, impOrig, motileSperm);
    else if (analysis.equals("Motility-Directory"))
      trial = new Trial(trialID, trialType, filename, tracks, null, motileSperm);
    return trial;
  }

}
