package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * A panel for editing an ODEHinge.
 */
public class ODESliderPanel extends JPanel {
    public ODESliderPanel() {
        this(new ODESlider());
    }

    public ODESliderPanel(ODESlider slider) {
        super(new GridLayout(0,2));
        this.setName(ODESlider.class.getSimpleName());

        addSelector("part A",slider.getPartA(),slider::setPartA);
        addSelector("part B",slider.getPartB(),slider::setPartB);
        addLimit("Distance Max",slider.getDistanceMax(),slider::setDistanceMax,Double.POSITIVE_INFINITY);
        addLimit("Distance Min",slider.getDistanceMin(),slider::setDistanceMin,Double.NEGATIVE_INFINITY);
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
}
