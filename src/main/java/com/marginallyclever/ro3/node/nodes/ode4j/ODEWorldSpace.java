package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.ode4j.ode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.createWorld;

/**
 * Manages the ODE4J physics world, space, and contact handling.  There must be exactly one of these in the scene
 * for physics to work.
 */
public class ODEWorldSpace extends Node {
    private static final Logger logger = LoggerFactory.getLogger(ODEWorldSpace.class);

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

    public ODEWorldSpace() {
        this("ODEWorldSpace");
    }

    public ODEWorldSpace(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEWorldSpacePanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        OdeHelper.initODE2(0);

        startPhysics();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        stopPhysics();
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

    @Override
    public void update(double dt) {
        super.update(dt);

        if(isPaused) return;

        try {
            OdeHelper.spaceCollide(getODESpace(), null, this::nearCallback);
            world.step(dt);
            contactGroup.empty();
        } catch(Exception e) {
            logger.error("update failed.", e);
        }
    }

    // this is called by dSpaceCollide when two objects in space are
    // potentially colliding.
    private void nearCallback(Object data, DGeom o1, DGeom o2) {
        DBody b1 = o1.getBody();
        DBody b2 = o2.getBody();
        if (b1 != null && b2 != null && OdeHelper.areConnected(b1, b2))
            return;

        try {
            ODEWorldSpace physics = Registry.getScene().findFirstChild(ODEWorldSpace.class);

            int n = OdeHelper.collide(o1, o2, CONTACT_BUFFER_SIZE, contacts.getGeomBuffer());
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = dContactSlip1 | dContactSlip2 | dContactSoftERP | dContactSoftCFM | dContactApprox1;

                    contact.surface.mu = 0.5;  // friction
                    contact.surface.slip1 = 0.0;  // how much the contact surfaces can slide
                    contact.surface.slip2 = 0.0;  // how much the contact surfaces can slide
                    contact.surface.soft_erp = 0.8;  // how spongy the contact is
                    contact.surface.soft_cfm = 0.001;  // how soft to make the contact
                    contact.surface.bounce = 0.9;  // how much the contact surfaces can bounce
                    contact.surface.bounce_vel = 0.5;  // how fast the contact surfaces can bounce
                    DJoint contactJoint = OdeHelper.createContactJoint(physics.getODEWorld(), contactGroup, contact);
                    contactJoint.attach(o1.getBody(), o2.getBody());
                }
            }
        } catch (Exception e) {
            logger.error("collision failed.", e);
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/ode4j/icons8-mechanics-16.png")));
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean state) {
        isPaused = state;
    }
}
