package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.apps.nodetreeview.TransferableNodeList;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.List;

/**
 * Allows {@link Node}s to be dropped onto a {@link NodeSelector}.
 * @param <T> the type of node allowed to drop.
 */
public class NodeSelectorDropTarget<T extends Node> implements DropTargetListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeSelectorDropTarget.class);
    private final NodeSelector<T> nodeSelector;
    private final Class<T> type;

    public NodeSelectorDropTarget(NodeSelector<T> nodeSelector,Class<T> type) {
        super();
        this.nodeSelector = nodeSelector;
        this.type = type;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(TransferableNodeList.flavor)) {
                List<?> list = (List<?>)tr.getTransferData(TransferableNodeList.flavor);
                if(list.size()!=1) {
                    dtde.rejectDrag();
                    return;
                }
                Node node = (Node)list.get(0);
                if(type.isInstance(node)) {
                    dtde.acceptDrag(DnDConstants.ACTION_LINK);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Drag error.",e);
        }
        dtde.rejectDrag();
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();

            if (tr.isDataFlavorSupported(TransferableNodeList.flavor)) {
                List<?> list = (List<?>)tr.getTransferData(TransferableNodeList.flavor);
                if(list.size()!=1) {
                    dtde.rejectDrop();
                    return;
                }
                Node node = (Node)list.get(0);
                if(type.isInstance(node)) {
                    dtde.acceptDrop(DnDConstants.ACTION_LINK);
                    nodeSelector.setSubject(type.cast(node));
                    dtde.dropComplete(true);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Drop error.",e);
        }
        dtde.rejectDrop();
    }
}
