package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class DriveCartesianPanel extends JPanel {
	private static final long serialVersionUID = -1276037188121630763L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton x = makeRadioButton(buttonGroup,"X");
	private JRadioButton y = makeRadioButton(buttonGroup,"Y");
	private JRadioButton z = makeRadioButton(buttonGroup,"Z");
	private JRadioButton roll = makeRadioButton(buttonGroup,"roll");
	private JRadioButton pitch = makeRadioButton(buttonGroup,"pitch");
	private JRadioButton yaw = makeRadioButton(buttonGroup,"yaw");
	private Dial dial = new Dial();

	DriveCartesianPanel() {
		x.setSelected(true);

		this.setBorder(BorderFactory.createTitledBorder("Finger tip control"));
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;
		
		this.add(x,c);
		c.gridy++;
		this.add(y,c);
		c.gridy++;
		this.add(z,c);
		c.gridy++;
		this.add(roll,c);
		c.gridy++;
		this.add(pitch,c);
		c.gridy++;
		this.add(yaw,c);
		
		c.gridx=2;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=6;
		c.gridheight=6;
		c.anchor=GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		this.add(dial,c);
		
		dial.addActionListener((e)->{
			notifyListeners(e);
		});
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}

	// OBSERVER PATTERN
	
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}
}
