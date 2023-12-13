package com.marginallyclever.ro3.nodetreepanel;

import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.FactoryPanel;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.nodes.Node;
import com.marginallyclever.ro3.nodes.NodeEvent;
import com.marginallyclever.ro3.nodes.NodeListener;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * NodeTreePanel is a panel that displays the node tree.
 */
public class NodeTreePanel extends DockingPanel implements NodeListener {
    private final JTree tree;
    private final NodeTreeNode treeModel = new NodeTreeNode(Registry.root);

    JToolBar menuBar = new JToolBar();

    public NodeTreePanel() {
        this("Node Tree");
    }

    public NodeTreePanel(String tabText) {
        super(tabText);
        setLayout(new BorderLayout());

        tree = new JTree(treeModel);

        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);

        add(menuBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        buildMenuBar();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.root.addNodeListener(this);
        scanTree(Registry.root);
    }

    private void scanTree(Node parent) {
        if(parent == null) return;
        NodeTreeNode me = findTreeNode(parent);
        if(me == null) return;

        for(Node child : parent.getChildren()) {
            NodeTreeNode node = new NodeTreeNode(child);
            me.add(node);
            child.addNodeListener(this);
            scanTree(child);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.root.removeNodeListener(this);
    }

    private void buildMenuBar() {
        menuBar.add(new AbstractAction("+") {
            @Override
            public void actionPerformed(ActionEvent e) {
                FactoryPanel<Node> nfd = new FactoryPanel<>(Registry.nodeFactory);
                int result = JOptionPane.showConfirmDialog(NodeTreePanel.this,nfd,"Create Node",JOptionPane.OK_CANCEL_OPTION);
                if(result != JOptionPane.OK_OPTION) return;
                String type = nfd.getSelectedNode();
                if(type.isEmpty()) return;
                Supplier<Node> factory = Registry.nodeFactory.getSupplierFor(type);
                if(factory==null) throw new RuntimeException("NodeTreePanel: no factory for "+type);

                // get the selected node, if any.
                TreePath[] paths = tree.getSelectionPaths();
                if(paths==null || paths.length==0) {
                    // no selection, add to root
                    Registry.root.addChild(factory.get());
                } else {
                    // add a new node to each selected node
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
                    // remove each selected node
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

    private void runNodeSelectionDialog() {
    }

    /**
     * Find node n in the tree.
     * @param target the node to find
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
            if(parent==null) throw new RuntimeException("NodeTreePanel: attached node has no parent");
            NodeTreeNode nodeParent = findTreeNode(parent);
            if(nodeParent==null) throw new RuntimeException("NodeTreePanel: attached node has no parent node");
            NodeTreeNode node = new NodeTreeNode(child);
            nodeParent.add(node);
            ((DefaultTreeModel)tree.getModel()).reload();
        } else if(event.type() == NodeEvent.DETACHED) {
            Node child = event.source();
            child.removeNodeListener(this);
            Node parent = child.getParent();
            if(parent==null) throw new RuntimeException("NodeTreePanel: attached node has no parent");
            NodeTreeNode nodeParent = findTreeNode(parent);
            if(nodeParent==null) throw new RuntimeException("NodeTreePanel: attached node has no parent node");
            nodeParent.remove(findTreeNode(child));
            ((DefaultTreeModel)tree.getModel()).reload();
        }
    }
}
