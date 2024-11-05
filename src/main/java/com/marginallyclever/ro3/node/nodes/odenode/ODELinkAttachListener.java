package com.marginallyclever.ro3.node.nodes.odenode;

import java.util.EventListener;

/**
 * Listeners are notified when an ODENode is attached to the scene.
 */
public interface ODELinkAttachListener extends EventListener {
    void linkAttached(ODENode node);
}
