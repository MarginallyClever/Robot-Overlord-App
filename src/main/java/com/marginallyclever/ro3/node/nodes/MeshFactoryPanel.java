package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.List;

/**
 * {@link MeshFactoryPanel} is a panel that displays controls to load a mesh from a file.
 * {@link Mesh} are created using the {@link MeshFactory}, which makes sure identical items are not loaded twice.
 */
public class MeshFactoryPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(MeshFactoryPanel.class);
    private static final JFileChooser chooser = new JFileChooser();
    private Mesh lastMeshLoaded;

    public MeshFactoryPanel() {
        super();

        List<FileFilter> filters = MeshFactory.getAllExtensions();
        if (filters.isEmpty()) throw new RuntimeException("No MeshFactory filters found?!");
        if (filters.size() == 1) {
            chooser.setFileFilter(filters.get(0));
        } else {
            for (FileFilter f : filters) {
                chooser.addChoosableFileFilter(f);
            }
        }
    }

    /**
     *
     * @return JFileChooser.APPROVE_OPTION or return JFileChooser.CANCEL_OPTION
     */
    public int run() {
        int returnVal = chooser.showDialog(SwingUtilities.getWindowAncestor(chooser), "Select");
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String absPath = chooser.getSelectedFile().getAbsolutePath();
            try {
                lastMeshLoaded = MeshFactory.load(absPath);
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
