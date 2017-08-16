package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class MorphWindow extends JFrame implements ChangeListener {

	
	JLabel imgLabel;
	
	/**
	 * Constructor. The main graphical user interface is created.
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
	
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.ipady = 10;  
		panel.add(imgLabel , c);
		
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 10;
		c.gridheight = 1; 
		panel.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		JButton btn1 = new JButton("Btn 1");
		//Add action listener
		btn1.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 

			}
		} );
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1; 
		panel.add(btn1, c);
		
		JButton btn2 = new JButton("Btn 2");
		//Add action listener
		btn2.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 

			} 
		} );
		c.gridx = 9;
		c.gridy = 5;
		panel.add(btn2, c);
		
		JFrame frame = new JFrame("Adjust Threshold");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		double width = screenSize.getWidth();
//		double height = screenSize.getHeight();
//		frame.setPreferredSize(new Dimension((int)width, 250));
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.pack();
//		frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		frame.setVisible(true);
	}
	/******************************************************/
	/**
	 * 
	 */
	public void setImage(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = screenSize.getWidth();
		double h = screenSize.getHeight();
		int targetWidth = (int) (w*0.6);
		int targetHeight = (int) (h*0.6);
		
		ImagePlus imp = IJ.openImage();
		ImageProcessor ip = imp.getProcessor();
	    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
	    ip = ip.resize(targetWidth, targetHeight);
	    imp.setProcessor(ip);
		imgLabel.setIcon(new ImageIcon(imp.getImage()));
		imgLabel.repaint();
	}
	/******************************************************/
	/**
	 * 
	 */
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
}
