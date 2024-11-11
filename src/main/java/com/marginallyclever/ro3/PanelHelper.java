package com.marginallyclever.ro3;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.MaterialPanel;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * {@link PanelHelper} is a collection of static methods to help build panels.
 */
public class PanelHelper {
    /**
     * <p>A convenience method to add a label and component to a panel that is built with
     * {@link GridLayout}.</p>
     * @param pane the panel to add to
     * @param labelText the text for the label
     * @param component the component to add
     */
    public static void addLabelAndComponent(JPanel pane, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        pane.add(label);
        pane.add(component);
    }

    /**
     * <p>A convenience method to add a label and component to a panel that is built with
     * {@link GridBagLayout}.</p>
     * @param pane the panel to add to
     * @param labelText the text for the label
     * @param component the component to add
     * @param gbc the GridBagConstraints to use
     */
    public static void addLabelAndComponent(JPanel pane, String labelText, JComponent component, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        gbc.gridwidth=1;
        gbc.gridx=0;
        pane.add(label,gbc);
        gbc.gridx=1;
        pane.add(component,gbc);
        gbc.gridy++;
        gbc.gridx=0;
    }

    /**
     * A convenience method to add a label and component to a panel that is expected to be built with GridBagConstraint.
     * @param pane the panel to add to
     * @param label the text for the label
     * @param nodePath the NodePath to use
     * @param gbc the {@link GridBagConstraints} to use
     * @return the NodeSelector
     * @param <T> the type filter for the {@link NodeSelector}.
     */
    public static <T extends Node> NodeSelector<T> addNodeSelector(JPanel pane,
                                                                   String label,
                                                                   NodePath<T> nodePath,
                                                                   GridBagConstraints gbc) {
        NodeSelector<T> selector = new NodeSelector<>(nodePath.getType(), nodePath.getSubject());
        selector.addPropertyChangeListener("subject", (e) -> {
            nodePath.setUniqueIDByNode((T)e.getNewValue());
        });
        PanelHelper.addLabelAndComponent(pane, label, selector, gbc);
        return selector;
    }

    public static void addColorChooser(JPanel parent, String title, Color startColor, Consumer<Color> consumer, GridBagConstraints gbc) {
        JButton button = new JButton();
        button.setBackground(startColor);
        button.addActionListener(e -> {
            Color color = JColorChooser.showDialog(parent,title,startColor);
            if(color!=null) consumer.accept(color);
            button.setBackground(color);
        });
        PanelHelper.addLabelAndComponent(parent,title,button,gbc);
    }

    public static JFormattedTextField addNumberField(String label, double value) {
        var formatter = NumberFormatHelper.getNumberFormatter();
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(value);
        field.setToolTipText(label);
        field.setColumns(1);
        field.setMinimumSize(new Dimension(0,20));
        return field;
    }


    public static void addLimit(JPanel pane,GridBagConstraints gbc,String label, double value, Consumer<Double> consumer, double infinite) {
        JCheckBox limitCheckBox = new JCheckBox("",!Double.isInfinite(value));
        SpinnerNumberModel model = new SpinnerNumberModel(Double.isInfinite(value) ? 0 : value, -180, 180, 0.1);
        JSpinner spinner = new JSpinner(model);

        limitCheckBox.addActionListener(e -> enableLimit(limitCheckBox.isSelected(),spinner,consumer,infinite) );
        spinner.addChangeListener(e -> {
            if (limitCheckBox.isSelected()) {
                consumer.accept((Double) spinner.getValue());
            }
        });
        enableLimit(!Double.isInfinite(value),spinner,consumer,infinite);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(limitCheckBox,BorderLayout.WEST);
        panel.add(spinner,BorderLayout.CENTER);

        PanelHelper.addLabelAndComponent(pane, label, panel,gbc);
        gbc.gridy++;
    }

    public static void enableLimit(boolean isSelected, JSpinner spinner, Consumer<Double> consumer,double infinite) {
        spinner.setEnabled(isSelected);
        consumer.accept( (!isSelected) ? infinite : (Double)spinner.getValue() );
    }


    public static void addSelector(JPanel pane,GridBagConstraints gbc, String label, NodePath<ODEBody> originalValue, Consumer<ODEBody> consumer) {
        NodeSelector<ODEBody> selector = new NodeSelector<>(ODEBody.class,originalValue.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->consumer.accept((ODEBody)evt.getNewValue()));
        PanelHelper.addLabelAndComponent(pane, label,selector,gbc);
        gbc.gridy++;
    }
}
