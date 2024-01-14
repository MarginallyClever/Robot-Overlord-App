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
			logger.info("your version: " + RO3Frame.VERSION);
			String latestVersion = getLatestVersionFromGithub();
			logger.info("last release: " + latestVersion);
			int result = compareVersions(RO3Frame.VERSION, latestVersion);
			StringBuilder sb = new StringBuilder("<html><body>"
				+ "<p>This app is version <b>"+RO3Frame.VERSION+"</b>.</p>"
				+ "<p>The latest version is <b>"+latestVersion+"</b>.</p>");
			if(result < 0) {
				sb.append("<p>Please visit <a href='"+UPDATE_URL+"'>"+UPDATE_URL+"</a></p></body></html>");
			} else if(result > 0) {
				sb.append("<p>This version is newer than the latest release.</p>");
			} else {
				sb.append("<p>This version is up to date.</p>");
			}
			sb.append("</body></html>");
			JOptionPane.showMessageDialog(parentFrame,sb.toString());
		} catch (Exception e) {
			logger.error("Failed to get latest version.",e);
			JOptionPane.showMessageDialog(parentFrame, "Failed to get latest version.");
		}
	}

	/**
	 * Versions are in the form "a.b.c" where a,b,c are integers.
	 * @param current this verison.
	 * @param latest the latest version.
	 * @return 1 if current is newer than latest, 0 if they are the same, -1 if current is older than latest.
	 */
	private int compareVersions(String current, String latest) {
		String[] currentParts = current.split("\\.");
		String[] latestParts = latest.split("\\.");

		for(int i=0;i<Math.min(currentParts.length, latestParts.length);++i) {
			int a = Integer.parseInt(currentParts[i]);
			int b = Integer.parseInt(latestParts[i]);
			if(a>b) return 1;
			if(a<b) return -1;
		}

        return Integer.compare(currentParts.length, latestParts.length);
    }

	public String getLatestVersionFromGithub() throws Exception {
		URL github = new URL(UPDATE_URL);
		HttpURLConnection conn = (HttpURLConnection) github.openConnection();
		conn.setInstanceFollowRedirects(false);  // you still need to handle redirect manually.
		conn.setConnectTimeout(5000);
		HttpURLConnection.setFollowRedirects(false);
		conn.connect();
		//int responseCode = conn.getResponseCode();
		String responseMessage = conn.getHeaderField("Location");
		conn.disconnect();

		// parse the last part of the redirect URL, which contains the
		// release tag (which is the VERSION)
		return responseMessage.substring(responseMessage.lastIndexOf("/") + 1);
	}
}
