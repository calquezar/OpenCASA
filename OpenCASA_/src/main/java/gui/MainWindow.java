package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import analysis.Chemotaxis;
import analysis.Morphometry;
import analysis.Motility;
import analysis.OscillatoryWalker;
import analysis.RandomPersistentWalker;
import analysis.Simulation;
import analysis.Viability;
import data.Params;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;
import utils.ComputerVision;

public class MainWindow extends JFrame {

	MainWindow mw;
	Chemotaxis ch;
	Motility mot;
	Viability via;
	Morphometry morph;
	Simulation sim;
	SettingsWindow sw;
	
	/**
	 * Constructor. The main graphical user interface is created.
	 * @param title - String that is used as title of the window.
	 */
	public MainWindow(String title) throws HeadlessException {
		super(title);
		createGUI();
		this.setPreferredSize(new Dimension(600, 300));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);		
		this.setLocationRelativeTo(null);
		mw = this;
		Params.resetParams();
	}

	public void addButton(final String label,int gridx,int gridy,Color background,String iconPath,JPanel panel){
		
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
		//Add action listener
		btn.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(label.equals("Chemotaxis")){
					ch = new Chemotaxis();
					try {ch.run(mw);} 
					catch (Exception e1) {e1.printStackTrace();}
				}else if(label.equals("Motility")){
					mot = new Motility();
					try{mot.run(mw);}
					catch(Exception e1){e1.printStackTrace();}
				}else if(label.equals("Viability")){
					via = new Viability();
					try{via.run(mw);}
					catch(Exception e1){e1.printStackTrace();}
				}else if(label.equals("Morphometry")){
					morph = new Morphometry();
					try{morph.run(mw);}
					catch(Exception e1){e1.printStackTrace();}
				}else if(label.equals("Simulation")){
					sim = new OscillatoryWalker();
					try{sim.run();}
					catch(Exception e1){e1.printStackTrace();}
				}else if(label.equals("Settings")){
					if(sw==null || !sw.isVisible()){
						sw = new SettingsWindow("Settings");
						sw.run(mw);
					}
				}
			}
		} );		
		panel.add(btn, c);
	}
	
	/**
	 * This method creates the main user interface.
	 */
	public void createGUI() {
		JPanel  panel = new JPanel(new GridBagLayout());
		addButton("Motility",0,0,new Color(255,255,255),"/motility.png",panel);
		addButton("Chemotaxis",1,0,new Color(255,255,255),"/chemotaxis.png",panel);
		addButton("Viability",0,1,new Color(255,255,255),"/viability.png",panel);
		addButton("Morphometry",1,1,new Color(255,255,255),"/Morphometry.png",panel);
		addButton("Simulation",0,2,new Color(255,255,255),"/Settings.png",panel);
		addButton("Settings",1,2,new Color(255,204,153),"/Settings.png",panel);
//		panel.setBackground(new Color(255,204,153));
		this.setContentPane(panel);
	}
}
