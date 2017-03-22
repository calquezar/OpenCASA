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
import analysis.Motility;

public class MainWindow extends JFrame {

	MainWindow mw;
	Chemotaxis ch;
	Motility ma;
	
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
					ma = new Motility();
					try{ma.run(mw);}
					catch(Exception e1){e1.printStackTrace();}
				}else if(label.equals("Viability")){
				}else if(label.equals("Morphometry")){}
			}
		} );		
		panel.add(btn, c);
	}
	
	/**
	 * This method creates the main user interface.
	 */
	public void createGUI() {
		JPanel  panel = new JPanel(new GridBagLayout());
		addButton("Motility",0,0,new Color(229,255,204),"/motility.png",panel);
		addButton("Chemotaxis",1,0,new Color(204,229,255),"/chemotaxis.png",panel);
		addButton("Viability",0,1,new Color(255,153,153),"/viability.png",panel);
		addButton("Morphometry",1,1,new Color(255,204,153),"/Morphometry.png",panel);
		this.setPreferredSize(new Dimension(600, 200));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(panel);
		this.pack();
		this.setVisible(true);
	}
}
