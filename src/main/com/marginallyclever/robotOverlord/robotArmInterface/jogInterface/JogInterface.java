package com.marginallyclever.robotOverlord.robotArmInterface.jogInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

public class JogInterface extends JPanel {
	private static final long serialVersionUID = 1L;
	private RobotArmIK myArm;
	private CartesianReportPanel eeReport, eeTargetReport;

	public JogInterface(RobotArmIK arm) {
		super();
		
		myArm = arm;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.weightx = 1;
		this.add(new AngleReportPanel(myArm), c);
		c.gridx++;
		c.weightx = 0;
		this.add(new AngleDrivePanel(myArm), c);
		c.gridx--;
		c.gridy++;
		c.weightx = 1;
		this.add(eeReport=new CartesianReportPanel(JogInterface.class.getName()+".EndEffector"), c);
		c.gridy++;
		this.add(eeTargetReport=new CartesianReportPanel(JogInterface.class.getName()+".EndEffectorTarget"), c);
		c.gridy--;
		c.gridx++;
		c.gridheight=2;
		c.weightx = 0;
		this.add(new CartesianDrivePanel(myArm), c);
		c.gridheight=1;
		c.gridx--;
		c.gridy+=2;
		c.gridwidth = 2;
		c.weightx = 1;
		this.add(new JacobianReportPanel(myArm), c);
		c.gridy++;
		c.weighty = 1;
		this.add(new JPanel(), c);

		myArm.addPropertyChangeListener( (e)-> updateReports() );
		
		updateReports();
	}
	
	private void updateReports() {
		Matrix4d m0=myArm.getEndEffector();
		eeReport.updateReport(m0);
		Matrix4d m1=myArm.getEndEffectorTarget();
		eeTargetReport.updateReport(m1);
	}

	public static void main(String[] args) {
		Log.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		JFrame frame = new JFrame(JogInterface.class.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JogInterface(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
