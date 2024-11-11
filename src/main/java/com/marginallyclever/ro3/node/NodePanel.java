package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that displays a {@link Node}.
 */
public class NodePanel extends JPanel {
    public NodePanel() {
        this(new Node());
    }
    
    public NodePanel(Node node) {
        super(new GridBagLayout());
        this.setName(Node.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(node.getName());
        nameField.addActionListener(e -> {
            // should not be allowed to match siblings?
            node.setName(nameField.getText());
        });
        nameField.setEditable(false);
        PanelHelper.addLabelAndComponent(this,"Name",nameField,gbc);
        gbc.gridy++;

        JTextField pathField = new JTextField(node.getAbsolutePath());
        pathField.setEditable(false);
        PanelHelper.addLabelAndComponent(this,"Path",pathField,gbc);
    }
}
