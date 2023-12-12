package com.marginallyclever.ro3;

import ModernDocking.DockingRegion;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;

import javax.swing.*;
import java.awt.*;

/**
 * RO3 is a robot arm control program.
 */
public class RO3 extends JFrame {
    public RO3() {
        super("RO3");
        setSize(800, 600);
        try {
            Docking.initialize(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}

        DockingPanel a = new DockingPanel("Hello World");
        Docking.dock(a, this);

        DockingPanel b = new DockingPanel("Something else");
        Docking.dock(b,a, DockingRegion.WEST);

        b = new DockingPanel("Isn't it lovely");
        Docking.dock(b,a, DockingRegion.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> (new RO3()).setVisible(true) );
    }
}
