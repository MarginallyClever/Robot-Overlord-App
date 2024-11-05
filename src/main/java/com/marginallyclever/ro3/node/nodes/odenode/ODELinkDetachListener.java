package com.marginallyclever.ro3.node.nodes.odenode;

import java.util.EventListener;

/**
 * Listeners are notified when an {@link ODENode} is detached from the scene.
 * This is not the same as when {@link com.marginallyclever.ro3.node.NodeDetachListener} fires on the parent because the child is removed.
 */
public interface ODELinkDetachListener extends EventListener {
    void linkDetached(ODENode node);
}
