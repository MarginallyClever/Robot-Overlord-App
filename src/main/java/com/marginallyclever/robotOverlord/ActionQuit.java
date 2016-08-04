package com.marginallyclever.robotOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionQuit extends JMenuItem implements ActionListener {
	protected RobotOverlord ro;
	
	public ActionQuit(RobotOverlord ro) {
		super("Quit");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.confirmClose();
	}
}
