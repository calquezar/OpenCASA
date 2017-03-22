package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import data.Trial;

public abstract class Utils {
	
	/**
	 * @return String[]
	 */	
	public static String[] getFileNames(){
		JFileChooser chooser = new JFileChooser();
		//chooser.setCurrentDirectory(new java.io.File("C:\\Users\\Carlos\\Documents\\Vet - Bioquimica\\1 - Zaragoza\\data"));
		chooser.setDialogTitle("Select a folder...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		  //System.out.println("Directory: "+chooser.getSelectedFile());
		  File folder = chooser.getSelectedFile();
		  File[] listOfFiles = folder.listFiles();
		  String[] listOfNames = new String[listOfFiles.length];
		  for(int i=0;i<listOfFiles.length;i++)
			  listOfNames[i]=folder.getAbsolutePath()+"\\"+listOfFiles[i].getName();
		  return listOfNames;	
		}
		return null;
	}
	/**
	 * @param String filename
	 */
	public static boolean isAVI (String filename){
		String[] parts = filename.split("\\.");
		if(parts[1].equals("avi"))
			return true;
		else 
			return false;
	}
	
	public static Map<String,Trial> readTrials(){
		Map<String,Trial> trials = null;
		try {
	    	  String file = Utils.selectFile();
	    	  if(file==null)
	    		  return null;			
			  FileInputStream streamIn = new FileInputStream(file);
			  ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
			  trials = (HashMap<String, Trial>) objectinputstream.readObject();
		} catch (Exception e) {e.printStackTrace();}
		return trials;
	}

	public static void saveTrials(Map<String,Trial> trials){
	    
		String filename = "";
		String dir = "";
		JFileChooser c = new JFileChooser();
		int rVal = c.showSaveDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
		  filename = c.getSelectedFile().getName();
		  dir=c.getCurrentDirectory().toString();
		}
		System.out.println(dir);
		try{
//			String folder = Utils.selectFolder();
			if(dir==null || dir.equals(""))
				return;
			FileOutputStream fos = new FileOutputStream(dir+"\\"+filename); 
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(trials);
			oos.close();
			fos.close();
		}catch(IOException ioe){ioe.printStackTrace();}		
	}
	/**
	 * @return String
	 */	
	public static String selectFile(){
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
	 * @return String
	 */	
	public static String selectFolder(){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select a folder...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		  //System.out.println("Directory: "+chooser.getSelectedFile());
		  File folder = chooser.getSelectedFile();
		  return folder.getAbsolutePath();	
		}
		return null;
	}	
}
