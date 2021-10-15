package com.marginallyclever.robotOverlord.robotArmInterface.jogInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

public class AngleReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JSlider [] joint;
	private JLabel [] values;
	
	public AngleReportPanel(RobotArmIK arm) {
		super();
		
		joint = new JSlider[arm.getNumBones()];
		values = new JLabel[arm.getNumBones()];
		this.setBorder(BorderFactory.createTitledBorder(AngleReportPanel.class.getSimpleName()));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		
		for(int i=0;i<joint.length;++i) {
			RobotArmBone bone = arm.getBone(i);
			c.gridx=0;
			c.weightx=0;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			this.add(new JLabel(bone.getName()),c);
			c.gridx=1;
			c.weightx=0;
			c.anchor=GridBagConstraints.EAST;
			c.fill = GridBagConstraints.NONE;
			this.add(values[i]=new JLabel(),c);
			
			c.gridx=2;
			c.weightx=1;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			joint[i] = makeSliderFromBone(bone);
			this.add(joint[i],c);
			
			c.gridy++;
		}

		arm.addPropertyChangeListener((e)-> updateReport(arm) );
		updateReport(arm);
	}

	private void updateReport(RobotArmIK sixi3) {
		for(int i=0;i<joint.length;++i) {
			RobotArmBone bone = sixi3.getBone(i);
			double t = bone.getTheta();
			joint[i].setValue((int)t);
			values[i].setText(String.format("%.3f",t));
		}
	}

	private JSlider makeSliderFromBone(RobotArmBone b) {
		JSlider slider = new JSlider((int)b.getAngleMin(),
							(int)b.getAngleMax(),
							(int)b.getTheta());
		slider.setEnabled(false);
		return slider;
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame(AngleReportPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleReportPanel(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
