package com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.physics.ODE4JHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODESphere.
 */
public class ODESpherePanel extends JPanel {
    public ODESpherePanel() {
        this(new ODESphere());
    }

    public ODESpherePanel(ODESphere body) {
        super(new GridLayout(0,2));
        this.setName(ODESphere.class.getSimpleName());

        var formatter = NumberFormatHelper.getNumberFormatterDouble();
        formatter.setMinimum(0.001);

        JFormattedTextField radiusValue = new JFormattedTextField(formatter);
        radiusValue.setValue(body.getRadius());
        radiusValue.addPropertyChangeListener("value", e -> body.setRadius( ((Number)radiusValue.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,"Radius",radiusValue);

        JButton setMassByVolume = new JButton("Set");
        setMassByVolume.addActionListener(e -> {
            body.setMassQty(ODE4JHelper.volumeSphere(body.getRadius()));
        });
        PanelHelper.addLabelAndComponent(this,"Mass by Volume",setMassByVolume);
    }
}
