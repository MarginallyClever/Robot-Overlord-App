package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeAttachListener} is an interface for listening to {@link Node} attach events.
 */
public interface NodeAttachListener extends EventListener {
    /**
     * Called when a child is attached to a new parent.
     * @param child the {@link Node} that was attached
     */
    void nodeAttached(Node child);
}
