package com.marginallyclever.robotOverlord.robotArmInterface.jogInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.RobotArmIK;

public class JogInterface extends JPanel {
	private static final long serialVersionUID = 1L;
	private RobotArmIK mySixi3;
	private CartesianReportPanel eeReport, eeTargetReport;

	public JogInterface(RobotArmIK sixi3) {
		super();
		
		mySixi3 = sixi3;
		
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
		this.add(new AngleReportPanel(mySixi3), c);
		c.gridx++;
		c.weightx = 0;
		this.add(new AngleDrivePanel(mySixi3), c);
		c.gridx--;
		c.gridy++;
		c.weightx = 1;
		this.add(eeReport=new CartesianReportPanel("JogInterface.EndEffector"), c);
		c.gridy++;
		this.add(eeTargetReport=new CartesianReportPanel("JogInterface.EndEffectorTarget"), c);
		c.gridy--;
		c.gridx++;
		c.gridheight=2;
		c.weightx = 0;
		this.add(new CartesianDrivePanel(mySixi3), c);
		c.gridheight=1;
		c.gridx--;
		c.gridy+=2;
		c.gridwidth = 2;
		c.weightx = 1;
		this.add(new JacobianReportPanel(mySixi3), c);
		c.gridy++;
		c.weighty = 1;
		this.add(new JPanel(), c);

		mySixi3.addPropertyChangeListener( (e)-> updateReports() );
		
		updateReports();
	}
	
	private void updateReports() {
		//System.out.println("JogInterface.updateReports()");
		Matrix4d m0=mySixi3.getEndEffector();
		eeReport.updateReport(m0);
		Matrix4d m1=mySixi3.getEndEffectorTarget();
		eeTargetReport.updateReport(m1);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("JogControlPanel");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JogInterface(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
