package functions;

import data.SList;
import data.Trial;
import ij.ImagePlus;
import plugins.AVI_Reader;

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
   * @param path
   * @return
   */
  public Trial getTrialFromAVI(String path) {
    FileManager fm = new FileManager();
    
    String filename = fm.getFilename(path);
    String parentDir = fm.getParentDirectory(path);
    String relativePath = parentDir+"\\"+filename;
    String trialID = filename;
    // Load video
    AVI_Reader ar = new AVI_Reader();
    ar.run(path);
    ImagePlus imp = ar.getImagePlus();
    return getTrialFromImp(imp, trialID, relativePath);
  }

  /**
   * @param impOrig
   * @param analysis
   * @param trialID
   * @param trialType
   * @param relativePath
   * @return
   */
  public Trial getTrialFromImp(ImagePlus impOrig, String trialID, String relativePath) {
    // Analyze the video
    // It's necessary to duplicate the ImagePlus if
    // we want to draw later sperm trajectories in the original video
    ImagePlus imp = impOrig.duplicate();
    VideoRecognition vr = new VideoRecognition();
    SList tracks = vr.analyzeVideo(imp);
    Trial trial =  new Trial(trialID, "", relativePath, tracks, impOrig.getWidth(), impOrig.getHeight());
    return trial;
  }

}
