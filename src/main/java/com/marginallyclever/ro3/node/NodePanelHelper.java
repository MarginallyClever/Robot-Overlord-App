package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;

import javax.swing.*;
import java.awt.*;

public class NodePanelHelper {

    /**
     * A convenience method to add a label and component to a panel that is expected to be built with
     * <code>new GridLayout(0, 2)</code>.
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
     * A convenience method to add a label and component to a panel that is expected to be built with
     * <code>new GridLayout(0, 2)</code>.
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
    }

    /**
     * A convenience method to add a label and component to a panel that is expected to be built with GridBagConstraint.
     * @param pane the panel to add to
     * @param label the text for the label
     * @param nodePath the NodePath to use
     * @param clazz the class of the Node
     * @param gbc the {@link GridBagConstraints} to use
     * @param origin the origin Node for the relative path.
     * @return the NodeSelector
     * @param <T> the type filter for the {@link NodeSelector}.
     */
    public static <T extends Node> NodeSelector<T> addNodeSelector(JPanel pane, String label, NodePath<T> nodePath, Class<T> clazz, GridBagConstraints gbc,Node origin) {
        NodeSelector<T> selector = new NodeSelector<>(clazz, nodePath.getSubject());
        selector.addPropertyChangeListener("subject", (e) -> {
            nodePath.setUniqueIDByNode((T)e.getNewValue());
        });
        NodePanelHelper.addLabelAndComponent(pane, label, selector, gbc);
        return selector;
    }
}
