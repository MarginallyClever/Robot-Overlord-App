package com.marginallyclever.robotOverlord.arm3.uArm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.arm3.Arm3;
import com.marginallyclever.robotOverlord.arm3.Arm3ControlPanel;

public class UArmControlPanel extends Arm3ControlPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UArmControlPanel(RobotOverlord gui, Arm3 arm) {
		super(gui, arm);
		// TODO Auto-generated constructor stub
	}

	// respond to buttons
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == super.about ) {
			doAbout();
			return;
		}
		
		super.actionPerformed(e);
	}
	
	protected void doAbout() {
		JOptionPane.showMessageDialog(null,"<html><body>"
				+"<h1>uArm</h1>"
				+"<p>Created by uFactory</p><br>"
				+"<p>Robot Overlord code by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A three axis robot arm, modelled after the ABB model IRB 460.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/uarm'>Click here for more details</a>.</p>"
				+"</body></html>");
	}
}
