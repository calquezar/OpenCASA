

import gui.MainWindow;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;


/**
 * OpenCASA
 *
 * 
 *
 * @author Biozar team
 */
public class OpenCASA_ implements PlugIn {
	protected ImagePlus image;

	@Override
	public void run(String arg) {

		(new MainWindow("OpenCASA")).show();
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = OpenCASA_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		//new ImageJ();
		
		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
}
