package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.physics.ODEPhysics;
import com.marginallyclever.ro3.view.View;
import com.marginallyclever.ro3.view.ViewProvider;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

/**
 * {@link ODEPhysicsSettingsPanel} is a {@link View} for {@link ODEPhysics}.
 */
@View(of= ODE4JPanel.class)
public class ODEPhysicsSettingsPanel extends JPanel implements ViewProvider<ODE4JPanel> {
    private ODE4JPanel subject;
    private final NumberFormatter formatter = NumberFormatHelper.getNumberFormatter();
    private final JFormattedTextField cfm = new JFormattedTextField(formatter);
    private final JFormattedTextField erp = new JFormattedTextField(formatter);
    private final JFormattedTextField gravity = new JFormattedTextField(formatter);

    public ODEPhysicsSettingsPanel() {
        super(new GridBagLayout());
        setName("Physics");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        var physics = Registry.getPhysics();
        // cfm
        cfm.setValue(physics.getCFM());
        PanelHelper.addLabelAndComponent(this, "CFM",cfm,gbc);
        cfm.addPropertyChangeListener("value", evt -> setCFM((Double) evt.getNewValue()));

        // erp
        gbc.gridy++;
        erp.setValue(physics.getERP());
        PanelHelper.addLabelAndComponent(this, "ERP",erp,gbc);
        erp.addPropertyChangeListener("value", evt -> setERP((Double) evt.getNewValue()));

        // gravity
        gbc.gridy++;
        gravity.setValue(physics.getGravity());
        PanelHelper.addLabelAndComponent(this, "Gravity",gravity,gbc);
        gravity.addPropertyChangeListener("value", evt ->setGravity((Double) evt.getNewValue()));
    }

    public void setCFM(double cfm) {
        var physics = Registry.getPhysics();
        physics.setCFM(cfm);
    }

    public void setERP(double erp) {
        var physics = Registry.getPhysics();
        physics.setERP(erp);
    }

    public void setGravity(double gravity) {
        var physics = Registry.getPhysics();
        physics.setGravity(gravity);
    }

    @Override
    public void setViewSubject(ODE4JPanel subject) {
        this.subject = subject;
    }
}
