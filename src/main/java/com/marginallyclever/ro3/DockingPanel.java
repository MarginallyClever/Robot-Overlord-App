package com.marginallyclever.ro3;


import ModernDocking.Dockable;
import ModernDocking.app.Docking;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class DockingPanel extends JPanel implements Dockable {
    private final String tabText;
    private final String persistentID = UUID.randomUUID().toString();

    public DockingPanel(String tabText) {
        super(new BorderLayout());
        this.tabText = tabText;
        Docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return persistentID;
    }

    @Override
    public String getTabText() {
        return tabText;
    }
}