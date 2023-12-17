package com.marginallyclever.ro3.node.nodetreeview;

import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * {@link NodeTreeBranchEditor} is a cell editor for the {@link NodeTreeView}.
 */
public class NodeTreeBranchEditor extends DefaultTreeCellEditor {
    public NodeTreeBranchEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (value instanceof NodeTreeBranch) {
            Node node = ((NodeTreeBranch) value).getNode();
            value = node.getName();
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
        String newName = (String) super.getCellEditorValue();
        NodeTreeBranch branch = (NodeTreeBranch) tree.getLastSelectedPathComponent();
        if(branch==null) return null; // no node selected

        Node subject = branch.getNode();
        if(!isValidName(subject,newName)) {
            return branch; // Return the current node without changing the name
        }
        // If the name is valid, set it
        subject.setName(newName);
        return branch;
    }

    @Override
    public boolean stopCellEditing() {
        String newName = (String) super.getCellEditorValue();
        NodeTreeBranch branch = (NodeTreeBranch) tree.getLastSelectedPathComponent();
        if(branch==null) return true; // no node selected

        Node subject = branch.getNode();

        if(!isValidName(subject,newName)) {
            return false; // Don't stop editing
        }

        // If the name is valid, set it and stop editing
        subject.setName(newName);
        return super.stopCellEditing();
    }

    private boolean isValidName(Node subject,String newName) {
        // Check if the new name is blank
        if (newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(tree, "Node name cannot be blank.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if(subject.isNameUsedBySibling(newName)) {
            JOptionPane.showMessageDialog(tree, "A sibling node already has this name.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}