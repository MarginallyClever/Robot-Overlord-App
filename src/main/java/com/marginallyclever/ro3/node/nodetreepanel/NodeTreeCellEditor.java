package com.marginallyclever.ro3.node.nodetreepanel;

import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class NodeTreeCellEditor extends DefaultTreeCellEditor {
    public NodeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (value instanceof NodeTreeNode) {
            Node node = ((NodeTreeNode) value).getNode();
            value = node.getName();
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
        String newName = (String) super.getCellEditorValue();
        NodeTreeNode node = (NodeTreeNode) tree.getLastSelectedPathComponent();
        Node subject = node.getNode();

        if(!isValidName(subject,newName)) return node; // Return the current node without changing the name

        // If the name is valid, set it
        subject.setName(newName);
        return node;
    }

    @Override
    public boolean stopCellEditing() {
        String newName = (String) super.getCellEditorValue();
        NodeTreeNode node = (NodeTreeNode) tree.getLastSelectedPathComponent();
        Node subject = node.getNode();

        if(!isValidName(subject,newName)) return false; // Don't stop editing

        // If the name is valid, set it and stop editing
        subject.setName(newName);
        return super.stopCellEditing();
    }

    private boolean isValidName(Node subject,String newName) {
        // Check if the new name is blank
        if (newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(tree, "Node name cannot be blank.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return false; // Don't stop editing
        }

        if(subject.isNameUsedBySibling(newName)) {
            JOptionPane.showMessageDialog(tree, "A sibling node already has this name.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return false; // Don't stop editing
        }

        return true;
    }
}