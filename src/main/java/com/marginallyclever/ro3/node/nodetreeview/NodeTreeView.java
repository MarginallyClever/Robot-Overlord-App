package com.marginallyclever.ro3.node.nodetreeview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.actions.AddNode;
import com.marginallyclever.ro3.actions.RemoveNode;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeAttachListener;
import com.marginallyclever.ro3.node.NodeDetachListener;
import com.marginallyclever.ro3.node.NodeRenameListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.*;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link NodeTreeView} is a panel that displays the node tree.
 */
public class NodeTreeView extends JPanel implements NodeAttachListener, NodeDetachListener, NodeRenameListener, SceneChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeTreeView.class);
    private final JTree tree;
    private final NodeTreeBranch treeModel = new NodeTreeBranch(Registry.getScene());
    private final EventListenerList listenerList = new EventListenerList();

    JToolBar menuBar = new JToolBar();

    public NodeTreeView() {
        super();
        setLayout(new BorderLayout());

        tree = new JTree(treeModel);
        setupTree();

        buildMenuBar();

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);
        add(scroll, BorderLayout.CENTER);
        add(menuBar, BorderLayout.NORTH);
    }

    private void setupTree() {
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        NodeTreeBranchRenderer cellRender = new NodeTreeBranchRenderer();
        tree.setCellRenderer(cellRender);
        tree.setCellEditor(new NodeTreeBranchEditor(tree, cellRender));
        tree.setTransferHandler(new NodeTreeTransferHandler());
        tree.addTreeSelectionListener((e) ->{
            // single selection
            TreePath path = e.getPath();
            NodeTreeBranch selectedNode = (NodeTreeBranch) path.getLastPathComponent();
            // Do something with selectedNode
            fireSelectionChangeEvent(List.of(selectedNode.getNode()));
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.addSceneChangeListener(this);
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

    private void buildMenuBar() {
        menuBar.add(new AddNode<>(this));
        menuBar.add(new RemoveNode(this));
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

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        listenerList.add(SelectionChangeListener.class, listener);
    }

    public void removeSelectionChangeListener(SelectionChangeListener listener) {
        listenerList.remove(SelectionChangeListener.class, listener);
    }

    private void fireSelectionChangeEvent(List<Node> newSelection) {
        for(SelectionChangeListener listener : listenerList.getListeners(SelectionChangeListener.class)) {
            listener.selectionChanged(newSelection);
        }
    }

    @Override
    public void nodeAttached(Node child) {
        //logger.debug("Attached "+child.getAbsolutePath());
        Node parent = child.getParent();
        if(parent==null) throw new RuntimeException("source node has no parent");
        NodeTreeBranch branchParent = findTreeNode(parent);
        if(branchParent==null) throw new RuntimeException("parent node has no branch");
        NodeTreeBranch branchChild = new NodeTreeBranch(child);
        int index = parent.getChildren().indexOf(child);
        branchParent.insert(branchChild,index);

        listenTo(child);
        scanTree(child);

        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(branchParent);
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
        branchParent.remove(branchChild);
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(branchParent);
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
        fireSelectionChangeEvent(List.of());
    }

    @Override
    public void afterSceneChange(Node newScene) {
        //logger.debug("afterSceneChange");
        listenTo(newScene);
        treeModel.removeAllChildren();
        treeModel.setUserObject(newScene);
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(treeModel.getRoot());
        scanTree(newScene);
    }

    /**
     * Add a new node to the selected branches.
     * @param factory the factory to use to create the new node.
     */
    public void addChildrenUsingFactory(Supplier<Node> factory) {
        if(factory==null) throw new InvalidParameterException("factory is null");

        // get the selected nodes, if any.
        TreePath[] paths = tree.getSelectionPaths();
        if(paths==null || paths.length==0) {
            // no selection, add to root
            Registry.getScene().addChild(factory.get());
        } else {
            // add a new node to each selected nodes
            for(TreePath path : paths) {
                NodeTreeBranch node = (NodeTreeBranch)path.getLastPathComponent();
                node.getNode().addChild(factory.get());
            }
        }
    }

    /**
     * Remove the selected nodes.
     */
    public void removeSelectedNodes() {
        TreePath[] paths = tree.getSelectionPaths();
        if(paths == null) return;  // nothing selected

        for(TreePath path : paths) {
            NodeTreeBranch treeNode = (NodeTreeBranch)path.getLastPathComponent();
            Node node = treeNode.getNode();
            Node parent = node.getParent();
            if(parent!=null) {
                parent.removeChild(node);
            } // else root node, can't remove.
        }
    }
}
