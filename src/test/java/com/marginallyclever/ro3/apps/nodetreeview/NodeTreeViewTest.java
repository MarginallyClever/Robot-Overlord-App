package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.Registry;

import javax.swing.*;

public class NodeTreeViewTest {
    public static void main(String[] args) {
        Registry.start();
        NodeTreeView view = new NodeTreeView();

        // Create a JFrame instance
        JFrame frame = new JFrame();

        // Add the JTree to the JFrame
        frame.add(new JScrollPane(view));

        // Set the default close operation, size, and visibility of the JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
