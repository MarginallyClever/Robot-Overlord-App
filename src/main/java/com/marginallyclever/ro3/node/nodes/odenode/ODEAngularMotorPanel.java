package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ODEAngularMotorPanel extends JPanel {
    public ODEAngularMotorPanel() {
        this(new ODEAngularMotor());
    }

    public ODEAngularMotorPanel(ODEAngularMotor motor) {
        super(new GridBagLayout());
        this.setName(ODEAngularMotor.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        addAction(gbc,"Torque",motor);
        PanelHelper.addLimit(this,gbc,"Force Max",motor.getForceMax(),motor::setForceMax,Double.POSITIVE_INFINITY);
        PanelHelper.addLimit(this,gbc,"Angle Max",motor.getAngleMax(),motor::setAngleMax,Double.POSITIVE_INFINITY);
        PanelHelper.addLimit(this,gbc,"Angle Min",motor.getAngleMin(),motor::setAngleMin,Double.NEGATIVE_INFINITY);
    }

    private void addAction(GridBagConstraints gbc,String label,ODEAngularMotor motor) {
        /*
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
        JButton selector = new JButton("+");
        selector.addActionListener((e)-> motor.addTorque(1));
        panel.add(selector);

        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> motor.addTorque(-1));
        panel.add(selector2);
        */
        JSlider slider = new JSlider(-100,100,0);
        slider.addChangeListener((e)-> motor.addTorque(slider.getValue()/50.0));
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        PanelHelper.addLabelAndComponent(this, label, slider,gbc);
        gbc.gridy++;
    }
}
