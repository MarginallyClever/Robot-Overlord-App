package com.marginallyclever.ro3.apps.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;

/**
 * Open a URL in the desktop web browser.
 */
public class BrowseURLAction extends AbstractAction implements ActionListener {
	private final Logger logger = LoggerFactory.getLogger(BrowseURLAction.class);
	private final String url;

    public BrowseURLAction(String url) {
		super("Browse URL");
		if(url == null || url.isBlank()) throw new InvalidParameterException("URL is null or blank.");

		this.url = url;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(URI.create(url));
			} catch (IOException e1) {
				logger.error("Error opening URL {}.",url,e1);
			}
		} else {
			String message =  "Web browsing is not supported.  Failed to open "+url;
			logger.error(message);
			JOptionPane.showMessageDialog((Component)e.getSource(),message);
		}
	}
}
