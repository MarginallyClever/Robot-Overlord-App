package com.marginallyclever.robotOverlord.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionQuit extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
