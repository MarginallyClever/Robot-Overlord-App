package com.marginallyclever.robotOverlord.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Checks online for a new version of Robot Overlord
 * @author Admin
 *
 */
public class ActionCheckForUpdate extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionCheckForUpdate(RobotOverlord ro) {
		super("Check for update");
        getAccessibleContext().setAccessibleDescription("Check if you are using the latest version of this program.");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		String updateURL = "https://github.com/MarginallyClever/Robot-Overlord-App/releases/latest";
		try {
			URL github = new URL(updateURL);
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false);  //you still need to handle redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			String inputLine;
			if ((inputLine = in.readLine()) != null) {
				// parse the URL in the text-only redirect
				String matchStart = "<a href=\"";
				String matchEnd = "\">";
				int start = inputLine.indexOf(matchStart);
				int end = inputLine.indexOf(matchEnd);
				if (start != -1 && end != -1) {
					inputLine = inputLine.substring(start + matchStart.length(), end);
					// parse the last part of the redirect URL, which contains the release tag (which is the VERSION)
					inputLine = inputLine.substring(inputLine.lastIndexOf("/") + 1);

					System.out.println("last release: " + inputLine);
					System.out.println("your VERSION: " + RobotOverlord.VERSION);
					//System.out.println(inputLine.compareTo(VERSION));

					if (inputLine.compareTo(RobotOverlord.VERSION) > 0) {
						JOptionPane.showMessageDialog(null, "A new version of this software is available.  The latest version is "+inputLine+"\n"
								+"Please visit http://www.marginallyclever.com/ to get the new hotness.");
					} else {
						JOptionPane.showMessageDialog(null, "This version is up to date.");
					}
				}
			} else {
				throw new Exception();
			}
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Sorry, I failed.  Please visit "+updateURL+" to check yourself.");
		}
	}
}
