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
        super(new GridLayout(0,2));
        this.setName(ODEBox.class.getSimpleName());

        addField("Size X", body.getSizeX(), body::setSizeX);
        addField("Size Y", body.getSizeY(), body::setSizeY);
        addField("Size Z", body.getSizeZ(), body::setSizeZ);

        JButton setMassByVolume = new JButton("Set");
        setMassByVolume.addActionListener(e -> {
            body.setMassQty(ODE4JHelper.volumeBox(body.getSizeX(),body.getSizeY(),body.getSizeZ()));
        });
        PanelHelper.addLabelAndComponent(this,"Mass by Volume",setMassByVolume);
    }

    private void addField(String label, double originalValue, DoubleConsumer setSize) {
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.001);

        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(originalValue);
        field.addPropertyChangeListener("value", e -> setSize.accept( ((Number)field.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,label,field);
    }
}
