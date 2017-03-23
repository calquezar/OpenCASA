package data;

import java.io.Serializable;

import ij.ImagePlus;

public class Trial implements Serializable{
	public String ID = "";
	public String type = "";
	public String source = "";//the name of the source file
	public SList tracks= null;
	public ImagePlus imp = null;
	public int width = 0;
	public int height = 0;
	public int[] motileSperm = new int[2];//[0] - motile; [1] - nonMotile
	
	public Trial(){}
	
	public Trial(String ID, String type,String source,SList t){
		this.ID = ID;
		this.type = type;
		this.source = source;
		this.tracks = t;
	}
	public Trial(String ID, String type,String source,SList t,ImagePlus imp,int[] motileSperm){
		this.ID = ID;
		this.type = type;
		this.source = source;
		this.tracks = t;
		this.imp = imp;
		this.motileSperm = motileSperm;
	}
	public Trial(String ID, String type,String source,SList t,int width,int height){
		this.ID = ID;
		this.type = type;
		this.source = source;
		this.tracks = t;
		this.width = width;
		this.height = height;
	}
}
