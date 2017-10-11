
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.MainWindow;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

/**
 * OpenCASA - OpenSource software for Computer Assisted Sperm Analysis
 *
 * @author Biozar team
 */
public class OpenCASA_ implements PlugIn {

  /**
   * Main method
   * 
   * @param args
   *          unused
   * @throws UnsupportedLookAndFeelException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static void main(String[] args)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    Class<?> clazz = OpenCASA_.class;
    new ImageJ();//start ImageJ
    IJ.runPlugIn(clazz.getName(), "");//run the plugin
  }
  /**
   * This method overrides the superclass run's method. Start point of the
   * plugin.
   **/
  @Override
  public void run(String arg) {
    (new MainWindow("OpenCASA")).setVisible(true);
  }
}
