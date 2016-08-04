package com.marginallyclever.robotOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionRedo extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionRedo(RobotOverlord ro) {
		super("Redo",KeyEvent.VK_Y);
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.redo();
	}
}
