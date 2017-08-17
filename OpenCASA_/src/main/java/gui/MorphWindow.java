package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
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

import data.Params;
import data.Spermatozoon;
import functions.ComputerVision;
import functions.Paint;
import functions.Utils;
import ij.ImagePlus;
import ij.measure.ResultsTable;
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
	
	boolean isThresholding =  false;
	JSlider sldThreshold;
	
	List<ImagePlus> images;
	int imgIndex;
	
	JFrame frame;
	//Resize parameters
	double resizeFactor;
	double xFactor;
	double yFactor;
	
	//Variable used to store spermatozoa
	List<Spermatozoon> spermatozoa = new ArrayList<Spermatozoon>();
		
	//Results table
	ResultsTable morphometrics = new ResultsTable();
	
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
	public void initImage(){
		setImage(0); //Initialization with the first image available
		processImage(false);
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
				threshold = -1.0;
				thresholdMethod="Otsu";
				processImage(false);
			} 
		} );
		JRadioButton btnMinimum = new JRadioButton("Minimum");
		btnMinimum.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				threshold = -1.0;
				thresholdMethod="Minimum";
				processImage(false);
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
		sldThreshold = new JSlider(JSlider.HORIZONTAL, 0, 255, 60);
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
		initImage(); //Initialization with the first image available
		
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 10;
		c.gridheight = 1; 
		panel.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
//		JButton btn1 = new JButton("Previous");
//		//Add action listener
//		btn1.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				if(imgIndex>0){
//					reset();
//					setImage(--imgIndex);
//					processImage(false);
//				}
//			}
//		} );
//		c.gridx = 0;
//		c.gridy = 6;
//		c.gridwidth = 1;
//		c.gridheight = 1; 
//		panel.add(btn1, c);
		
		JButton btn2 = new JButton("Next");
		//Add action listener
		btn2.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(imgIndex<(images.size()-1)){
					reset();
					setImage(++imgIndex);
					processImage(false);
				}
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
	public void setResizeFactor(){
		double origW = impOrig.getWidth();
		double origH = impOrig.getHeight();
		double resizeW = impDraw.getWidth();
		double resizeH = impDraw.getHeight();
		xFactor = origW/resizeW;
		yFactor = origH/resizeH;
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
		setImage();
		setResizeFactor();
	}
	/******************************************************/
	/**
	 * 
	 */	
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
	
	public void reset(){
		impOrig.close();
		impDraw.close();
		impGray.close();
		impTh.close();
		impOutline.close();
		threshold = -1.0;
		spermatozoa.clear();
	}
	/******************************************************/
	/**
	 * 
	 */	
	public void processImage(boolean isEvent){
		if(threshold==-1 || isEvent){//First time or threshold's refreshing event
			impTh = impOrig.duplicate();
			ComputerVision.convertToGrayscale(impTh);
			impGray = impTh.duplicate();
			thresholdImagePlus(impTh);
			//Update sliderbar with the new threshold
			sldThreshold.setValue((int)threshold);	
			List<Spermatozoon>[] sperm = ComputerVision.detectSpermatozoa(impTh);
			if(sperm != null)
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
			threshold = ComputerVision.autoThresholdImagePlus(imp, thresholdMethod);
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
//		System.out.println("X: "+ x+"; Y: "+ y);
		int realX = (int) (x*xFactor);
		int realY = (int) (y*yFactor);
//		System.out.println("realX: "+ realX+"; realY: "+ realY);
		checkSelection(realX,realY);
		doMouseRefresh();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}		
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
	/******************************************************/	
	/** Listen events from slider */
    public void stateChanged(ChangeEvent inEvent) {
    	Object auxWho = inEvent.getSource();
    	if ((auxWho==sldThreshold)) {
			//Updating threshold value from slider
			threshold = sldThreshold.getValue();
			doSliderRefresh();
    	}
    }
    
	private void doSliderRefresh() {
		if(!isThresholding){
			isThresholding = true;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					resetSelections();
					processImage(true);
					isThresholding = false;
				}
			});  
			t1.start();
		}
    }
	
	public void resetSelections(){
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon spermatozoon= (Spermatozoon)j.next();
			spermatozoon.selected=false;
		}
	}	
	 
	/******************************************************/
	/**
	 * @param 
	 * @return 
	 */	
	public void checkSelection(int x, int y){
		Point click = new Point(x,y);
		for (ListIterator j=spermatozoa.listIterator();j.hasNext();) {
			Spermatozoon sperm=(Spermatozoon) j.next();
			if(isClickInside(sperm,click)){
				sperm.selected=!sperm.selected;
				if(sperm.selected){
					Spermatozoon spermatozoon = Utils.getSpermatozoon(sperm.id,spermatozoa);
					generateResults(spermatozoon);
				}
				break;
			}
		}
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
	
	/******************************************************/
	/**
	 *
	 */
    private void doMouseRefresh() {
		if(!isThresholding){
			isThresholding = true;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					impDraw = impOrig.duplicate();
					Paint.drawOutline(impDraw,impOutline);
					Paint.drawBoundaries(impDraw,spermatozoa);
					setImage();	
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
	public void generateResults(Spermatozoon spermatozoon){
	
		double total_meanGray = (double)ComputerVision.getMeanGrayValue(spermatozoon,impGray,impTh);
		double total_area=spermatozoon.total_area*Math.pow(Params.micronPerPixel,2);
		double total_perimeter=spermatozoon.total_perimeter*Params.micronPerPixel; 
		double total_feret=spermatozoon.total_feret*Params.micronPerPixel;
		double total_minFeret=spermatozoon.total_minFeret*Params.micronPerPixel;
		double total_ellipticity=total_feret/total_minFeret;
		double total_roughness=4*Math.PI*total_area/(Math.pow(total_perimeter,2));
		double total_elongation=(total_feret-total_minFeret)/(total_feret+total_minFeret);
		double total_regularity=(Math.PI*total_feret*total_minFeret)/(4*total_area);

		morphometrics.incrementCounter();
//		morphometrics.addValue("Male",male);
//		morphometrics.addValue("Date",date);
		morphometrics.addValue("ID",spermatozoon.id);
		morphometrics.addValue("total_meanGray",total_meanGray);
		morphometrics.addValue("total_area(um^2)",total_area);
		morphometrics.addValue("total_perimeter(um)",total_perimeter);
		morphometrics.addValue("total_length(um)",total_feret);
		morphometrics.addValue("total_width(um)",total_minFeret);
		morphometrics.addValue("total_ellipticity",total_ellipticity);
		morphometrics.addValue("total_roughness",total_roughness);
		morphometrics.addValue("total_elongation",total_elongation);
		morphometrics.addValue("total_regularity",total_regularity);
		morphometrics.addValue("Filename",impOrig.getTitle());
		morphometrics.show("Morphometrics");
	}	

}
