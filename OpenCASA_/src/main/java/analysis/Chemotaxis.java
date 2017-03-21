package analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

import data.SList;
import data.Trial;
import gui.MainWindow;
import ij.ImagePlus;
import plugins.AVI_Reader;
import utils.Utils;

public class Chemotaxis {

//	private Map<String, Trial> trials = new HashMap<String, Trial>();
	
	public Chemotaxis() {}
	
	public int analysisSelectionDialog(){
		Object[] options = {"RatioQ", "Bootstrapping"};
		int n = JOptionPane.showOptionDialog(null,
				"Which analysis do you want to apply to the data?",
				"Choose one analysis",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button title
		return n;
	}
	
	public void ratioQ(){
		
	}
	public void bootstrapping(){
		
	}
	public Map<String, Trial> generateTrials(){
		
		Map<String, Trial> trials = new HashMap<String, Trial>();
		String[] listOfFiles = Utils.getFileNames();
		if(listOfFiles==null || listOfFiles.length==0){
			return null;
		}
		for (int i = 0; i < listOfFiles.length; i++) {
		    if (new File(listOfFiles[i]).isFile()) {
		    	final String filename = listOfFiles[i];
				if(Utils.isAVI(filename)){
//					System.out.println("Loading video...");
					int trialType = ChFunctions.getTrialType(filename);
					String trialID = ChFunctions.getID(filename);
					
					switch(trialType){
					case 0: //Control
					case 1: //10pM
					case 2: //100pM
//						case 3: //10nM
					AVI_Reader ar = new  AVI_Reader();
					ar.run(filename);
					final ImagePlus imp = ar.getImagePlus();
					SList t = analyze(imp,filename);
	
					Trial tr;
					if(trials.get(trialID)!=null){
						tr = trials.get(trialID);
						tr.ID = trialID;
					}
					else 
						tr = new Trial();
					switch(trialType){
						case 0: tr.control=t;break;
						case 1: tr.p10pM=t;break;
						case 2: tr.p100pM=t; break;
						case 3: tr.p10nM=t;break;
					}
					
					int sampleSize = ChFunctions.calculateSampleSize(t);
					if((tr.minSampleSize==-1)||(sampleSize<tr.minSampleSize))
						tr.minSampleSize = sampleSize;
					trials.put(trialID, tr);

						//new Thread(new Runnable() {public void run() {analyze(imp,filename);}}).start();							
					}
				}
				
		    } else if (new File(listOfFiles[i]).isDirectory()) {}		    
		    return trials;
		}
	}
	public void run(MainWindow mw) throws IOException, ClassNotFoundException{
		mw.setVisible(false);
		int n = analysisSelectionDialog();
		if(n<0){
			mw.setVisible(true);
			return;			
		}else
			generateTrials();
		if(n==0)
			ratioQ();
		else if(n==1)
			bootstrapping();
		mw.setVisible(true);
	}
}
