//package gui;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.HeadlessException;
//import java.awt.Image;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//
//import javax.imageio.ImageIO;
//import javax.swing.ButtonGroup;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JSlider;
//
//import analysis.Chemotaxis;
//import analysis.Morphometry;
//import analysis.Motility;
//import analysis.Viability;
//import data.OscillatoryWalker;
//import data.Params;
//import data.PersistentRandomWalker;
//import data.Simulation;
//import functions.ComputerVision;
//import ij.IJ;
//import ij.ImagePlus;
//import ij.ImageStack;
//import ij.plugin.ChannelSplitter;
//import ij.process.ImageProcessor;
//
//public class MorphWindow extends JFrame {
//
//
//
//	
//	/**
//	 * Constructor. The main graphical user interface is created.
//	 * @param title - String that is used as title of the window.
//	 */
//	public MorphWindow(String title) throws HeadlessException {
//		super(title);
//		createGUI();
//	}
//	/******************************************************/
//	/**
//	 * 
//	 */
//	public void createGUI() {
//
//		JPanel panel = new JPanel(new GridBagLayout());
//		GridBagConstraints c = new GridBagConstraints();
//		//natural height, maximum width
//		c.weightx = 0.5;
//		c.fill = GridBagConstraints.HORIZONTAL;
//
//		JButton btnCompleteSpermatozoon = new JButton("Complete");
//		btnCompleteSpermatozoon.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnCompleteSpermatozoon.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
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
//			} 
//		} );
//		c.gridx = 0;
//		c.gridy = 0;
//		c.gridwidth = 2;
//		panel.add(btnCompleteSpermatozoon, c);
//
//		JButton btnAcrosome = new JButton("Acrosome");
//		btnAcrosome.setBackground(Color.GREEN);
//		btnAcrosome.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				if(acrosomeImpOrig==null)
//					acrosomeImpOrig = IJ.openImage();
//				if(acrosomeImpOrig!=null){//Usefull when the user cancel before load an image
//					activeImage = 2;
//					acrosomeImpDraw = acrosomeImpOrig.duplicate();
//					imp.setProcessor(acrosomeImpDraw.getProcessor());
//					imp.setTitle("Acrosome");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}				
//					setAcrosomeImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)acrosomeThreshold);
//				}
//			} 
//		} );		
//		c.gridx = 2;
//		c.gridy = 0;
//		c.gridwidth = 2;
//		panel.add(btnAcrosome, c);
//
//		JButton btnNucleusA = new JButton("Nucleus A");
//		btnNucleusA.setBackground(Color.CYAN);
//		btnNucleusA.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				if(nucleusAImpOrig==null)
//					nucleusAImpOrig = IJ.openImage();
//				if(nucleusAImpOrig!=null){//Usefull when the user cancel before load an image
//					activeImage = 3;
//					nucleusAImpDraw = nucleusAImpOrig.duplicate();
//					imp.setProcessor(nucleusAImpDraw.getProcessor());
//					imp.setTitle("Alive's Nucleus");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}				
//					setNucleusAImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusAThreshold);
//				}
//			} 
//		} );		
//		c.gridx = 4;
//		c.gridy = 0;
//		c.gridwidth = 2;
//		panel.add(btnNucleusA, c);
//
//		JButton btnNucleusD = new JButton("Nucleus D");
//		btnNucleusD.setBackground(Color.MAGENTA);
//		btnNucleusD.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				if(nucleusDImpOrig==null)
//					nucleusDImpOrig = IJ.openImage();
//				if(nucleusDImpOrig!=null){//Usefull when the user cancel before load an image
//					activeImage = 4;
//					nucleusDImpDraw = nucleusDImpOrig.duplicate();
//					imp.setProcessor(nucleusDImpDraw.getProcessor());
//					imp.setTitle("Dead's Nucleus");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setNucleusDImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusDThreshold);	
//				}
//			} 
//		} );			
//		c.gridx = 6;
//		c.gridy = 0;
//		c.gridwidth = 2;
//		panel.add(btnNucleusD, c);	
//		
//		//RADIO BUTTONS
//		JRadioButton completeOtsuButton = new JRadioButton("Otsu");
//		completeOtsuButton.setSelected(true);
//		completeOtsuButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				completeThresholdMethod="Otsu";
//				completeThreshold = -1.0;
//				if((completeImpOrig!=null)&&(activeImage==1)){//Usefull when the user cancel before load an image
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
//			} 
//		} );		
//		JRadioButton completeMinimumButton = new JRadioButton("Minimum");
//		completeMinimumButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				completeThresholdMethod="Minimum";
//				completeThreshold = -1.0;
//				if((completeImpOrig!=null)&&(activeImage==1)){//Usefull when the user cancel before load an image
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
//			} 
//		} );	
//		//Group the radio buttons.
//		ButtonGroup completeGroup = new ButtonGroup();
//		completeGroup.add(completeOtsuButton);
//		completeGroup.add(completeMinimumButton);
//		c.gridx = 0;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		panel.add(completeOtsuButton,c);
//		c.gridy = 2;
//		panel.add(completeMinimumButton,c);
//		
//		JRadioButton acrosomeOtsuButton = new JRadioButton("Otsu");
//		acrosomeOtsuButton.setSelected(true);
//		acrosomeOtsuButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				acrosomeThresholdMethod="Otsu";
//				acrosomeThreshold = -1.0;
//				if((acrosomeImpOrig!=null)&&(activeImage==2)){//Usefull when the user cancel before load an image
//					acrosomeImpDraw = acrosomeImpOrig.duplicate();
//					imp.setProcessor(acrosomeImpDraw.getProcessor());
//					imp.setTitle("Acrosome");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setAcrosomeImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)acrosomeThreshold);
//				}
//			} 
//		} );			
//		JRadioButton acrosomeMinimumButton = new JRadioButton("Minimum");
//		acrosomeMinimumButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				acrosomeThresholdMethod="Minimum";
//				acrosomeThreshold = -1.0;
//				if((acrosomeImpOrig!=null)&&(activeImage==2)){//Usefull when the user cancel before load an image
//					acrosomeImpDraw = acrosomeImpOrig.duplicate();
//					imp.setProcessor(acrosomeImpDraw.getProcessor());
//					imp.setTitle("Acrosome");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setAcrosomeImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)acrosomeThreshold);
//				}
//			} 
//		} );			
//		//Group the radio buttons.
//		ButtonGroup acrosomeGroup = new ButtonGroup();
//		acrosomeGroup.add(acrosomeOtsuButton);
//		acrosomeGroup.add(acrosomeMinimumButton);
//		c.gridx = 2;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		panel.add(acrosomeOtsuButton,c);
//		c.gridy = 2;
//		panel.add(acrosomeMinimumButton,c);
//		
//		JRadioButton nucleusAOtsuButton = new JRadioButton("Otsu");
//		nucleusAOtsuButton.setSelected(true);
//		nucleusAOtsuButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				nucleusAThresholdMethod="Otsu";
//				nucleusAThreshold = -1.0;
//				if((nucleusAImpOrig!=null)&&(activeImage==3)){//Usefull when the user cancel before load an image
//					nucleusAImpDraw = nucleusAImpOrig.duplicate();
//					imp.setProcessor(nucleusAImpDraw.getProcessor());
//					imp.setTitle("NucleusA");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setNucleusAImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusAThreshold);
//				}
//			} 
//		} );			
//		JRadioButton nucleusAMinimumButton = new JRadioButton("Minimum");
//		nucleusAMinimumButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				nucleusAThresholdMethod="Minimum";
//				nucleusAThreshold = -1.0;
//				if((nucleusAImpOrig!=null)&&(activeImage==3)){//Usefull when the user cancel before load an image
//					nucleusAImpDraw = nucleusAImpOrig.duplicate();
//					imp.setProcessor(nucleusAImpDraw.getProcessor());
//					imp.setTitle("NucleusA");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setNucleusAImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusAThreshold);
//				}
//			} 
//		} );		
//		//Group the radio buttons.
//		ButtonGroup nucleusAGroup = new ButtonGroup();
//		nucleusAGroup.add(nucleusAOtsuButton);
//		nucleusAGroup.add(nucleusAMinimumButton);
//		c.gridx = 4;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		panel.add(nucleusAOtsuButton,c);
//		c.gridy = 2;
//		panel.add(nucleusAMinimumButton,c);
//		
//		JRadioButton nucleusDOtsuButton = new JRadioButton("Otsu");
//		nucleusDOtsuButton.setSelected(true);
//		nucleusDOtsuButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				nucleusDThresholdMethod="Otsu";
//				nucleusDThreshold = -1.0;
//				if((nucleusDImpOrig!=null)&&(activeImage==4)){//Usefull when the user cancel before load an image
//					nucleusDImpDraw = nucleusDImpOrig.duplicate();
//					imp.setProcessor(nucleusDImpDraw.getProcessor());
//					imp.setTitle("NucleusD");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setNucleusDImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusDThreshold);
//				}
//			} 
//		} );			
//		JRadioButton nucleusDMinimumButton = new JRadioButton("Minimum");
//		nucleusDMinimumButton.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {
//				nucleusDThresholdMethod="Minimum";
//				nucleusDThreshold = -1.0;
//				if((nucleusDImpOrig!=null)&&(activeImage==4)){//Usefull when the user cancel before load an image
//					nucleusDImpDraw = nucleusDImpOrig.duplicate();
//					imp.setProcessor(nucleusDImpDraw.getProcessor());
//					imp.setTitle("NucleusD");
//					imp.show();
//					if(!hasCanvas){
//						setCanvas();
//						hasCanvas=true;
//					}
//					setNucleusDImage(false);
//					hideSliderEvent=true;
//					sldThreshold.setValue((int)nucleusDThreshold);
//				}
//			} 
//		} );			
//		//Group the radio buttons.
//		ButtonGroup nucleusDGroup = new ButtonGroup();
//		nucleusDGroup.add(nucleusDOtsuButton);
//		nucleusDGroup.add(nucleusDMinimumButton);
//		c.gridx = 6;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		panel.add(nucleusDOtsuButton,c);
//		c.gridy = 2;
//		panel.add(nucleusDMinimumButton,c);		
//	
//		// THRESHOLD SLIDERBAR
//		sldThreshold= new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
//		sldThreshold.setMinorTickSpacing(2);
//		sldThreshold.setMajorTickSpacing(10);
//		sldThreshold.setPaintTicks(true);
//		sldThreshold.setPaintLabels(true);
//		// We'll just use the standard numeric labels for now...
//		sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
//		sldThreshold.addChangeListener(this);		
//		c.weightx = 1;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.ipady = 40;  
//		c.gridwidth = 8;
//		c.gridx = 0;
//		c.gridy = 3;
//		panel.add(sldThreshold, c);
//		
//		JButton btnMNAN = new JButton("MNAN");
//		btnMNAN.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnMNAN.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = true;
//				showMDAN = false;
//				showMNAD = false;
//				showMDAD = false;
//				showGreen = false;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: MNAN");
//				
//			} 
//		} );
//		c.ipady = 0;
//		c.gridx = 0;
//		c.gridy = 4;
//		c.gridwidth = 1;
//		panel.add(btnMNAN, c);
//		
//		JButton btnMDAN = new JButton("MDAN");
//		btnMDAN.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnMDAN.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = false;
//				showMDAN = true;
//				showMNAD = false;
//				showMDAD = false;
//				showGreen = false;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: MDAN");
//			} 
//		} );
//		c.gridx = 1;
//		c.gridy = 4;
//		panel.add(btnMDAN, c);		
//		
//		JButton btnMNAD = new JButton("MNAD");
//		btnMNAD.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnMNAD.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = false;
//				showMDAN = false;
//				showMNAD = true;
//				showMDAD = false;
//				showGreen = false;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: MNAD");
//			} 
//		} );
//		c.gridx = 2;
//		c.gridy = 4;
//		panel.add(btnMNAD, c);		
//
//		JButton btnMDAD = new JButton("MDAD");
//		btnMDAD.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnMDAD.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = false;
//				showMDAN = false;
//				showMNAD =false;
//				showMDAD = true;
//				showGreen = false;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: MDAD");
//			} 
//		} );
//		c.gridx = 3;
//		c.gridy =4;
//		panel.add(btnMDAD, c);
//		
//		JButton btnGreen= new JButton("Green");
//		btnGreen.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnGreen.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = false;
//				showMDAN = false;
//				showMNAD =false;
//				showMDAD = false;
//				showGreen = true;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: Green");
//			} 
//		} );
//		c.gridx = 4;
//		c.gridy = 4;
//		panel.add(btnGreen, c);		
//
//		JButton btnUnknown= new JButton("Unknown");
//		btnUnknown.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnUnknown.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = false;
//				showMNAN = false;
//				showMDAN = false;
//				showMNAD = false;
//				showMDAD = false;
//				showGreen = false;
//				showUnknown = true;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: Unknown");
//			} 
//		} );
//		c.gridx = 5;
//		c.gridy = 4;
//		panel.add(btnUnknown, c);		
//
//		JButton btnAll= new JButton("No Pencil");
//		btnAll.setBackground(Color.LIGHT_GRAY);
//		//Add action listener
//		btnAll.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				showAll = true;
//				showMNAN = false;
//				showMDAN = false;
//				showMNAD = false;
//				showMDAD = false;
//				showGreen = false;
//				showUnknown = false;
//				//refreshImage();
//				JLPencilUsed.setText("Pencil Used: -");
//			} 
//		} );
//		c.gridx = 6;
//		c.gridy = 4;
//		panel.add(btnAll, c);	
//		
//		
//		JLPencilUsed = new JLabel("Pencil Used: -");//,JLabel.CENTER);
//		JLPencilUsed.setFont(new Font("Serif", Font.PLAIN, 22));
//		//c.ipady = 0;  
//		c.gridx = 0;
//		c.gridy = 5;
//		c.gridwidth = 8;
//		panel.add(JLPencilUsed , c);
//			
//		
//		JButton btnResetImages = new JButton("Reset Images");
//		btnResetImages.setBackground(Color.YELLOW);
//		//Add action listener
//		btnResetImages.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				imp.close();
//				//Here we'll store the original images
//				completeImpOrig = null;
//				acrosomeImpOrig = null;
//				nucleusAImpOrig = null;
//				nucleusDImpOrig = null;
//				//These ImagePlus will be used to draw over them
//				completeImpDraw = null;
//				acrosomeImpDraw = null;
//				nucleusAImpDraw = null;
//				nucleusDImpDraw = null;
//				//These ImagePlus will be used to calculate mean gray values
//				completeImpGray = null;
//				acrosomeImpGray = null;
//				nucleusAImpGray = null;
//				nucleusDImpGray = null;
//				//These ImagePlus will be used to identify spermatozoa
//				completeImpTh = null;
//				acrosomeImpTh = null;
//				nucleusAImpTh = null;
//				nucleusDImpTh = null;	
//				//These ImagePlus will be used to store outlines
//				completeImpOutline = null;
//				acrosomeImpOutline = null;
//				nucleusAImpOutline = null;
//				nucleusDImpOutline = null;
//
//				//Thresholds
//				completeThreshold = -1.0;
//				acrosomeThreshold = -1.0;
//				nucleusAThreshold = -1.0;
//				nucleusDThreshold = -1.0;
//	
//				//Variables used to identify spermatozoa
//				completeSpermatozoa = new ArrayList();
//				acrosomeSpermatozoa = new ArrayList();
//				nucleusASpermatozoa = new ArrayList();
//				nucleusDSpermatozoa = new ArrayList();
//	
//				//GUI elements and variables
//				hasCanvas = false;	
//			} 
//		} );
//		//c.weightx = 0.5;
//		//c.fill = GridBagConstraints.HORIZONTAL;
//		//c.ipady = 5;  
//		c.gridx = 7;
//		c.gridy = 4;
//		c.gridwidth = 1;
//		panel.add(btnResetImages, c);
//
//		
//		JFrame frame = new JFrame("Adjust Threshold");
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		double width = screenSize.getWidth();
//		double height = screenSize.getHeight();
//		frame.setPreferredSize(new Dimension((int)width, 250));
//		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setContentPane(panel);
//		frame.pack();
//		frame.setVisible(true);
//	}
//}
