package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * {@link NeuronPanel} is a panel that displays the details of a {@link Neuron}.
 */
public class NeuronPanel extends JPanel {
    private final Neuron neuron;
    private final JFormattedTextField tx;
    private final JFormattedTextField ty;

    public NeuronPanel() {
        this(new Neuron());
    }

    public NeuronPanel(Neuron neuron) {
        super(new GridBagLayout());
        this.neuron = neuron;
        setName("Neuron");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        var local = neuron.position;

        tx = PanelHelper.addNumberFieldInt("x",local.x);
        ty = PanelHelper.addNumberFieldInt("y",local.y);
        JPanel pos = new JPanel(new GridLayout(0,2));
        pos.add(tx);
        pos.add(ty);
        tx.addPropertyChangeListener("value",(e)->updatePosition());
        ty.addPropertyChangeListener("value",(e)->updatePosition());

        PanelHelper.addLabelAndComponent(this,"Position",pos,gbc);
        gbc.gridy++;

        var bias = PanelHelper.addNumberFieldDouble("Bias",neuron.getBias());
        bias.addPropertyChangeListener("value",(e)->neuron.setBias(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Bias",bias,gbc);
        gbc.gridy++;

        var sum = PanelHelper.addNumberFieldDouble("Sum",neuron.getSum());
        sum.addPropertyChangeListener("value",(e)->neuron.setSum(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Sum",sum,gbc);
        gbc.gridy++;

        var typeChoice = new JComboBox<>(Neuron.Type.values());
        typeChoice.setSelectedItem(neuron.getNeuronType());
        typeChoice.addActionListener((e)->neuron.setNeuronType((Neuron.Type)typeChoice.getSelectedItem()));
        PanelHelper.addLabelAndComponent(this,"Type",typeChoice,gbc);
        gbc.gridy++;

        var modulation = PanelHelper.addNumberFieldDouble("Modulation",neuron.getModulation());
        modulation.addPropertyChangeListener("value",(e)->neuron.setModulation(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Modulation",modulation,gbc);
        gbc.gridy++;
    }

    private void updatePosition() {
        neuron.position.x = ((Number)tx.getValue()).intValue();
        neuron.position.y = ((Number)ty.getValue()).intValue();
    }
}
