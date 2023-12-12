package com.marginallyclever.ro3;


import ModernDocking.Dockable;
import ModernDocking.app.Docking;

import javax.swing.*;

public class DockingPanel extends JPanel implements Dockable {
    private final String text;

    public DockingPanel(String text) {
        this.text = text;
        Docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return text;
    }

    @Override
    public String getTabText() {
        return text;
    }
}