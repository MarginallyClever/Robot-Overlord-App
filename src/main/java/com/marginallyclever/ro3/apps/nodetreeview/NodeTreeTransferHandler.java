package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;

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
        TreePath [] paths = tree.getSelectionPaths();
        if (paths != null) {
            var list = new ArrayList<Node>();
            for(TreePath path : paths) {
                if (path.getLastPathComponent() instanceof NodeTreeBranch branch) {
                    Node node = branch.getNode();
                    if(node == Registry.getScene()) continue;
                    list.add(node);
                }
            }
            return new TransferableNodeList(list);
        }
        return null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) return false;
        if(!support.isDataFlavorSupported(TransferableNodeList.flavor)) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        NodeTreeBranch newParentBranch = (NodeTreeBranch) destPath.getLastPathComponent();
        Node newParent = newParentBranch.getNode();

        try {
            Transferable transferable = support.getTransferable();
            List<?> beingMoved = (List<?>) transferable.getTransferData(TransferableNodeList.flavor);
            for(Object obj : beingMoved) {
                Node node = (Node)obj;
                if (node == newParent) {
                    return false;  // Prevent a node from being dragged to itself
                }
                if (newParent.hasParent(node)) {
                    return false;  // I can't become my own grandpa
                }
                // TODO: prevent a parent and a child being dragged to the same place?
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
            List<?> beingMoved = (List<?>)transferable.getTransferData(TransferableNodeList.flavor);
            for(Object obj : beingMoved) {
                Node node = (Node)obj;
                // Remove node from its current parent
                Node oldParent = node.getParent();
                int oldIndex = -1;
                if (oldParent != null) {
                    oldIndex = oldParent.getChildren().indexOf(node);
                    oldParent.removeChild(node);
                }

                // Get the index at which the source node will be added
                int newIndex = dl.getChildIndex();
                if (newIndex == -1) {
                    // If the drop location is a node, add the node at the end
                    newParent.addChild(node);
                } else {
                    // If oldParent and newParent are the same instance, adjust the index accordingly
                    if (oldParent == newParent && oldIndex < newIndex) {
                        newIndex--;
                    }
                    // If the drop location is an index, add the node at the specified index
                    newParent.addChild(newIndex, node);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("import failed.", e);
        }
        return false;
    }
}
