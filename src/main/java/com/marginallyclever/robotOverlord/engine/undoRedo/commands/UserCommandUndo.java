package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * step once back through undoable actions.
 * @author Dan Royer
 *
 */
public class UserCommandUndo extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public UserCommandUndo(RobotOverlord ro) {
		super("Undo",KeyEvent.VK_Z);
		this.ro = ro;
		this.setAccelerator(KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.undo();
	}
}
