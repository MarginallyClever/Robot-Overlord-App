package com.marginallyclever.evilOverlord.ArmTool;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ArmToolGripperControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3185118388061444534L;
	
	private JButton arm5ServoOpen;
	private JButton arm5ServoClose;
	public JLabel servo;
	
	ArmToolGripper tool;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	
	public ArmToolGripperControlPanel(ArmToolGripper tool) {
		super();
		
		this.tool = tool;
		
		this.setLayout(new GridLayout(0,1));
		
		this.add(new JLabel("Gripper"));

		JPanel p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5ServoOpen = createButton("Open"));
		p.add(servo = new JLabel("0.00"));
		p.add(arm5ServoClose = createButton("Close"));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		// TODO Auto-generated method stub

		if( subject == arm5ServoOpen ) {
			tool.moveServo(-1);
		}
		if( subject == arm5ServoClose ) {
			tool.moveServo(1);
		}
	}

	void updateGUI() {
		servo.setText(Float.toString(tool.angle));
	}
}
