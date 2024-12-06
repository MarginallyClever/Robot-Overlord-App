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

        tx = PanelHelper.addNumberFieldDouble("x",local.x);
        ty = PanelHelper.addNumberFieldDouble("y",local.y);

        gbc.gridx=0;        this.add(new JLabel("Position"),gbc);
        gbc.gridx=1;        this.add(tx,gbc);
        gbc.gridx=2;        this.add(ty,gbc);

        tx.addPropertyChangeListener("value",(e)->updatePosition());
        ty.addPropertyChangeListener("value",(e)->updatePosition());
        gbc.gridy++;

        var bias = PanelHelper.addNumberFieldDouble("Bias",neuron.getBias());
        bias.addPropertyChangeListener("value",(e)->neuron.setBias(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Bias",bias,gbc);
        gbc.gridy++;

        var sum = PanelHelper.addNumberFieldDouble("Sum",neuron.getSum());
        sum.addPropertyChangeListener("value",(e)->neuron.setSum(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Sum",sum,gbc);
        gbc.gridy++;
    }

    private void updatePosition() {
        neuron.position.x = ((Number)tx.getValue()).intValue();
        neuron.position.y = ((Number)ty.getValue()).intValue();
    }
}
