package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class FullNameTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final int HORIZONTAL_PADDING = 20; // Additional padding to avoid truncation
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        // Let the default cell renderer prepare the component
        JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // Get the full name from the node's user object
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (userObject instanceof Entity) {
                Entity entity = ((Entity)userObject);
                String fullName = entity.getName();

                setText(fullName);
                // Set the full name as the tooltip
                setToolTipText(entity.getUniqueID());

                // Calculate the preferred width based on the length of the full name
                FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
                int preferredWidth = fontMetrics.stringWidth(fullName) + HORIZONTAL_PADDING;

                // Set the preferred size of the component
                Dimension preferredSize = new Dimension(preferredWidth, component.getPreferredSize().height);
                setPreferredSize(preferredSize);
                setMinimumSize(preferredSize);
            }
        }

        return component;
    }
}
