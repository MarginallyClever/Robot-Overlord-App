package com.marginallyclever.ro3.node;

import java.util.EventListener;

/**
 * {@link NodeReadyListener} is an interface for listening to node ready events.
 */
public interface NodeReadyListener extends EventListener {
    /**
     * Called when a node is ready, after all it's children are ready.
     * @param source the node that is ready
     */
    void nodeReady(Node source);
}
