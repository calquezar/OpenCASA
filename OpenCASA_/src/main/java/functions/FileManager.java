/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquezar
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

package functions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import data.Params;
import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import third_Party.AVI_Reader;

/**
 * @author Carlos Alquezar
 *
 */
public class FileManager {

  /**   */
  public FileManager() {
  }

  /**
   * 
   * @param path
   * @return
   */
  public ImagePlus getAVI(String path) {
    AVI_Reader ar = new AVI_Reader();
    int first = (int) (Params.firstFrame*Params.frameRate+1);
    int last;
    if(Params.lastFrame>0)
      last = (int) (Params.lastFrame*Params.frameRate);
    else
      last = -1;
    ar.setInterval(first, last);
    ar.run(path);
    ImagePlus imp = ar.getImagePlus();
    return imp;
  }

  /**
   * @param dir
   * @return
   */
  public String[] getContent(String path) {
    if (path == null)
      return null;
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    if (listOfFiles.length <= 0)
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
  public String getFilename(String path) {
    String[] parts = path.split("\\\\");
    return removeExtension(parts[parts.length - 1]);
  }

  public List<String> getFiles(String path) {
    String[] content = getContent(path);
    List<String> files = new ArrayList<String>();
    for (int i = 0; i < content.length; i++) {
      if (new File(content[i]).isFile()) {
        files.add(content[i]);
      }
    }
    return files;
  }

  /**
   * @param path
   * @return
   */
  public String getParentDirectory(String path) {
    String[] parts = path.split("\\\\");
    return parts[parts.length - 2];
  }

  public List<String> getSubfolders(String path) {

    String[] content = getContent(path);
    List<String> subfolders = new ArrayList<String>();
    for (int i = 0; i < content.length; i++) {
      if (new File(content[i]).isDirectory()) {
        subfolders.add(content[i]);
      }
    }
    return subfolders;
  }

  /**
   * @param filename
   * @return
   */
  public boolean isAVI(String filename) {
    String[] parts = filename.split("\\.");
    if (parts.length < 2)
      return false;
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
    String dir = selectFolder();
    return loadImageDirectory(dir);
  }

  /**
   * 
   * @return
   */
  public List<ImagePlus> loadImageDirectory(String dir) {

    String[] listOfFiles = getContent(dir);
    if (listOfFiles == null || listOfFiles.length == 0) {
      if (listOfFiles != null)
        JOptionPane.showMessageDialog(null, "Please, select a non-empty folder.");
      return null;
    }
    List<ImagePlus> images = new ArrayList<ImagePlus>();
    for (int i = 0; i < listOfFiles.length; i++) {
      IJ.showProgress((double) i / listOfFiles.length);
      IJ.showStatus("Loading image " + i + "...");
      String absoluteFilePath = listOfFiles[i];
      if (isAVI(absoluteFilePath))
        continue;
      String parentsDirectory = getParentDirectory(absoluteFilePath);
      ImagePlus imp = IJ.openImage(absoluteFilePath);
      if (imp != null) {
        imp.setTitle(parentsDirectory + "\\" + imp.getTitle());
        images.add(imp);
      }
      // else - possibly the file is not an image nor AVI
    }
    IJ.showProgress(2); // To remove progresBar
    if (images.size() < 1) {
      JOptionPane.showMessageDialog(null, "Please, select a valid folder.");
      return null;
    }
    return images;
  }

  /**
   * 
   * @return an array with only one ImagePlus, compatible with the input
   *         specification of other functions
   */
  public List<ImagePlus> loadImageFile() {
    String absoluteFilePath = selectFile();
    if (absoluteFilePath == null) {
      return null;
    }
    if (isAVI(absoluteFilePath)) {
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

  public String removeExtension(String filename) {
    String[] parts = filename.split("\\.");
    return parts[0];
  }

  // /**
  // *
  // * @return
  // */
  // public List<ImagePlus> loadImages() {
  // // Ask user which analysis wants to apply
  // int userSelection = dialog();
  // if (userSelection < 0)
  // return null;
  // if (userSelection == 0)
  // return loadImageFile();
  // else if (userSelection == 1)
  // return loadImageDirectory();
  // else
  // return null;
  // }
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
    DirectoryChooser dc = new DirectoryChooser("Select folder...");
    return dc.getDirectory();
  }
}
