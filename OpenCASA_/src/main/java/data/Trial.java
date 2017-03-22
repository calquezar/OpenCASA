package data;

import java.io.Serializable;

import ij.ImagePlus;

public class Trial implements Serializable{
	public String ID = "";
	public String type = "";
	public String source = "";//the name of the source file
	public SList tracks= null;
	public ImagePlus imp = null;
	
	public Trial(){}
	
	public Trial(String ID, String type,String source,SList t){
		this.ID = ID;
		this.type = type;
		this.source = source;
		this.tracks = t;
	}
	public Trial(String ID, String type,String source,SList t,ImagePlus imp){
		this.ID = ID;
		this.type = type;
		this.source = source;
		this.tracks = t;
		this.imp = imp;
	}
}
