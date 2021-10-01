package com.marginallyclever.robotOverlord.sixi3Interface.jogInterface;

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
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class AngleDrivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton [] buttons;
	private Dial dial = new Dial();

	public AngleDrivePanel(Sixi3IK sixi3) {
		super();
		
		buttons = new JRadioButton[sixi3.getNumBones()];
		for(int i=0;i<buttons.length;++i) {
			buttons[i] = makeRadioButton(buttonGroup,sixi3.getBone(i).getName());
		}
		buttons[0].setSelected(true);

		dial.addActionListener((evt)-> {
			//System.out.println("FK " + buttonGroup.getSelection().getActionCommand() + " V"+dial.getChange());
			
			double [] fk = sixi3.getAngles();
			
			for(int i=0;i<buttons.length;++i) {
				if(buttons[i].isSelected()) {
					fk[i] += dial.getChange();
				}
			}
			
			sixi3.setAngles(fk);
			sixi3.setEndEffectorTarget(sixi3.getEndEffector());
		});
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
		frame.add(new AngleDrivePanel(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
