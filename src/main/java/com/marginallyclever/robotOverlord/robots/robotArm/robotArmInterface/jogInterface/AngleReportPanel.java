package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.jogInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.Robot;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

/**
 * Displays the angle of each robot joint.  To control the angles use {@link AngleDrivePanel}
 */
public class AngleReportPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final JSlider [] sliders;
	private final JLabel [] labels;
	
	public AngleReportPanel(Robot robot) {
		super();

		int numJoints = (int)robot.get(Robot.NUM_JOINTS);

		sliders = new JSlider[numJoints];
		labels = new JLabel[numJoints];
		this.setBorder(BorderFactory.createTitledBorder(AngleReportPanel.class.getSimpleName()));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;

		for(int i=0;i<numJoints;++i) {
			robot.set(Robot.ACTIVE_JOINT,i);
			c.gridx=0;
			c.weightx=0;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			this.add(new JLabel((String)robot.get(Robot.JOINT_NAME)),c);

			c.gridx=1;
			c.weightx=0;
			c.anchor=GridBagConstraints.EAST;
			c.fill = GridBagConstraints.NONE;
			this.add(labels[i]=new JLabel(),c);
			
			c.gridx=2;
			c.weightx=1;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			sliders[i] = makeSliderFromBone(robot,i);
			this.add(sliders[i],c);

			c.gridy++;
		}

		robot.addPropertyChangeListener((e)-> updateReport(robot) );
		updateReport(robot);
	}

	private void updateReport(Robot robot) {
		int numJoints = (int)robot.get(Robot.NUM_JOINTS);
		for(int i=0;i<numJoints;++i) {
			robot.set(Robot.ACTIVE_JOINT,i);
			double t = (double)robot.get(Robot.JOINT_VALUE);
			sliders[i].setValue((int)t);
			labels[i].setText(String.format("%.3f",t));
		}
	}

	private JSlider makeSliderFromBone(Robot robot,int i) {
		robot.set(Robot.ACTIVE_JOINT,i);

		double bottom = (double)robot.get(Robot.JOINT_RANGE_MIN);
		double top = (double)robot.get(Robot.JOINT_RANGE_MAX);
		double current = (double)robot.get(Robot.JOINT_VALUE);
		JSlider slider = new JSlider( (int)bottom, (int)top, (int)current );
		slider.setEnabled(false);
		return slider;
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		JFrame frame = new JFrame(AngleReportPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleReportPanel(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
