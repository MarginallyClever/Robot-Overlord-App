package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Admin
 *
 */
public class UserCommandNew extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public UserCommandNew(RobotOverlord ro) {
		super("New",KeyEvent.VK_N);
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO don't check if they just saved?
        int result = JOptionPane.showConfirmDialog(
                ro.getMainFrame(),
                "Are you sure?",
                this.getText(),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
        	ro.newWorld();
        }
	}
}
