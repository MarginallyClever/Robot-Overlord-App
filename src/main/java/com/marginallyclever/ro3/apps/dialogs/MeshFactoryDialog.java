package com.marginallyclever.ro3.apps.dialogs;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.MeshFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.List;

/**
 * <p>{@link MeshFactoryDialog} displays controls to load a {@link Mesh} from a file.</p>
 * <p>{@link Mesh} are created using the {@link MeshFactory}, which makes sure identical items are not loaded twice.</p>
 */
public class MeshFactoryDialog {
    private static final Logger logger = LoggerFactory.getLogger(MeshFactoryDialog.class);
    private final PersistentJFileChooser chooser = new PersistentJFileChooser();
    private Mesh lastMeshLoaded;

    public MeshFactoryDialog() {
        super();

        List<FileFilter> filters = Registry.meshFactory.getAllExtensions();
        if (filters.isEmpty()) throw new RuntimeException("No filters found?!");
        if (filters.size() == 1) {
            chooser.setFileFilter(filters.get(0));
        } else {
            for (FileFilter f : filters) {
                chooser.addChoosableFileFilter(f);
            }
        }
    }

    /**
     * @return JFileChooser.APPROVE_OPTION or JFileChooser.CANCEL_OPTION
     */
    public int run() {
        int returnVal = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(chooser));
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String absPath = chooser.getSelectedFile().getAbsolutePath();
            try {
                lastMeshLoaded = Registry.meshFactory.load(absPath);
            } catch(Exception e) {
                logger.error("Failed to load from "+absPath,e);
                returnVal = JFileChooser.CANCEL_OPTION;
            }
        }

        return returnVal;
    }

    /**
     * @return the last mesh loaded by this panel.
     */
    public Mesh getMesh() {
        return lastMeshLoaded;
    }
}
