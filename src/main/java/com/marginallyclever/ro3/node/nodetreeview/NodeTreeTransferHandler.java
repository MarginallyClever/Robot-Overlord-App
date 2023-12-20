package com.marginallyclever.ro3.node.nodetreeview;
import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

public class NodeTreeTransferHandler extends TransferHandler {
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            Object component = path.getLastPathComponent();
            if (component instanceof NodeTreeBranch componentNode) {
                Node node = componentNode.getNode();
                return new NodeTransferable(node);
            }
        }
        return null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop() || !support.isDataFlavorSupported(NodeTransferable.nodeFlavor)) {
            return false;
        }
        // Additional checks can be added here if needed
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        Node newParent = ((NodeTreeBranch) destPath.getLastPathComponent()).getNode();

        try {
            Transferable transferable = support.getTransferable();
            Node sourceNode = (Node) transferable.getTransferData(NodeTransferable.nodeFlavor);

            // Remove node from its current parent
            Node oldParent = sourceNode.getParent();
            if (oldParent != null) {
                oldParent.removeChild(sourceNode);
            }

            // Add node to the new parent
            newParent.addChild(sourceNode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class NodeTransferable implements Transferable {
        static DataFlavor nodeFlavor = new DataFlavor(Node.class, "Node");
        private final Node node;

        public NodeTransferable(Node node) {
            this.node = node;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{nodeFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodeFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return node;
        }
    }
}
