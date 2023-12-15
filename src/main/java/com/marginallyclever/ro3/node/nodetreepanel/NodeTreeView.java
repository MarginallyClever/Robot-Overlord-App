package com.marginallyclever.ro3.node.nodetreepanel;

import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.FactoryPanel;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeEvent;
import com.marginallyclever.ro3.node.NodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * NodeTreePanel is a panel that displays the nodes tree.
 */
public class NodeTreeView extends DockingPanel implements NodeListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeTreeView.class);
    private final JTree tree;
    private final NodeTreeNode treeModel = new NodeTreeNode(Registry.scene);
    private final EventListenerList listenerList = new EventListenerList();

    JToolBar menuBar = new JToolBar();

    public NodeTreeView() {
        this("Node Tree");
    }

    public NodeTreeView(String tabText) {
        super(tabText);
        setLayout(new BorderLayout());

        tree = new JTree(treeModel);

        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);

        add(scroll, BorderLayout.CENTER);
        add(menuBar, BorderLayout.NORTH);

        buildMenuBar();
        watchTree();
    }

    private void watchTree() {
        tree.addTreeSelectionListener((e) ->{
            // single selection
            TreePath path = e.getPath();
            NodeTreeNode selectedNode = (NodeTreeNode) path.getLastPathComponent();
            // Do something with selectedNode
            fireSelectionChangeEvent(List.of(selectedNode.getNode()));
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.scene.addNodeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.scene.removeNodeListener(this);
    }

    /**
     * Scan the tree for existing nodes.
     * @param parent the nodes to scan
     */
    public void scanTree(Node parent) {
        if(parent == null) return;
        NodeTreeNode me = findTreeNode(parent);
        if(me == null) return;

        for (Node child : parent.getChildren()) {
            logger.debug("scanTree "+parent.getAbsolutePath()+" has child "+child.getAbsolutePath());
            NodeTreeNode node = findTreeNode(child);
            if(node==null) {
                node = new NodeTreeNode(child);
                me.add(node);
            }
            child.addNodeListener(this);
            scanTree(child);
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
                    // add a new nodes to each selected nodes
                    for(TreePath path : paths) {
                        NodeTreeNode node = (NodeTreeNode)path.getLastPathComponent();
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
                    // remove each selected nodes
                    for(TreePath path : paths) {
                        NodeTreeNode treeNode = (NodeTreeNode)path.getLastPathComponent();
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
     * Find nodes n in the tree.
     * @param target the nodes to find
     * @return the NodeTreeNode that contains e, or null if not found.
     */
    private NodeTreeNode findTreeNode(Node target) {
        NodeTreeNode root = ((NodeTreeNode)treeModel.getRoot());
        if(root==null) return null;

        List<TreeNode> list = new ArrayList<>();
        list.add(root);
        while(!list.isEmpty()) {
            TreeNode treeNode = list.remove(0);
            if(treeNode instanceof NodeTreeNode node) {
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
    public void nodeEvent(NodeEvent event) {
        if(event.type()== NodeEvent.ATTACHED) {
            Node child = event.source();
            child.addNodeListener(this);
            Node parent = child.getParent();
            scanTree(parent);
            ((DefaultTreeModel)tree.getModel()).reload();
        } else if(event.type() == NodeEvent.DETACHED) {
            Node child = event.source();
            child.removeNodeListener(this);
            Node parent = child.getParent();
            if(parent==null) throw new RuntimeException("NodeTreePanel: attached nodes has no parent");
            NodeTreeNode nodeParent = findTreeNode(parent);
            if(nodeParent==null) throw new RuntimeException("NodeTreePanel: attached nodes has no parent nodes");
            nodeParent.remove(findTreeNode(child));
            ((DefaultTreeModel)tree.getModel()).reload();
        }
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
}
