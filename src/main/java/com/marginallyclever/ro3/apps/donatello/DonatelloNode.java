package com.marginallyclever.ro3.apps.donatello;

import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * {@link Donatello} uses {@link DonatelloNode} to associate a {@link com.marginallyclever.ro3.node.Node} with its
 * Swing {@link javax.swing.JPanel} and remember the 2D position for graphing.
 */
public class DonatelloNode {
    private final Node node;
    private final ArrayList<JPanel> panelList = new ArrayList<>();
    private final Rectangle rectangle = new Rectangle();
    private final JFrame frame;
    private JPanel panel;

    public DonatelloNode(Node node) {
        this.node = node;
        frame = new JFrame(node.getName());
        node.getComponents(panelList);
    }

    private ArrayList<JPanel> getListOfPanels() {
        return new ArrayList<>(panelList);
    }

    /**
     * Assemble the components of a {@link Node} into a {@link JPanel}.
     * @return a {@link JPanel} containing all the components of the {@link Node}.
     */
    private JPanel buildComponent() {
        if(panel==null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for(var v : panelList) {
                panel.add(v);
            }
            var dims = panel.getPreferredSize();
            panel.setSize(dims);
            rectangle.setSize(dims);
            frame.add(panel);
            frame.pack();
        }
        return panel;
    }
    
    public JComponent getJComponent() {
        var p = buildComponent();
        p.setLocation(rectangle.x,rectangle.y);
        return p;
    }

    public Point getPosition() {
        return rectangle.getLocation();
    }

    public void setPosition(Point p) {
        this.rectangle.setLocation(p);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("x",rectangle.x);
        json.put("y",rectangle.y);
        return json;
    }

    public void fromJSON(JSONObject json) {
        rectangle.x = json.getInt("x");
        rectangle.y = json.getInt("y");
    }

    public Rectangle getRectangle() {
        return new Rectangle(rectangle);
    }

    public void setLocation(int x,int y) {
        rectangle.setLocation(x,y);
    }

    public void paint(GraphViewPanel dn,Graphics2D g,Rectangle clippingRegion) {
        var bi = drawJComponentToBufferedImageUnlessClipped(getJComponent(),clippingRegion,rectangle.x,rectangle.y);
        if(bi!=null) {
            // TODO draw the node at the correct position
            dn.drawBufferedImageAtPosition(g, bi, rectangle.x, rectangle.y);
        }
        /*
        // get the list of panels
        var list = getListOfPanels();
        // draw the list of panels.
        for(var v : list) {
            v.paint(g);
        }*/
    }

    /**
     * Draw a {@link JPanel} to a {@link BufferedImage} unless the panel is clipped by the .
     * @param panel          the {@link JPanel} to draw
     * @param clippingRegion the bounds of the panel in world coordinates.
     * @param x              the x position to draw the panel
     * @param y              the y position to draw the panel
     * @return a {@link BufferedImage} containing the panel or null.
     */
    public BufferedImage drawJComponentToBufferedImageUnlessClipped(JComponent panel, Rectangle clippingRegion, int x, int y) {
        if (panel.getWidth() == 0 || panel.getHeight() == 0) {
            return null;
        }

        // check the clipping region
        var dims = panel.getPreferredSize();
        Rectangle r = new Rectangle(x,y,dims.width,dims.height);
        if(!r.intersects(clippingRegion)) {
            return null;
        }

        return drawJComponentToBufferedImage(panel);
    }

    public BufferedImage drawJComponentToBufferedImage(JComponent panel) {
        BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        // JPanel requires a Graphics2D object to draw to.  Create one from the BufferedImage.
        Graphics2D g2d = bi.createGraphics();
        // draw it.
        panel.paint(g2d);
        // done with the Graphics2D object.
        g2d.dispose();
        return bi;
    }

    public void doLayout() {
        // get the list of panels
        var list = getListOfPanels();
        // layout the list of panels.
        for(var v : list) {
            v.validate();

        }
    }
}
