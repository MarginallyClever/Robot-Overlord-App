package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class NodePanel extends JPanel {
    public NodePanel(Node node) {
        super(new GridLayout(0,2));
        this.setName(Node.class.getSimpleName());

        JTextField nameField = new JTextField(node.getName());
        nameField.addActionListener(e -> {
            // should not be allowed to match siblings?
            node.setName(nameField.getText());
        });
        nameField.setEditable(false);
        PanelHelper.addLabelAndComponent(this,"Name",nameField);
    }
}
