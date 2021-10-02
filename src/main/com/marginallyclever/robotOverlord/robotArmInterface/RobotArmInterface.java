package com.marginallyclever.robotOverlord.robotArmInterface;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robotArmInterface.jogInterface.JogInterface;
import com.marginallyclever.robotOverlord.robotArmInterface.marlinInterface.MarlinInterface;
import com.marginallyclever.robotOverlord.robotArmInterface.programInterface.ProgramInterface;
import com.marginallyclever.robotOverlord.robots.sixi3.RobotArmIK;

public class RobotArmInterface extends JPanel {
	private static final long serialVersionUID = 1L;

	public RobotArmInterface(RobotArmIK sixi3) {
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
		frame.add(new RobotArmInterface(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
