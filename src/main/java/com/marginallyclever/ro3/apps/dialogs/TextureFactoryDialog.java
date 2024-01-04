package com.marginallyclever.ro3.apps.dialogs;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;
import com.marginallyclever.ro3.texture.TextureFactory;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import com.marginallyclever.ro3.mesh.load.MeshFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.List;

/**
 * <p>{@link TextureFactoryDialog} displays controls to load a {@link com.jogamp.opengl.util.texture.Texture} from a
 * file, wrapped in a {@link TextureWithMetadata}.</p>
 * <p>{@link TextureWithMetadata} are created using the {@link TextureFactory}, which makes sure identical items
 * are not loaded twice.</p>
 */
public class TextureFactoryDialog {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactoryDialog.class);
    private static final JFileChooser chooser = new PersistentJFileChooser();
    private TextureWithMetadata lastTextureLoaded;

    public TextureFactoryDialog() {
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
            lastTextureLoaded = Registry.textureFactory.load(absPath);
            if(lastTextureLoaded==null) {
                logger.error("Failed to load from "+absPath);
                returnVal = JFileChooser.CANCEL_OPTION;
            }
        }

        return returnVal;
    }

    /**
     * @return the last texture loaded by this panel.
     */
    public TextureWithMetadata getTexture() {
        return lastTextureLoaded;
    }
}
