package com.marginallyclever.robotOverlord.textInterfaces;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class Sixi3Interface extends JPanel {
	private static final long serialVersionUID = 1L;

	public Sixi3Interface(Sixi3IK sixi3) {
		super();
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("MarlinInterface", new MarlinInterface(sixi3));
		pane.addTab("JogInterface", new JogInterface(sixi3));
		pane.addTab("ProgramInterface", new ProgramInterface(sixi3));
		this.add(pane);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("Sixi3Interface");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Sixi3Interface(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
