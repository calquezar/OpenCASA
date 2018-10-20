
/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.MainWindow;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

/**
 * OpenCASA - OpenSource software for Computer Assisted Sperm Analysis
 *
 * @author Carlos Alquezar
 */
public class OpenCASA_ implements PlugIn {

  /**
   * Main method
   */
  public static void main(String[] args)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    final Class<?> clazz = OpenCASA_.class;
    new ImageJ();// start ImageJ
    IJ.runPlugIn(clazz.getName(), "");// run the plugin
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
