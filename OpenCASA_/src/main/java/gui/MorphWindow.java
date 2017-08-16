package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class MorphWindow extends JFrame implements ChangeListener {

	
	JLabel imgLabel;
	
	/**
	 * Constructor. The main graphical user interface is created.
	 * @param title - String that is used as title of the window.
	 */
	public MorphWindow() throws HeadlessException {
		super();
		//Setting image 
		imgLabel = new JLabel();
		setImage();
	}

	/******************************************************/
	/**
	 * 
	 */
	public void showGUI() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		//natural height, maximum width
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;

		JButton btnNewImage = new JButton("New Image");
		btnNewImage.setBackground(Color.LIGHT_GRAY);
		//Add action listener
		btnNewImage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 

			}
		} );
		c.gridx = 0;
		c.gridy = 0;
//		c.gridwidth = 1;
		panel.add(btnNewImage, c);
		
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
		c.gridy = 1;
		c.gridwidth = 1;
		panel.add(btnOtsu,c);
		c.gridy = 2;
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
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
//		c.ipady = 40;  
		c.gridwidth = 10;
		c.gridx = 0;
		c.gridy = 3;
		panel.add(sldThreshold, c);			
		
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 6;
		//		c.gridwidth = 1;
		panel.add(imgLabel , c);
		
		JButton btnFinish = new JButton("Finish");
		btnFinish.setBackground(Color.YELLOW);
		//Add action listener
		btnFinish.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 

			} 
		} );
		//c.weightx = 0.5;
		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.ipady = 5;  
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		panel.add(btnFinish, c);


		
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

	public void setImage(){
		ImagePlus imp = IJ.openImage();
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		double h = screenSize.getHeight();
		
		int targetWidth = (int) (w*0.7);
		int targetHeight = (int) (h*0.7);
		
		ImageProcessor ip = imp.getProcessor();
	    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
	    ip = ip.resize(targetWidth, targetHeight);
	    imp.setProcessor(ip);
		imgLabel.setIcon(new ImageIcon(imp.getImage()));
		imgLabel.repaint();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
}
