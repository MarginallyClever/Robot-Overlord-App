package com.marginallyclever.ro3.node.nodetreeview;

import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

/**
 * {@link NodeTreeTransferHandler} handles drag and drop operations for the {@link NodeTreeView}.
 */
public class NodeTreeTransferHandler extends TransferHandler {
    private static final Logger logger = LoggerFactory.getLogger(NodeTreeTransferHandler.class);
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
        return support.isDrop() && support.isDataFlavorSupported(NodeTransferable.nodeFlavor);
        // Additional checks can be added here if needed
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        JTree tree = (JTree) support.getComponent();
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        NodeTreeBranch newParentBranch = (NodeTreeBranch) destPath.getLastPathComponent();
        Node newParent = newParentBranch.getNode();

        try {
            Transferable transferable = support.getTransferable();
            Node beingMoved = (Node) transferable.getTransferData(NodeTransferable.nodeFlavor);

            if (beingMoved == newParent) {
                return false;  // Prevent a node from being dragged to itself
            }
            if( newParent.hasParent(beingMoved) ) {
                return false;  // i can't become my own grandpa
            }

            // Remove node from its current parent
            Node oldParent = beingMoved.getParent();
            int oldIndex = -1;
            if (oldParent != null) {
                oldIndex = oldParent.getChildren().indexOf(beingMoved);
                oldParent.removeChild(beingMoved);
                logger.debug("oldIndex: {}", oldIndex);
            }

            // Get the index at which the source node will be added
            int newIndex = dl.getChildIndex();
            logger.debug("newIndex: {}", newIndex);
            if (newIndex == -1) {
                // If the drop location is a node, add the node at the end
                newParent.addChild(beingMoved);
            } else {
                // If oldParent and newParent are the same instance, adjust the index accordingly
                if (oldParent == newParent && oldIndex < newIndex) {
                    newIndex--;
                }
                // If the drop location is an index, add the node at the specified index
                newParent.addChild(newIndex, beingMoved);
            }
            return true;
        } catch (Exception e) {
            logger.error("importData", e);
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
