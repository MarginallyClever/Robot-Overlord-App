package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.neuralnetworkview.BrainView;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;

import javax.swing.*;
import java.util.List;
import java.awt.*;

/**
 * {@link BrainPanel} controls settings for a {@link Brain}.  This is different from a
 * {@link BrainView} which illustrates the {@link Brain}'s
 * contents.
 */
public class BrainPanel extends JPanel {
    private final Brain brain;

    public BrainPanel() {
        this(new Brain());
    }

    public BrainPanel(Brain brain) {
        super(new GridLayout(0,1));
        this.brain = brain;
        setName("Brain");

        addNeuronsPanel("Input",brain.inputs.getList());
        addNeuronsPanel("Output",brain.outputs.getList());
    }

    private void addNeuronsPanel(String label,List<NodePath<Neuron>> list) {
        var containerPanel = new CollapsiblePanel(label+" Neurons");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridLayout(0, 1));

        int i=0;
        for (NodePath<Neuron> neuron : list) {
            // add a selector
            var motorSelector = new NodeSelector<>(Neuron.class, neuron.getSubject());
            int jFinal = i;
            motorSelector.addPropertyChangeListener("subject",(e)-> {
                brain.inputs.getList().get(jFinal).setUniqueIDByNode((Neuron)e.getNewValue());
            });
            PanelHelper.addLabelAndComponent(outerPanel, ""+i, motorSelector);
            i++;
        }

        this.add(containerPanel);
    }
}
