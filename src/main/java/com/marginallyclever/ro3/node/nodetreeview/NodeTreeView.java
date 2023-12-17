package com.marginallyclever.ro3.node.nodetreeview;

import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.FactoryPanel;
import com.marginallyclever.ro3.Registry;
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
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link NodeTreeView} is a panel that displays the node tree.
 */
public class NodeTreeView extends DockingPanel implements NodeAttachListener, NodeDetachListener, NodeRenameListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeTreeView.class);
    private final JTree tree;
    private final NodeTreeBranch treeModel = new NodeTreeBranch(Registry.scene);
    private final EventListenerList listenerList = new EventListenerList();

    JToolBar menuBar = new JToolBar();

    public NodeTreeView() {
        this("Node Tree");
    }

    public NodeTreeView(String tabText) {
        super(tabText);
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
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        NodeTreeBranchRenderer cellRender = new NodeTreeBranchRenderer();
        tree.setCellRenderer(cellRender);
        tree.setCellEditor(new NodeTreeBranchEditor(tree, cellRender));
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
        Registry.scene.addAttachListener(this);
        Registry.scene.addDetachListener(this);
        Registry.scene.addRenameListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.scene.removeAttachListener(this);
        Registry.scene.removeDetachListener(this);
        Registry.scene.removeRenameListener(this);
    }

    /**
     * Scan the tree for existing nodes.
     * @param parent the node to scan
     */
    public void scanTree(Node parent) {
        if(parent == null) return;
        NodeTreeBranch parentBranch = findTreeNode(parent);
        if(parentBranch == null) return;

        for (Node child : parent.getChildren()) {
            logger.debug("scanTree "+parent.getAbsolutePath()+" has child "+child.getAbsolutePath());
            nodeAttached(child);
        }
    }

    private void buildMenuBar() {
        menuBar.add(new AbstractAction("+") {
            @Override
            public void actionPerformed(ActionEvent e) {
                FactoryPanel<Node> nfd = new FactoryPanel<>(Registry.nodeFactory);
                int result = JOptionPane.showConfirmDialog(NodeTreeView.this,nfd,"Create Node",JOptionPane.OK_CANCEL_OPTION);
                if(result != JOptionPane.OK_OPTION) return;
                if(nfd.getResult() != JOptionPane.OK_OPTION) return;
                String type = nfd.getSelectedNode();
                if(type.isEmpty()) return;
                Supplier<Node> factory = Registry.nodeFactory.getSupplierFor(type);
                if(factory==null) throw new RuntimeException("NodeTreePanel: no factory for "+type);

                // get the selected nodes, if any.
                TreePath[] paths = tree.getSelectionPaths();
                if(paths==null || paths.length==0) {
                    // no selection, add to root
                    Registry.scene.addChild(factory.get());
                } else {
                    // add a new node to each selected nodes
                    for(TreePath path : paths) {
                        NodeTreeBranch node = (NodeTreeBranch)path.getLastPathComponent();
                        node.getNode().addChild(factory.get());
                    }
                }
            }
        });
        menuBar.add(new AbstractAction("-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // remove the selected nodes and all child nodes.
                TreePath[] paths = tree.getSelectionPaths();
                if(paths != null) {
                    // remove all selected nodes
                    for(TreePath path : paths) {
                        NodeTreeBranch treeNode = (NodeTreeBranch)path.getLastPathComponent();
                        Node node = treeNode.getNode();
                        Node parent = node.getParent();
                        if(parent!=null) {
                            parent.removeChild(node);
                        }
                    }
                }
            }
        });
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
    public void nodeAttached(Node source) {
        logger.debug("attached "+source.getAbsolutePath());
        Node parent = source.getParent();
        if(parent==null) throw new RuntimeException("source node has no parent");
        NodeTreeBranch branchParent = findTreeNode(parent);
        if(branchParent==null) throw new RuntimeException("parent node has no branch");
        NodeTreeBranch branchChild = new NodeTreeBranch(source);
        branchParent.add(branchChild);

        source.addAttachListener(this);
        source.addDetachListener(this);
        source.addRenameListener(this);

        scanTree(source);

        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(branchParent);
    }

    @Override
    public void nodeDetached(Node source) {
        logger.debug("detached "+source.getAbsolutePath());
        source.removeAttachListener(this);
        source.removeDetachListener(this);
        source.removeRenameListener(this);

        Node parent = source.getParent();
        if(parent==null) throw new RuntimeException("source node has no parent");
        NodeTreeBranch branchParent = findTreeNode(parent);
        if(branchParent==null) throw new RuntimeException("parent node has no branch");
        branchParent.remove(findTreeNode(source));
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(branchParent);
    }

    @Override
    public void nodeRenamed(Node source) {
        logger.debug("renamed "+source.getAbsolutePath());
        NodeTreeBranch branch = findTreeNode(source);
        if (branch != null) {
            ((DefaultTreeModel) tree.getModel()).nodeChanged(branch);
        }
    }
}
