package com.marginallyclever.ro3.apps.donatello;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Donatello} is a panel that displays the {@link com.marginallyclever.ro3.Registry}. as a 2D graph.  It
 * uses Reflection to the fields of each Node.
 */
public class Donatello extends App {
    private final Map<Node, DonatelloNode> nodePanels = new HashMap<>();
    private final Vector2d camera = new Vector2d();
    private double zoom = 1.0;
    private int previousX = 0;
    private int previousY = 0;
    private JFrame hiddenFrame;

    public Donatello() {
        super();

        // add mouse listeners to handle camera movement.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                previousX = e.getX();
                previousY = e.getY();

                super.mousePressed(e);
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(SwingUtilities.isMiddleMouseButton(e)) {
                    // drag camera
                    camera.x -= (e.getX() - previousX) * zoom;
                    camera.y -= (e.getY() - previousY) * zoom;
                    previousX = e.getX();
                    previousY = e.getY();
                    super.mouseDragged(e);
                    repaint();
                }
            }
        });
        // handle zoom at cursor
        addMouseWheelListener(e -> {
            // zoom to cursor
            Point before = transformMousePoint(e.getPoint());
            zoom = zoom - e.getWheelRotation() * 0.1;
            Point after = transformMousePoint(e.getPoint());

            camera.x -= after.x - before.x;
            camera.y -= after.y - before.y;
            repaint();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        hiddenFrame = new JFrame("hiddenFrame");
        hiddenFrame.setName("hiddenFrame");
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        hiddenFrame.dispose();
    }

    AffineTransform getTransform() {
        Rectangle r = getBounds();
        int w2 = (int)(r.getWidth()/2.0);
        int h2 = (int)(r.getHeight()/2.0);
        AffineTransform tx = new AffineTransform();
        double dx=camera.x-w2*zoom;
        double dy=camera.y-h2*zoom;
        tx.scale(1.0/zoom, 1.0/zoom);
        tx.translate(-dx,-dy);
        return tx;
    }

    public Point transformMousePoint(Point point) {
        AffineTransform tf = getTransform();
        java.awt.geom.Point2D from = new java.awt.geom.Point2D.Double(point.x,point.y);
        java.awt.geom.Point2D to = new java.awt.geom.Point2D.Double();
        try {
            tf.inverseTransform(from,to);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        return new Point((int)to.getX(),(int)to.getY());
    }

    /**
     * Custom rendering of the JPanel.
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        super.paintComponent(g);
        g2.transform(getTransform());

        Rectangle viewableArea = getBounds();
        viewableArea.width  = (int)(viewableArea.width  * zoom * 0.9);
        viewableArea.height = (int)(viewableArea.height * zoom * 0.9);

        // use the camera position and the size of the panel to determine drawing
        // bounds as a Rectangle in world coordinates.
        viewableArea.x = (int)camera.x - viewableArea.width / 2;
        viewableArea.y = (int)camera.y - viewableArea.height / 2;

        drawBackground(g2);
        drawNodes(g2,viewableArea);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.LIGHT_GRAY);
        drawGrid(g2,10);
        g2.setColor(Color.DARK_GRAY);
        drawGrid(g2,160);
    }

    /**
     * Draw every {@link Node} in the {@link Registry}.
     * @param g the graphics context
     * @param clippingRegion the bounds of the view in world coordinates.
     */
    private void drawNodes(Graphics2D g, Rectangle clippingRegion) {
        List<Node> toScan = new ArrayList<>();
        toScan.add(Registry.getScene());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());
            drawOneNode(g,clippingRegion,node);
        }
    }

    int gx=0,gy=0;

    /**
     * For every {@link Node} there is a {@link com.marginallyclever.ro3.node.NodePanel}.
     * Draw the {@link com.marginallyclever.ro3.node.NodePanel} to a
     * {@link BufferedImage} and then draw the {@link BufferedImage} to the screen.
     * @param g              the graphics context
     * @param clippingRegion the bounds of the panel in world coordinates.
     * @param node          the {@link Node} to draw
     */
    private void drawOneNode(Graphics2D g,Rectangle clippingRegion,Node node) {
        // Set preferred size based on the components
        if(nodePanels.get(node)==null) {
            var newPanel = buildPanel(node);
            var dims = newPanel.getPreferredSize();
            newPanel.setSize(dims);
            var dn = new DonatelloNode(newPanel);
            dn.setPosition(new Vector2d(gx+=dims.width+10, gy));
            nodePanels.put(node, dn);
        }

        DonatelloNode dn = nodePanels.get(node);

        // TODO check if the dn is in the clipping region
        int x = (int)dn.getPosition().x;
        int y = (int)dn.getPosition().y;
        var bi = drawPanelToBufferedImageUnlessClipped(dn.getPanel(),clippingRegion,x,y);

        // TODO draw the node at the correct position
        drawBufferedImageAtPosition(g,bi,x,y);
    }

    private void drawBufferedImageAtPosition(Graphics2D g,BufferedImage bi, int x, int y) {
        // draw bitmap scaled to the screen
        g.drawImage(bi,
                x, y,
                null);
    }

    /**
     * Draw a {@link JPanel} to a {@link BufferedImage} unless the panel is clipped by the .
     * @param panel          the {@link JPanel} to draw
     * @param clippingRegion the bounds of the panel in world coordinates.
     * @param x              the x position to draw the panel
     * @param y              the y position to draw the panel
     * @return a {@link BufferedImage} containing the panel or null.
     */
    private BufferedImage drawPanelToBufferedImageUnlessClipped(JPanel panel,Rectangle clippingRegion, int x, int y) {
        try {
            hiddenFrame.add(panel);
            hiddenFrame.pack();
            if (panel.getWidth() == 0 || panel.getHeight() == 0) {
                return null;
            }

            // check the clipping region
            var dims = panel.getPreferredSize();
            Rectangle r = new Rectangle(x,y,dims.width,dims.height);
            if(!r.intersects(clippingRegion)) {
                return null;
            }

            return drawPanelToBufferedImage(panel);
        }
        finally {
            hiddenFrame.remove(panel);
        }
    }

    private BufferedImage drawPanelToBufferedImage(JPanel panel) {
        BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        // JPanel requires a Graphics2D object to draw to.  Create one from the BufferedImage.
        Graphics2D g2d = bi.createGraphics();
        // draw it.
        panel.paint(g2d);
        // done with the Graphics2D object.
        g2d.dispose();
        return bi;
    }

    /**
     * Assemble the components of a {@link Node} into a {@link JPanel}.
     * @param node the {@link Node} to build
     * @return a {@link JPanel} containing all the components of the {@link Node}.
     */
    private JPanel buildPanel(Node node) {
        var list = new ArrayList<JPanel>();
        node.getComponents(list);

        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        for(var v : list) {
            result.add(v);
        }
        return result;
    }

    /**
     * Draw the background.  This is the first thing drawn so it's always in the back.
     * Draw the grid at the current zoom level.  the grid is grey lines every 1 unit and black lines every 16 units.
     * @param g the graphics context
     */
    private void drawGrid(Graphics2D g,int gridSize) {
        var r = getBounds();
        int width = (int)( r.getWidth()*zoom )+gridSize*2;
        int height = (int)( r.getHeight()*zoom )+gridSize*2;
        int size = Math.max(width,height);
        int startX = (int)camera.x - width/2 - gridSize;
        int startY = (int)camera.y - height/2 - gridSize;

        startX -= startX % gridSize;
        startY -= startY % gridSize;

        for(int i = 0; i <= size; i+=gridSize) {
            g.drawLine(startX+i,startY,startX+i,startY+height);
            g.drawLine(startX,startY+i,startX+width,startY+i);
        }
    }

    public JSONObject toJSON() {
        var json = new JSONObject();
        json.put("camera.x",camera.x);
        json.put("camera.y",camera.y);
        json.put("zoom",zoom);

        JSONArray childrenArray = new JSONArray();
        for(Node entry : nodePanels.keySet()) {
            var dn = nodePanels.get(entry);
            JSONObject child = dn.toJSON();
            child.put("node",entry.getAbsolutePath());
            childrenArray.put(child);
        }
        json.put("children",childrenArray);

        return json;
    }

    public void fromJSON(JSONObject json) {
        camera.x = json.getDouble("camera.x");
        camera.y = json.getDouble("camera.y");
        zoom = json.getDouble("zoom");

        JSONArray childrenArray = json.getJSONArray("children");
        for(int i=0;i<childrenArray.length();++i) {
            JSONObject child = childrenArray.getJSONObject(i);
            String nodeName = child.getString("node");
            Node node = Registry.getScene().findByPath(nodeName);
            assert(node!=null);
            DonatelloNode dn = new DonatelloNode(buildPanel(node));
            dn.fromJSON(child);
            nodePanels.put(node,dn);
        }
    }
}
