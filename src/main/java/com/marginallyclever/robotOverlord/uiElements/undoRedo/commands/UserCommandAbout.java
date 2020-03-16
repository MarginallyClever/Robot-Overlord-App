package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Display an About dialog box. This action is not an undoable action.
 * @author Admin
 *
 */
public class UserCommandAbout extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UserCommandAbout() {
		super("About");
        getAccessibleContext().setAccessibleDescription("About this program");
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(null,"<html><body>"
				+"<h1>"+RobotOverlord.APP_TITLE+" "+RobotOverlord.VERSION+"</h1>"
				+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>To get the latest version please visit<br><a href='"+RobotOverlord.APP_URL+"'>"+RobotOverlord.APP_URL+"</a></p><br>"
				+"<p>This program is open source and free.  If this was helpful<br> to you, please buy me a thank you beer through Paypal.</p>"
				+"</body></html>");
	}
}
