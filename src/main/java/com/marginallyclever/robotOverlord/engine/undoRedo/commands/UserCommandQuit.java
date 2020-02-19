package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Quit the application. This action is not an undoable action.
 * @author Admin
 *
 */
public class UserCommandQuit extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public UserCommandQuit(RobotOverlord ro) {
		super("Quit");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.confirmClose();
	}
}
