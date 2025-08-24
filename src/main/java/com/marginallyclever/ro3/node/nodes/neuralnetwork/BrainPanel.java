package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.brainview.BrainView;
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
    private final JToggleButton hebbianLearningActive = new JToggleButton();

    public BrainPanel() {
        this(new Brain());
    }

    public BrainPanel(Brain brain) {
        super(new GridBagLayout());
        this.brain = brain;
        setName("Brain");

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        var sumDecay = PanelHelper.createSlider(1.0, 0.0, brain.getSumDecay(), brain::setSumDecay);
        PanelHelper.addLabelAndComponent(this,"Sum Decay",sumDecay,c);
        c.gridy++;

        hebbianLearningActive.setSelected(brain.isHebbianLearningActive());
        hebbianLearningActive.addActionListener((e)->{
            brain.setHebbianLearningActive(hebbianLearningActive.isSelected());
            setLearningLabel();
        });
        PanelHelper.addLabelAndComponent(this,"Hebbian Learning",hebbianLearningActive,c);
        setLearningLabel();
        c.gridy++;

        var learningRate = PanelHelper.createSlider(1.0, 0.0, brain.getLearningRate(), brain::setLearningRate);
        PanelHelper.addLabelAndComponent(this,"Learning Rate",learningRate,c);
        c.gridy++;

        var forgettingRate = PanelHelper.createSlider(1.0, 0.0, brain.getForgettingRate(), brain::setForgettingRate);
        PanelHelper.addLabelAndComponent(this,"Forgetting Rate",forgettingRate,c);
        c.gridy++;

        var modRate = PanelHelper.createSlider(1.0, 0.0, brain.getModulationDegradationRate(), brain::setModulationDegradationRate);
        PanelHelper.addLabelAndComponent(this,"Mod Reduction Rate",modRate,c);
        c.gridy++;

        c.gridwidth=2;
        this.add(addNeuronsPanel("Input",brain.inputs),c);
        c.gridy++;
        this.add(addNeuronsPanel("Output",brain.outputs),c);
        c.gridy++;

        // run brain scan and display the new count of neurons and synapses.
        var scanButton = new JButton("Scan Brain");
        scanButton.addActionListener((e)->{
            brain.scan();
            var s = brain.getSynapses().size();
            var n = brain.getNeurons().size();
            JOptionPane.showMessageDialog(this,"Brain has "+n+" neurons and "+s+" synapses.");
        });
        PanelHelper.addLabelAndComponent(this,"Start",scanButton,c);

        revalidate();
        repaint();
    }

    private void setLearningLabel() {
        hebbianLearningActive.setText(brain.isHebbianLearningActive() ? "Active" : "Inactive");
    }

    /**
     * Displays a list of {@link NodePath}&lt;{@link Neuron}&gt;&gt; in a collapsible panel.
     * @param label the label to display
     * @param list the list of neurons
     * @return the panel that was added
     */
    private JPanel addNeuronsPanel(String label, ListWithEvents<NodePath<Neuron>> list) {
        var containerPanel = new CollapsiblePanel(label+" Neurons");
        var contentPane = containerPanel.getContentPane();
        contentPane.setLayout(new BorderLayout());

        var countPanel = new JPanel(new GridBagLayout());

        var listPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth=1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        // display the list length
        var count = PanelHelper.addNumberFieldInt("Count",list.size());
        final var finalList = list;
        count.addPropertyChangeListener("value", e->changeListSize( listPanel, finalList, ((Number)count.getValue()).intValue() ) );
        PanelHelper.addLabelAndComponent(countPanel, "Count", count,c);
        contentPane.add(countPanel,BorderLayout.NORTH);
        contentPane.add(listPanel,BorderLayout.CENTER);

        addNeuronsToPanel(listPanel,list);
        return containerPanel;
    }

    private void addNeuronsToPanel(JPanel outerPanel, ListWithEvents<NodePath<Neuron>> list) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth=1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        int i=0;
        for (NodePath<Neuron> neuron : list.getList()) {
            // add a selector
            var motorSelector = new NodeSelector<>(Neuron.class, neuron.getSubject());
            int jFinal = i;
            motorSelector.addPropertyChangeListener("subject",(e)-> {
                list.getList().get(jFinal).setUniqueIDByNode((Neuron)e.getNewValue());
            });
            PanelHelper.addLabelAndComponent(outerPanel, ""+i, motorSelector,c);
            c.gridy++;
            i++;
        }
    }

    /**
     * Change the list size and rebuild the panel as needed.
     * @param listPanel the panel to change
     * @param list the list to change
     * @param newCount the new size
     */
    private void changeListSize(JPanel listPanel,ListWithEvents<NodePath<Neuron>> list, int newCount) {
        brain.setListSize(list,newCount);
        listPanel.removeAll();
        addNeuronsToPanel(listPanel,list);
    }
}
