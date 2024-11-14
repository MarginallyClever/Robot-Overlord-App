package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class ODEJointPanel extends JPanel {
    public ODEJointPanel() {
        this(new ODEJoint());
    }

    public ODEJointPanel(ODEJoint link) {
        super(new GridBagLayout());
        this.setName(ODEJoint.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        PanelHelper.addSelector(this,gbc,"part A",link.getPartA(),link::setPartA);
        PanelHelper.addSelector(this,gbc,"part B",link.getPartB(),link::setPartB);
    }
}
