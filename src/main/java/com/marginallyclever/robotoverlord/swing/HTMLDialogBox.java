package com.marginallyclever.robotoverlord.swing;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Display a message dialog box with HTML inside.
 * @author Dan Royer
 *
 */
public class HTMLDialogBox {
	/**
	 * Turns HTML into a clickable dialog text component.
	 * @param html String of valid HTML.
	 * @return a JTextComponent with the HTML inside.
	 */
	public JTextComponent createHyperlinkListenableJEditorPane(String html) {
		final JEditorPane bottomText = new JEditorPane();
		bottomText.setContentType("text/html");
		bottomText.setEditable(false);
		bottomText.setText(html);
		bottomText.setOpaque(false);
		final HyperlinkListener hyperlinkListener = new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
						} catch (IOException | URISyntaxException exception) {
							// Auto-generated catch block
							exception.printStackTrace();
						}
					}

				}
			}
		};
		bottomText.addHyperlinkListener(hyperlinkListener);
		return bottomText;
	}


	/**
	 * Display a message dialog box with HTML inside.
	 * @param parent the parent component
	 * @param html the HTML to put in the dialog box
	 * @param title the title of the dialog box
	 */
	public void display(Component parent,String html,String title) {
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(html);
		JOptionPane.showMessageDialog(parent, bottomText, title, JOptionPane.INFORMATION_MESSAGE);
	}
}