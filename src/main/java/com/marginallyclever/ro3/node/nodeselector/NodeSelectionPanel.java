package com.marginallyclever.ro3.node.nodeselector;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodetreeview.NodeTreeBranch;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class NodeSelectionPanel<T extends Node> extends JPanel {
    private final JTree tree = new JTree();
    private T selectedNode;

    public NodeSelectionPanel(Class<T> type) {
        super(new BorderLayout());

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            NodeTreeBranch branch = (NodeTreeBranch) tree.getLastSelectedPathComponent();
            if (branch == null || !type.equals(branch.getNode().getClass())) {
                branch = null;
            }
            selectedNode = branch == null ? null : type.cast(branch.getNode());
        });

        add(new JScrollPane(tree), BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            tree.clearSelection();
            selectedNode = null;
        });
        add(clearButton, BorderLayout.SOUTH);

        populateTree();

        // Set the custom tree cell renderer.  Must come after the tree has been populated because JTree model,
        // by default, is not empty.  The default branches are not NodeTreeBranches.
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                NodeTreeBranch branch = (NodeTreeBranch) value;
                Node node = branch.getNode();
                if (!type.equals(node.getClass())) {
                    setForeground(Color.GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
    }

    public void setSubject(T subject) {
        selectedNode = subject;
        if (subject != null) {
            selectNodeInTree(subject);
        } else {
            tree.clearSelection();
        }
    }

    private void populateTree() {
        Node rootNode = Registry.getScene();  // Implement this method to get the root node
        NodeTreeBranch rootTreeNode = new NodeTreeBranch(rootNode);
        addChildren(rootNode, rootTreeNode);
        tree.setModel(new DefaultTreeModel(rootTreeNode));
    }

    private void addChildren(Node node, NodeTreeBranch treeNode) {
        for (Node child : node.getChildren()) {
            NodeTreeBranch childTreeNode = new NodeTreeBranch(child);
            treeNode.add(childTreeNode);
            addChildren(child, childTreeNode);
        }
    }

    private void selectNodeInTree(Node node) {
        NodeTreeBranch rootNode = (NodeTreeBranch) tree.getModel().getRoot();
        TreePath path = findNodeInTree(rootNode, node);
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    private TreePath findNodeInTree(NodeTreeBranch root, Node node) {
        if (root.getNode() == node) {
            return new TreePath(root.getPath());
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            TreePath path = findNodeInTree((NodeTreeBranch) root.getChildAt(i), node);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    public T getSelectedNode() {
        return selectedNode;
    }
}