package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_5axis;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.jogInterface.JogInterface;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.marlinInterface.MarlinInterface;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.programInterface.ProgramInterface;

public class RobotArmInterface extends JPanel {
	private static final long serialVersionUID = 1L;
	private MarlinInterface marlinInterface;
	private JogInterface jogInterface;
	private ProgramInterface programInterface;
	
	public RobotArmInterface(RobotArmIK sixi3) {
		super();
		
		marlinInterface = new MarlinInterface(sixi3);
		jogInterface = new JogInterface(sixi3);
		programInterface = new ProgramInterface(sixi3);
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("MarlinInterface", marlinInterface);
		pane.addTab("JogInterface", jogInterface);
		pane.addTab("ProgramInterface", programInterface);
		this.add(pane);
	}

	public void closeConnection() {
		marlinInterface.closeConnection();
	}
	
	// TEST

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("RobotArmInterface");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotArmInterface(new RobotArmIK(new Sixi3_5axis())));
		frame.pack();
		frame.setVisible(true);
	}
}
