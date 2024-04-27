package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODESphere.
 */
public class ODESpherePanel extends JPanel {
    public ODESpherePanel() {
        this(new ODESphere());
    }

    public ODESpherePanel(ODESphere sphere) {
        super(new GridLayout(0,2));
        this.setName(ODESphere.class.getSimpleName());

        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.001);

        JFormattedTextField radiusValue = new JFormattedTextField(formatter);
        radiusValue.setValue(sphere.getRadius());
        radiusValue.addPropertyChangeListener("value", e -> sphere.setRadius( ((Number)radiusValue.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,"Radius",radiusValue);
    }
}
