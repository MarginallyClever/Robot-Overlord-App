package com.marginallyclever.ro3.node.nodes.odenode;

import java.util.EventListener;

/**
 * Listeners are notified when an {@link ODENode} is attached to the scene.
 * This is not the same as when {@link com.marginallyclever.ro3.node.NodeAttachListener} fires on the parent because the child is attached.
 */
public interface ODELinkAttachListener extends EventListener {
    void linkAttached(ODENode node);
}
