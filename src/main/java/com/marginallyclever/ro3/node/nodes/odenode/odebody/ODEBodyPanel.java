package com.marginallyclever.ro3.node.nodes.odenode.odebody;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODESphere;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODEBody.

 */
public class ODEBodyPanel extends JPanel {
    public ODEBodyPanel() {
        this(new ODESphere());  // I had to choose one non-abstract class to use here.
    }

    public ODEBodyPanel(ODEBody body) {
        super(new GridBagLayout());
        this.setName(ODEBody.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        var formatter = NumberFormatHelper.getNumberFormatterDouble();
        formatter.setMinimum(0.000);

        JFormattedTextField massQty = new JFormattedTextField(formatter);
        massQty.setValue(body.getMassQty());
        massQty.addPropertyChangeListener("value", e -> body.setMassQty( ((Number)massQty.getValue()).doubleValue() ));
        PanelHelper.addLabelAndComponent(this,"Mass",massQty,gbc);
    }
}
