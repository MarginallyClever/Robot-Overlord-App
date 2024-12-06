package com.marginallyclever.ro3.apps.brainview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Neuron;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Synapse;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link BrainView} visualizes the details of a {@link Brain}, its {@link Neuron}s, and {@link Synapse}s.
 */
public class BrainView extends App implements ItemAddedListener<Node>, ItemRemovedListener<Node>, ActionListener,
        MouseListener, MouseMotionListener, MouseWheelListener {
    // TODO put a NodePath<Brain> in a toolbar?
    private Brain brain = null;

    private Node selectedNode = null;
    private final int RADIUS = 5;
    private final int HIGHLIGHT_RADIUS = 4;
    private final Color HIGHLIGHT_COLOR = new Color(255,128,0,128);

    // add a timer that runs when physics is unpaused.  the timer should call repaint() every 100ms.
    // this will allow the BrainView to animate the neurons and synapses.
    // the timer should be removed when physics is paused.
    // the timer should be added when physics is unpaused.
    // the timer should be removed when the BrainView is removed from the screen.
    // the timer should be added when the BrainView is added to the screen.
    private Timer timer;
    private final Point previousMousePosition = new Point();

    public BrainView() {
        super(new BorderLayout());
        selectionChanged();
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
        // if there is one selected item and it (or one of its parents) is a Brain, remember the Brain.
        var selectedNodes = Registry.selection.getList();
        brain = null;
        if(selectedNodes.size()==1) {
            Node n = selectedNodes.get(0);
            selectedNode = n;

            while(n!=null) {
                if (n instanceof Brain nn) {
                    brain = nn;
                    break;
                }
                n = n.getParent();
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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

        drawAllSynapses(g2d);
        drawAllNeurons(g2d);
    }

    private void drawAllSynapses(Graphics2D g2d) {
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
        var absW = Math.abs(s.getWeight());
        if(s==selectedNode) {
            // selected items are highlighted.
            g2d.setStroke(new BasicStroke((float)(HIGHLIGHT_RADIUS+absW+1)));
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.drawLine( from.position.x, from.position.y, to.position.x, to.position.y );
        }

        g2d.setStroke(new BasicStroke((float)(1+absW)));
        // positive weights are more green.  negative weights are more red.
        if(w>0) g2d.setColor(interpolateColor(Color.BLUE,Color.GREEN,Math.min(1, w)));
        else    g2d.setColor(interpolateColor(Color.BLUE,Color.RED  ,Math.min(1,-w)));
        g2d.drawLine( from.position.x, from.position.y, to.position.x, to.position.y );
    }

    private Color interpolateColor(Color zero, Color one, double unit) {
        return new Color(
            (int)(zero.getRed()   + (one.getRed()   - zero.getRed()  ) * unit),
            (int)(zero.getGreen() + (one.getGreen() - zero.getGreen()) * unit),
            (int)(zero.getBlue()  + (one.getBlue()  - zero.getBlue() ) * unit)
        );
    }

    private void drawAllNeurons(Graphics2D g2d) {
        for(Neuron nn : brain.getNeurons()) {
            drawOneNeuron(g2d,nn);
        }
    }

    private int getNeuronRadius(Neuron nn) {
        var b = nn.getBias();
        return RADIUS + Math.abs((int)(b/2.0));
    }

    private void drawOneNeuron(Graphics2D g2d, Neuron nn) {
        final var d = getNeuronRadius(nn);

        if(nn == selectedNode) {
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

    public Neuron getFirstNeuronAt(Point p) {
        for(Neuron nn : brain.getNeurons()) {
            var r = getNeuronRadius(nn);
            if(nn.position.distanceSq(p) <= r*r) return nn;
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        var n = getFirstNeuronAt(e.getPoint());
        if(n!=null) {
            if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                // is control held down?  toggle selection.
                if(Registry.selection.contains(n)) {
                    Registry.selection.remove(n);
                } else {
                    Registry.selection.add(n);
                }
            } else if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                // is shift held down?  add to selection.
                Registry.selection.add(n);
            } else {
                Registry.selection.set(n);
            }
        }
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
