package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JMenuItem;

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
	
	public UserCommandForums() {
		super("Online help forums");
        getAccessibleContext().setAccessibleDescription("Go to online forums");
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			java.awt.Desktop.getDesktop().browse(URI.create(this.FORUM_URL));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
