package com.marginallyclever.robotoverlord.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Creates an Icon based on a Unicode symbol.
 * @author Dan Royer
 * @since 2022-03-15
 */
public class UnicodeIcon implements Icon {
    private final static int HEIGHT = 24;
    private final static int WIDTH = 24;
    private final String unicode;

    public UnicodeIcon(String unicode) {
        super();
        this.unicode = unicode;
    }

    @Override
    public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
        //g.drawImage(image,0,0,new Color(1,1,1,1),null);
        Graphics g2 = g.create();
        Font font = new Font("SansSerif",Font.PLAIN,(int)((double)HEIGHT/1.25));
        g2.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x2 = (WIDTH - fm.stringWidth(unicode)) / 2;
        int y2 = (fm.getAscent() + (HEIGHT - (fm.getAscent() + fm.getDescent())) / 2);

        g2.setColor(Color.GRAY);
        g2.drawString(unicode,x2, y+y2);
    }

    @Override
    public int getIconWidth() {
        return WIDTH;
    }

    @Override
    public int getIconHeight() {
        return HEIGHT;
    }
}
