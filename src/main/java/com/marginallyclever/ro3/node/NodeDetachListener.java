package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeDetachListener} is an interface for listening to node detach events.
 * A detach event fires on a parent after the child is removed.
 */
public interface NodeDetachListener extends EventListener {
    /**
     * Called when a parent node has a child removed.
     * @param child the node that was removed
     */
    void nodeDetached(Node child);
}
