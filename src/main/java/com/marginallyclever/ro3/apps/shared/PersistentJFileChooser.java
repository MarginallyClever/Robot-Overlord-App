package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;
import java.io.File;

public class PersistentJFileChooser extends JFileChooser {
    private static final String LAST_USED_DIR = "lastUsedDirectory";
    private final Preferences prefs;

    public PersistentJFileChooser() {
        super();
        prefs = Preferences.userNodeForPackage(PersistentJFileChooser.class);
    }

    @Override
    public int showOpenDialog(java.awt.Component parent) throws HeadlessException {
        retrieveLastUsedDirectory();
        int result = super.showOpenDialog(parent);
        updateLastUsedDirectory();
        return result;
    }

    @Override
    public int showSaveDialog(java.awt.Component parent) throws HeadlessException {
        retrieveLastUsedDirectory();
        int result = super.showSaveDialog(parent);
        updateLastUsedDirectory();
        return result;
    }

    private void retrieveLastUsedDirectory() {
        String lastDirPath = prefs.get(LAST_USED_DIR, null);
        if (lastDirPath != null) {
            setCurrentDirectory(new File(lastDirPath));
        }
    }

    private void updateLastUsedDirectory() {
        File currentDir = getCurrentDirectory();
        if (currentDir != null) {
            prefs.put(LAST_USED_DIR, currentDir.getAbsolutePath());
        }
    }
}