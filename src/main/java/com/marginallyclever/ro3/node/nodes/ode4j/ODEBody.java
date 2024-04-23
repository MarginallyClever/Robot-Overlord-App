package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.Objects;

public abstract class ODEBody extends Pose {
    protected DBody body;
    protected DGeom geom;
    protected DMass mass;


    public ODEBody() {
        this("ODE Body");
    }

    public ODEBody(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        body = OdeHelper.createBody(physics.getODEWorld());
        mass = OdeHelper.createMass();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(body != null) {
            try {
                body.destroy();
            } catch(Exception ignored) {}  // if the worldspace is destroyed first, this will throw an exception.
            body = null;
        }
        if(geom != null) {
            geom.destroy();
            geom = null;
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // adjust the position of the Node to match the body.
        if(body == null) return;

        DVector3C translation = body.getPosition();
        DMatrix3C rotation = body.getRotation();
        super.setWorld(ODE4JHelper.assembleMatrix(translation, rotation));
    }

    @Override
    public void setWorld(Matrix4d world) {
        super.setWorld(world);
        if(body == null) return;

        body.setPosition(world.m03, world.m13, world.m23);
        body.setRotation(ODE4JHelper.convertRotationToODE(world));
        // stop movement so object does not fight user.
        body.setAngularVel(new DVector3(0, 0, 0));
        body.setLinearVel(new DVector3(0, 0, 0));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/ode4j/icons8-mechanics-16.png")));
    }
}
