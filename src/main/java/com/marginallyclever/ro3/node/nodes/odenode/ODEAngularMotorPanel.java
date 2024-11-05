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
        super(new GridLayout(0,2));
        this.setName(ODEHinge.class.getSimpleName());

        addSelector("part A",motor.getPartA(),motor::setPartA);
        addSelector("part B",motor.getPartB(),motor::setPartB);
        addAction("Torque",motor);
        addLimit("Angle Max",motor.getAngleMax(),motor::setAngleMax,Double.POSITIVE_INFINITY);
        addLimit("Angle Min",motor.getAngleMin(),motor::setAngleMin,Double.NEGATIVE_INFINITY);
        addLimit("Force Max",motor.getForceMax(),motor::setForceMax,Double.POSITIVE_INFINITY);
    }

    private void addLimit(String label, double value, Consumer<Double> consumer, double infinite) {
        JCheckBox limitCheckBox = new JCheckBox("Has Limit", !Double.isInfinite(value));
        SpinnerNumberModel model = new SpinnerNumberModel(Double.isInfinite(value) ? 0 : value, -180, 180, 0.1);
        JSpinner spinner = new JSpinner(model);

        limitCheckBox.addActionListener(e -> enableLimit(limitCheckBox.isSelected(),spinner,consumer,infinite) );
        spinner.addChangeListener(e -> {
            if (limitCheckBox.isSelected()) {
                consumer.accept((Double) spinner.getValue());
            }
        });
        enableLimit(!Double.isInfinite(value),spinner,consumer,infinite);

        PanelHelper.addLabelAndComponent(this, label, limitCheckBox);
        PanelHelper.addLabelAndComponent(this, "Value", spinner);
    }

    private void enableLimit(boolean isSelected, JSpinner spinner, Consumer<Double> consumer,double infinite) {
        spinner.setEnabled(isSelected);
        consumer.accept( (!isSelected) ? infinite : (Double)spinner.getValue() );
    }

    private void addSelector(String label, NodePath<ODEBody> originalValue, Consumer<ODEBody> setPartA) {
        NodeSelector<ODEBody> selector = new NodeSelector<>(ODEBody.class,originalValue.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->setPartA.accept((ODEBody)evt.getNewValue()));
        PanelHelper.addLabelAndComponent(this, label,selector);
    }

    private void addAction(String label,ODEAngularMotor hinge) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));

        JButton selector = new JButton("+");
        selector.addActionListener((e)-> hinge.addTorque(1));
        panel.add(selector);

        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> hinge.addTorque(-10));
        panel.add(selector2);

        PanelHelper.addLabelAndComponent(this, label, panel);
    }
}
