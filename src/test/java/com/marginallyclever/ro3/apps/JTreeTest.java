package com.marginallyclever.ro3.apps;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import javax.swing.tree.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class JTreeTest {
    public static void main(String[] args) {
        // Create a JFrame instance
        JFrame frame = new JFrame();

        // Create a JTree instance
        JTree tree = new JTree();

        // Set the selection mode to discontiguous
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            TreePath[] paths = tree.getSelectionPaths();
            if (paths != null) {
                System.out.println("Selected paths: ");
                for (TreePath path : paths) {
                    System.out.println("\t"+path);
                }
            }
            // should be the same result.
            TreePath[] paths2 = e.getPaths();
            if (paths != null) {
                System.out.println("Selected paths 2: ");
                for (TreePath path : paths) {
                    System.out.println("\t"+path);
                }
            }
        });

        // Add the JTree to the JFrame
        frame.add(new JScrollPane(tree));

        // Set the default close operation, size, and visibility of the JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}