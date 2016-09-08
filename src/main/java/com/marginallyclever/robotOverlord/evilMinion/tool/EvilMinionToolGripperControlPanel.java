package com.marginallyclever.robotOverlord.evilMinion.tool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class EvilMinionToolGripperControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3185118388061444534L;
	
	private JButton arm5ServoOpen;
	private JButton arm5ServoClose;
	public JLabel servo;
	
	EvilMinionToolGripper tool;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	
	public EvilMinionToolGripperControlPanel(RobotOverlord gui,EvilMinionToolGripper tool) {
		super();
		
		this.tool = tool;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		
		CollapsiblePanel gripper = new CollapsiblePanel("Gripper");
		this.add(gripper,c);

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		JPanel p = new JPanel(new GridLayout(1,0));
		gripper.getContentPane().add(p);
		p.add(arm5ServoOpen = createButton("Open"));
		p.add(servo = new JLabel("0.00"));
		p.add(arm5ServoClose = createButton("Close"));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if( subject == arm5ServoOpen ) {
			tool.moveServo(-1);
		}
		if( subject == arm5ServoClose ) {
			tool.moveServo(1);
		}
	}
}
