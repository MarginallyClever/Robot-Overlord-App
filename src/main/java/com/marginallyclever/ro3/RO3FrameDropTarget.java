package com.marginallyclever.ro3;

import com.marginallyclever.ro3.apps.actions.Import;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Allows the user to drop Scene or a supported mesh file onto the main window.  They will be
 * imported to the existing scene and appear at the world origin.
 */
public class RO3FrameDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RO3FrameDropTarget.class);

    public RO3FrameDropTarget() {
        super();
        logger.info("adding drag + drop support...");
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        try {
            Transferable tr = event.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            int complete = 0;
            for (DataFlavor flavor : flavors) {
                logger.debug("Possible flavor: {}", flavor.getMimeType());
                if (flavor.isFlavorJavaFileListType()) {
                    Import importer = new Import();

                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object object = tr.getTransferData(flavor);
                    if (object instanceof List<?> list) {
                        for(Object item : list) {
                            if (item instanceof File file) {
                                if(importer.commitImport(file)) {
                                    complete++;
                                }
                            }
                        }
                    }
                }
            }
            if(complete>0) {
                //logger.debug("Drop ok: {}", complete);
                event.dropComplete(true);
                return;
            }
            logger.debug("Drop failed: {}", event);
            event.rejectDrop();
        } catch (Exception e) {
            logger.error("Drop error", e);
            event.rejectDrop();
        }
    }
}
