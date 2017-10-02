
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import data.Params;
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
    *           unused
    * @throws UnsupportedLookAndFeelException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws ClassNotFoundException
    */
   public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
         UnsupportedLookAndFeelException {

      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // set the plugins.dir property to make the plugin appear in the Plugins
      // menu
      Class<?> clazz = OpenCASA_.class;
      String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
      String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
      System.setProperty("plugins.dir", pluginsDir);
      // start ImageJ
      new ImageJ();
      // run the plugin
      IJ.runPlugIn(clazz.getName(), "");

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
