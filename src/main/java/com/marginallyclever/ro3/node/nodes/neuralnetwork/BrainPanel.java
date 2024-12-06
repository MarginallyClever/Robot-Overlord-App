package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.neuralnetworkview.BrainView;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.NodePath;

import javax.swing.*;
import java.awt.*;

/**
 * {@link BrainPanel} controls settings for a {@link Brain}.  This is different from a {@link BrainView} which
 * illustrates the {@link Brain}'s contents.
 */
public class BrainPanel extends JPanel {
    private final Brain brain;

    public BrainPanel() {
        this(new Brain());
    }

    public BrainPanel(Brain brain) {
        super(new GridBagLayout());
        this.brain = brain;
        setName("Brain");

        redo();
    }

    /**
     * Remove everything from this panel, and rebuild it.
     */
    public void redo() {
        // TODO this is extremely lazy.  When the user changes the count of neurons the whole panel rebuild
        //      causes the count component to lose focus.  This is a bad user experience.
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        this.add(addNeuronsPanel("Input",brain.inputs),c);
        c.gridy++;
        this.add(addNeuronsPanel("Output",brain.outputs),c);
        c.gridy++;

        revalidate();
        repaint();
    }

    /**
     * Displays a list of {@link NodePath}&lt;{@link Neuron}&gt;&gt; in a collapsible panel.
     * @param label the label to display
     * @param list the list of neurons
     * @return the panel that was added
     */
    private JPanel addNeuronsPanel(String label, ListWithEvents<NodePath<Neuron>> list) {
        var containerPanel = new CollapsiblePanel(label+" Neurons");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridLayout(0, 2));

        // display the list length
        var count = PanelHelper.addNumberFieldInt("Count",list.size());
        final var finalList = list;
        count.addPropertyChangeListener("value", e->changeListSize( finalList, ((Number)count.getValue()).intValue() ) );
        PanelHelper.addLabelAndComponent(outerPanel, "Count", count);

        int i=0;
        for (NodePath<Neuron> neuron : list.getList()) {
            // add a selector
            var motorSelector = new NodeSelector<>(Neuron.class, neuron.getSubject());
            int jFinal = i;
            motorSelector.addPropertyChangeListener("subject",(e)-> {
                list.getList().get(jFinal).setUniqueIDByNode((Neuron)e.getNewValue());
            });
            PanelHelper.addLabelAndComponent(outerPanel, ""+i, motorSelector);
            i++;
        }
        return containerPanel;
    }

    /**
     * Change the list size and rebuild the panel as needed.
     * @param list the list to change
     * @param newCount the new size
     */
    private void changeListSize(ListWithEvents<NodePath<Neuron>> list, int newCount) {
        brain.setListSize(list,newCount);
        redo();
    }
}
