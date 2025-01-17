package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODEHinge.
 */
public class ODESliderPanel extends JPanel {
    public ODESliderPanel() {
        this(new ODESlider());
    }

    public ODESliderPanel(ODESlider slider) {
        super(new GridBagLayout());
        this.setName(ODESlider.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        PanelHelper.addLimit(this,gbc,"Distance Max",slider.getDistanceMax(), slider::setDistanceMax,Double.POSITIVE_INFINITY);
        PanelHelper.addLimit(this,gbc,"Distance Min",slider.getDistanceMin(), slider::setDistanceMin,Double.NEGATIVE_INFINITY);
    }
}
