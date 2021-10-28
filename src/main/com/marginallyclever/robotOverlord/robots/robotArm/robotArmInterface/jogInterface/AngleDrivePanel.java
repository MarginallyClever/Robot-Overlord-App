package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.jogInterface;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.demos.Sixi3;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

public class AngleDrivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton [] buttons;
	private JSpinner stepScale = new JSpinner(new SpinnerNumberModel(1,1,5,1));
	private Dial dial = new Dial();

	public AngleDrivePanel(RobotArmIK arm) {
		super();
		
		buttons = new JRadioButton[arm.getNumBones()];
		for(int i=0;i<buttons.length;++i) {
			buttons[i] = makeRadioButton(buttonGroup,arm.getBone(i).getName());
		}
		buttons[0].setSelected(true);

		dial.addActionListener((evt)-> onDialTurn(arm) );
		dial.setPreferredSize(new Dimension(120,120));
		
		this.setBorder(BorderFactory.createTitledBorder(AngleDrivePanel.class.getSimpleName()));
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=2;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel scaleSelection = new JPanel(new FlowLayout(SwingConstants.HORIZONTAL));
		scaleSelection.add(new JLabel("Scale 1/(2^-x)"));
		scaleSelection.add(stepScale);
		this.add(scaleSelection,c);

		c.gridwidth=1;
		c.gridy++;

		for(int i=0;i<buttons.length;++i) {
			this.add(buttons[i],c);
			c.gridy++;
		}
		
		c.gridx=1;
		c.gridy=1;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=buttons.length;
		c.gridheight=buttons.length;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		this.add(dial,c);
	}
	
	private double getMovementStepSize() {
		double d = ((Number)stepScale.getValue()).doubleValue();
		double scale = 1.0/Math.pow(2.0, d);
		return dial.getChange()*scale;
	}

	private void onDialTurn(RobotArmIK arm) {
		double [] fk = arm.getAngles();
		
		for(int i=0;i<buttons.length;++i) {
			if(buttons[i].isSelected()) {
				fk[i] += getMovementStepSize();
			}
		}
		
		arm.setAngles(fk);
		arm.setEndEffectorTarget(arm.getEndEffector());
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

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame(AngleDrivePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleDrivePanel(new RobotArmIK(new Sixi3())));
		frame.pack();
		frame.setVisible(true);
	}
}
