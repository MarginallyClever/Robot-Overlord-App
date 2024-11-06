package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.RO3;
import com.marginallyclever.ro3.RO3Frame;
import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class NodeTreeViewTest {
    public static void main(String[] args) {
        RO3.setLookAndFeel();
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
