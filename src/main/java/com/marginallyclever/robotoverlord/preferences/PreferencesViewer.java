package com.marginallyclever.robotoverlord.preferences;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesViewer extends JPanel {
    private final JTree tree;
    private final JTextArea textArea = new JTextArea();

    public PreferencesViewer(Preferences prefs) {
        super(new BorderLayout());

        tree = new JTree(createNodes(prefs));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);

                // Get the name from the node's user object
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    Object userObject = node.getUserObject();
                    if (userObject instanceof Preferences) {
                        this.setText(((Preferences) userObject).name());
                    }
                }
                return this;
            }
        });

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;
                displayPreferences((Preferences) node.getUserObject());
            }
        });

        JScrollPane treeView = new JScrollPane(tree);
        JScrollPane textView = new JScrollPane(textArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, textView);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(200);

        this.add(splitPane, BorderLayout.CENTER);
    }

    private DefaultMutableTreeNode createNodes(Preferences prefs) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(prefs);
        try {
            String[] childrenNames = prefs.childrenNames();
            for (String childName : childrenNames) {
                Preferences childPrefs = prefs.node(childName);
                DefaultMutableTreeNode childNode = createNodes(childPrefs);
                root.add(childNode);
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return root;
    }

    private void displayPreferences(Preferences p) {
        try {
            textArea.setText("");
            String[] keys = p.keys();
            for (String key : keys) {
                String value = p.get(key, "");
                textArea.append(key + ": " + value + "\n");
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}

        JFrame frame = new JFrame("Preferences Viewer");
        Preferences prefs = Preferences.userRoot();
        frame.setContentPane(new PreferencesViewer(prefs));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
