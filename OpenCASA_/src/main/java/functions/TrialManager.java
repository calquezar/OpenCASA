package functions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import data.PersistentRandomWalker;
import data.SList;
import data.Simulation;
import data.Trial;
import ij.IJ;
import ij.ImagePlus;
import third_Party.AVI_Reader;

/**
 * @author Carlos Alquezar
 *
 */
public class TrialManager {
  

  /**
   * @param analysis
   * @param path
   * @return
   */    
  public Trial getTrialFromAVI(String path) {
    if (path == null)
      return null;
    FileManager fm = new FileManager();
    if (!fm.isAVI(path))
      return new Trial();
    // Load video
    AVI_Reader ar = new AVI_Reader();
    ar.run(path);
    ImagePlus imp = ar.getImagePlus();
    return getTrialFromImp(imp, path);
  }

  /**
   * @param impOrig
   * @param analysis
   * @param trialID
   * @param trialType
   * @param relativePath
   * @return
   */
  public Trial getTrialFromImp(ImagePlus impOrig, String path) {
    // Analyze the video
    // It's necessary to duplicate the ImagePlus if
    // we want to draw later sperm trajectories in the original video
    ImagePlus imp = impOrig.duplicate();
    //Extract trajectories
    VideoRecognition vr = new VideoRecognition();
    SList tracks = vr.analyzeVideo(imp);
    //Set metadata
    FileManager fm = new FileManager();
    String filename = fm.getFilename(path);
    String ID = filename.toLowerCase();
    String type = fm.getParentDirectory(path).toLowerCase();// the call toLowerCase() is to avoid user mistakes
                                                                    // while naming folders. This variable is useful 
                                                                    // in directory analysis
    //Create and return trial
    Trial trial =  new Trial(ID, type, path, tracks, impOrig.getWidth(), impOrig.getHeight());
    return trial;
  }
  
  /**
   * @return
   */
  public Map<String, Trial> readTrials() {
    Map<String, Trial> trials = null;
    try {
      FileManager fm = new FileManager();
      String file = fm.selectFile();
      if (file == null)
        return null;
      FileInputStream streamIn = new FileInputStream(file);
      ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
      trials = (HashMap<String, Trial>) objectinputstream.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return trials;
  } 

  /**
   * @param trials
   */
  public void saveTrials(Map<String, Trial> trials) {

    String filename = "";
    String dir = "";
    JFileChooser c = new JFileChooser();
    int rVal = c.showSaveDialog(null);
    if (rVal == JFileChooser.APPROVE_OPTION) {
      filename = c.getSelectedFile().getName();
      dir = c.getCurrentDirectory().toString();
    }
    System.out.println(dir);
    try {
      // String folder = Utils.selectFolder();
      if (dir == null || dir.equals(""))
        return;
      FileOutputStream fos = new FileOutputStream(dir + "\\" + filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(trials);
      oos.close();
      fos.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
  
  /**
   * @param trialID
   * @param beta
   * @param responsiveCells
   * @return
   */
  public Trial simulateTrial(String trialID, double beta, double responsiveCells) {
    Simulation sim = new PersistentRandomWalker(beta, responsiveCells);
    ImagePlus imp = sim.createSimulation();
    String trialType = "Beta: "+Double.toString(beta)+";Resp: "+Double.toString(responsiveCells);
    String simName = trialType+"\\"+trialID;
    Trial tr = getTrialFromImp(imp,simName);
    return tr;
  }

  /**
   * @param beta
   * @param responsiveCells
   * @param MAXSIMULATIONS
   * @return
   */
  public Map<String, Trial> simulateTrials(double beta, double responsiveCells,int MAXSIMULATIONS) {
    Map<String, Trial> trials = new HashMap<String, Trial>();
    for (int i = 0; i < MAXSIMULATIONS; i++) {
      IJ.showProgress((double) i / (double)MAXSIMULATIONS);
      IJ.showStatus("Simulating trial "+i+"...");
      Trial tr = simulateTrial(Integer.toString(i),beta,responsiveCells);
      trials.put(tr.ID, tr);
    }
    return trials;
  }

}
