package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeRenameListener} is an interface for listening to node rename events.
 */
public interface NodeRenameListener extends EventListener {
    /**
     * Called when a node is renamed.
     * @param node the node that was renamed
     */
    void nodeRenamed(Node node);
}
