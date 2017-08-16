package analysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import data.Trial;
import functions.Paint;
import functions.Utils;
import gui.MainWindow;
import ij.measure.ResultsTable;

public class Morphometry {


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

	}	
	
	public void analyzeDirectory(){
		
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0)
			return;
		ResultsTable rt= new ResultsTable();	
		for (int i = 0; i < listOfFiles.length; i++) {
			String absoluteFilePath = listOfFiles[i];
		}
	}
	public void run(MainWindow mw){
		mw.setVisible(false);
		//Ask user which analysis wants to apply
		int userSelection = analysisSelectionDialog();
		if(userSelection<0){
			mw.setVisible(true);
			return;			
		}
		if(userSelection==0)
			analyzeFile();
		else if(userSelection==1)
			analyzeDirectory();
		mw.setVisible(true);
	}
}
