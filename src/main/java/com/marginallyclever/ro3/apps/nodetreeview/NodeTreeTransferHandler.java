package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.commands.MoveNode;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * This is where nodes are being dragged from.
     * @param c  the component holding the data to be transferred;
     *              provided to enable sharing of <code>TransferHandler</code>s
     * @return a representation of the data to be transferred
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath [] paths = tree.getSelectionPaths();
        if (paths != null) {
            // getSelectionPaths() returns paths in the order they were selected.
            // Sort the paths to preserve their order in the tree.
            Arrays.sort(paths, (o1, o2) -> {
                Node node1 = ((NodeTreeBranch) o1.getLastPathComponent()).getNode();
                Node node2 = ((NodeTreeBranch) o2.getLastPathComponent()).getNode();
                Node parent1 = node1.getParent();
                Node parent2 = node2.getParent();
                int pathComparison = parent1.getAbsolutePath().compareTo(parent2.getAbsolutePath());
                if(pathComparison!=0) return pathComparison;
                var children = parent1.getChildren();
                int a = children.indexOf(node1);
                int b = children.indexOf(node2);
                return a-b;
            });

            // build a list of nodes to move.  Don't allow the scene root to be moved.
            var list = new ArrayList<Node>();
            for(TreePath path : paths) {
                if(path.getLastPathComponent() instanceof NodeTreeBranch branch) {
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
                // TODO prevent a parent and a child being dragged to the same place?
            }
            return true;
        } catch (Exception e) {
            logger.error("canImport failed.", e);
        }
        return false;
    }

    /**
     * This is where nodes are being dropped.
     * @param support the object containing the details of
     *        the transfer, not <code>null</code>.
     * @return <code>true</code> if the data was inserted into the tree.
     */
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
            List<Node> beingMoved = (List<Node>)transferable.getTransferData(TransferableNodeList.flavor);
            int newIndex = dl.getChildIndex();
            UndoSystem.addEvent(new MoveNode(beingMoved,newParent,newIndex));
            return true;
        } catch (Exception e) {
            logger.error("import failed.", e);
        }
        return false;
    }
}
