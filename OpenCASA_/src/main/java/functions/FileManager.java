package functions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;

/**
 * @author Carlos Alquezar
 *
 */
public class FileManager {


  /**   */
  public FileManager() {
  }

  /**
   * @return
   */
  public int dialog() {
    Object[] options = { "File", "Directory" };
    int n = JOptionPane.showOptionDialog(null, "What do you want to analyze?", "Choose one analysis...",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, // do not use a custom Icon
        options, // the titles of buttons
        options[0]); // default button title
    return n;
  }

  /**
   * @param path
   * @return
   */
  public String getFilename(String path) {
    String[] parts = path.split("\\\\");
    return parts[parts.length - 1];
  }

  /**
   * @return
   */
  public String[] getListOfFiles() {
      return getListOfFiles(selectFolder());
  }

  public String[] getListOfImages(String[] listOfFiles){
    
    
  }
  
  /**
   * @param dir
   * @return
   */
  public String[] getListOfFiles(String path) {
    if(path==null)
      return null;
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    if(listOfFiles.length<=0)
      return null;
    String[] listOfNames = new String[listOfFiles.length];
    for (int i = 0; i < listOfFiles.length; i++)
      listOfNames[i] = folder.getAbsolutePath() + "\\" + listOfFiles[i].getName();
    return listOfNames;
  }
  /**
   * @param path
   * @return
   */
  public String getParentDirectory(String path) {
    String[] parts = path.split("\\\\");
    return parts[parts.length - 2];
  }
  
  /**
   * @param filename
   * @return
   */
  public boolean isAVI(String filename) {
    String[] parts = filename.split("\\.");
    if (parts[1].equals("avi"))
      return true;
    else
      return false;
  }
  /**
   * 
   * @return
   */
  public List<ImagePlus> loadImageDirectory() {

    String[] listOfFiles = getListOfFiles();
    if (listOfFiles == null || listOfFiles.length == 0) {
      if (listOfFiles != null)
        JOptionPane.showMessageDialog(null, "Please, select a non-empty folder.");
      return null;
    }
    List<ImagePlus> images = new ArrayList<ImagePlus>();
    for (int i = 0; i < listOfFiles.length; i++) {
      String absoluteFilePath = listOfFiles[i];
      String parentsDirectory = getParentDirectory(absoluteFilePath);
      ImagePlus imp = IJ.openImage(absoluteFilePath);
      if (imp != null) {
        imp.setTitle(parentsDirectory + "\\" + imp.getTitle());
        images.add(imp);
      }
      // else - possibly the file is not an image
    }
    if (images.size() < 1) {
      JOptionPane.showMessageDialog(null, "Please, select a valid folder.");
      return null;
    }
    return images;
  }
  
  /**
   * 
   * @return
   */
  public List<ImagePlus> loadImageFile() {
    String absoluteFilePath = getAbsoluteFileName();
    if (absoluteFilePath == null) {
      return null;
    }
    if(isAVI(absoluteFilePath)){
      JOptionPane.showMessageDialog(null, "Please, select a valid file.");
      return null;
    }
    String parentsDirectory = getParentDirectory(absoluteFilePath);
    ImagePlus imp = IJ.openImage(absoluteFilePath);
    if (imp == null) {
      JOptionPane.showMessageDialog(null, "Please, select a valid file.");
      return null;
    }
    imp.setTitle(parentsDirectory + "\\" + imp.getTitle());
    List<ImagePlus> images = new ArrayList<ImagePlus>();
    images.add(imp);
    return images;
  }
  /**
   * 
   * @return
   */
  public List<ImagePlus> loadImages() {
    // Ask user which analysis wants to apply
    int userSelection = dialog();
    if (userSelection < 0)
      return null;
    if (userSelection == 0)
      return loadImageFile();
    else if (userSelection == 1)
      return loadImageDirectory();
    else
      return null;
  }
  /**
   * @return
   */
  public String selectFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a file...");
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      return file.getAbsolutePath();
    }
    return null;
  } 

  /**
   * @return
   */
  public String selectFolder() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a folder...");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File folder = chooser.getSelectedFile();
      return folder.getAbsolutePath();
    }
    return null;
  }  
}
