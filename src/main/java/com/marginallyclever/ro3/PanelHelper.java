package com.marginallyclever.ro3;

import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.MaterialPanel;

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
}
