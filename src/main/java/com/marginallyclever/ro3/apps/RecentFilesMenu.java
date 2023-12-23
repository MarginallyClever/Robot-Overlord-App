package com.marginallyclever.ro3.apps;

import com.marginallyclever.ro3.apps.actions.LoadScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A menu that keeps track of recently loaded files.
 */
public class RecentFilesMenu extends JMenu {
    private static final Logger logger = LoggerFactory.getLogger(RecentFilesMenu.class);
    private final Preferences preferences;
    private static final int MAX_RECENT_FILES = 10;
    private final List<String> recentFiles = new ArrayList<>();

    public RecentFilesMenu(Preferences preferences) {
        this(preferences,"Recent Files");
    }

    public RecentFilesMenu(Preferences preferences,String title) {
        super(title);
        this.preferences = preferences;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadFromPreferences();
        updateRecentFilesMenu();
    }

    @Override
    public void removeNotify() {
        saveToPreferences();
        super.removeNotify();
    }

    /**
     * Loads the list of recent files from preferences.
     * Loads at most {@link RecentFilesMenu#MAX_RECENT_FILES} items.
     */
    public void loadFromPreferences() {
        logger.info("Loading recent files from preferences.");

        int count = preferences.getInt("recentFiles.count", MAX_RECENT_FILES);
        for (int i = 0; i < count; i++) {
            String filePath = preferences.get("recentFiles." + i, "");
            if (!filePath.isEmpty()) {
                recentFiles.remove(filePath);
                recentFiles.add(filePath);
            }
        }
    }

    /**
     * Saves the list of recent files to preferences.
     * Saves at most {@link RecentFilesMenu#MAX_RECENT_FILES} items.
     */
    public void saveToPreferences() {
        logger.info("Saving recent files to preferences.");

        preferences.putInt("recentFiles.count", recentFiles.size());
        int count = Math.min(recentFiles.size(), MAX_RECENT_FILES);
        for (int i = 0; i < count; i++) {
            preferences.put("recentFiles." + i, recentFiles.get(i));
        }

        // remove any extra entries
        for (int i = count; preferences.get("recentFiles." + i, null) != null; i++) {
            preferences.remove("recentFiles." + i);
        }
    }

    private void updateRecentFilesMenu() {
        this.removeAll();
        int index=0;
        for (String filePath : recentFiles) {
            JMenuItem menuItem = new JMenuItem(new LoadScene(this,filePath));
            this.add(menuItem);
            menuItem.setMnemonic(KeyEvent.VK_0 + index);
            ++index;
        }
        this.setVisible(!recentFiles.isEmpty());
    }

    public void removePath(String filePath) {
        recentFiles.remove(filePath);
        saveToPreferences();
    }

    /**
     * Adds a path to the list of recent files.  If the path already exists in the list, it will be moved to the head of the list.
     * @param filePath the path to add.
     */
    public void addPath(String filePath) {
        // remove the path if it already exists so it will be moved to the top of the list.
        recentFiles.remove(filePath);
        recentFiles.add(0,filePath);
        trimList();
        updateRecentFilesMenu();
    }

    private void trimList() {
        while(recentFiles.size()> MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size()-1);
        }
    }

    /**
     * Clears the list of recent files.  To commit the change, call {@link #saveToPreferences()}.
     */
    public void clear() {
        recentFiles.clear();
        updateRecentFilesMenu();
    }
}
