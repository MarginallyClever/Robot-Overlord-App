package com.marginallyclever.ro3.physics;

import org.ode4j.ode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.createWorld;

/**
 * Manages the ODE4J physics world, space, and contact handling.  There must be exactly one of these in the scene
 * for physics to work.
 */
public class ODEPhysics {
    private static final Logger logger = LoggerFactory.getLogger(ODEPhysics.class);

    public double WORLD_CFM = 1e-5;
    public double WORLD_ERP = 0.8;
    public double WORLD_GRAVITY = -9.81;
    private final int ITERS = 20;
    private final int CONTACT_BUFFER_SIZE = 4;

    private DWorld world;
    private DSpace space;
    private DContactBuffer contacts;
    private DJointGroup contactGroup;
    private boolean isPaused = true;

    protected final EventListenerList listeners = new EventListenerList();

    public ODEPhysics() {
        super();
        OdeHelper.initODE2(0);
    }

    public void reset() {
        stopPhysics();
        startPhysics();
    }

    private void startPhysics() {
        logger.info("Starting Physics");

        // build the world
        if(world == null) {
            world = createWorld();
            world.setGravity(0, 0, WORLD_GRAVITY);
            world.setCFM(WORLD_CFM);
            world.setERP(WORLD_ERP);
            world.setQuickStepNumIterations(ITERS);
        }

        // setup a space in the world
        if(space == null) {
            space = OdeHelper.createSapSpace(null, DSapSpace.AXES.XYZ);
            //space = OdeHelper.createSimpleSpace();
        }

        if(contacts == null) {
            contacts = new DContactBuffer(CONTACT_BUFFER_SIZE);
        }

        if(contactGroup == null) {
            contactGroup = OdeHelper.createJointGroup();
        }
    }

    private void stopPhysics() {
        logger.info("Stopping Physics");

        if(contactGroup!=null) {
            contactGroup.empty();
            contactGroup.destroy();
            contactGroup=null;
        }

        if(space!=null) {
            space.destroy();
            space=null;
        }

        if(world!=null) {
            world.destroy();
            world=null;
        }
    }

    public DWorld getODEWorld() {
        return world;
    }

    public DSpace getODESpace() {
        return space;
    }

    public void update(double dt) {
        if(isPaused) return;

        try {
            OdeHelper.spaceCollide(getODESpace(), null, this::nearCallback);
            world.step(dt);
            contactGroup.empty();
        } catch(Exception e) {
            logger.error("update failed.", e);
        }
    }

    /**
     * This is called by ODE4J's dSpaceCollide when two objects in space are potentially colliding.
     * @param data user data
     * @param o1 the first object
     * @param o2 the second object
     */
    private void nearCallback(Object data, DGeom o1, DGeom o2) {
        DBody b1 = o1.getBody();
        DBody b2 = o2.getBody();
        if (b1 != null && b2 != null && OdeHelper.areConnected(b1, b2))
            return;

        try {
            int n = OdeHelper.collide(o1, o2, CONTACT_BUFFER_SIZE, contacts.getGeomBuffer());
            for (int i = 0; i < n; ++i) {
                DContact contact = contacts.get(i);
                contact.surface.mode = dContactSlip1 | dContactSlip2 | dContactSoftERP | dContactSoftCFM | dContactApprox1;

                contact.surface.mu = 0.5;  // friction
                contact.surface.slip1 = 0.0;  // how much the contact surfaces can slide
                contact.surface.slip2 = 0.0;  // how much the contact surfaces can slide
                contact.surface.soft_erp = 0.8;  // how spongy the contact is
                contact.surface.soft_cfm = 0.001;  // how soft to make the contact
                contact.surface.bounce = 0.9;  // how much the contact surfaces can bounce
                contact.surface.bounce_vel = 0.5;  // how fast the contact surfaces can bounce

                DJoint contactJoint = OdeHelper.createContactJoint(world, contactGroup, contact);
                contactJoint.attach(o1.getBody(), o2.getBody());

                // inform any listeners that a collision has occurred.  Use the DGeom to find the associated ODENode
                // and use teh ODENode in the notification.
                fireCollisionEvent(o1,o2,contact);
            }
        } catch (Exception e) {
            logger.error("collision failed.", e);
        }
    }

    /**
     * Notify all listeners that a collision has occurred.
     * @param g1 the first object
     * @param g2 the second object
     * @param contact the contact information
     */
    private void fireCollisionEvent(DGeom g1,DGeom g2,DContact contact) {
        for(CollisionListener listener : listeners.getListeners(CollisionListener.class)) {
            listener.onCollision(g1,g2,contact);
        }
    }

    public void addCollisionListener(CollisionListener listener) {
        listeners.add(CollisionListener.class, listener);
    }

    public void removeCollisionListener(CollisionListener listener) {
        listeners.remove(CollisionListener.class, listener);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean state) {
        isPaused = state;
    }
}
