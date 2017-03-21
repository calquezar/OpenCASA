package utils;

import java.io.File;

import javax.swing.JFileChooser;

public abstract class Utils {

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
	
	/**
	 * 
	 * @return File[]
	 */	
	public static String[] getFileNames(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("F:\\VIDEOS QUIMIOTAXIS\\Validación voluntarios\\"));
		//chooser.setCurrentDirectory(new java.io.File("C:\\Users\\Carlos\\Documents\\Vet - Bioquimica\\1 - Zaragoza\\data"));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		  //System.out.println("Directory: "+chooser.getSelectedFile());
		  File folder = chooser.getSelectedFile();
		  File[] listOfFiles = folder.listFiles();
		  File directory = chooser.getSelectedFile();
		  String[] listOfNames = new String[listOfFiles.length];
		  for(int i=0;i<listOfFiles.length;i++)
			  listOfNames[i]=directory.getAbsolutePath()+"\\"+listOfFiles[i].getName();
		  return listOfNames;	
		}
		return null;
	}
}
