package analysis;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import functions.Utils;
import gui.MainWindow;
import gui.MorphWindow;
import ij.IJ;
import ij.ImagePlus;

public class Morphometry {

	MorphWindow morphW;
	MainWindow mainW;
	
	public Morphometry() {}
	
	public int analysisSelectionDialog(){
		Object[] options = {"File", "Directory"};
		int n = JOptionPane.showOptionDialog(null,
				"What do you want to analyze?",
				"Choose one analysis...",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button title
		return n;
	}
	
	public void analyzeFile(){
		ImagePlus imp = IJ.openImage();
		// MorphWindow works with an ImagePlus array.
		// If we want to analyze only one image, we have to pass
		// an array of one element
		if(imp==null){
			JOptionPane.showMessageDialog(null, "Please, select a valid file.");
			mainW.setVisible(true);
			return;
		}
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		images.add(imp);
		morphW.setImages(images);
		morphW.showWindow();
	}	
	
	public void analyzeDirectory(){
		
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0){
			JOptionPane.showMessageDialog(null, "Please, select a valid folder.");
			mainW.setVisible(true);
			return;
		}
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		for (int i = 0; i < listOfFiles.length; i++) {
			String absoluteFilePath = listOfFiles[i];
			ImagePlus imp = IJ.openImage(absoluteFilePath);
			if(imp!=null)
				images.add(imp);
			// else - possibly the file is not an image
		}
		if(images.size()<1){
			JOptionPane.showMessageDialog(null, "Please, select a valid folder.");
			mainW.setVisible(true);
			return;
		}
		morphW.setImages(images);
		morphW.showWindow();
	}
	public void run(MainWindow mw){
		mainW = mw;
		mainW.setVisible(false);
		//Ask user which analysis wants to apply
		int userSelection = analysisSelectionDialog();
		if(userSelection<0){
			mw.setVisible(true);
			return;			
		}
		morphW = new MorphWindow(mainW);
		if(userSelection==0)
			analyzeFile();
		else if(userSelection==1)
			analyzeDirectory();
	}
	
}
