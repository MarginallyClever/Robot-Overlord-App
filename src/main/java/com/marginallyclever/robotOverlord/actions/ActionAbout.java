package com.marginallyclever.robotOverlord.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Display an About dialog box
 * @author Admin
 *
 */
public class ActionAbout extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionAbout(RobotOverlord ro) {
		super("About");
        getAccessibleContext().setAccessibleDescription("About this program");
		this.ro = ro;
		addActionListener(this);
	}

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
