package gui;

import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MessageWindow extends JFrame {

	JLabel label;
	JPanel panel;
	public MessageWindow(String title) throws HeadlessException {
		super(title);
		panel = new JPanel();
		label = new JLabel("Pensando");
		label.setFont(new Font("Serif", Font.PLAIN, 24));
		panel.add(label);
		this.add(panel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void setText(String text){
		label.setText(text);
	}


}
