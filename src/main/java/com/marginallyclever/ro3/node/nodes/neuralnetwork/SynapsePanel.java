package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class SynapsePanel extends JPanel {
    public SynapsePanel() {
        this(new Synapse());
    }

    public SynapsePanel(Synapse synapse) {
        super(new GridBagLayout());
        setName("Synapse");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addNeuronSelector( gbc, "From", synapse.getFrom(), synapse::setFrom );
        gbc.gridy++;
        addNeuronSelector( gbc, "To", synapse.getTo(), synapse::setTo );
        gbc.gridy++;
        addWeightField( gbc, synapse );
        gbc.gridy++;
    }

    private void addNeuronSelector(GridBagConstraints c, String label, Neuron startValue, Consumer<Neuron> p) {
        NodeSelector<Neuron> selector = new NodeSelector<>(Neuron.class,startValue);
        selector.addPropertyChangeListener("subject", (evt) -> p.accept(selector.getSubject()));
        PanelHelper.addLabelAndComponent(this, label,selector,c);
    }

    private void addWeightField(GridBagConstraints gbc, Synapse synapse) {
        JFormattedTextField f = PanelHelper.addNumberFieldDouble("Weight", synapse.weight);
        f.addPropertyChangeListener("value", (evt) -> synapse.weight = ((Number) f.getValue()).floatValue());
        PanelHelper.addLabelAndComponent(this, "Weight", f, gbc);
    }
}
