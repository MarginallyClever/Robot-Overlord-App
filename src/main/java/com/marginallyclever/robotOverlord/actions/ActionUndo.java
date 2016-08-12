package com.marginallyclever.robotOverlord.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionUndo extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionUndo(RobotOverlord ro) {
		super("Undo",KeyEvent.VK_Z);
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.undo();
	}
}
