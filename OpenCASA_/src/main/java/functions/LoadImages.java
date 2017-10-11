package functions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import gui.MainWindow;
import gui.MorphWindow;
import ij.IJ;
import ij.ImagePlus;

/**
 * @author Carlos Alquezar
 *
 */
public class LoadImages {

  /**
   * 
   */
  MainWindow mainW;

  /**
   * 
   */
  public LoadImages(MainWindow mw) {
    mainW = mw;
  }

  /**
   * @return
   */
  public int analysisSelectionDialog() {
    Object[] options = { "File", "Directory" };
    int n = JOptionPane.showOptionDialog(null, "What do you want to analyze?", "Choose one analysis...",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, // do not use a custom Icon
        options, // the titles of buttons
        options[0]); // default button title
    return n;
  }

  /**
   * 
   * @return
   */
  public List<ImagePlus> analyzeDirectory() {

    String[] listOfFiles = Utils.getFileNames();
    if (listOfFiles == null || listOfFiles.length == 0) {
      if (listOfFiles != null)
        JOptionPane.showMessageDialog(null, "Please, select a non-empty folder.");
      mainW.setVisible(true);
      return null;
    }
    List<ImagePlus> images = new ArrayList<ImagePlus>();
    for (int i = 0; i < listOfFiles.length; i++) {
      String absoluteFilePath = listOfFiles[i];
      String parentsDirectory = Utils.getParentDirectory(absoluteFilePath);
      ImagePlus imp = IJ.openImage(absoluteFilePath);
      if (imp != null) {
        imp.setTitle(parentsDirectory + "\\" + imp.getTitle());
        images.add(imp);
      }
      // else - possibly the file is not an image
    }
    if (images.size() < 1) {
      JOptionPane.showMessageDialog(null, "Please, select a valid folder.");
      mainW.setVisible(true);
      return null;
    }
    return images;
    // morphW.showWindow();
  }

  /**
   * 
   * @return
   */
  public List<ImagePlus> analyzeFile() {
    String absoluteFilePath = Utils.getAbsoluteFileName();
    if (absoluteFilePath == null) {
      mainW.setVisible(true);
      return null;
    }
    String parentsDirectory = Utils.getParentDirectory(absoluteFilePath);
    ImagePlus imp = IJ.openImage(absoluteFilePath);
    // MorphWindow works with an ImagePlus array.
    // If we want to analyze only one image, we have to pass
    // an array of one element
    if (imp == null) {
      JOptionPane.showMessageDialog(null, "Please, select a valid file.");
      mainW.setVisible(true);
      return null;
    }
    imp.setTitle(parentsDirectory + "\\" + imp.getTitle());
    List<ImagePlus> images = new ArrayList<ImagePlus>();
    images.add(imp);
    return images;
    // morphW.showWindow();
  }

  /**
   * 
   * @return
   */
  public List<ImagePlus> run() {
    mainW.setVisible(false);
    // Ask user which analysis wants to apply
    int userSelection = analysisSelectionDialog();
    if (userSelection < 0) {
      mainW.setVisible(true);
      return null;
    }
    // morphW = new MorphWindow(mainW);
    if (userSelection == 0)
      return analyzeFile();
    else if (userSelection == 1)
      return analyzeDirectory();
    else
      return null;
  }
}
