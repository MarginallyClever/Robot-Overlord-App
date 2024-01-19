package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.apps.RO3Frame;
import com.marginallyclever.ro3.apps.UndoSystem;
import com.marginallyclever.ro3.apps.RecentFilesMenu;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;
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
 * Load a scene from a file.  Completely replaces the current Scene.
 */
public class LoadScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadScene.class);
    private final JFileChooser chooser;
    private final String filePath;
    private SaveScene saveScene;
    private final RecentFilesMenu menu;

    /**
     * <p>This constructor is used when the user selects a file from the RecentFilesMenu.  Therefore no selection
     * dialog will appear, therefore a file chooser is not needed.</p>
     * @param menu the RecentFilesMenu that this action is attached to.
     * @param filePath the path to the file to load.
     */
    public LoadScene(RecentFilesMenu menu, String filePath) {
        this(menu,filePath,null);
    }

    /**
     * <p>This constructor is used when the user has not selected a file from the RecentFilesMenu.  Therefore the
     * file selection dialog will appear, therefore a file chooser is needed.</p>
     * @param menu the RecentFilesMenu that this action is attached to.
     * @param filePath the path to the file to load.
     * @param chooser the file chooser to use.
     */
    public LoadScene(RecentFilesMenu menu, String filePath,JFileChooser chooser) {
        super();
        this.chooser = chooser;
        this.menu = menu;
        this.filePath = filePath;
        putValue(Action.NAME,filePath==null || filePath.isEmpty() ? "Load..." : filePath);
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-load-16.png"))));
        putValue(SHORT_DESCRIPTION,"Load a scene from a file.  Completely replaces the current Scene.");
    }

    public void setSaveScene(SaveScene saveScene) {
        this.saveScene = saveScene;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        File src = (filePath != null)
                ? new File(filePath)  // use the given filename
                : runFileDialog((Component) e.getSource());  // ask the user for a filename
        if( src == null ) return;  // cancelled
        try {
            commitLoad(src);
        } catch(Exception ex) {
            logger.error("Error loading file.  ", ex);
            JOptionPane.showMessageDialog((Component) e.getSource(),
                    "Error loading file.  " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        UndoSystem.reset();
    }

    private File runFileDialog(Component source) {
        if( chooser == null ) throw new InvalidParameterException("file chooser cannot be null");
        chooser.setFileFilter(RO3Frame.FILE_FILTER);
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;  // cancelled
    }

    public void commitLoad(File selectedFile) {
        if( selectedFile == null ) throw new InvalidParameterException("File cannot be null");
        if( !selectedFile.exists() ) {
            menu.removePath(selectedFile.getAbsolutePath());
            throw new InvalidParameterException("File does not exist");
        }

        logger.info("Load from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = System.getProperty("user.dir");
        System.setProperty("user.dir",newCWD);

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            // if the json is bad, this will throw an exception before removing the previous scene.
            JSONObject json = new JSONObject(content);

            // reset everything
            NewScene newScene = new NewScene();
            newScene.commitNewScene();

            Node loaded = new Node("Scene");
            loaded.fromJSON(json);
            Registry.setScene(loaded);

            if(menu!=null) menu.addPath(selectedFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error loading file.", e);
        }

        System.setProperty("user.dir",oldCWD);

        if(saveScene!=null) {
            saveScene.setPath(selectedFile.getAbsolutePath());
            saveScene.setEnabled(true);
        }

        logger.info("done.");
    }
}