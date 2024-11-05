package com.marginallyclever.ro3.node.nodes.odenode;

import java.util.EventListener;

/**
 * Listeners are notified when an ODENode is detached from the scene.
 */
public interface ODELinkDetachListener extends EventListener {
    void linkDetached(ODENode node);
}
