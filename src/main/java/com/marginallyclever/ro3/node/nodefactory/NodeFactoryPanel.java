package com.marginallyclever.ro3.node.nodefactory;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.shared.SearchBar;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link NodeFactoryPanel} allows a user to select from a list of things that can be created by a given {@link NodeFactory}.
 */
public class NodeFactoryPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(NodeFactoryPanel.class);
    private final JTree tree = new JTree();
    private final NodeFactory factory;
    private final JButton okButton = new JButton("OK");
    private final SearchBar searchBar = new SearchBar();

    private static class FactoryCategoryCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            var branch = (DefaultMutableTreeNode) value;
            if(branch.getUserObject() instanceof NodeFactory.Category category) {
                if (category.getSupplier() == null) {
                    setForeground(Color.LIGHT_GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
                setText(category.getName());
                Supplier<Node> supplier = category.getSupplier();
                if (supplier != null) {
                    Node node = category.getSupplier().get();
                    setIcon(node.getIcon());
                }
            }
            return this;
        }
    }

    public NodeFactoryPanel() {
        this(Registry.nodeFactory);
    }

    public NodeFactoryPanel(NodeFactory factory) {
        super(new BorderLayout());
        this.factory = factory;

        setupTree();
        setupSearch();

        add(searchBar, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        populateTree();
    }

    private void setupSearch() {
        searchBar.addPropertyChangeListener("match", e-> populateTree() );
    }

    private void populateTree() {
        var root = factory.getRoot();

        var matches = findAllTypesMatching(root);
        //logger.debug("Found {} matches", matches.size());
        addAllParents(matches);
        //logger.debug("Grown to {} matches", matches.size());

        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(root);
        addBranches(root, rootTreeNode, matches);
        tree.setModel(new DefaultTreeModel(rootTreeNode));

        // Select the first match if there are any matches
        if (!matches.isEmpty()) {
            selectNodeInTree(matches.get(0));
        }
    }

    private void selectNodeInTree(NodeFactory.Category node) {
        var rootNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
        TreePath path = findNodeInTree(rootNode, node);
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    private TreePath findNodeInTree(DefaultMutableTreeNode branch, NodeFactory.Category node) {
        if (getCategory(branch) == node) {
            return new TreePath(branch.getPath());
        }
        for (int i = 0; i < branch.getChildCount(); i++) {
            TreePath path = findNodeInTree((DefaultMutableTreeNode) branch.getChildAt(i), node);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private void addBranches(NodeFactory.Category node, DefaultMutableTreeNode branch, List<NodeFactory.Category> matches) {
        for(NodeFactory.Category child : node.getChildren()) {
            if(!matches.contains(child)) continue;

            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
            branch.add(childTreeNode);
            addBranches(child, childTreeNode,matches);
        }
    }

    private void addAllParents(List<NodeFactory.Category> matches) {
        List<NodeFactory.Category> toAdd = new ArrayList<>();
        for (NodeFactory.Category node : matches) {
            NodeFactory.Category parent = node.getParent();
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
     * Find all categories that match the search criteria.
     * @param root the root of the tree to search
     * @return a list of all categories that match the search criteria
     */
    private List<NodeFactory.Category> findAllTypesMatching(NodeFactory.Category root) {
        List<NodeFactory.Category> matches = new ArrayList<>();
        List<NodeFactory.Category> toSearch = new ArrayList<>();
        toSearch.add(root);
        while(!toSearch.isEmpty()) {
            NodeFactory.Category category = toSearch.remove(0);
            String name = category.getName();
            if(searchBar.matches(name)) {
                matches.add(category);
            }
            toSearch.addAll(category.getChildren());
        }
        return matches;
    }

    private void setupTree() {
        tree.setCellRenderer(new FactoryCategoryCellRenderer());
        tree.addTreeSelectionListener(e -> {
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                NodeFactory.Category category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
                okButton.setEnabled(category.getSupplier() != null);
            }
        });
        tree.setToolTipText("");
    }

    /**
     * @return either JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    public int getResult() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            NodeFactory.Category category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
            if (category.getSupplier() != null) {
                return JOptionPane.OK_OPTION;
            }
        }
        return JOptionPane.CANCEL_OPTION;
    }

    public String getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            NodeFactory.Category category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
            return category.getName();
        }
        return null;
    }

    NodeFactory.Category getCategory(DefaultMutableTreeNode branch) {
        Object obj = branch.getUserObject();
        return (NodeFactory.Category)obj;
    }
}
