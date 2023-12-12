package com.marginallyclever.ro3;

import ModernDocking.Docking;
import ModernDocking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;

/**
 * RO3 is a robot arm control program.
 */
public class RO3 extends JFrame {
    public RO3() {
        setSize(800, 600);
        try {
            Docking.initialize(this);
        } catch(Exception e) {
            e.printStackTrace();
        }

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}

    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> (new RO3()).setVisible(true) );
    }
}
