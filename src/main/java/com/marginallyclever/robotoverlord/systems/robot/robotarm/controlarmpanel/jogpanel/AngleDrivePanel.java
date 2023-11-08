package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jogpanel;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * Direct drive robot motors.  To display current robot motor position use {@link AngleReportPanel}.
 */
public class AngleDrivePanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final JRadioButton [] buttons;
	private final ScalePanel stepScale = new ScalePanel();
	private final Dial dial = new Dial();

	public AngleDrivePanel(Robot robot) {
		super(new GridBagLayout());

		int numJoints = (int)robot.get(Robot.NUM_JOINTS);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttons = new JRadioButton[numJoints];
		if(numJoints>0) {
			for (int i = 0; i < numJoints; ++i) {
				robot.set(Robot.ACTIVE_JOINT, i);
				buttons[i] = makeRadioButton(buttonGroup, (String) robot.get(Robot.JOINT_NAME));
			}
			buttons[0].setSelected(true);
		}

		dial.addActionListener((evt)-> onDialTurn(robot) );
		dial.setPreferredSize(new Dimension(120,120));
		
		this.setBorder(BorderFactory.createTitledBorder(AngleDrivePanel.class.getSimpleName()));

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=2;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		this.add(stepScale,c);

		c.gridwidth=1;
		c.gridy++;

		for (JRadioButton button : buttons) {
			this.add(button, c);
			c.gridy++;
		}
		
		c.gridx=1;
		c.gridy=1;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=1;
		c.gridheight=buttons.length;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		this.add(dial,c);
	}
	
	private double getMovementStepSize() {
		double d = stepScale.getScale();
		double scale = 10.0*Math.pow(10.0, -d);
		return dial.getChange()*scale;
	}

	private void onDialTurn(Robot robot) {
		for(int i=0;i<buttons.length;++i) {
			if(buttons[i].isSelected()) {
				robot.set(Robot.ACTIVE_JOINT,i);
				double angle = (double)robot.get(Robot.JOINT_VALUE);
				angle += getMovementStepSize();
				robot.set(Robot.JOINT_VALUE,angle);
				robot.set(Robot.END_EFFECTOR_TARGET,robot.get(Robot.END_EFFECTOR));
			}
		}
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}

	// TEST

	public static void main(String[] args) {
		Log.start();
		Translator.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		JFrame frame = new JFrame(AngleDrivePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleDrivePanel(new RobotComponent()));
		frame.pack();
		frame.setVisible(true);
	}
}
