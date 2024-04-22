package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;

import javax.swing.*;
import javax.vecmath.Matrix4d;

import java.awt.*;
import java.util.Objects;

import static org.ode4j.ode.OdeHelper.*;

/**
 * Wrapper for a ODE4J Sphere.
 */
public class ODESphere extends Pose {
    private static final double BALL_RADIUS = 2.5;
    private static final double BALL_MASS = BALL_RADIUS*5;

    private DBody body;
    private DGeom geom;

    public ODESphere() {
        this("ODE Sphere");
    }

    public ODESphere(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        body = OdeHelper.createBody(physics.getODEWorld());
        geom = createSphere(physics.getODESpace(), BALL_RADIUS);
        geom.setBody(body);

        DMass mass = OdeHelper.createMass();
        mass.setSphereTotal(BALL_MASS, BALL_RADIUS);
        body.setMass(mass);
        body.setPosition(0, 0, BALL_RADIUS * 2 * 5);

        // add a Node with a MeshInstance to represent the ball.
        MeshInstance meshInstance = new MeshInstance();
        addChild(meshInstance);
        meshInstance.setMesh(new Sphere());
        Matrix4d m = meshInstance.getLocal();
        m.setScale(BALL_RADIUS);
        meshInstance.setLocal(m);

        // add a Material with random diffuse color
        Material material = new Material();
        material.setDiffuseColor(new Color(
                (int)(Math.random()*255.0),
                (int)(Math.random()*255.0),
                (int)(Math.random()*255.0)));
        addChild(material);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(body != null) {
            body.destroy();
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
