package com.marginallyclever.ro3.apps.pathtracer;

import javax.swing.*;
import java.awt.*;

/**
 * A panel to display a {@link java.awt.image.BufferedImage} in the top left corner without resizing.
 */
public class PathTracerResultPanel extends JPanel {
    private Image image;

    public PathTracerResultPanel() {
        super(null);
        setName(getClass().getSimpleName());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }
}
