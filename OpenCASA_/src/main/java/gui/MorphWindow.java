package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Spermatozoon;
import functions.ComputerVision;
import functions.Paint;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class MorphWindow extends JFrame implements ChangeListener,MouseListener {
	

	//Here we'll store the original images
	ImagePlus impOrig = null;
	//These ImagePlus will be used to draw over them
	ImagePlus impDraw = null;
	//These ImagePlus will be used to calculate mean gray values
	ImagePlus impGray = null;
	//These ImagePlus will be used to identify spermatozoa
	ImagePlus impTh = null;
	//These ImagePlus will be used to store outlines
	ImagePlus impOutline = null;
	
	double threshold = -1.0;
	String thresholdMethod = "Otsu";
	
	JLabel imgLabel;
	JLabel title;
	MainWindow mainW;
	
	List<ImagePlus> images;
	int imgIndex;
	
	JFrame frame;
	//Resize parameters
	double resizeFactor;
	double xFactor;
	double yFactor;
	
	//Variable used to store spermatozoa
	List<Spermatozoon> spermatozoa = new ArrayList<Spermatozoon>();
		
		
	/**
	 * Constructor. The main graphical user interface is created.
	 */
	public MorphWindow(MainWindow mw) throws HeadlessException {
		super();
		//Setting image 
		imgLabel = new JLabel();
		imgLabel.addMouseListener(this);
		mainW = mw;
		imgIndex = 0;
		resizeFactor = 0.6;
		
	}
	/******************************************************/
	/**
	 * 
	 */
	public void showWindow() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//RADIO BUTTONS
		JRadioButton btnOtsu = new JRadioButton("Otsu");
		btnOtsu.setSelected(true);
		btnOtsu.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {

			} 
		} );
		JRadioButton btnMinimum = new JRadioButton("Minimum");
		btnMinimum.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {

			} 
		} );
		//Group the radio buttons.
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(btnOtsu);
		btnGroup.add(btnMinimum);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		panel.add(btnOtsu,c);
		c.gridy = 1;
		panel.add(btnMinimum,c);
		// THRESHOLD SLIDERBAR
		JSlider sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
		sldThreshold.setMinorTickSpacing(2);
		sldThreshold.setMajorTickSpacing(10);
		sldThreshold.setPaintTicks(true);
		sldThreshold.setPaintLabels(true);
		// We'll just use the standard numeric labels for now...
		sldThreshold.setLabelTable(sldThreshold.createStandardLabels(10));
		sldThreshold.addChangeListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 10;
		c.gridheight = 2;
		c.ipady = 10; 
		panel.add(sldThreshold, c);		
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 10;
		c.gridheight = 1; 
		panel.add(new JSeparator(SwingConstants.HORIZONTAL),c);
	
		title = new JLabel();
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.ipady = 10;  
		panel.add(title , c);
		
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.ipady = 10;  
		panel.add(imgLabel , c);
		setImage(0); //Initialization with the first image available
		
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 10;
		c.gridheight = 1; 
		panel.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		JButton btn1 = new JButton("Previous");
		//Add action listener
		btn1.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(imgIndex>0)
					setImage(--imgIndex);
			}
		} );
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		c.gridheight = 1; 
		panel.add(btn1, c);
		
		JButton btn2 = new JButton("Next");
		//Add action listener
		btn2.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(imgIndex<(images.size()-1))
						setImage(++imgIndex);
			} 
		} );
		c.gridx = 9;
		c.gridy = 6;
		panel.add(btn2, c);
		
		frame = new JFrame("Analyze morphometry...");
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		double width = screenSize.getWidth();
//		double height = screenSize.getHeight();
//		frame.setPreferredSize(new Dimension((int)width, 250));
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.pack();
//		frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent e) {
				close();
			    mainW.setVisible(true);
			  }
			});		
		frame.setVisible(true);
	}
	/******************************************************/
	/**
	 * 
	 */
	public void setImage(int index){
		if(index<0 || index>=images.size())
			return;
		impOrig = images.get(index).duplicate();
		impOrig.setTitle(images.get(index).getTitle());
		impDraw = impOrig.duplicate();
		title.setText(impOrig.getTitle());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		double h = screenSize.getHeight();
		int targetWidth = (int) (w*resizeFactor);
		int targetHeight = (int) (h*resizeFactor);
		
		ImageProcessor ip = impDraw.getProcessor();
	    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
	    ip = ip.resize(targetWidth, targetHeight);
	    impDraw.setProcessor(ip);
		imgLabel.setIcon(new ImageIcon(impDraw.getImage()));
		imgLabel.repaint();
//		impOrig.show();
		
		double origW = impOrig.getWidth();
		double origH = impOrig.getHeight();
		double resizeW = impDraw.getWidth();
		double resizeH = impDraw.getHeight();
		xFactor = origW/resizeW;
		yFactor = origH/resizeH;
		
		processImage(false);
	}
	
	public void setImage(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		double h = screenSize.getHeight();
		int targetWidth = (int) (w*resizeFactor);
		int targetHeight = (int) (h*resizeFactor);
		ImageProcessor ip = impDraw.getProcessor();
	    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
	    ip = ip.resize(targetWidth, targetHeight);
	    impDraw.setProcessor(ip);
		imgLabel.setIcon(new ImageIcon(impDraw.getImage()));
		imgLabel.repaint();		
	}
	/******************************************************/
	/**
	 * 
	 */	
	public void processImage(boolean isEvent){
		if(threshold==-1 || isEvent){//First time
			impTh = impOrig.duplicate();
			ComputerVision.convertToGrayscale(impTh);
			impGray = impTh.duplicate();
			thresholdImagePlus(impTh);
			List<Spermatozoon>[] sperm = ComputerVision.detectSpermatozoa(impTh);
			spermatozoa = sperm[0];
			//Calculate outlines
			impOutline = impTh.duplicate();
			ComputerVision.outlineThresholdImage(impOutline);
			idenfitySperm();
		}
		impDraw = impOrig.duplicate();
		Paint.drawOutline(impDraw,impOutline);
		Paint.drawBoundaries(impDraw,spermatozoa);
		setImage();
	}
	
	public void idenfitySperm(){
		int SpermNr=0;
		for (ListIterator<Spermatozoon> j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			SpermNr++;
			sperm.id = ""+SpermNr;
		}
	}
	
	/******************************************************/
	/**
	 * @param imp ImagePlus
	 */	
	public void thresholdImagePlus(ImagePlus imp){
		if(threshold==-1){
			ComputerVision.autoThresholdImagePlus(imp, thresholdMethod);
		}else{
			ComputerVision.thresholdImagePlus(imp, threshold);
		}
	}
	
	public void close(){
		impOrig.changes = false;
		impDraw.changes = false;
		impOrig.close();
		impDraw.close();
	}
	/******************************************************/
	/**
	 * 
	 */
	public void setImages(List<ImagePlus> i){
		images = i;
	}	
    /******************************************************	
	*	MOUSE LISTENER
	******************************************************/
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("X: "+ x+"; Y: "+ y);
		int realX = (int) (x*xFactor);
		int realY = (int) (y*yFactor);
		System.out.println("realX: "+ realX+"; realY: "+ realY);
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}		
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void stateChanged(ChangeEvent e) {}

}
