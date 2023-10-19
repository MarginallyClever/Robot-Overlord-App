package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

class EntityTreeCellEditor extends DefaultTreeCellEditor {
    public EntityTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (value instanceof EntityTreeNode) {
            Entity entity = ((EntityTreeNode) value).getEntity();
            value = entity.getName();
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
        String name = (String) super.getCellEditorValue();
        EntityTreeNode node = (EntityTreeNode) tree.getLastSelectedPathComponent();
        Entity entity = node.getEntity();
        entity.setName(name); // Assuming Entity has a setName method

        return node;
    }

    @Override
    public boolean stopCellEditing() {
        String newName = (String) super.getCellEditorValue();
        EntityTreeNode node = (EntityTreeNode) tree.getLastSelectedPathComponent();
        Entity entity = node.getEntity();
        entity.setName(newName); // Assuming Entity has a setName method
        return super.stopCellEditing();
    }
}

