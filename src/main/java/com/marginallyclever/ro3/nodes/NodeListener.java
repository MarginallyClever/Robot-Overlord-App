package com.marginallyclever.ro3.nodes;

import java.util.EventListener;

/**
 * {@link NodeListener} is a listener for {@link Node} events.
 */
public interface NodeListener extends EventListener {
    void nodeEvent(NodeEvent event);
}
