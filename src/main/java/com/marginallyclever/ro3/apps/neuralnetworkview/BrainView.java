package com.marginallyclever.ro3.apps.neuralnetworkview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Neuron;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Synapse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.awt.*;

/**
 * {@link BrainView} visualizes the details of a {@link Brain}, its {@link Neuron}s, and {@link Synapse}s.
 */
public class BrainView extends App implements ItemAddedListener<Node>, ItemRemovedListener<Node> {
    private Brain brain = null;
    private Node selectedNode = null;
    private final int RADIUS = 5;
    private final int HIGHLIGHT_RADIUS = 4;
    private final Color HIGHLIGHT_COLOR = new Color(255,128,0,128);
    // TODO put a NodePath<Brain> in a toolbar?

    public BrainView() {
        super(new BorderLayout());
        selectionChanged();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
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
            g2d.drawLine(
                    from.position.x+RADIUS,
                    from.position.y+RADIUS,
                    to.position.x+RADIUS,
                    to.position.y+RADIUS );
        }

        g2d.setStroke(new BasicStroke((float)(1+absW)));
        // positive weights are more green.  negative weights are more red.
        if(w>0) g2d.setColor(interpolateColor(Color.BLUE,Color.GREEN,Math.min(1, w)));
        else    g2d.setColor(interpolateColor(Color.BLUE,Color.RED  ,Math.min(1,-w)));
        g2d.drawLine(
                from.position.x+RADIUS,
                from.position.y+RADIUS,
                to.position.x+RADIUS,
                to.position.y+RADIUS );
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

    private void drawOneNeuron(Graphics2D g2d, Neuron nn) {
        final var d = RADIUS*2;

        if(nn == selectedNode) {
            // selected items are highlighted.
            final var b = HIGHLIGHT_RADIUS;
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.fillRect(nn.position.x-b,nn.position.y-b,d+b*2+1,d+b*2+1);
        }

        // interior
        g2d.setColor(getBackground());
        g2d.fillRect(nn.position.x,nn.position.y,d,d);

        g2d.setColor(new Color(0,128,255));
        var b = nn.getBias();
        var s = nn.getSum();
        if(b!=0) {
            // fill -> how close it is to firing.
            var r = Math.floor(d * Math.max(0,Math.min(1,s/b)));
            var ir = d-r;
            g2d.fillRect(nn.position.x,nn.position.y+(int)ir,d,(int)r);
        }

        // border
        g2d.setColor(Color.BLUE);
        g2d.drawRect(nn.position.x,nn.position.y,d,d);

        int j=0;
        if(brain.inputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            j=2;
            g2d.setColor(Color.RED);
            g2d.drawRect(nn.position.x-j,nn.position.y-j,d+j*2,d+j*2);
        }
        if(brain.outputs.getList().stream().anyMatch(n->n.getSubject()==nn)) {
            j=4;
            g2d.setColor(Color.GREEN);
            g2d.drawRect(nn.position.x-j,nn.position.y-j,d+j*2,d+j*2);
        }

        // it would be nice to visualize the bias and the sum.
        g2d.drawString(nn.getName()+" "+s+"/"+b,nn.position.x+d+j+2,nn.position.y+d);
    }
}
