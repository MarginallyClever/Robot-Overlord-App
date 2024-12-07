package com.marginallyclever.ro3.apps.brainview;

import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Neuron;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Synapse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link BrainView} visualizes the details of a {@link Brain}, its {@link Neuron}s, and {@link Synapse}s.
 */
public class BrainView extends App implements ItemAddedListener<Node>, ItemRemovedListener<Node>, ActionListener,
        MouseListener, MouseMotionListener, MouseWheelListener {
    private final NodeSelector<Brain> brainPath = new NodeSelector<>(Brain.class);

    private final int RADIUS = 5;
    private final int HIGHLIGHT_RADIUS = 4;
    private final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 192);

    // add a timer that runs when physics is unpaused.  the timer should call repaint() every 100ms.
    // this will allow the BrainView to animate the neurons and synapses.
    // the timer should be removed when physics is paused.
    // the timer should be added when physics is unpaused.
    // the timer should be removed when the BrainView is removed from the screen.
    // the timer should be added when the BrainView is added to the screen.
    private Timer timer;
    private final Point previousMousePosition = new Point();
    private final JToolBar toolbar = new JToolBar();

    public BrainView() {
        super(new BorderLayout());
        selectionChanged();
        addToolBar();
    }

    private void addToolBar() {
        brainPath.setMaximumSize(new Dimension(150, 24));
        toolbar.setFloatable(false);
        toolbar.add(brainPath);
        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
        Registry.getPhysics().addActionListener(this);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
        if(timer != null) {
            timer.cancel();
            timer=null;
        }
    }

    @Override
    public void itemAdded(Object source, Node item) {
        selectionChanged();
    }

    @Override
    public void itemRemoved(Object source, Node item) {
        selectionChanged();
    }

    private void selectionChanged() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        var brain = brainPath.getSubject();
        if(brain == null) return;
        brain.scan();
        if(brain.isEmpty()) return;

        // clear the area with the default panel background color
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setColor(getBackground());
        g2d.fillRect(0,0,getWidth(),getHeight());

        drawAllSynapses(g2d,brain);
        drawAllNeurons(g2d,brain);
    }

    private void drawAllSynapses(Graphics2D g2d,Brain brain) {
        for(Synapse s : brain.getSynapses()) {
            drawOneSynapse(g2d,s);
        }
    }

    private void drawOneSynapse(Graphics2D g2d, Synapse s) {
        // find the from and to neurons.
        Neuron from = s.getFrom();
        Neuron to = s.getTo();
        if(from==null || to==null) return;

        var w = s.getWeight();
        var width = getSynapseWidth(s);
        if(Registry.selection.contains(s)) {
            // selected items are highlighted.
            g2d.setStroke(new BasicStroke((float)(HIGHLIGHT_RADIUS+width)));
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.drawLine( from.position.x, from.position.y, to.position.x, to.position.y );
        }

        g2d.setStroke(new BasicStroke(width));
        // positive weights are more green.  negative weights are more red.
        if(w>0) g2d.setColor(interpolateColor(Color.BLUE,Color.GREEN,Math.min(1, w)));
        else    g2d.setColor(interpolateColor(Color.BLUE,Color.RED  ,Math.min(1,-w)));
        g2d.drawLine( from.position.x, from.position.y, to.position.x, to.position.y );
    }

    private float getSynapseWidth(Synapse s) {
        return 1.0f+(float)Math.abs(s.getWeight());
    }

    private Color interpolateColor(Color zero, Color one, double unit) {
        return new Color(
            (int)(zero.getRed()   + (one.getRed()   - zero.getRed()  ) * unit),
            (int)(zero.getGreen() + (one.getGreen() - zero.getGreen()) * unit),
            (int)(zero.getBlue()  + (one.getBlue()  - zero.getBlue() ) * unit)
        );
    }

    private void drawAllNeurons(Graphics2D g2d,Brain brain) {
        for(Neuron nn : brain.getNeurons()) {
            drawOneNeuron(g2d,nn,brain);
        }
    }

    private int getNeuronRadius(Neuron nn) {
        var b = nn.getBias();
        return RADIUS + Math.abs((int)(b/2.0));
    }

    private void drawOneNeuron(Graphics2D g2d, Neuron nn,Brain brain) {
        final var d = getNeuronRadius(nn);

        if(Registry.selection.contains(nn)) {
            // selected items are highlighted.
            g2d.setColor(HIGHLIGHT_COLOR);
            fillBox(g2d,nn.position,d+HIGHLIGHT_RADIUS);
        }

        // interior
        g2d.setColor(getBackground());
        fillBox(g2d,nn.position,d);

        g2d.setColor(new Color(0,128,255));
        var b = nn.getBias();
        if(b!=0) {
            var s = nn.getSum();
            // fill -> how close it is to firing.
            var r = Math.floor(d*2 * Math.max(0,Math.min(1,s/b)));
            var ir = d*2-r;
            g2d.fillRect(nn.position.x-d,nn.position.y-d+(int)ir,d*2,(int)r);
        }

        // border
        g2d.setColor(nn.getBias()>=0?Color.GREEN: Color.RED);
        drawBox(g2d,nn.position,d);

        // inputs as pink box
        int j=0;
        if(brain.inputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            j=2;
            g2d.setColor(Color.ORANGE);
            drawBox(g2d,nn.position,d+j);
        }
        // outputs as blue box
        if(brain.outputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            j=4;
            g2d.setColor(Color.BLUE);
            drawBox(g2d,nn.position,d+j);
        }

        // it would be nice to visualize the bias and the sum.
        g2d.setColor(Color.BLACK);
        g2d.drawString(nn.getName(),nn.position.x+d+j+2,nn.position.y+d);//+" "+s+"/"+b
    }

    private void drawBox(Graphics2D g, Point p,int r) {
        g.drawRect(p.x-r,p.y-r,r*2+1,r*2+1);
    }

    private void fillBox(Graphics2D g, Point p,int r) {
        g.fillRect(p.x-r,p.y-r,r*2+1,r*2+1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getID()%2==0) {
            // physics stopped
            if(timer!=null) {
                timer.cancel();
                timer=null;
            }
        } else {
            // physics started
            if(timer==null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        repaint();
                    }
                },0,100);
            }
        }
    }

    public Neuron getFirstNeuronAt(Point p,Brain brain) {
        for(Neuron nn : brain.getNeurons()) {
            var r = getNeuronRadius(nn);
            if(nn.position.distanceSq(p) <= r*r) return nn;
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        var brain = brainPath.getSubject();
        if(brain == null) return;

        Node n = getFirstNeuronAt(e.getPoint(),brain);
        if(n==null) n = getFirstSynapseAt(e.getPoint(),brain);

        if(n!=null) {
            if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                // is control held down?  toggle selection.
                if (Registry.selection.contains(n)) {
                    Registry.selection.remove(n);
                } else {
                    Registry.selection.add(n);
                }
            } else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                // is shift held down?  add to selection.
                Registry.selection.add(n);
            } else {
                // select only this item.
                Registry.selection.set(n);
            }
        } else {
            // if no shift or ctrl, clear the selection.
            if ((e.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {
                Registry.selection.removeAll();
            }
        }
    }

    private Synapse getFirstSynapseAt(Point point, Brain brain) {
        double bestD = Double.MAX_VALUE;
        Synapse bestS = null;

        for(Synapse s : brain.getSynapses()) {
            var from = s.getFrom();
            var to = s.getTo();
            if(from==null || to==null) continue;

            var d = PointLineDistance(point,from.position,to.position);
            if(d<bestD) {
                bestD = d;
                bestS = s;
                if(bestD<=1) break;
            }
        }
        if(bestS!=null) {
            var w = 2+getSynapseWidth(bestS);
            if(bestD>w*w) return null;
        }
        return bestS;
    }

    /**
     * Calculate the distance from a point to a line segment.
     * @param point the point to test
     * @param lineStart the line segment start
     * @param lineEnd the line segment end
     * @return the square of the distance from the point to the line segment.
     */
    private double PointLineDistance(Point point, Point lineStart, Point lineEnd) {
        var x1 = lineStart.x;
        var y1 = lineStart.y;
        var x2 = lineEnd.x;
        var y2 = lineEnd.y;
        var x = point.x;
        var y = point.y;

        var dx = x2 - x1;
        var dy = y2 - y1;
        var d = Math.sqrt(dx*dx + dy*dy);
        var u = ((x - x1) * dx + (y - y1) * dy) / (d*d);
        // projection of point onto line
        var px = x1 + u * dx;
        var py = y1 + u * dy;

        // outside the line segment?  return a large number.
        if(px < Math.min(x1,x2) || px > Math.max(x1,x2)) return Double.MAX_VALUE;
        if(py < Math.min(y1,y2) || py > Math.max(y1,y2)) return Double.MAX_VALUE;
        // return the square of the distance to the point because we only need to compare distances.
        return (px-x)*(px-x)+(py-y)*(py-y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        previousMousePosition.setLocation(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        // get the delta
        Point delta = new Point(e.getPoint().x-previousMousePosition.x,e.getPoint().y-previousMousePosition.y);

        // if the mouse is down, move the selected neuron(s).
        if((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            for(Node n : Registry.selection.getList()) {
                if(n instanceof Neuron nn) {
                    nn.position.x += delta.x;
                    nn.position.y += delta.y;
                }
            }
            repaint();
        }
        previousMousePosition.setLocation(e.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}
}
