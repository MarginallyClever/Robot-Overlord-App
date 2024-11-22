package com.marginallyclever.ro3.physics;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.odenode.ODEJoint;
import org.ode4j.ode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.createWorld;

/**
 * Manages the ODE4J physics world, space, and contact handling.  There must be exactly one of these in the scene
 * for physics to work.
 */
public class ODEPhysics {
    private static final Logger logger = LoggerFactory.getLogger(ODEPhysics.class);

    private double WORLD_CFM = 1e-5;
    private double WORLD_ERP = 0.8;
    private double WORLD_GRAVITY = -9.81;
    private final int ITERS = 20;
    private final int CONTACT_BUFFER_SIZE = 4;

    private int spaceType = 0;

    private DWorld world;
    private DSpace space;
    private DContactBuffer contacts;
    private DJointGroup contactGroup;
    private boolean isPaused;

    protected final EventListenerList listeners = new EventListenerList();

    public ODEPhysics() {
        super();
    }

    public void reset() {
        stopPhysics();
        startPhysics();
    }

    private void startPhysics() {
        logger.info("Starting Physics");

        OdeHelper.initODE2(0);

        // build the world
        if(world == null) {
            world = createWorld();
            world.setGravity(0, 0, WORLD_GRAVITY);
            world.setCFM(WORLD_CFM);
            world.setERP(WORLD_ERP);
            world.setQuickStepNumIterations(ITERS);
        }

        // create a space in the world
        if(space == null) {
            space = switch(spaceType) {
                case 1 -> OdeHelper.createHashSpace(null);
                case 2 -> OdeHelper.createSapSpace(null, DSapSpace.AXES.XYZ);
                //case 3 -> OdeHelper.createQuadTreeSpace(null, new DVector3(0, 0, 0), new DVector3(100, 100, 100), 0);
                default -> OdeHelper.createSimpleSpace();
            };
        }

        if(contacts == null) {
            contacts = new DContactBuffer(CONTACT_BUFFER_SIZE);
        }

        if(contactGroup == null) {
            contactGroup = OdeHelper.createJointGroup();
        }

        fireActionEvent(new ActionEvent(this, 1, "Physics Started"));
        setPaused(true);
    }

    private void stopPhysics() {
        logger.info("Stopping Physics");

        if (contactGroup != null) {
            contactGroup.empty();
            contactGroup.destroy();
            contactGroup = null;
        }

        if (space != null) {
            space.destroy();
            space = null;
        }

        if (world != null) {
            world.destroy();
            world = null;
        }

        fireActionEvent(new ActionEvent(this, 0, "Physics Stopped"));
    }

    private void fireActionEvent(ActionEvent e) {
        for(ActionListener listener : listeners.getListeners(ActionListener.class)) {
            listener.actionPerformed(e);
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
            if(dt>0) world.quickStep(dt);  // advance the simulation.  reportedly better than using step().
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

        // Notify all listeners that the physics engine is doing something.
        if(isPaused) fireActionEvent(new ActionEvent(this, 2, "Physics Paused"));
        else         fireActionEvent(new ActionEvent(this, 3, "Physics Running"));
    }

    public double getCFM() {
        return WORLD_CFM;
    }

    public void setCFM(double WORLD_CFM) {
        this.WORLD_CFM = WORLD_CFM;
        if(world!=null) world.setCFM(WORLD_CFM);
    }

    public double getERP() {
        return WORLD_ERP;
    }

    public void setERP(double WORLD_ERP) {
        this.WORLD_ERP = WORLD_ERP;
        if(world!=null) world.setERP(WORLD_ERP);
    }

    public double getGravity() {
        return WORLD_GRAVITY;
    }

    public void setGravity(double WORLD_GRAVITY) {
        this.WORLD_GRAVITY = WORLD_GRAVITY;
        if(world!=null) world.setGravity(0, 0, WORLD_GRAVITY);
    }

    public void addActionListener(ActionListener a) {
        listeners.add(ActionListener.class, a);
    }

    public void removeActionListener(ActionListener a) {
        listeners.remove(ActionListener.class, a);
    }

    /**
     * Deferred action to be taken after some nodes have been added to the scene.
     */
    public void deferredAction(Node created) {
        logger.debug("Deferred action on {}",created.getName());
        // search for all ODEJoint nodes and connect them.
        Queue<Node> toScan = new LinkedList<>();
        toScan.add(created);
        while(!toScan.isEmpty()) {
            Node n = toScan.remove();
            toScan.addAll(n.getChildren());
            if(n instanceof ODEJoint j) {
                j.setPartA(j.getPartA().getSubject());
                j.setPartB(j.getPartB().getSubject());
            }
        }
    }
}
