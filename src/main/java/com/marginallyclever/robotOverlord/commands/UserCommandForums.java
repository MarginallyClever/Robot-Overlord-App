package com.marginallyclever.robotOverlord.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JMenuItem;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Go to the online help forums. This action is not an undoable action.
 * @author Admin
 *
 */
public class UserCommandForums extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String FORUM_URL = "https://www.marginallyclever.com/learn/forum/forum/sixi-robot-arm/";
	protected RobotOverlord ro;
	
	public UserCommandForums(RobotOverlord ro) {
		super("Online help forums");
        getAccessibleContext().setAccessibleDescription("Go to online forums");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			java.awt.Desktop.getDesktop().browse(URI.create(this.FORUM_URL));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
