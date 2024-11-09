package com.marginallyclever.ro3.apps.donatello;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws a graph of the {@link DonatelloNode}s.
 */
public class GraphViewPanel extends JPanel {
    private final Map<Node, DonatelloNode> donatelloNodeList = new HashMap<>();

    private final Point camera = new Point();
    private double zoom = 1.0;
    private final Point previous = new Point();  // in screen coordinates
    boolean drawGrid = true;

    // temp value to make new nodes appear in a line
    private final Point newNodePoint = new Point(0,0);

    // control dragging of nodes
    private DonatelloNode nodeBeingDragged = null;
    private final Point dragStartOffset = new Point();  // in world coordinates

    public GraphViewPanel() {
        super();
        setLayout(null);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                nodeBeingDragged = null;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseDrag(e);
                super.mouseDragged(e);
            }
        });

        addMouseWheelListener(this::zoomAtCursor);
    }


    /**
     * If the mouse is over a {@link DonatelloNode} when the SHIFT is held then begin dragging it.
     * @param e the mouse event
     */
    private void possiblyBeginDrag(MouseEvent e) {
        if(!e.isShiftDown()) return;
        Point click = screenToWorld(e.getPoint());
        System.out.println("possible "+click);
        nodeBeingDragged = findNodeUnderPoint(click);
        if(nodeBeingDragged!=null) {
            // remember the offset between the mouse and the node
            Point p = nodeBeingDragged.getRectangle().getLocation();
            p.translate(-click.x,-click.y);
            dragStartOffset.setLocation(p);
        }
    }

    private void mousePress(MouseEvent e) {
        previous.setLocation(e.getPoint());

        // if there's a DonatelloNode under the mouse, drag it?
        possiblyBeginDrag(e);
    }

    private void mouseDrag(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e)) {
            // drag camera
            Point delta = new Point(e.getX() - previous.x, e.getY() - previous.y);
            camera.translate(
                    -(int)(delta.x * zoom),
                    -(int)(delta.y * zoom));
            previous.setLocation(e.getPoint());
            repaint();
        }
        if(nodeBeingDragged!=null) {
            // drag node
            Point delta = new Point(e.getX() - previous.x, e.getY() - previous.y);
            previous.setLocation(e.getPoint());

            Point p = nodeBeingDragged.getPosition();
            p.translate(
                    (int)(delta.x * zoom),
                    (int)(delta.y * zoom));
            System.out.println("drag "+p.x+","+p.y);
            nodeBeingDragged.setPosition(p);
            repaint();
        }
    }

    private void zoomAtCursor(MouseWheelEvent e) {
        Point before = worldToScreen(e.getPoint());
        zoom = zoom - e.getWheelRotation() * 0.1;
        Point after = worldToScreen(e.getPoint());

        camera.x -= after.x - before.x;
        camera.y -= after.y - before.y;
        repaint();
    }

    private DonatelloNode findNodeUnderPoint(Point p) {
        for(var dn : donatelloNodeList.values()) {
            System.out.println("  "+dn.getRectangle());
            if(dn.getRectangle().contains(p)) {
                System.out.println("  hit!");
                return dn;
            }
        }
        return null;
    }

    /**
     * Custom rendering of the JPanel.
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        paintInterior(g2);
        g2.dispose();

        super.paintComponent(g);
    }

    private void paintInterior(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2d.transform(getWorldToScreenTransform());

        Rectangle viewableArea = getBounds();
        viewableArea.width  = (int)Math.ceil(viewableArea.width  * zoom);
        viewableArea.height = (int)Math.ceil(viewableArea.height * zoom);

        // use the camera position and the size of the panel to determine drawing
        // bounds as a Rectangle in world coordinates.
        viewableArea.x = camera.x - viewableArea.width / 2;
        viewableArea.y = camera.y - viewableArea.height / 2;

        //if(drawGrid)
        drawBackground(g2d,viewableArea);
        drawNodes(g2d,viewableArea);

        drawCursorInWorld(g2d);
    }

    private void drawCursorInWorld(Graphics2D g) {
        Point p = screenToWorld(previous);
        g.setColor(Color.RED);
        g.drawLine(p.x-100,p.y,p.x+100,p.y);
        g.drawLine(p.x,p.y-100,p.x,p.y+100);
    }

    private void drawBackground(Graphics2D g2,Rectangle viewableArea) {
        g2.setColor(Color.LIGHT_GRAY);
        drawGrid(g2,10,viewableArea);
        g2.setColor(Color.DARK_GRAY);
        drawGrid(g2,160,viewableArea);
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
        if(donatelloNodeList.get(node)==null) {
            var dn = new DonatelloNode(node);
            dn.setPosition(newNodePoint);
            newNodePoint.x += dn.getRectangle().width;
            donatelloNodeList.put(node, dn);
        }

        DonatelloNode dn = donatelloNodeList.get(node);

        // TODO check if the dn is in the clipping region
        dn.paint(this,g,clippingRegion);
    }

    public void drawBufferedImageAtPosition(Graphics2D g,BufferedImage bi, int x, int y) {
        // draw bitmap scaled to the screen
        g.drawImage(bi,
                x, y,
                null);
    }


    /**
     * Draw the background.  This is the first thing drawn so it's always in the back.
     * Draw the grid at the current zoom level.  the grid is grey lines every 1 unit and black lines every 16 units.
     * @param g the graphics context
     */
    private void drawGrid(Graphics2D g,int gridSize,Rectangle viewableArea) {
        int width = viewableArea.width + gridSize;
        int height = viewableArea.height + gridSize;
        int startX = viewableArea.x - gridSize;
        int startY = viewableArea.y - gridSize;

        startX -= startX % gridSize;
        startY -= startY % gridSize;

        for(int i = 0; i <= width; i += gridSize) {
            g.drawLine(startX + i, startY, startX + i, startY + height);
        }
        for(int i = 0; i <= height; i += gridSize) {
            g.drawLine(startX, startY + i, startX + width, startY + i);
        }
    }


    /**
     * @param p a Point in screen coordinates.
     * @return a new Point in world coordinates.
     */
    public Point screenToWorld(Point p) {
        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        Point relativeToScreenCenter = new Point(p.x - w2, p.y - h2);
        return new Point(
                (int)( relativeToScreenCenter.x * zoom ) + camera.x,
                (int)( relativeToScreenCenter.y * zoom ) + camera.y);
    }

    /**
     * @return the {@link AffineTransform} that converts world coordinates to screen coordinates.
     */
    AffineTransform getWorldToScreenTransform() {
        Rectangle r = getBounds();
        int w2 = (int)(r.getWidth()/2.0);
        int h2 = (int)(r.getHeight()/2.0);
        AffineTransform tx = new AffineTransform();
        double dx = camera.x - w2 * zoom;
        double dy = camera.y - h2 * zoom;
        tx.scale(1.0/zoom, 1.0/zoom);
        tx.translate(-dx,-dy);
        return tx;
    }

    public Point worldToScreen(Point point) {
        AffineTransform tf = getWorldToScreenTransform();
        java.awt.geom.Point2D from = new java.awt.geom.Point2D.Double(point.x,point.y);
        java.awt.geom.Point2D to = new java.awt.geom.Point2D.Double();
        try {
            tf.inverseTransform(from,to);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        return new Point((int)to.getX(),(int)to.getY());
    }

    public void setDrawGrid(boolean selected) {
        drawGrid = selected;
    }

    public boolean getDrawGrid() {
        return drawGrid;
    }

    public JSONObject toJSON() {
        var json = new JSONObject();
        json.put("camera.x",camera.x);
        json.put("camera.y",camera.y);
        json.put("zoom",zoom);

        JSONArray childrenArray = new JSONArray();
        for(Node entry : donatelloNodeList.keySet()) {
            var dn = donatelloNodeList.get(entry);
            JSONObject child = dn.toJSON();
            child.put("node",entry.getAbsolutePath());
            childrenArray.put(child);
        }
        json.put("children",childrenArray);

        return json;
    }

    public void fromJSON(JSONObject json) {
        camera.x = json.getInt("camera.x");
        camera.y = json.getInt("camera.y");
        zoom = json.getDouble("zoom");

        JSONArray childrenArray = json.getJSONArray("children");
        for(int i=0;i<childrenArray.length();++i) {
            JSONObject child = childrenArray.getJSONObject(i);
            String nodeName = child.getString("node");
            Node node = Registry.getScene().findByPath(nodeName);
            assert(node!=null);
            DonatelloNode dn = new DonatelloNode(node);
            dn.fromJSON(child);
            donatelloNodeList.put(node,dn);
        }
    }
}
