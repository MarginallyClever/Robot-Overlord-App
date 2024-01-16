package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeAttachListener} is an interface for listening to {@link Node} attach events.
 * Attach events are fired on a parent when it gains a new child.
 */
public interface NodeAttachListener extends EventListener {
    /**
     * Called when parent gains a new child.
     * @param child the {@link Node} that was attached
     */
    void nodeAttached(Node child);
}
