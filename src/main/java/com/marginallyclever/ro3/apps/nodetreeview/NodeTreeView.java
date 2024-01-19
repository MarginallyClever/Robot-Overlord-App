package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.actions.*;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeAttachListener;
import com.marginallyclever.ro3.node.NodeDetachListener;
import com.marginallyclever.ro3.node.NodeRenameListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;

/**
 * {@link NodeTreeView} is a panel that displays the tree of nodes in the {@link Registry} scene.
 */
public class NodeTreeView extends App
        implements NodeAttachListener, NodeDetachListener, NodeRenameListener,
        SceneChangeListener, ItemAddedListener<Node>, ItemRemovedListener<Node> {
    private static final Logger logger = LoggerFactory.getLogger(NodeTreeView.class);
    private final JTree tree;
    private final NodeTreeBranch treeModel = new NodeTreeBranch(Registry.getScene());
    private final JToolBar toolBar = new JToolBar();
    private final CutNode cutNode = new CutNode();
    private final CopyNode copyNode = new CopyNode();
    private final PasteNode pasteNode = new PasteNode();
    private final RemoveNode removeNode = new RemoveNode();
    private boolean isExternalChange = false;

    public NodeTreeView() {
        super();
        setLayout(new BorderLayout());

        tree = new JTree(treeModel);
        setupTree();

        buildToolBar();

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);
        add(scroll, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);
    }

    private void setupTree() {
        tree.setRootVisible(true);
        //tree.setShowsRootHandles(true);
        NodeTreeBranchRenderer cellRender = new NodeTreeBranchRenderer();
        tree.setCellRenderer(cellRender);

        tree.setEditable(true);
        tree.setCellEditor(new NodeTreeBranchEditor(tree, cellRender));

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new NodeTreeTransferHandler());

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addTreeSelectionListener(this::changeSelection);
    }

    private void changeSelection(TreeSelectionEvent e) {
        // if this is an external change, we are being told about a change to Registry.selection.
        // we don't want to concurrently modify Registry.selection, so ignore this event.
        if(isExternalChange) return;

        // this is an internal change, so we need to tell Registry.selection about it.
        // TreeSelectionEvent.getPaths() contains the list of all the currently selected rows
        // in the tree.
        TreePath [] paths = e.getPaths();
        if (paths == null) return;

        // handle all selection changes
        for(TreePath path : paths) {
            NodeTreeBranch selectedNode = (NodeTreeBranch) path.getLastPathComponent();
            Node node = selectedNode.getNode();
            if(e.isAddedPath(path)) {
                Registry.selection.add(node);
            } else {
                Registry.selection.remove(node);
            }
        }

        // scene root cannot be deleted.
        removeNode.setEnabled(!Registry.selection.getList().contains(Registry.getScene()));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.addSceneChangeListener(this);
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
        listenTo(Registry.getScene());
    }

    private void listenTo(Node node) {
        node.addAttachListener(this);
        node.addDetachListener(this);
        node.addRenameListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        stopListeningTo(Registry.getScene());
        Registry.removeSceneChangeListener(this);
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
    }

    private void stopListeningTo(Node node) {
        node.removeAttachListener(this);
        node.removeDetachListener(this);
        node.removeRenameListener(this);

        // stop listening to all the children of this node, a reverse of scanTree()
        List<Node> toRemove = new ArrayList<>(node.getChildren());
        for(Node progeny : toRemove) {
            stopListeningTo(progeny);
        }
    }

    /**
     * Scan the tree for existing nodes.
     * @param toScan the node to scan
     */
    public void scanTree(Node toScan) {
        if(toScan == null) throw new InvalidParameterException("node is null");
        //logger.debug("Scanning "+toScan.getAbsolutePath());

        NodeTreeBranch parentBranch = findTreeNode(toScan);
        if(parentBranch == null) {
            logger.error("Node has no branch");
            return;
        }

        for (Node child : toScan.getChildren()) {
            //logger.debug("node has child "+child.getAbsolutePath());
            nodeAttached(child);
        }
    }

    private void buildToolBar() {
        var addButton = new JButton(new AddNode<>());
        var cutButton = new JButton(cutNode);
        var removeButton = new JButton(removeNode);
        var copyButton = new JButton(copyNode);
        var pasteButton = new JButton(pasteNode);
        toolBar.add(addButton);
        toolBar.add(pasteButton);
        toolBar.add(copyButton);
        toolBar.add(cutButton);
        toolBar.add(removeButton);
        removeNode.setEnabled(false);  // nothing selected at first
    }

    /**
     * Find a node in the tree.
     * @param target the node to find
     * @return the NodeTreeNode that contains e, or null if not found.
     */
    private NodeTreeBranch findTreeNode(Node target) {
        NodeTreeBranch root = ((NodeTreeBranch)treeModel.getRoot());
        if(root==null) return null;

        List<TreeNode> list = new ArrayList<>();
        list.add(root);
        while(!list.isEmpty()) {
            TreeNode treeNode = list.remove(0);
            if(treeNode instanceof NodeTreeBranch node) {
                if (target == node.getUserObject()) {
                    return node;
                }
            } else {
                System.err.println("findTreeNode problem @ "+treeNode);
            }
            list.addAll(Collections.list(treeNode.children()));
        }
        return null;
    }

    @Override
    public void nodeAttached(Node child) {
        logger.debug("Attached "+child.getAbsolutePath());
        Node parent = child.getParent();
        if(parent==null) throw new RuntimeException("source node has no parent");
        NodeTreeBranch branchParent = findTreeNode(parent);
        if(branchParent==null) throw new RuntimeException("parent node has no branch");
        NodeTreeBranch branchChild = new NodeTreeBranch(child);
        int index = parent.getChildren().indexOf(child);
        branchParent.insert(branchChild,index);

        // Notify the JTree model that a new node has been inserted
        var model = (DefaultTreeModel) tree.getModel();
        model.nodesWereInserted(branchParent, new int[]{index});

        listenTo(child);

        // scan the new node for children
        scanTree(child);
    }

    @Override
    public void nodeDetached(Node child) {
        //logger.debug("Detached "+child.getAbsolutePath());

        stopListeningTo(child);

        Node parent = child.getParent();
        if(parent==null) throw new RuntimeException("Source node has no parent");
        NodeTreeBranch branchParent = findTreeNode(parent);
        if(branchParent==null) throw new RuntimeException("Parent node has no branch");
        NodeTreeBranch branchChild = findTreeNode(child);
        if(branchChild==null) {
            logger.warn("No branch for "+child.getAbsolutePath());
            return;
        }
        int index = parent.getChildren().indexOf(child);
        branchParent.remove(branchChild);

        // Notify the JTree model that a new node has been removed
        var model = (DefaultTreeModel) tree.getModel();
        model.nodesWereRemoved(branchParent, new int[]{index}, new Object[]{branchChild});
    }

    @Override
    public void nodeRenamed(Node source) {
        //logger.debug("Renamed "+source.getAbsolutePath());
        NodeTreeBranch branch = findTreeNode(source);
        if (branch != null) {
            ((DefaultTreeModel) tree.getModel()).nodeChanged(branch);
        }
    }

    @Override
    public void beforeSceneChange(Node oldScene) {
        //logger.debug("beforeSceneChange");
        stopListeningTo(oldScene);
        tree.clearSelection();  // does not trigger selection change event?
        removeNode.setEnabled(false);
    }

    @Override
    public void afterSceneChange(Node newScene) {
        //logger.debug("afterSceneChange");
        listenTo(newScene);
        treeModel.removeAllChildren();
        treeModel.setUserObject(newScene);
        scanTree(newScene);
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(treeModel.getRoot());
    }

    /**
     * Called when an item is added to the selection.
     * @param source the list that was modified
     * @param item the item that was added
     */
    @Override
    public void itemAdded(Object source,Node item) {
        isExternalChange = true;
        try {
            var branch = findTreeNode(item);
            if (branch == null) {
                //throw new InvalidParameterException("item not found in tree "+item.getAbsolutePath());
                return;
            }
            var leaf = new TreePath(branch.getPath());
            tree.addSelectionPath(leaf);
            tree.scrollPathToVisible(leaf);
        } finally {
            isExternalChange = false;
        }
    }

    /**
     * Called when an item is removed from the selection.
     * @param source the list that was modified
     * @param item the item that was removed
     */
    @Override
    public void itemRemoved(Object source,Node item) {
        isExternalChange = true;
        try {
            var branch = findTreeNode(item);
            if(branch==null) {
                // this is not an error.  The node may have been removed from the tree by another means.
                // throwing new InvalidParameterException is too aggressive.
                return;
            }
            tree.removeSelectionPath(new TreePath(branch.getPath()));
        } finally {
            isExternalChange = false;
        }
    }
}
