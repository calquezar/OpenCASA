package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import spermAnalysis.ChemotaxisAnalysis;
import spermAnalysis.MotilityAnalysis;

public class MainWindow extends JFrame {

	MainWindow mw;
	ChemotaxisAnalysis ch;
	MotilityAnalysis ma;
	/**
	 * Constructor. This method doesn't get any argument. The main graphical user interface is created.
	 * @throws HeadlessException
	 */
	public MainWindow() throws HeadlessException {
		createGUI();
		setLocationRelativeTo(null);
		mw = this;
	}
	/**
	 * Constructor. The main graphical user interface is created.
	 * @param gc - GraphicsConfiguration is passed to superclass JFrame constructor.
	 */
	public MainWindow(GraphicsConfiguration gc) {
		super(gc);
		createGUI();
		setLocationRelativeTo(null);
		mw = this;
	}
	/**
	 * Constructor. The main graphical user interface is created.
	 * @param title - String that is used as title of the window.
	 */
	public MainWindow(String title) throws HeadlessException {
		super(title);
		createGUI();
		setLocationRelativeTo(null);
		mw = this;
	}
	/**
	 * Constructor. The main graphical user interface is created.
	 * @param title - String that is used as title of the window.
	 * @param gc - GraphicsConfiguration is passed to superclass JFrame constructor.
	 */
	public MainWindow(String title, GraphicsConfiguration gc) {
		super(title, gc);
		createGUI();
		setLocationRelativeTo(null);
		mw = this;
	}
	public JButton createButton(String label,int gridx,int gridy,Color background,String iconPath,JPanel panel){
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady=0;
		c.gridx = gridx;
		c.gridy = gridy;
		JButton btn = new JButton(label);
		btn.setBackground(background);
		try{
//			Image img = ImageIO.read(getClass().getResource("/resources/motility.png"));
			Image img = ImageIO.read(getClass().getResource(iconPath));
			btn.setIcon(new ImageIcon(img));
		} catch (Exception ex) {System.out.println(ex);}
		panel.add(btn, c);
		return btn;
	}
	/**
	 * This method creates the main user interface.
	 */
	public void createGUI() {

		JPanel panel = new JPanel(new GridBagLayout());
//		JButton motilityBtn = createButton("Motility",0,0,new Color(229,255,204),"/motility.png",panel);
//		//Add action listener
//		motilityBtn.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { 
//				ma = new MotilityAnalysis();
//				ma.run(mw);
//			}
//		} );
		JButton chemotaxisBtn = createButton("Chemotaxis",1,0,new Color(204,229,255),"/chemotaxis.png",panel);
		//Add action listener
		chemotaxisBtn.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				ch = new ChemotaxisAnalysis();
				try {ch.run(mw);} 
				catch (Exception e1) {e1.printStackTrace();}
			}
		} );
//		JButton viabilityBtn = createButton("Viability",0,1,new Color(255,153,153),"/viability.png",panel);
//		//Add action listener
//		viabilityBtn.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) {}
//		} );
//		JButton morphometryBtn = createButton("Morphometry",1,1,new Color(255,204,153),"/Morphometry.png",panel);
//		//Add action listener
//		morphometryBtn.addActionListener(new ActionListener() { 
//			public void actionPerformed(ActionEvent e) { }
//		} );
		this.setPreferredSize(new Dimension(600, 200));
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(panel);
		this.pack();
		this.setVisible(true);
	}
}
