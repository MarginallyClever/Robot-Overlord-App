package com.marginallyclever.robotOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionNew extends JMenuItem implements ActionListener {
	protected RobotOverlord ro;
	
	public ActionNew(RobotOverlord ro) {
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
