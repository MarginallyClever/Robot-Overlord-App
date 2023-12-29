package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.node.Node;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.security.InvalidParameterException;

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
        if(!support.isDrop()) return false;
        if(!support.isDataFlavorSupported(NodeTransferable.nodeFlavor)) return false;

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
            if (newParent.hasParent(beingMoved)) {
                return false;  // I can't become my own grandpa
            }
            return true;
        } catch (Exception e) {
            logger.error("canImport failed.", e);
        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        NodeTreeBranch newParentBranch = (NodeTreeBranch) destPath.getLastPathComponent();
        Node newParent = newParentBranch.getNode();

        try {
            Transferable transferable = support.getTransferable();
            Node beingMoved = (Node) transferable.getTransferData(NodeTransferable.nodeFlavor);

            // Remove node from its current parent
            Node oldParent = beingMoved.getParent();
            int oldIndex = -1;
            if (oldParent != null) {
                oldIndex = oldParent.getChildren().indexOf(beingMoved);
                oldParent.removeChild(beingMoved);
            }

            // Get the index at which the source node will be added
            int newIndex = dl.getChildIndex();
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
            logger.error("import failed.", e);
        }
        return false;
    }

    private record NodeTransferable(Node node) implements Transferable {
        static DataFlavor nodeFlavor = new DataFlavor(Node.class, Node.class.getSimpleName());

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{nodeFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodeFlavor.equals(flavor);
        }

        @Override
        public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return node;
        }
    }
}
