package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.node.Node;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

/**
 * A {@link Transferable} that contains a list of {@link Node}s.
 * @param list the list of nodes to transfer
 */
public record TransferableNodeList(List<Node> list) implements Transferable {
    public final static DataFlavor flavor = new DataFlavor(List.class, "List of Nodes");

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return TransferableNodeList.flavor.equals(flavor);
    }

    @Override
    public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return list;
    }
}
