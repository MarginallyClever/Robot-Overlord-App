package com.marginallyclever.robotoverlord.swinginterface.view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A {@link JButton} filled with the background color.  Especially useful for color selection dialogs.
 * @author Dan Royer
 * @since 7.31.0
 *
 */
public class BackgroundPaintedButton extends JButton {
	public BackgroundPaintedButton(String label) {
		super(label);
		this.setOpaque(true);
		this.setMinimumSize(new Dimension(80,20));
		this.setMaximumSize(this.getMinimumSize());
		this.setPreferredSize(this.getMinimumSize());
		this.setSize(this.getMinimumSize());
		this.setBorder(new LineBorder(Color.BLACK));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D)g;

	    Color c = getBackground();
	    g2.setPaint(c);
	    g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,0,0);

	}
}
