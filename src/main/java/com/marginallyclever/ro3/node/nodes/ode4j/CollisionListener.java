package com.marginallyclever.ro3.node.nodes.ode4j;

import org.ode4j.ode.DContact;
import org.ode4j.ode.DGeom;

import java.util.EventListener;

/**
 * Listens for collisions between objects in the ODE physics world.
 */
public interface CollisionListener extends EventListener {
    /**
     * Called when two objects collide.
     * @param g1 The first object
     * @param g2 The second object
     * @param contact The contact information
     */
    void onCollision(DGeom g1, DGeom g2, DContact contact);
}
