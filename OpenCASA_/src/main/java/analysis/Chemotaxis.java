package analysis;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import functions.FileManager;
import functions.Utils;
import gui.MainWindow;
import ij.IJ;
import ij.gui.GenericDialog;

/**
 * 
 * @author Carlos Alquezar
 *
 */
public class Chemotaxis extends SwingWorker<Void, Void> {

  private enum TypeOfAnalysis {
    ChIndexFile, ChIndexDirectory, Bootstrapping, ChIndexSimulations, BootstrappingSimulations
  }
  /** */
  private static final Float FLOAT = (Float) null;
  
  /** */
  private TypeOfAnalysis analysis;

  private void analyzeFile(){
    FileManager fm = new FileManager();
    String file = fm.selectFile();
    
  }
  
  @Override
  public Void doInBackground() throws Exception {
    
    switch (analysis) {
      case ChIndexFile:
        break;
      case ChIndexDirectory:
        break;
      case Bootstrapping:
        break;
      case ChIndexSimulations:
        break;
      case BootstrappingSimulations:
        break;
    }

    return null;
  }

  @Override
  protected void done() {

    switch (analysis) {
      case ChIndexFile:
        break;
      case ChIndexDirectory:
        break;
      case Bootstrapping:
        break;
      case ChIndexSimulations:
        break;
      case BootstrappingSimulations:
        System.out.println("bootstrapping simulations");
        break;
    }
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
      analysis = TypeOfAnalysis.ChIndexFile; // It's not possible to carry on
                                             // bootstrapping analysis in a
                                             // single file
    } else if (sourceSelection == DIR || sourceSelection == SIMULATION) {// Directory or simulations
      // Ask user which analysis wants to apply
      Object[] options2 = { "Ch-Index", "Bootstrapping" };
      question = "Which analysis do you want to apply to the data?";
      title = "Choose one analysis...";
      int analysisSelection = utils.analysisSelectionDialog(options2, question, title);
      final int CHINDEX = 0;
      final int BOOTSTRAPPING = 1;
      if (analysisSelection < 0)
        return;
      if (sourceSelection == DIR) {
        if (analysisSelection == CHINDEX) {
          analysis = TypeOfAnalysis.ChIndexDirectory;
        } else if (analysisSelection == BOOTSTRAPPING) {
          analysis = TypeOfAnalysis.Bootstrapping;
        }
      } else if (sourceSelection == SIMULATION) { // Simulations
        if (analysisSelection == CHINDEX)
          analysis = TypeOfAnalysis.ChIndexSimulations;
      } else if (analysisSelection == BOOTSTRAPPING) {
        analysis = TypeOfAnalysis.BootstrappingSimulations;
      }
    }
  }
}
