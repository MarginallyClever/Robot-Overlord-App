package com.marginallyclever.ro3;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
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

    /**
     * <p>A convenience method to add a color chooser to a panel.</p>
     * @param parent the parent panel
     * @param title the title for the color chooser
     * @param startColor the initial color
     * @param consumer the consumer to accept the color
     * @param gbc the {@link GridBagConstraints} to use
     */
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

    /**
     * <p>A convenience method to add a number field to a panel.</p>
     * @param toolTip the tooltip for the field
     * @param value the initial value
     * @return the {@link JFormattedTextField}
     */
    public static JFormattedTextField addNumberFieldDouble(String toolTip, double value) {
        return addNumberField(toolTip,value,NumberFormatHelper.getNumberFormatterDouble());
    }

    /**
     * <p>A convenience method to add a number field to a panel.</p>
     * @param toolTip the tooltip for the field
     * @param value the initial value
     * @return the {@link JFormattedTextField}
     */
    public static JFormattedTextField addNumberFieldInt(String toolTip, int value) {
        return addNumberField(toolTip,value,NumberFormatHelper.getNumberFormatterInt());
    }

    /**
     * <p>A convenience method to add a number field to a panel.</p>
     * @param toolTip the tooltip for the field
     * @param value the initial value
     * @param formatter the {@link NumberFormatter} to use
     * @return the {@link JFormattedTextField}
     */
    private static JFormattedTextField addNumberField(String toolTip, double value, NumberFormatter formatter) {
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(value);
        field.setToolTipText(toolTip);
        field.setColumns(1);
        field.setMinimumSize(new Dimension(0,20));
        return field;
    }

    /**
     * <p>A convenience method to add an angle limit to a panel (in degrees).</p>
     * @param pane the panel to add to
     * @param gbc the {@link GridBagConstraints} to use
     * @param label the label for the limit
     * @param value the initial value
     * @param consumer the consumer to accept the value
     * @param infinite the value to use for infinite (no limit)
     */
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

    /**
     * <p>A convenience method to add a {@link NodeSelector} to a panel.</p>
     * @param pane the panel to add to
     * @param gbc the {@link GridBagConstraints} to use
     * @param label the label for the selector
     * @param originalValue the original value
     * @param consumer the consumer to accept the value
     */
    public static void addSelector(JPanel pane,GridBagConstraints gbc, String label, NodePath<ODEBody> originalValue, Consumer<ODEBody> consumer) {
        NodeSelector<ODEBody> selector = new NodeSelector<>(ODEBody.class,originalValue.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->consumer.accept((ODEBody)evt.getNewValue()));
        PanelHelper.addLabelAndComponent(pane, label,selector,gbc);
        gbc.gridy++;
    }
}
