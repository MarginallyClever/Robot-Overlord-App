package com.marginallyclever.ro3;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.AppFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * {@link DockingPanel} is a {@link JPanel} that implements {@link Dockable}.  It is aware of the {@link AppFactory}
 * and will create and add the appropriate {@link com.marginallyclever.ro3.apps.App} when it is added to the component
 * hierarchy.
 */
public class DockingPanel extends JPanel implements Dockable {
    private static final Logger logger = LoggerFactory.getLogger(DockingPanel.class);

    private final String tabText;
    private final String persistentID;

    public DockingPanel(String persistentID, String tabText) {
        super(new BorderLayout());
        this.persistentID = persistentID;
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

    @Override
    public void addNotify() {
        // attempt to create and add the app.
        var app = AppFactory.create(persistentID);
        if (app != null) {
            this.add(app, BorderLayout.CENTER);
        } else {
            logger.warn("no app found for "+persistentID);
        }
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        this.removeAll();  // lose the app
        super.removeNotify();
    }

    /**
     * Refuse to wrap this {@link DockingPanel} in a {@link JScrollPane}.  The panel is responsibile for scrolling,
     * not the docking system.
     * @return false
     */
    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}