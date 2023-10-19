package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.EntityReorganizeEdit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;

class EntityTreeTransferHandler extends TransferHandler {

    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] nodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.nodes = nodes;
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(EntityTreeTransferHandler.class);

    private DataFlavor nodesFlavor;
    private final DataFlavor[] flavors = new DataFlavor[1];

    private final EntityManager entityManager;

    public EntityTreeTransferHandler(EntityManager entityManager) {
        this.entityManager = entityManager;

        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=\"" +
                    EntityTreeNode.class.getName() +
                    "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            logger.error("ClassNotFound: " + e.getMessage());
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for (int selRow : selRows) {
            if (selRow == dropRow) {
                return false;
            }
            DefaultMutableTreeNode treeNode =
                    (DefaultMutableTreeNode) tree.getPathForRow(selRow).getLastPathComponent();
            for (TreeNode offspring : Collections.list(treeNode.depthFirstEnumeration())) {
                if (tree.getRowForPath(new TreePath(((DefaultMutableTreeNode) offspring).getPath())) == dropRow) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return null;
        }
        // Make up a node array of copies for transfer and another for/of the nodes that will be removed in
        // exportDone after a successful drop.
        List<DefaultMutableTreeNode> copies = new ArrayList<>();
        List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
        EntityTreeNode firstNode = (EntityTreeNode) paths[0].getLastPathComponent();
        HashSet<TreeNode> doneItems = new LinkedHashSet<>(paths.length);
        DefaultMutableTreeNode copy = copyBranch(firstNode, doneItems, tree);
        copies.add(copy);
        toRemove.add(firstNode);
        for (int i = 1; i < paths.length; i++) {
            EntityTreeNode next = (EntityTreeNode) paths[i].getLastPathComponent();
            if (doneItems.contains(next)) {
                continue;
            }
            // Do not allow higher level nodes to be added to list.
            if (next.getLevel() < firstNode.getLevel()) {
                break;
            } else if (next.getLevel() > firstNode.getLevel()) {  // child node
                copy.add(copyBranch(next, doneItems, tree));
                // node already contains child
            } else {  // sibling
                copies.add(copyBranch(next, doneItems, tree));
                toRemove.add(next);
            }
            doneItems.add(next);
        }
        DefaultMutableTreeNode[] nodes = copies.toArray(new DefaultMutableTreeNode[0]);

        return new NodesTransferable(nodes);
    }

    private EntityTreeNode copyBranch(EntityTreeNode node, HashSet<TreeNode> doneItems, JTree tree) {
        EntityTreeNode copy = new EntityTreeNode(node.getEntity());
        doneItems.add(node);
        for (int i=0; i<node.getChildCount(); i++) {
            copy.add(copyBranch((EntityTreeNode)node.getChildAt(i), doneItems, tree));
        }
        int row = tree.getRowForPath(new TreePath(copy.getPath()));
        tree.expandRow(row);
        return copy;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (DefaultMutableTreeNode[])t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            logger.error("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            logger.error("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for (DefaultMutableTreeNode node : nodes) {
            Entity parentEntity = (Entity)parent.getUserObject();
            Entity entity = (Entity)node.getUserObject();

            if (entity.getParent() == parentEntity) {
                continue;
            }

            UndoSystem.addEvent(new EntityReorganizeEdit(entityManager,entity, parentEntity));
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}