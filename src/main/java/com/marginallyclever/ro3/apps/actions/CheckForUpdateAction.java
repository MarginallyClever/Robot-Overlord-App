package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.apps.RO3Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Checks online for a new version of Robot Overlord. This action is not undoable.
 * @author Dan Royer
 *
 */
public class CheckForUpdateAction extends AbstractAction implements ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(CheckForUpdateAction.class);
	public static final String UPDATE_URL = "https://github.com/MarginallyClever/Robot-Overlord-App/releases/latest";

	public CheckForUpdateAction() {
		super();
		putValue(Action.NAME,"Check for Update");
		putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-install-16.png"))));
        putValue(Action.SHORT_DESCRIPTION, "Check if there is a new version of Robot Overlord.");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Component source = (Component)arg0.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

		try {
			String latestVersion = getLatestVersionFromGithub();

			logger.info("last release: " + latestVersion);
			logger.info("your version: " + RO3Frame.VERSION);
			if (latestVersion.compareTo(RO3Frame.VERSION) > 0) {
				JOptionPane.showMessageDialog(parentFrame, "A new version of this software is available.\n" +
						"  The latest version is " + latestVersion + "\n"
						+ "Please visit http://www.marginallyclever.com/ to get the new hotness.");
			} else {
				JOptionPane.showMessageDialog(parentFrame, "This version is up to date.");
			}
		} catch (Exception e) {
			logger.error("Failed to get latest version.",e);
			JOptionPane.showMessageDialog(parentFrame, "Failed to get latest version.");
		}
	}

	public String getLatestVersionFromGithub() throws Exception {
		String inputLine = null;

		URL github = new URL(UPDATE_URL);
		HttpURLConnection conn = (HttpURLConnection) github.openConnection();
		conn.setInstanceFollowRedirects(false);  // you still need to handle redirect manually.
		HttpURLConnection.setFollowRedirects(false);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			if ((inputLine = in.readLine()) == null) throw new IOException("no data");
			// parse the URL in the text-only redirect
			String matchStart = "<a href=\"";
			String matchEnd = "\">";
			int start = inputLine.indexOf(matchStart);
			int end = inputLine.indexOf(matchEnd);
			if (start != -1 && end != -1) {
				inputLine = inputLine.substring(start + matchStart.length(), end);
				// parse the last part of the redirect URL, which contains the release tag (which is the VERSION)
				inputLine = inputLine.substring(inputLine.lastIndexOf("/") + 1);
			}
		}
		return inputLine;
	}
}
