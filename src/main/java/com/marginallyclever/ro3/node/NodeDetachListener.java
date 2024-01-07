package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeDetachListener} is an interface for listening to node detach events.
 */
public interface NodeDetachListener extends EventListener {
    /**
     * Called when a node is detached from its parent.
     * @param child the node that was detached
     */
    void nodeDetached(Node child);
}
