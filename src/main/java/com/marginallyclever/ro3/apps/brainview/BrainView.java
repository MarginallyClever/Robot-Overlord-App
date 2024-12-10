package com.marginallyclever.ro3.apps.brainview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.commands.AddNode;
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
import java.util.function.Supplier;

/**
 * {@link BrainView} visualizes the details of a {@link Brain}, its {@link Neuron}s, and {@link Synapse}s.
 */
public class BrainView extends App implements ItemAddedListener<Node>, ItemRemovedListener<Node>, ActionListener,
        MouseListener, MouseMotionListener, MouseWheelListener, SceneChangeListener {
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
    private final JToggleButton showNames = new JToggleButton("Names");
    private final JToggleButton showAlt = new JToggleButton("Alt");
    private final JButton connectNeuronsButton = new JButton("+S");

    public BrainView() {
        super(new BorderLayout());
        selectionChanged();
        addToolBar();
    }

    private void addToolBar() {
        brainPath.setMaximumSize(new Dimension(150, 24));
        toolbar.setFloatable(false);
        toolbar.add(brainPath);
        toolbar.add(showNames);
        toolbar.add(showAlt);
        toolbar.add(connectNeuronsButton);
        add(toolbar, BorderLayout.NORTH);

        showNames.addActionListener(e->BrainView.this.repaint());
        showAlt.addActionListener(e->BrainView.this.repaint());
        connectNeuronsButton.addActionListener(e->connectNeurons());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
        Registry.getPhysics().addActionListener(this);
        Registry.addSceneChangeListener(this);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
        Registry.removeSceneChangeListener(this);
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
        if(nn.activationFunction()) {
            g2d.setColor(Color.GREEN);
            fillBox(g2d, nn.position, d);
        }
        g2d.setColor(Color.BLUE);
        drawBox(g2d, nn.position, d);

        final int margin = 3;
        // input
        if(brain.inputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            g2d.setColor(Color.ORANGE);
            // draw a triangle above the neuron, pointing down.
            var p1 = new Point(nn.position.x-d,nn.position.y-margin-d*2);
            var p2 = new Point(nn.position.x+d,nn.position.y-margin-d*2);
            var p3 = new Point(nn.position.x,nn.position.y-margin-d);
            g2d.fillPolygon(new int[]{p1.x,p2.x,p3.x},new int[]{p1.y,p2.y,p3.y},3);
        }
        // output
        if(brain.outputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            g2d.setColor(Color.BLUE);
            // draw a triangle below the neuron, pointing down.
            var p1 = new Point(nn.position.x-d,nn.position.y+margin+d);
            var p2 = new Point(nn.position.x+d,nn.position.y+margin+d);
            var p3 = new Point(nn.position.x,nn.position.y+margin+d*2);
            g2d.fillPolygon(new int[]{p1.x,p2.x,p3.x},new int[]{p1.y,p2.y,p3.y},3);
        }

        if(showNames.isSelected()) {
            g2d.setColor(Color.BLACK);
            g2d.drawString(nn.getName(),nn.position.x+d+2,nn.position.y+d);//+" "+s+"/"+b
        }
        if(showAlt.isSelected()) {
            g2d.setColor(Color.BLACK);
            String letter = nn.getNeuronType().toString().substring(0,1);
            var fm = g2d.getFontMetrics();
            var w = fm.charWidth(letter.charAt(0));
            var h = fm.getHeight();
            g2d.drawString(letter,nn.position.x-w/2,nn.position.y+h/4);
        }
    }

    private void drawBox(Graphics2D g, Point p,int r) {
        g.drawRect(p.x-r-1,p.y-r-1,r*2+2,r*2+2);
    }

    private void fillBox(Graphics2D g, Point p,int r) {
        g.fillRect(p.x-r-1,p.y-r-1,r*2+2,r*2+2);
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
    public void mouseReleased(MouseEvent e) {}

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

    @Override
    public void beforeSceneChange(Node oldScene) {
        brainPath.setSubject(null);
    }

    @Override
    public void afterSceneChange(Node newScene) {}

    /**
     * Create a synapse between the two selected neurons.
     */
    private void connectNeurons() {
        var brain = brainPath.getSubject();
        if(brain == null) return;
        // get the selected neurons.
        var selected = Registry.selection.getList().stream().filter(n->n instanceof Neuron).map(n->(Neuron)n).toArray(Neuron[]::new);
        // if there are two neurons selected...
        if(selected.length!=2) return;
        var n0 = selected[0];
        var n1 = selected[1];
        // ...and no synapse between them...
        if(n0==n1) return;
        if(brain.getSynapses().stream().anyMatch(s->(s.getFrom()==n0 && s.getTo()==n1) || (s.getFrom()==n1 && s.getTo()==n0))) return;
        // then create a synapse...
        Supplier<Node> factory = Registry.nodeFactory.getSupplierFor("Synapse");
        var an = new com.marginallyclever.ro3.apps.commands.AddNode<>(factory,brain);
        UndoSystem.addEvent(an);
        var s = (Synapse)an.getFirstCreated();

        // and connect the synapse to the neurons.  Assume the neuron with the smaller y is the from neuron.
        if(n1.position.y < n0.position.y) {
            s.setFrom(n1);
            s.setTo(n0);
        } else {
            s.setFrom(n0);
            s.setTo(n1);
        }
        // select the synapse.
        Registry.selection.set(s);
    }
}
