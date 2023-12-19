package com.marginallyclever.ro3.node.nodetreeview;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class NodeTreeTransferHandler extends TransferHandler {
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
        return new StringSelection(Integer.toString(node.getParent().getIndex(node)));
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        try {
            JTree tree = (JTree) info.getComponent();
            DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) tree.getPathForLocation((int) info.getDropLocation().getDropPoint().getX(), (int) info.getDropLocation().getDropPoint().getY()).getLastPathComponent();
            DefaultMutableTreeNode draggedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
            draggedNode.removeFromParent();
            dropNode.insert(draggedNode, 0);
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(dropNode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}