package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

/**
 * This {@link AbstractAction} checks the filename extension and adds it if it's missing.
 */
public class FilenameExtensionChecker extends AbstractAction {
    private final String [] extensions;
    private final JFileChooser chooser;

    public FilenameExtensionChecker(String [] extensions,JFileChooser chooser) {
        super();
        this.extensions = extensions;
        this.chooser = chooser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File f = addExtentionIfMissing(chooser.getSelectedFile());
        chooser.setSelectedFile(f);
    }

    public File addExtentionIfMissing(File f) {
        String fname = f.getName().toLowerCase();
        // check if the file has an extension that matches
        boolean matches = Arrays.stream(extensions).anyMatch((ext) -> fname.toLowerCase().endsWith("." + ext.toLowerCase()));
        if (!matches) {
            // if not, add the first extension
            f = new File(f.getPath() + "." + extensions[0]);  // append the first extension
        }
        return f;
    }
}
