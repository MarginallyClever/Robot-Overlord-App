package com.marginallyclever.robotoverlord.swinginterface;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

@Deprecated
public class FooterBar extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;
	
	public FooterBar(Frame parent) {
		// create the status bar panel and shove it down the bottom of the frame
		
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		//this.setPreferredSize(new Dimension(parent.getWidth(), 16));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		statusLabel = new JLabel("");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(statusLabel);
	}

	public void setStatusLabelText(String statusMessage) {
		statusLabel.setText(statusMessage);
	}
}
