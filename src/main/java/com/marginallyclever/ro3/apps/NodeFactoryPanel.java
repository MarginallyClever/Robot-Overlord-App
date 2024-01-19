package com.marginallyclever.ro3.apps;

import com.marginallyclever.ro3.NodeFactory;
import com.marginallyclever.ro3.apps.shared.SearchBar;
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

/**
 * {@link NodeFactoryPanel} allows a user to select from a list of things that can be created by a given {@link NodeFactory}.
 * @param <T> the class of thing to create.
 */
public class NodeFactoryPanel<T> extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(NodeFactoryPanel.class);
    private final JTree tree = new JTree();
    private final NodeFactory<T> factory;
    private final JButton okButton = new JButton("OK");
    private final SearchBar searchBar = new SearchBar();

    private static class FactoryCategoryCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            var branch = (DefaultMutableTreeNode) value;
            if(branch.getUserObject() instanceof NodeFactory.Category<?> category) {
                if (category.getSupplier() == null) {
                    setForeground(Color.LIGHT_GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
                setText(category.getName());
            }
            return this;
        }
    }

    public NodeFactoryPanel(NodeFactory<T> factory) {
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
        logger.debug("Found {} matches", matches.size());
        addAllParents(matches);
        logger.debug("Grown to {} matches", matches.size());

        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(root);
        addBranches(root, rootTreeNode, matches);
        tree.setModel(new DefaultTreeModel(rootTreeNode));

        // Select the first match if there are any matches
        if (!matches.isEmpty()) {
            selectNodeInTree(matches.get(0));
        }
    }

    private void selectNodeInTree(NodeFactory.Category<T> node) {
        var rootNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
        TreePath path = findNodeInTree(rootNode, node);
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    private TreePath findNodeInTree(DefaultMutableTreeNode branch, NodeFactory.Category<T> node) {
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

    private void addBranches(NodeFactory.Category<T> node, DefaultMutableTreeNode branch, List<NodeFactory.Category<T>> matches) {
        for(NodeFactory.Category<T> child : node.getChildren()) {
            if(!matches.contains(child)) continue;

            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
            branch.add(childTreeNode);
            addBranches(child, childTreeNode,matches);
        }
    }

    private void addAllParents(List<NodeFactory.Category<T>> matches) {
        List<NodeFactory.Category<T>> toAdd = new ArrayList<>();
        for (NodeFactory.Category<T> node : matches) {
            NodeFactory.Category<T> parent = node.getParent();
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
     * @param searchCriteria the search criteria
     * @return a list of all categories that match the search criteria
     */
    private List<NodeFactory.Category<T>> findAllTypesMatching(NodeFactory.Category<T> root) {
        List<NodeFactory.Category<T>> matches = new ArrayList<>();
        List<NodeFactory.Category<T>> toSearch = new ArrayList<>();
        toSearch.add(root);
        while(!toSearch.isEmpty()) {
            NodeFactory.Category<T> category = toSearch.remove(0);
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
                NodeFactory.Category<T> category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
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
            NodeFactory.Category<T> category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
            if (category.getSupplier() != null) {
                return JOptionPane.OK_OPTION;
            }
        }
        return JOptionPane.CANCEL_OPTION;
    }

    public String getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            NodeFactory.Category<T> category = getCategory((DefaultMutableTreeNode) path.getLastPathComponent());
            return category.getName();
        }
        return null;
    }

    NodeFactory.Category<T> getCategory(DefaultMutableTreeNode branch) {
        Object obj = branch.getUserObject();
        return (NodeFactory.Category<T>)obj;
    }
}
