package com.marginallyclever.robotoverlord.swing.componentmanagerpanel;

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
	    g2.fillRect(0,0,getWidth()-1,getHeight()-1);

		Color c0 = new Color(255,255,255,255-c.getAlpha());
		Color c1 = new Color(  128,  128,  128,255-c.getAlpha());

		int size=8;
		int stepsX = (int)Math.ceil(getWidth()/(float)size);
		int stepsY = (int)Math.ceil(getHeight()/(float)size);
		for(int x=0;x<stepsX;++x) {
			for(int y=0;y<stepsY;++y) {
				g2.setPaint(((x+y)%2==0) ? c0 : c1);
				g2.fillRect(x*size,y*size,size,size);
			}
		}
	}
}
