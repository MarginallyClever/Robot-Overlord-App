package com.marginallyclever.ro3.apps.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Load a file into the {@link EditorPanel}
 */
public class LoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadAction.class);
    private final EditorPanel editorPanel;
    private final JFileChooser chooser;
    private final String filePath;

    public LoadAction(EditorPanel editorPanel,JFileChooser chooser) {
        this(editorPanel,chooser,null);
    }

    public LoadAction(EditorPanel editorPanel,JFileChooser chooser,String filePath) {
        super(filePath==null || filePath.isEmpty() ? "" : filePath);
        this.editorPanel = editorPanel;
        this.chooser = chooser;
        this.filePath = filePath;
        putValue(Action.NAME,"Load");
        putValue(SHORT_DESCRIPTION,"Load text from a file.  Replaces the current text.");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-load-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File src = (filePath != null)
                ? new File(filePath)  // use the given filename
                : runFileDialog((Component) e.getSource());  // ask the user for a filename
        if( src == null ) return;  // cancelled
        commitLoad(src);
    }

    private File runFileDialog(Component source) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;  // cancelled
    }

    private void commitLoad(File selectedFile) {
        if( selectedFile == null ) throw new InvalidParameterException("selectedFile cannot be null");

        logger.info("Load from {}",selectedFile.getAbsolutePath());

        if( !selectedFile.exists() ) {
            logger.error("File does not exist.");
            //menu.removePath(selectedFile.getAbsolutePath());
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));

            editorPanel.setText(content);

            //menu.addPath(selectedFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error loading file.", e);
        }

        logger.info("done.");
    }
}
