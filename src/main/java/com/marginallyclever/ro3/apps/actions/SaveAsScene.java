package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.RO3Frame;
import com.marginallyclever.ro3.apps.RecentFilesMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Save the entire scene to a file.
 */
public class SaveAsScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveAsScene.class);
    private final JFileChooser chooser;
    private final RecentFilesMenu menu;

    public SaveAsScene(RecentFilesMenu menu, JFileChooser chooser) {
        super();
        this.chooser = chooser;
        this.menu = menu;
        putValue(Action.NAME,"Save As...");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-save-16.png"))));
        putValue(SHORT_DESCRIPTION,"Save to a file.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        String destinationPath = askUserForDestinationPath(source);
        if (destinationPath == null) return;  // cancelled
        try {
            commitSave(destinationPath);
        } catch (IOException ioException) {
            logger.error("Error saving file.  ", ioException);
            JOptionPane.showMessageDialog(source,
                    "Error saving file.  " + ioException.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        menu.addPath(destinationPath);
    }

    private String askUserForDestinationPath(Component source) {
        if( chooser == null ) throw new InvalidParameterException("file chooser cannot be null");
        chooser.setFileFilter(RO3Frame.FILE_FILTER);

        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        var myListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                    String[] extensions = RO3Frame.FILE_FILTER.getExtensions();
                    File f = chooser.getSelectedFile();
                    String fname = f.getName().toLowerCase();
                    boolean matches = Arrays.stream(extensions).anyMatch((ext) -> fname.toLowerCase().endsWith("." + ext));
                    if (!matches) {
                        f = new File(f.getPath() + "." + extensions[0]);  // append the first extension from ZIP_FILTER
                        chooser.setSelectedFile(f);
                    }
                }
            }
        };

        chooser.addActionListener(myListener);
        try {
            int response;
            do {
                if (chooser.showSaveDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
                    return null;  // cancelled
                }
                // check before overwriting.
                response = JOptionPane.YES_OPTION;
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile.exists()) {
                    response = JOptionPane.showConfirmDialog(parentFrame,
                            "Do you want to replace the existing file?",
                            "Confirm Overwrite",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                }
                // if the user says no, then loop back to the file chooser.
            } while (response == JOptionPane.NO_OPTION);
        }
        finally {
            chooser.removeActionListener(myListener);
        }
        return chooser.getSelectedFile().getAbsolutePath();
    }

    public static void commitSave(String absolutePath) throws IOException {
        logger.info("Save to {}",absolutePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absolutePath))) {
            writer.write(Registry.getScene().toJSON().toString());
        }

        logger.info("done.");
    }
}
