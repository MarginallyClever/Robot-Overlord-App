package com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.physics.ODE4JHelper;

import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleConsumer;

/**
 * A panel for editing an ODEBox.
 */
public class ODEBoxPanel extends JPanel {
    public ODEBoxPanel() {
        this(new ODEBox());
    }

    public ODEBoxPanel(ODEBox body) {
        super(new GridBagLayout());
        this.setName(ODEBox.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx=0;

        // size xyz
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        JPanel panel = new JPanel(new GridBagLayout());
        JFormattedTextField sx = PanelHelper.addNumberField("Size X", body.getSizeX());
        JFormattedTextField sy = PanelHelper.addNumberField("Size Y", body.getSizeY());
        JFormattedTextField sz = PanelHelper.addNumberField("Size Z", body.getSizeZ());
        panel.add(sx,c);
        panel.add(sy,c);
        panel.add(sz,c);
        PanelHelper.addLabelAndComponent(this,"Size",panel,gbc);
        gbc.gridy++;
        sx.addPropertyChangeListener("value", e -> body.setSizeX(((Number)sx.getValue()).doubleValue()));
        sy.addPropertyChangeListener("value", e -> body.setSizeY(((Number)sy.getValue()).doubleValue()));
        sz.addPropertyChangeListener("value", e -> body.setSizeZ(((Number)sz.getValue()).doubleValue()));

        // mass
        JButton setMassByVolume = new JButton("Set");
        setMassByVolume.addActionListener(e -> {
            body.setMassQty(ODE4JHelper.volumeBox(body.getSizeX(),body.getSizeY(),body.getSizeZ()));
        });
        PanelHelper.addLabelAndComponent(this,"Mass by Volume",setMassByVolume,gbc);
    }
}
