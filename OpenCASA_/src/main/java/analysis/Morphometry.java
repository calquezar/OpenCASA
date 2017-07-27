package analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Spermatozoon;
import functions.ComputerVision;
import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Morphometry implements Measurements,ChangeListener,MouseListener {
	
	public static double pixelsPerUm = 12.33;
	public static float minDistance = 10; //um
	//GENERIC DIALOG PARAMETERS
	static float	minSize = 20;//minimum sperm size (um^2)
	static float	maxSize = 50;//maximum sperm size (um^2)
	private double	pixelWidth=1.0, pixelHeight=1.0;
	String male = "";
	
	String date = "";
	ImagePlus imp = new ImagePlus();
	JLabel imgLabel = new JLabel();
	
	//Here we'll store the original images
	ImagePlus completeImpOrig = null;
	ImagePlus acrosomeImpOrig = null;
	ImagePlus nucleusAImpOrig = null;
	ImagePlus nucleusDImpOrig = null;
	//These ImagePlus will be used to draw over them
	ImagePlus completeImpDraw = null;
	ImagePlus acrosomeImpDraw = null;
	ImagePlus nucleusAImpDraw = null;
	ImagePlus nucleusDImpDraw = null;
	//These ImagePlus will be used to calculate mean gray values
	ImagePlus completeImpGray = null;
	ImagePlus acrosomeImpGray = null;
	ImagePlus nucleusAImpGray = null;
	ImagePlus nucleusDImpGray = null;
	//These ImagePlus will be used to identify spermatozoa
	ImagePlus completeImpTh = null;
	ImagePlus acrosomeImpTh = null;
	ImagePlus nucleusAImpTh = null;	
	ImagePlus nucleusDImpTh = null;
	//These ImagePlus will be used to store outlines
	ImagePlus completeImpOutline = null;
	ImagePlus acrosomeImpOutline = null;
	ImagePlus nucleusAImpOutline = null;	
	
	ImagePlus nucleusDImpOutline = null;
	//Thresholds
	double completeThreshold = -1.0;
	double acrosomeThreshold = -1.0;
	double nucleusAThreshold = -1.0;
	double nucleusDThreshold = -1.0;
	String completeThresholdMethod = "Otsu";
	String acrosomeThresholdMethod = "Otsu";
	String nucleusAThresholdMethod = "Otsu";
	String nucleusDThresholdMethod = "Otsu";
	
	boolean isThresholding =  false;
	//GUI elements and variables
	int activeImage = 1; // 1-Complete;2-Acrosome;3-NucleusA;4-NucleusD
	JSlider sldThreshold;
	boolean hasCanvas = false;
	ImageCanvas canvas;
	boolean hideSliderEvent = false;//Used to make transparent Slider event
	
	JLabel JLPencilUsed; //Used to identify manually subtypes
	//Variables used to identify spermatozoa
	List completeSpermatozoa = new ArrayList();
	List acrosomeSpermatozoa = new ArrayList();
	List nucleusASpermatozoa = new ArrayList();
	
	List nucleusDSpermatozoa = new ArrayList();
	//Variables used to show types
	boolean showAll = true;
	boolean showMNAN = false;
	boolean showMDAN = false;
	boolean showMNAD = false;
	boolean showMDAD = false;
	boolean showGreen = false;
	
	boolean showUnknown = false;
	
	//Results table
	ResultsTable morphometrics = new ResultsTable();

	public Morphometry() {}
	
	/******************************************************/
	/**
	 * @param
	 * @return
	 */
	public void changeSelectedStatus(String id,List spermatozoa){
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon candidate= (Spermatozoon)j.next();
			//IJ.log("candidate.id: "+candidate.id+"; id: "+id);
			if(candidate.id.equals(id) && id!="***"){
				//IJ.log("son iguales");
				candidate.selected=!candidate.selected;
				//break;
			}
		}
	}
	/******************************************************/
	/**
	 * @param 
	 * @return 
	 */	
	public void checkSelection(int x, int y){
		
		Point click = new Point(x,y);
		List spermatozoa = new ArrayList();
		switch(activeImage){
			case 1: spermatozoa = completeSpermatozoa;
					break;
			case 2: spermatozoa = acrosomeSpermatozoa;
					break;
			case 3: spermatozoa = nucleusASpermatozoa;
					break;
			case 4: spermatozoa = nucleusDSpermatozoa;
					break;						
		}
		
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			if(isClickInside(sperm,click)){
				
				if((!showAll)&&(activeImage==1)){
					if(showMNAN)
						sperm.type="MNAN";
					else if(showMDAN)
						sperm.type="MDAN";
					else if(showMNAD)
						sperm.type="MNAD";
					else if(showMDAD)
						sperm.type="MDAD";
					else if(showGreen)
						sperm.type="Green";
					else if(showUnknown)
						sperm.type="Unknown";
				}
				else {
					sperm.selected=!sperm.selected;
					if(activeImage!=1)
						changeSelectedStatus(sperm.id,completeSpermatozoa);
					if(activeImage!=2){
						changeSelectedStatus(sperm.id,acrosomeSpermatozoa);
						/*Spermatozoon acrosome = getSpermatozoon(sperm.id,acrosomeSpermatozoa);
						if(sperm.selected==acrosome.selected)
							IJ.log("Son iguales.ID: "+sperm.id);
						else
							IJ.log("Son distintos.ID: "+sperm.id);*/
					}
					if(activeImage!=3)
						changeSelectedStatus(sperm.id,nucleusASpermatozoa);
					if(activeImage!=4)
						changeSelectedStatus(sperm.id,nucleusDSpermatozoa);
					//IJ.log("Is Inside "+sperm.id);
					
					if(sperm.selected){
						Spermatozoon completeSpermatozoon = getSpermatozoon(sperm.id,completeSpermatozoa);
						generateResults(completeSpermatozoon);
					}
				}
				break;
			}
		}
	}
	public int[] convertLongArrayToInt(long[] orig){
		int[] arrayInt = new int[orig.length];
		for(int i=0;i<orig.length;i++)
			arrayInt[i] = (int)orig[i];
		return arrayInt;
	}
	/******************************************************/
	/**
	 * 
	 */
	public void createGUI() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		//natural height, maximum width
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;

		JButton btnCompleteSpermatozoon = new JButton("Complete");
		btnCompleteSpermatozoon.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnCompleteSpermatozoon.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
//				activeImage = 1;
//				if(completeImpOrig==null)
//					completeImpOrig = IJ.openImage();
//				if(completeImpOrig!=null){//Usefull when the user cancel before load an image
//					completeImpDraw = completeImpOrig.duplicate();
//					imp.setProcessor(completeImpDraw.getProcessor());
//					imp.setTitle("Complete");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setCompleteImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)completeThreshold);
				
//				}
				setImage();
			} 
		} );
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(btnCompleteSpermatozoon, c);

		
		JButton btnAcrosome = new JButton("Acrosome");
		btnAcrosome.setBackground(Color.GREEN);
		btnAcrosome.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(acrosomeImpOrig==null)
					acrosomeImpOrig = IJ.openImage();
				if(acrosomeImpOrig!=null){//Usefull when the user cancel before load an image
					activeImage = 2;
					acrosomeImpDraw = acrosomeImpOrig.duplicate();
					imp.setProcessor(acrosomeImpDraw.getProcessor());
					imp.setTitle("Acrosome");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}				
					setAcrosomeImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)acrosomeThreshold);
				}
			} 
		} );		
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(btnAcrosome, c);

		JButton btnNucleusA = new JButton("Nucleus A");
		btnNucleusA.setBackground(Color.CYAN);
		btnNucleusA.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(nucleusAImpOrig==null)
					nucleusAImpOrig = IJ.openImage();
				if(nucleusAImpOrig!=null){//Usefull when the user cancel before load an image
					activeImage = 3;
					nucleusAImpDraw = nucleusAImpOrig.duplicate();
					imp.setProcessor(nucleusAImpDraw.getProcessor());
					imp.setTitle("Alive's Nucleus");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}				
					setNucleusAImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusAThreshold);
				}
			} 
		} );		
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(btnNucleusA, c);

		JButton btnNucleusD = new JButton("Nucleus D");
		btnNucleusD.setBackground(Color.MAGENTA);
		btnNucleusD.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(nucleusDImpOrig==null)
					nucleusDImpOrig = IJ.openImage();
				if(nucleusDImpOrig!=null){//Usefull when the user cancel before load an image
					activeImage = 4;
					nucleusDImpDraw = nucleusDImpOrig.duplicate();
					imp.setProcessor(nucleusDImpDraw.getProcessor());
					imp.setTitle("Dead's Nucleus");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setNucleusDImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusDThreshold);	
				}
			} 
		} );			
		c.gridx = 6;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(btnNucleusD, c);	
		
		//RADIO BUTTONS
		JRadioButton completeOtsuButton = new JRadioButton("Otsu");
		completeOtsuButton.setSelected(true);
		completeOtsuButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				completeThresholdMethod="Otsu";
				completeThreshold = -1.0;
				if((completeImpOrig!=null)&&(activeImage==1)){//Usefull when the user cancel before load an image
					completeImpDraw = completeImpOrig.duplicate();
					imp.setProcessor(completeImpDraw.getProcessor());
					imp.setTitle("Complete");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setCompleteImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)completeThreshold);
				}
			} 
		} );		
		JRadioButton completeMinimumButton = new JRadioButton("Minimum");
		completeMinimumButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				completeThresholdMethod="Minimum";
				completeThreshold = -1.0;
				if((completeImpOrig!=null)&&(activeImage==1)){//Usefull when the user cancel before load an image
					completeImpDraw = completeImpOrig.duplicate();
					imp.setProcessor(completeImpDraw.getProcessor());
					imp.setTitle("Complete");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setCompleteImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)completeThreshold);
				}
			} 
		} );	
		//Group the radio buttons.
		ButtonGroup completeGroup = new ButtonGroup();
		completeGroup.add(completeOtsuButton);
		completeGroup.add(completeMinimumButton);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		panel.add(completeOtsuButton,c);
		c.gridy = 2;
		panel.add(completeMinimumButton,c);
		
		JRadioButton acrosomeOtsuButton = new JRadioButton("Otsu");
		acrosomeOtsuButton.setSelected(true);
		acrosomeOtsuButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				acrosomeThresholdMethod="Otsu";
				acrosomeThreshold = -1.0;
				if((acrosomeImpOrig!=null)&&(activeImage==2)){//Usefull when the user cancel before load an image
					acrosomeImpDraw = acrosomeImpOrig.duplicate();
					imp.setProcessor(acrosomeImpDraw.getProcessor());
					imp.setTitle("Acrosome");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setAcrosomeImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)acrosomeThreshold);
				}
			} 
		} );			
		JRadioButton acrosomeMinimumButton = new JRadioButton("Minimum");
		acrosomeMinimumButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				acrosomeThresholdMethod="Minimum";
				acrosomeThreshold = -1.0;
				if((acrosomeImpOrig!=null)&&(activeImage==2)){//Usefull when the user cancel before load an image
					acrosomeImpDraw = acrosomeImpOrig.duplicate();
					imp.setProcessor(acrosomeImpDraw.getProcessor());
					imp.setTitle("Acrosome");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setAcrosomeImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)acrosomeThreshold);
				}
			} 
		} );			
		//Group the radio buttons.
		ButtonGroup acrosomeGroup = new ButtonGroup();
		acrosomeGroup.add(acrosomeOtsuButton);
		acrosomeGroup.add(acrosomeMinimumButton);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		panel.add(acrosomeOtsuButton,c);
		c.gridy = 2;
		panel.add(acrosomeMinimumButton,c);
		
		JRadioButton nucleusAOtsuButton = new JRadioButton("Otsu");
		nucleusAOtsuButton.setSelected(true);
		nucleusAOtsuButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				nucleusAThresholdMethod="Otsu";
				nucleusAThreshold = -1.0;
				if((nucleusAImpOrig!=null)&&(activeImage==3)){//Usefull when the user cancel before load an image
					nucleusAImpDraw = nucleusAImpOrig.duplicate();
					imp.setProcessor(nucleusAImpDraw.getProcessor());
					imp.setTitle("NucleusA");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setNucleusAImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusAThreshold);
				}
			} 
		} );			
		JRadioButton nucleusAMinimumButton = new JRadioButton("Minimum");
		nucleusAMinimumButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				nucleusAThresholdMethod="Minimum";
				nucleusAThreshold = -1.0;
				if((nucleusAImpOrig!=null)&&(activeImage==3)){//Usefull when the user cancel before load an image
					nucleusAImpDraw = nucleusAImpOrig.duplicate();
					imp.setProcessor(nucleusAImpDraw.getProcessor());
					imp.setTitle("NucleusA");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setNucleusAImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusAThreshold);
				}
			} 
		} );		
		//Group the radio buttons.
		ButtonGroup nucleusAGroup = new ButtonGroup();
		nucleusAGroup.add(nucleusAOtsuButton);
		nucleusAGroup.add(nucleusAMinimumButton);
		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		panel.add(nucleusAOtsuButton,c);
		c.gridy = 2;
		panel.add(nucleusAMinimumButton,c);
		
		JRadioButton nucleusDOtsuButton = new JRadioButton("Otsu");
		nucleusDOtsuButton.setSelected(true);
		nucleusDOtsuButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				nucleusDThresholdMethod="Otsu";
				nucleusDThreshold = -1.0;
				if((nucleusDImpOrig!=null)&&(activeImage==4)){//Usefull when the user cancel before load an image
					nucleusDImpDraw = nucleusDImpOrig.duplicate();
					imp.setProcessor(nucleusDImpDraw.getProcessor());
					imp.setTitle("NucleusD");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setNucleusDImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusDThreshold);
				}
			} 
		} );			
		JRadioButton nucleusDMinimumButton = new JRadioButton("Minimum");
		nucleusDMinimumButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				nucleusDThresholdMethod="Minimum";
				nucleusDThreshold = -1.0;
				if((nucleusDImpOrig!=null)&&(activeImage==4)){//Usefull when the user cancel before load an image
					nucleusDImpDraw = nucleusDImpOrig.duplicate();
					imp.setProcessor(nucleusDImpDraw.getProcessor());
					imp.setTitle("NucleusD");
					imp.show();
					if(!hasCanvas){
						setCanvas();
						hasCanvas=true;
					}
					setNucleusDImage(false);
					hideSliderEvent=true;
					sldThreshold.setValue((int)nucleusDThreshold);
				}
			} 
		} );			
		//Group the radio buttons.
		ButtonGroup nucleusDGroup = new ButtonGroup();
		nucleusDGroup.add(nucleusDOtsuButton);
		nucleusDGroup.add(nucleusDMinimumButton);
		c.gridx = 6;
		c.gridy = 1;
		c.gridwidth = 1;
		panel.add(nucleusDOtsuButton,c);
		c.gridy = 2;
		panel.add(nucleusDMinimumButton,c);		
	
		// THRESHOLD SLIDERBAR
		sldThreshold= new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
		sldThreshold.setMinorTickSpacing(2);
		sldThreshold.setMajorTickSpacing(10);
		sldThreshold.setPaintTicks(true);
		sldThreshold.setPaintLabels(true);
		// We'll just use the standard numeric labels for now...
		sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
		sldThreshold.addChangeListener(this);		
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40;  
		c.gridwidth = 8;
		c.gridx = 0;
		c.gridy = 3;
		panel.add(sldThreshold, c);
		
		JButton btnMNAN = new JButton("MNAN");
		btnMNAN.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnMNAN.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = true;
				showMDAN = false;
				showMNAD = false;
				showMDAD = false;
				showGreen = false;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: MNAN");
				
			} 
		} );
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		panel.add(btnMNAN, c);
		
		JButton btnMDAN = new JButton("MDAN");
		btnMDAN.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnMDAN.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = false;
				showMDAN = true;
				showMNAD = false;
				showMDAD = false;
				showGreen = false;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: MDAN");
			} 
		} );
		c.gridx = 1;
		c.gridy = 4;
		panel.add(btnMDAN, c);		
		
		JButton btnMNAD = new JButton("MNAD");
		btnMNAD.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnMNAD.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = false;
				showMDAN = false;
				showMNAD = true;
				showMDAD = false;
				showGreen = false;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: MNAD");
			} 
		} );
		c.gridx = 2;
		c.gridy = 4;
		panel.add(btnMNAD, c);		

		JButton btnMDAD = new JButton("MDAD");
		btnMDAD.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnMDAD.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = false;
				showMDAN = false;
				showMNAD =false;
				showMDAD = true;
				showGreen = false;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: MDAD");
			} 
		} );
		c.gridx = 3;
		c.gridy =4;
		panel.add(btnMDAD, c);
		
		JButton btnGreen= new JButton("Green");
		btnGreen.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnGreen.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = false;
				showMDAN = false;
				showMNAD =false;
				showMDAD = false;
				showGreen = true;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: Green");
			} 
		} );
		c.gridx = 4;
		c.gridy = 4;
		panel.add(btnGreen, c);		

		JButton btnUnknown= new JButton("Unknown");
		btnUnknown.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnUnknown.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = false;
				showMNAN = false;
				showMDAN = false;
				showMNAD = false;
				showMDAD = false;
				showGreen = false;
				showUnknown = true;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: Unknown");
			} 
		} );
		c.gridx = 5;
		c.gridy = 4;
		panel.add(btnUnknown, c);		

		JButton btnAll= new JButton("No Pencil");
		btnAll.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnAll.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				showAll = true;
				showMNAN = false;
				showMDAN = false;
				showMNAD = false;
				showMDAD = false;
				showGreen = false;
				showUnknown = false;
				//refreshImage();
				JLPencilUsed.setText("Pencil Used: -");
			} 
		} );
		c.gridx = 6;
		c.gridy = 4;
		panel.add(btnAll, c);	
		
		
		JLPencilUsed = new JLabel("Pencil Used: -");//,JLabel.CENTER);
		JLPencilUsed.setFont(new Font("Serif", Font.PLAIN, 22));
		//c.ipady = 0;  
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 8;
		panel.add(JLPencilUsed , c);
			
		
		JButton btnResetImages = new JButton("Reset Images");
		btnResetImages.setBackground(Color.YELLOW);
		//Add action listener
		btnResetImages.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				imp.close();
				//Here we'll store the original images
				completeImpOrig = null;
				acrosomeImpOrig = null;
				nucleusAImpOrig = null;
				nucleusDImpOrig = null;
				//These ImagePlus will be used to draw over them
				completeImpDraw = null;
				acrosomeImpDraw = null;
				nucleusAImpDraw = null;
				nucleusDImpDraw = null;
				//These ImagePlus will be used to calculate mean gray values
				completeImpGray = null;
				acrosomeImpGray = null;
				nucleusAImpGray = null;
				nucleusDImpGray = null;
				//These ImagePlus will be used to identify spermatozoa
				completeImpTh = null;
				acrosomeImpTh = null;
				nucleusAImpTh = null;
				nucleusDImpTh = null;	
				//These ImagePlus will be used to store outlines
				completeImpOutline = null;
				acrosomeImpOutline = null;
				nucleusAImpOutline = null;
				nucleusDImpOutline = null;

				//Thresholds
				completeThreshold = -1.0;
				acrosomeThreshold = -1.0;
				nucleusAThreshold = -1.0;
				nucleusDThreshold = -1.0;
	
				//Variables used to identify spermatozoa
				completeSpermatozoa = new ArrayList();
				acrosomeSpermatozoa = new ArrayList();
				nucleusASpermatozoa = new ArrayList();
				nucleusDSpermatozoa = new ArrayList();
	
				//GUI elements and variables
				hasCanvas = false;	
			} 
		} );
		//c.weightx = 0.5;
		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.ipady = 5;  
		c.gridx = 7;
		c.gridy = 4;
		c.gridwidth = 1;
		panel.add(btnResetImages, c);

		
		c.gridx = 0;
		c.gridy = 7;
//		c.gridwidth = 1;
		panel.add(imgLabel, c);
		
		
		
		JFrame frame = new JFrame("Adjust Threshold");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		frame.setPreferredSize(new Dimension((int)width, 250));
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.pack();
		frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		frame.setVisible(true);
	}
	/******************************************************/
	/**
	 * @param imageP ImagePlus
	 * @return 2D-ArrayList with all spermatozoa detected for each frame
	 */
	public List detectSpermatozoa(ImagePlus imageP){

		int options = 0;//ParticleAnalyzer.DISPLAY_SUMMARY; // set all PA options false
		int measurements = MEAN+CENTROID+RECT+AREA+PERIMETER+FERET;
		// Initialize results table
		ResultsTable rt = new ResultsTable();
		rt.reset();
		// create storage for Spermatozoon positions
		List spermatozoa = new ArrayList();
		//////////////////////////////////////////////////////////////// 
		// Record Spermatozoon positions in an ArrayList
		////////////////////////////////////////////////////////////////
		ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
		pa.analyze(imageP, imageP.getProcessor());
		//rt.show("resultados");
		//if(iFrame==1)
		//	rt.show("Resultados");
		float[] sxRes = rt.getColumn(ResultsTable.X_CENTROID);
		float[] syRes = rt.getColumn(ResultsTable.Y_CENTROID);
		float[] bxRes = rt.getColumn(ResultsTable.ROI_X);
		float[] byRes = rt.getColumn(ResultsTable.ROI_Y);
		float[] widthRes = rt.getColumn(ResultsTable.ROI_WIDTH);
		float[] heightRes = rt.getColumn(ResultsTable.ROI_HEIGHT);
		float[] areaRes = rt.getColumn(ResultsTable.AREA);
		float[] perimeterRes = rt.getColumn(ResultsTable.PERIMETER);
		float[] feretRes = rt.getColumn(ResultsTable.FERET);
		float[] minFeretRes = rt.getColumn(ResultsTable.MIN_FERET);
		if (sxRes!=null){//There are some spermatozo
			for (int iPart=0; iPart<sxRes.length; iPart++) {
				//System.out.println("heightRes: "+heightRes[iPart]);
				Spermatozoon sperm = new Spermatozoon();	
				sperm.id="***";
				sperm.x=sxRes[iPart];
				sperm.y=syRes[iPart];
				sperm.z=activeImage;
				sperm.bx = bxRes[iPart];
				sperm.by = byRes[iPart];
				sperm.width = widthRes[iPart];
				sperm.height = heightRes[iPart];
				switch(activeImage){
					case 1://First frame => total morphometrics 
						sperm.total_area=areaRes[iPart];
						sperm.total_perimeter=perimeterRes[iPart];
						sperm.total_feret=feretRes[iPart];
						sperm.total_minFeret=minFeretRes[iPart];
						break;
					case 2://Acrosome
						sperm.acrosome_area=areaRes[iPart];
						sperm.acrosome_perimeter=perimeterRes[iPart];
						break;
					case 3://Alive's nucleus
					case 4://Dead's nucleus
						sperm.nucleus_area=areaRes[iPart];
						sperm.nucleus_perimeter=perimeterRes[iPart];
						sperm.nucleus_feret=feretRes[iPart];
						sperm.nucleus_minFeret=minFeretRes[iPart];
						break;
				}
				spermatozoa.add(sperm);
				IJ.showStatus("Identifying spermatozoa...");
			}
		}
		return spermatozoa;
	}
	/******************************************************/
	/**
	 *
	 */
    private void doMouseRefresh() {
		if(!isThresholding){
			isThresholding = true;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					switch(activeImage){
						case 1: completeImpDraw = completeImpOrig.duplicate();
								imp.setProcessor(completeImpDraw.getProcessor());
								setCompleteImage(false);							
								break;
						case 2: acrosomeImpDraw = acrosomeImpOrig.duplicate();
								imp.setProcessor(acrosomeImpDraw.getProcessor());
								setAcrosomeImage(false);
								break;
						case 3: nucleusAImpDraw = nucleusAImpOrig.duplicate();
								imp.setProcessor(nucleusAImpDraw.getProcessor());
								setNucleusAImage(false);
								break;
						case 4: nucleusDImpDraw = nucleusDImpOrig.duplicate();
								imp.setProcessor(nucleusDImpDraw.getProcessor());
								setNucleusDImage(false);
								break;						
					}
					isThresholding = false;
				}
			});  
			t1.start();
		}
    }
	int doOffset (int center, int maxSize, int displacement) {
		if ((center - displacement) < 2*displacement) {
			return (center + 4*displacement);
		}
		else {
			return (center - displacement);
		}
	}			

	private void doSliderRefresh() {
		if(!isThresholding){
			isThresholding = true;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					switch(activeImage){
						case 1: completeImpDraw = completeImpOrig.duplicate();
								imp.setProcessor(completeImpDraw.getProcessor());
								setCompleteImage(true);
								break;
						case 2: acrosomeImpDraw = acrosomeImpOrig.duplicate();
								imp.setProcessor(acrosomeImpDraw.getProcessor());
								setAcrosomeImage(true);
								break;
						case 3: nucleusAImpDraw = nucleusAImpOrig.duplicate();
								imp.setProcessor(nucleusAImpDraw.getProcessor());
								setNucleusAImage(true);
								break;
						case 4: nucleusDImpDraw = nucleusDImpOrig.duplicate();
								imp.setProcessor(nucleusDImpDraw.getProcessor());
								setNucleusDImage(true);
								break;						
					}
					resetAllSelections();
					isThresholding = false;
				}
			});  
			t1.start();
		}
    }	
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * @return 2D-ArrayList with all spermatozoa detected for each frame
	 */
	public void drawBoundaries(ImagePlus imp,List spermatozoa){
		int xHeight=imp.getHeight();
		int yWidth=imp.getWidth();	
		IJ.showStatus("Drawing boundaries...");
		ImageProcessor ip = imp.getProcessor();
		ip.setColor(Color.white);
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			if(sperm.selected)
				ip.drawRect((int)sperm.bx,(int)sperm.by,(int)sperm.width,(int)sperm.height);
			//Draw numbers
			ip.setFont(new Font("SansSerif", Font.PLAIN, 32));
			// we could do someboundary testing here to place the labels better when we are close to the edge
			ip.moveTo((int)(sperm.x/pixelWidth+0),doOffset((int)(sperm.y/pixelHeight),yWidth,5) );
			ip.drawString(sperm.id+"-"+sperm.type);
		}
	}
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 */	
	public void drawOutline(ImagePlus impOrig,ImagePlus impTh){

		IJ.showStatus("Changing background...");
		ColorProcessor ipOrig = (ColorProcessor)impOrig.getProcessor();
		ipOrig.setColor(Color.yellow);
		ImageProcessor ipTh = impTh.getProcessor();
		int ipWidth = ipOrig.getWidth();
		int ipHeight = ipOrig.getHeight();
		for (int x=0; x< ipWidth;x++){
			IJ.showStatus("scanning pixels...");				
			for (int y=0;y<ipHeight;y++){
				int pixel = ipTh.get(x,y);
				if(pixel==0)//It's background
					ipOrig.drawPixel(x,y);
			}
		}
	}
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 * @return 2D-ArrayList with all spermatozoa detected for each frame
	 */
	public void generateResults(Spermatozoon completeSpermatozoon){
			
		//int numSpermatozoa = completeSpermatozoa.size();
		//morphometrics.reset(); //Results table 
		
		//Find acrosome
		Spermatozoon acrPart = getSpermatozoon(completeSpermatozoon.id,acrosomeSpermatozoa);
		//Find nucleus
		Spermatozoon nclPart = new Spermatozoon();
		if(isAlive(completeSpermatozoon))
			nclPart= getSpermatozoon(completeSpermatozoon.id,nucleusASpermatozoa);
		else
			nclPart= getSpermatozoon(completeSpermatozoon.id,nucleusDSpermatozoa);
		//Morphometrics
		//Total
		double total_meanGray = (double)getMeanGrayValue(completeSpermatozoon,completeImpGray,completeImpTh);
		double total_area=completeSpermatozoon.total_area/Math.pow(pixelsPerUm,2);
		double total_perimeter=completeSpermatozoon.total_perimeter/pixelsPerUm; 
		double total_feret=completeSpermatozoon.total_feret/pixelsPerUm;
		double total_minFeret=completeSpermatozoon.total_minFeret/pixelsPerUm;
		double total_ellipticity=total_feret/total_minFeret;
		double total_roughness=4*Math.PI*total_area/(Math.pow(total_perimeter,2));
		double total_elongation=(total_feret-total_minFeret)/(total_feret+total_minFeret);
		double total_regularity=(Math.PI*total_feret*total_minFeret)/(4*total_area);
		//Acrosome
		double acrosome_meanGray=-1;
		double acrosome_area=-1;
		double acrosome_perimeter=-1;
		double acrosome_percentage=-1;
		if(acrPart!=null){
			acrosome_meanGray=(double)getMeanGrayValue(acrPart,acrosomeImpGray,acrosomeImpTh);
			acrosome_area=acrPart.acrosome_area/Math.pow(pixelsPerUm,2);
			acrosome_perimeter=acrPart.acrosome_perimeter/pixelsPerUm;
			acrosome_percentage=(acrosome_area/total_area)*100;
		}
		//Nucleus
		double nucleus_meanGray = -1;
		double nucleus_area=-1;
		double nucleus_perimeter=-1;
		double nucleus_feret=-1;
		double nucleus_minFeret=-1;
		double nucleus_ellipticity=-1;
		double nucleus_roughness=-1;
		double nucleus_elongation=-1;
		double nucleus_regularity=-1;
		if(nclPart!=null){
			if(isAlive(completeSpermatozoon))
				nucleus_meanGray = (double)getMeanGrayValue(nclPart,nucleusAImpGray,nucleusAImpTh);
			else
				nucleus_meanGray = (double)getMeanGrayValue(nclPart,nucleusDImpGray,nucleusDImpTh);
			nucleus_area=nclPart.nucleus_area/Math.pow(pixelsPerUm,2);
			nucleus_perimeter=nclPart.nucleus_perimeter/pixelsPerUm;
			nucleus_feret=nclPart.nucleus_feret/pixelsPerUm;
			nucleus_minFeret=nclPart.nucleus_minFeret/pixelsPerUm;
			nucleus_ellipticity=nucleus_feret/nucleus_minFeret;
			nucleus_roughness=4*Math.PI*nucleus_area/(Math.pow(nucleus_perimeter,2));
			nucleus_elongation=(nucleus_feret-nucleus_minFeret)/(nucleus_feret+nucleus_minFeret);
			nucleus_regularity=(Math.PI*nucleus_feret*nucleus_minFeret)/(4*nucleus_area);
		}
		morphometrics.incrementCounter();
		morphometrics.addValue("Male",male);
		morphometrics.addValue("Date",date);
		morphometrics.addValue("ID",completeSpermatozoon.id);
		morphometrics.addValue("Type",completeSpermatozoon.type);
		morphometrics.addValue("total_meanGray",total_meanGray);
		morphometrics.addValue("total_area(um^2)",total_area);
		morphometrics.addValue("total_perimeter(um)",total_perimeter);
		morphometrics.addValue("total_length(um)",total_feret);
		morphometrics.addValue("total_width(um)",total_minFeret);
		morphometrics.addValue("total_ellipticity",total_ellipticity);
		morphometrics.addValue("total_roughness",total_roughness);
		morphometrics.addValue("total_elongation",total_elongation);
		morphometrics.addValue("total_regularity",total_regularity);
		morphometrics.addValue("acrosome_meanGray",acrosome_meanGray);
		morphometrics.addValue("acrosome_area(um^2)",acrosome_area);
		morphometrics.addValue("acrosome_perimeter(um)",acrosome_perimeter);
		morphometrics.addValue("acrosome_percentage(%)",acrosome_percentage);
		morphometrics.addValue("nucleus_meanGray",nucleus_meanGray);
		morphometrics.addValue("nucleus_area(um^2)",nucleus_area);
		morphometrics.addValue("nucleus_perimeter(um)",nucleus_perimeter);
		morphometrics.addValue("nucleus_length(um)",nucleus_feret);
		morphometrics.addValue("nucleus_width(um)",nucleus_minFeret);
		morphometrics.addValue("nucleus_ellipticity",nucleus_ellipticity);
		morphometrics.addValue("nucleus_roughness",nucleus_roughness);
		morphometrics.addValue("nucleus_elongation",nucleus_elongation);
		morphometrics.addValue("nucleus_regularity",nucleus_regularity);	
		
		morphometrics.show("Morphometrics");
	}
	/******************************************************/
	/**
	 * @param
	 */	
	public float getMeanGrayValue(Spermatozoon part,ImagePlus impGray,ImagePlus impTh){

		ImageProcessor ipTh = impTh.getProcessor();
		ImageProcessor ipGray = impGray.getProcessor();
		int bx = (int)part.bx;
		int by = (int)part.by;
		int width = (int)part.width;
		int height = (int)part.height;
		float totalGray=0;
		float totalPixels=0;
		for (int x=bx; x< (width+bx);x++){
			IJ.showStatus("scanning pixels...");				
			for (int y=by;y<(height+by);y++){
				int pixel = ipTh.get(x,y);
				if(pixel==0){
					totalGray+=(float)ipGray.get(x,y);
					totalPixels++;
				}
			}
		}
		return totalGray/totalPixels;
	}
	/******************************************************/
	/**
	 * @param
	 * @return
	 */
	public Spermatozoon getSpermatozoon(String id,List spermatozoa){
		Spermatozoon spermatozoon = null;
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon candidate= (Spermatozoon)j.next();
			if(candidate.id.equals(id) && id!="***"){
				spermatozoon=candidate;
				break;
			}
		}
		return spermatozoon;
	}
	/**
	 * 
	 * 
	 */
	public void getThresholdFromSlider(){	
		if(activeImage==1)
			completeThreshold = sldThreshold.getValue();
		else if(activeImage==2)
			acrosomeThreshold = sldThreshold.getValue();
		else if(activeImage==3)	
			nucleusAThreshold = sldThreshold.getValue();
		else if (activeImage==4)
			nucleusDThreshold = sldThreshold.getValue();
	}
	
	public void idenfityCompletes(){//Coplete heads
		int SpermNr=0;
		IJ.showStatus("Calculating Completes...");
		for (ListIterator j=completeSpermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			// This must be the beginning of a new track
			//List aTrack = new ArrayList();
			SpermNr++;
			sperm.inTrack=true;
			sperm.id = ""+SpermNr;
		}
	}	
	
	public void idenfityParts(List SpermParts){//Acrosomes & Nucleus
		int SpermNr=0;
		
		IJ.showStatus("Calculating Tracks...");
		for (ListIterator j=completeSpermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();

			// This must be the beginning of a new track
			//List aTrack = new ArrayList();
			SpermNr++;
			sperm.inTrack=true;
			sperm.id = ""+SpermNr;
			//aTrack.add(sperm);
			//************************************************************* 
			// search in next frames for more spermatozoa to be added to track
			//*************************************************************
			boolean searchOn=true;
			Spermatozoon oldParticle=new Spermatozoon();
			Spermatozoon tmpParticle=new Spermatozoon();
			oldParticle.copy(sperm);

			boolean foundOne=false;
			Spermatozoon newParticle=new Spermatozoon();
			//*
			//* For each Spermatozoon in this frame
			//*
			for (ListIterator jF=SpermParts.listIterator();jF.hasNext() && searchOn;) {
				Spermatozoon testParticle =(Spermatozoon) jF.next();
				float distance = testParticle.distance(oldParticle);
				// record a Spermatozoon when it is within the search radius, and when it had not yet been claimed by another track
				if ( (distance < minDistance) && !testParticle.inTrack) {
					// if we had not found a Spermatozoon before, it is easy
					if (!foundOne) {
						tmpParticle=testParticle;
						testParticle.inTrack=true;
						//testParticle.trackNr=SpermNr;
						testParticle.id = ""+SpermNr;
						testParticle.type = oldParticle.type;
						newParticle.copy(testParticle);
						foundOne=true;
					}
					else {
						// if we had one before, we'll take this one if it is closer.  In any case, flag these spermatozoa
						testParticle.flag=true;
						if (distance < newParticle.distance(oldParticle)) {
							testParticle.inTrack=true;
							testParticle.id = ""+SpermNr;
							testParticle.type = oldParticle.type;
							newParticle.copy(testParticle);
							tmpParticle.inTrack=false;
							tmpParticle.id = "***";
							tmpParticle.type = "Unknown";
							tmpParticle=testParticle;
						}
						else {
							newParticle.flag=true;
						}
					}
				}
				else if (distance < minDistance) {
				// this Spermatozoon is already in another track but could have been part of this one
				// We have a number of choices here:
				// 1. Sort out to which track this Spermatozoon really belongs (but how?)
				// 2. Stop this track
				// 3. Stop this track, and also delete the remainder of the other one
				// 4. Stop this track and flag this Spermatozoon:
					testParticle.flag=true;
				}
			}
			if (!foundOne)
				searchOn=false;
			oldParticle.copy(newParticle);
			
		}
	}
	/******************************************************/
	/**
	 * @param spermatozoa 2D-ArrayList with all spermatozoa detected for each frame
	 * @return 2D-ArrayList with all tracks detected
	 */
	public List idenfityTracks(List[] spermatozoa){
		
		int nFrames = 4;
		List theTracks = new ArrayList();
		int SpermNr=0;
		
		IJ.showStatus("Calculating Tracks...");
		for (ListIterator j=spermatozoa[0].listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			// This must be the beginning of a new track
			List aTrack = new ArrayList();
			SpermNr++;
			sperm.inTrack=true;
			sperm.id = ""+SpermNr;
			aTrack.add(sperm);
			//************************************************************* 
			// search in next frames for more spermatozoa to be added to track
			//*************************************************************
			boolean searchOn=true;
			Spermatozoon oldParticle=new Spermatozoon();
			Spermatozoon tmpParticle=new Spermatozoon();
			oldParticle.copy(sperm);
			//*
			//* For each frame
			//*
			for (int iF=1; iF<=(nFrames-1);iF++) {
				boolean foundOne=false;
				Spermatozoon newParticle=new Spermatozoon();
				//*
				//* For each Spermatozoon in this frame
				//*
				for (ListIterator jF=spermatozoa[iF].listIterator();jF.hasNext() && searchOn;) {
					Spermatozoon testParticle =(Spermatozoon) jF.next();
					float distance = testParticle.distance(oldParticle);
					// record a Spermatozoon when it is within the search radius, and when it had not yet been claimed by another track
					if ( (distance < minDistance) && !testParticle.inTrack) {
						// if we had not found a Spermatozoon before, it is easy
						if (!foundOne) {
							tmpParticle=testParticle;
							testParticle.inTrack=true;
							//testParticle.trackNr=SpermNr;
							testParticle.id = ""+SpermNr;
							testParticle.type = oldParticle.type;
							newParticle.copy(testParticle);
							foundOne=true;
						}
						else {
							// if we had one before, we'll take this one if it is closer.  In any case, flag these spermatozoa
							testParticle.flag=true;
							if (distance < newParticle.distance(oldParticle)) {
								testParticle.inTrack=true;
								testParticle.id = ""+SpermNr;
								testParticle.type = oldParticle.type;
								newParticle.copy(testParticle);
								tmpParticle.inTrack=false;
								tmpParticle.id = "***";
								tmpParticle.type = "Unknown";
								tmpParticle=testParticle;
							}
							else {
								newParticle.flag=true;
							}
						}
					}
					else if (distance < minDistance) {
					// this Spermatozoon is already in another track but could have been part of this one
					// We have a number of choices here:
					// 1. Sort out to which track this Spermatozoon really belongs (but how?)
					// 2. Stop this track
					// 3. Stop this track, and also delete the remainder of the other one
					// 4. Stop this track and flag this Spermatozoon:
						testParticle.flag=true;
					}
				}
				if (foundOne)
					aTrack.add(newParticle);
				else
					searchOn=false;
				oldParticle.copy(newParticle);
			}
			theTracks.add(aTrack);
			
		}
		return theTracks;
	}
	/******************************************************/
	/**
	 * @param 
	 * @return 
	 */
	public void identifySpermatozoa(List[] spermatozoa){
		int nFrames = 4;
		int SpermNr = 1;
		for (ListIterator j=spermatozoa[0].listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			sperm.id = ""+SpermNr++;
			for (int i=1; i<=(nFrames-1); i++){
				Spermatozoon candidate = null;
				for (ListIterator k=spermatozoa[i].listIterator();k.hasNext();) {
					Spermatozoon newParticle=(Spermatozoon) k.next();
					float distance= sperm.distance(newParticle);
					if((distance<minDistance) &&(newParticle.id.equals("***"))){
						minDistance = distance;
						candidate = newParticle;
					}
				}
				if(candidate!=null){
					candidate.id = sperm.id;
					candidate.type = sperm.type;
				}
			}
		}
	}
	/******************************************************/
	/**
	 * @param 
	 * 
	 */
	public boolean isAlive(Spermatozoon p){
		if(p.type.equals("MNAD") || p.type.equals("MNAN"))
			return true;
		else
			return false;		
	}
	/******************************************************/
	/**
	 * @param 
	 * @return 
	 */	
	public boolean isClickInside(Spermatozoon part, Point click){
		//Get boundaries
		double offsetX = (double)part.bx;
		double offsetY = (double)part.by;
		int w=(int)part.width;
		int h=(int)part.height;
		//correct offset
		int pX = (int)(click.getX()-offsetX);
		int pY = (int)(click.getY()-offsetY);
		//IJ.log("offsetX: "+offsetX+" ; offsetY: "+offsetY+" ;w: "+w+"; h: "+h+"px: "+pX+"; py: "+pY);
		Rectangle r = new Rectangle(w,h);
		
		return r.contains(new Point(pX,pY));
	}

    /******************************************************	
	*	MOUSE LISTENER
	******************************************************/
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int offscreenX = canvas.offScreenX(x);
		int offscreenY = canvas.offScreenY(y);
		checkSelection(offscreenX,offscreenY);
		doMouseRefresh();
		imp.updateAndRepaintWindow();
	}
	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}		
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	/******************************************************/
	/**
	 * @param 
	 * @return 
	 */
	/*public void updateAllSpermatozoaList(){
		allSpermatozoa = new ArrayList[4];
		allSpermatozoa[0] = completeSpermatozoa;
		allSpermatozoa[1] = acrosomeSpermatozoa;
		allSpermatozoa[2] = nucleusASpermatozoa;
		allSpermatozoa[3] = nucleusDSpermatozoa;
	}*/
	/******************************************************/
	/**
	 * @param
	 */	
	public void outlineThresholdImage(ImagePlus imp){
		ImageProcessor ip = imp.getProcessor();
		BinaryProcessor bp = new BinaryProcessor((ByteProcessor)ip);
		bp.outline();
	}
	/******************************************************/
	/**
	 * @param
	 * @return
	 */
	public void resetAllSelections(){
		resetSelections(completeSpermatozoa);
		resetSelections(acrosomeSpermatozoa);
		resetSelections(nucleusASpermatozoa);
		resetSelections(nucleusDSpermatozoa);
	}	
	
	public void resetSelections(List spermatozoa){
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon spermatozoon= (Spermatozoon)j.next();
			spermatozoon.selected=false;
		}
	}	
	/******************************************************/
	/**
	 * @param arg String
	 */
	public void run(MainWindow mw) {

		
		//the stuff below is the box that pops up to ask for pertinant values - why doesn't it remember the values entered????		
		GenericDialog gd = new GenericDialog("Sperm Analyzer");
		gd.addMessage("PARAMETERS USED TO CALCULATE MORPHOMETRICS");
		gd.addNumericField("Scale (pixels per um):", (float)pixelsPerUm,2);	
		gd.addMessage("____________________________________________");		
		gd.addMessage("PARAMETERS USED TO DETECT PARTICLES:");
		gd.addNumericField("Minimum sperm size (um^2):", minSize,2);
		gd.addNumericField("Maximum sperm size (um^2):", maxSize,2);
		gd.addNumericField("Minimum distance between head and acrosome (um):", minDistance,2);		
		gd.addMessage("____________________________________________");
		gd.addMessage("PARAMETERS USED TO IDENTIFY SAMPLES");
		gd.addStringField("Male","");
		gd.addStringField("Date","");
		
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		
		//PARAMETERS USED TO CALCULATE MORPHOMETRICS
		pixelsPerUm = (double)gd.getNextNumber();
		//PARAMETERS USED TO DETECT PARTICLES
		minSize = (float)gd.getNextNumber()*(float)Math.pow(pixelsPerUm,2);
		maxSize = (float)gd.getNextNumber()*(float)Math.pow(pixelsPerUm,2);
		minDistance = (float)gd.getNextNumber()*(float)pixelsPerUm;
		//PARAMETERS USED TO IDENTIFY SAMPLES
		male = gd.getNextString();
		date = gd.getNextString();
		
		createGUI();
		morphometrics.show("Morphometrics");
	}
	
	public void setImage(){
		ImagePlus imp = IJ.openImage();
		imgLabel.setIcon(new ImageIcon(imp.getImage()));
		imgLabel.repaint();
	}
	
	double s2d(String s) {
		Double d;
		try {d = new Double(s);}
		catch (NumberFormatException e) {d = null;}
		if (d!=null)
			return(d.doubleValue());
		else
			return(0.0);
	}

	/******************************************************/
	/**
	 * 
	 */	
	public void setAcrosomeImage(boolean isEvent){
		if(acrosomeThreshold==-1 || isEvent){//First time
			acrosomeImpGray = ComputerVision.getGreenChannel(acrosomeImpOrig);
			acrosomeImpTh = acrosomeImpGray.duplicate();
			thresholdImagePlus(acrosomeImpTh);
			acrosomeSpermatozoa = detectSpermatozoa(acrosomeImpTh);
			//Calculate outlines
			acrosomeImpOutline = acrosomeImpTh.duplicate();
			outlineThresholdImage(acrosomeImpOutline);
			//identifySpermatozoa(allSpermatozoa);
			idenfityParts(acrosomeSpermatozoa);
		}
		drawOutline(imp,acrosomeImpOutline);
		drawBoundaries(imp,acrosomeSpermatozoa);
		imp.updateAndRepaintWindow();
	}
	/******************************************************/
	/**
	 * 
	 */	
	public void setCanvas(){
		ImageWindow win = imp.getWindow();
		canvas = win.getCanvas();
		canvas.addMouseListener(this);
	}
	/******************************************************/
	/**
	 * 
	 */	
	public void setCompleteImage(boolean isEvent){
		if(completeThreshold==-1 || isEvent){//First time
			completeImpTh = completeImpOrig.duplicate();
			ComputerVision.convertToGrayscale(completeImpTh);
			completeImpGray = completeImpTh.duplicate();
			thresholdImagePlus(completeImpTh);
			completeSpermatozoa = detectSpermatozoa(completeImpTh);
			typeSpermatozoa(imp,completeSpermatozoa);
			//Calculate outlines
			completeImpOutline = completeImpTh.duplicate();
			outlineThresholdImage(completeImpOutline);
			idenfityCompletes();
		}
		drawOutline(imp,completeImpOutline);
		drawBoundaries(imp,completeSpermatozoa);
		imp.updateAndRepaintWindow();
	}
	
	/******************************************************/
	/**
	 * 
	 */	
	public void setNucleusAImage(boolean isEvent){
		if(nucleusAThreshold==-1 || isEvent){//First time
			nucleusAImpGray = ComputerVision.getBlueChannel(nucleusAImpOrig);
			nucleusAImpTh = nucleusAImpGray.duplicate();
			thresholdImagePlus(nucleusAImpTh);
			nucleusASpermatozoa = detectSpermatozoa(nucleusAImpTh);
			//Calculate outlines
			nucleusAImpOutline = nucleusAImpTh.duplicate();
			outlineThresholdImage(nucleusAImpOutline);
			idenfityParts(nucleusASpermatozoa);
		}
		
		drawOutline(imp,nucleusAImpOutline);
		drawBoundaries(imp,nucleusASpermatozoa);
		imp.updateAndRepaintWindow();
	}
	/******************************************************/
	/**
	 * 
	 */		
	public void setNucleusDImage(boolean isEvent){
		if(nucleusDThreshold==-1 || isEvent){//First time
			nucleusDImpGray = ComputerVision.getRedChannel(nucleusDImpOrig);
			nucleusDImpTh = nucleusDImpGray.duplicate();
			thresholdImagePlus(nucleusDImpTh);
			nucleusDSpermatozoa = detectSpermatozoa(nucleusDImpTh);
			//Calculate outlines
			nucleusDImpOutline = nucleusDImpTh.duplicate();
			outlineThresholdImage(nucleusDImpOutline);
			idenfityParts(nucleusDSpermatozoa);
		}
		drawOutline(imp,nucleusDImpOutline);
		drawBoundaries(imp,nucleusDSpermatozoa);
		imp.updateAndRepaintWindow();
	}
	/******************************************************/		
	// Utility functions
	double sqr(double n) {return n*n;}
	/******************************************************/	
	/** Listen events from checkboxes and slider */
    public void stateChanged(ChangeEvent inEvent) {
    	Object auxWho = inEvent.getSource();
    	if ((auxWho==sldThreshold)) {
			if(!hideSliderEvent){
				getThresholdFromSlider();
				doSliderRefresh();
			}else{
				hideSliderEvent=false;
			}
    	}
    }
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 */	
	public void thresholdImagePlus(ImagePlus imp){
		
		ImageProcessor ip = imp.getProcessor();
		double lowerThreshold = 0;
		String thresholdMethod = "";
		if(activeImage==1){
			lowerThreshold = completeThreshold;
			thresholdMethod = completeThresholdMethod;
		}
		else if(activeImage==2){
			lowerThreshold = acrosomeThreshold;
			thresholdMethod = acrosomeThresholdMethod;
		}
		else if(activeImage==3){
			lowerThreshold = nucleusAThreshold;
			thresholdMethod = nucleusAThresholdMethod;
		}
		else if(activeImage==4){
			lowerThreshold = nucleusDThreshold;	
			thresholdMethod = nucleusDThresholdMethod;
		}			
		//First we look at if threshold has been set before
		//Else we have to calculate it
		if(lowerThreshold==-1){
			ImageStatistics st = ip.getStatistics();
			long[] histlong = st.getHistogram();
			int histogram[] = convertLongArrayToInt(histlong);
			AutoThresholder at = new AutoThresholder();
			lowerThreshold = (double)at.getThreshold(thresholdMethod,histogram); 
			hideSliderEvent=true;
			sldThreshold.setValue((int)lowerThreshold);
			
			if(activeImage==1)
				completeThreshold = lowerThreshold;
			else if(activeImage==2)
				acrosomeThreshold = lowerThreshold;
			else if(activeImage==3)	
				nucleusAThreshold = lowerThreshold;
			else if (activeImage==4)
				nucleusDThreshold = lowerThreshold;	
		}
		//Upper threshold set to maximum
		double upperThreshold = 255;	
		//Threshold image processor
		ComputerVision.thresholdImageProcessor(ip,lowerThreshold,upperThreshold);
	}

	/******************************************************/
	/**
	 * @param imp ImagePlus
	 */	
	public void thresholdImageProcessor(ImageProcessor ip,double lowerThreshold,double upperThreshold){
		//Make binary
		int[] lut = new int[256];
		for (int j=0; j<256; j++) {
			if (j>=lowerThreshold && j<=upperThreshold)
				lut[j] = (byte)0;
			else
				lut[j] = (byte)255;
		}
        ip.applyTable(lut);
	}

 	/******************************************************/
	/**
	 * @param
	 * @return 
	 */
	public void typeSpermatozoa(ImagePlus imp,List spermatozoa){
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
				Spermatozoon sperm=(Spermatozoon) j.next();
				typeSpermatozoon(imp,sperm);
		}
	}
	/******************************************************/
	/**
	 * @param
	 * @return 
	 */
	public void typeSpermatozoon(ImagePlus imp,Spermatozoon p){
		
		ColorProcessor cp = (ColorProcessor)imp.getProcessor();

		ImageStack hsbStack = cp.getHSBStack();
		ImageProcessor hueIp = hsbStack.getProcessor(1);
		ImageProcessor saturationIp = hsbStack.getProcessor(2);
		ImageProcessor brightnessIp = hsbStack.getProcessor(3);
		int red = 0;
		int green = 0;
		int blue = 0;
		for(int x=(int)p.bx;x<(int)(p.bx+p.width);x++){
			for(int y=(int)p.by;y<(int)(p.by+p.height);y++){
				int pixel = hueIp.get(x,y);
				if(pixel>200 || pixel<20)
					red++;
				else if(pixel>50 && pixel<125)
					green++;
				else if(pixel>130 && pixel<185)
					blue++;
			}
		}
		
		//Check type
		int total = red+green+blue;
		float redRate = 100*(float)red/(float)total;
		float greenRate = 100*(float)green/(float)total;
		float blueRate = 100*(float)blue/(float)total;
		
		if(redRate<10)
			if(greenRate<10)
				p.type="MNAD";//"Blue"
			else if(blueRate<10)
				p.type="Acrosome";//"Green"
			else
				p.type="MNAN";//"Green-Blue"
		else if(blueRate<10)
			if(greenRate>10)
				p.type="MDAN";//"Green-Red"
			else
				p.type="MDAD";//"Red"
	}	

}
