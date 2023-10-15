package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class EntityTreeModel extends DefaultTreeModel {
    public EntityTreeModel(TreeNode root) {
        super(root);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path != null && path.getLastPathComponent() instanceof EntityTreeNode) {
            EntityTreeNode node = (EntityTreeNode) path.getLastPathComponent();
            //Entity entity = node.getEntity();
            //entity.setName((String) newValue); // Assuming Entity has a setName method
            nodeChanged(node); // Notify listeners that the node has changed
        }
    }
}

