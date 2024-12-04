package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeBranch;
import com.marginallyclever.ro3.apps.shared.SearchBar;
import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog that allows the user to select a node from the scene graph.
 * @param <T> the type of node to select
 */
public class NodeSelectionDialog<T extends Node> extends JPanel {
    private final SearchBar searchBar = new SearchBar();
    private final JTree tree = new JTree();
    private T selectedNode;

    @SuppressWarnings("unchecked")
    public NodeSelectionDialog() {
        this((Class<T>) Node.class);
    }

    public NodeSelectionDialog(Class<T> type) {
        super(new BorderLayout());

        setupTree(type);
        setupSearchBar();

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            tree.clearSelection();
            selectedNode = null;
        });

        add(searchBar, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        add(clearButton, BorderLayout.SOUTH);

        populateTree();
    }

    private void setupSearchBar() {
        searchBar.addPropertyChangeListener("match", e-> populateTree() );
    }

    private void setupTree(Class<T> type) {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            NodeTreeBranch branch = (NodeTreeBranch) tree.getLastSelectedPathComponent();
            if (branch == null || !type.isInstance(branch.getNode())) {
                branch = null;
            }
            selectedNode = branch == null ? null : type.cast(branch.getNode());
        });
        // Set the custom tree cell renderer.  Must come after the tree has been populated because JTree model,
        // by default, is not empty.  The default branches are not NodeTreeBranches.
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if(!(value instanceof NodeTreeBranch branch)) return this;

                if (!type.isInstance(branch.getNode())) {
                    setForeground(Color.LIGHT_GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
                setIcon(branch.getNode().getIcon());
                setToolTipText(branch.getNode().getClass().getSimpleName());
                return this;
            }
        });
        tree.setToolTipText("");
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
        Node rootNode = Registry.getScene();

        List<Node> matches = findAllNodesMatching(rootNode);
        addAllParents(matches);

        NodeTreeBranch rootTreeNode = new NodeTreeBranch(rootNode);
        addBranches(rootNode, rootTreeNode, matches);
        tree.setModel(new DefaultTreeModel(rootTreeNode));

        // Select the first match if there are any matches
        if (!matches.isEmpty()) {
            selectNodeInTree(matches.get(0));
        }
    }

    /**
     * Add all parents of matching nodes to the list of matches
     * @param matches a list of nodes that match the search criteria
     */
    private void addAllParents(List<Node> matches) {
        List<Node> toAdd = new ArrayList<>();
        for (Node node : matches) {
            Node parent = node.getParent();
            while (parent != null) {
                if(!matches.contains(parent) && !toAdd.contains(parent)) {
                    toAdd.add(parent);
                }
                parent = parent.getParent();
            }
        }
        matches.addAll(toAdd);
    }

    /**
     * find all nodes matching the search criteria
     * @param rootNode the root node of the tree to search
     * @return a list of all nodes matching the search criteria
     */
    private List<Node> findAllNodesMatching(Node rootNode) {
        List<Node> matches = new ArrayList<>();
        List<Node> toSearch = new ArrayList<>();
        toSearch.add(rootNode);
        while(!toSearch.isEmpty()) {
            Node node = toSearch.remove(0);
            String name = node.getName();
            if(searchBar.matches(name)) {
                matches.add(node);
            }
            toSearch.addAll(node.getChildren());
        }
        return matches;
    }

    private void addBranches(Node node, NodeTreeBranch treeNode, List<Node> matches) {
        for (Node child : node.getChildren()) {
            if(!matches.contains(child)) continue;

            NodeTreeBranch childTreeNode = new NodeTreeBranch(child);
            treeNode.add(childTreeNode);
            addBranches(child, childTreeNode, matches);
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

    private TreePath findNodeInTree(NodeTreeBranch branch, Node node) {
        if (branch.getNode() == node) {
            return new TreePath(branch.getPath());
        }
        for (int i = 0; i < branch.getChildCount(); i++) {
            TreePath path = findNodeInTree((NodeTreeBranch) branch.getChildAt(i), node);
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