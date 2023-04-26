package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * Display an About dialog box. This action is not undoable.
 * @author Dan Royer
 *
 */
public class AboutAction extends AbstractAction implements ActionListener {
	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	public AboutAction() {
		super(Translator.get("AboutAction.name"));
        putValue(SHORT_DESCRIPTION, Translator.get("AboutAction.shortDescription"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(
				null,
				"<html><body>"
						+"<h1>"+RobotOverlord.APP_TITLE+" "+RobotOverlord.VERSION+"</h1>"
						+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
						+"<h4>Created by</h4>"
						+"<p>Dan Royer (dan@marginallyclever.com).</p>"
						+"<h4>Testers</h4>"
						+"<p>Omar al rafei (Arc robotics)</p>"
						+"<h4>More info</h4>"
						+"<p>To get the latest version please visit<br><a href='"+RobotOverlord.APP_URL+"'>"+RobotOverlord.APP_URL+"</a></p><br>"
						+"<p>This program is open source and free.  If this was helpful<br> to you, please buy me a thank you beer through Paypal.</p>"
						+"</body></html>");
	}

	public static void main(String[] args) {
		new AboutAction().actionPerformed(null);
	}
}
