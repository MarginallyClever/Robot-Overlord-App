package com.marginallyclever.robotOverlord.robotArmInterface.jogInterface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

public class AngleDrivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton [] buttons;
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
		
		this.setBorder(BorderFactory.createTitledBorder("AngleDrive"));
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.NORTHWEST;

		for(int i=0;i<buttons.length;++i) {
			this.add(buttons[i],c);
			c.gridy++;
		}
		
		c.gridx=1;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=buttons.length;
		c.gridheight=buttons.length;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		this.add(dial,c);
	}

	private void onDialTurn(RobotArmIK arm) {
		//System.out.println("FK " + buttonGroup.getSelection().getActionCommand() + " V"+dial.getChange());
		
		double [] fk = arm.getAngles();
		
		for(int i=0;i<buttons.length;++i) {
			if(buttons[i].isSelected()) {
				fk[i] += dial.getChange();
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

		JFrame frame = new JFrame("AngleDrivePanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleDrivePanel(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
