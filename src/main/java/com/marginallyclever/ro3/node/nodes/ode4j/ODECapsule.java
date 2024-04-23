package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Objects;

/**
 * Wrapper for a ODE4J capsule.
 */
public class ODECapsule extends Pose {
    private static final double CAPSULE_RADIUS = 2.5;
    private static final double CAPSULE_LENGTH = 5.0;
    private static final double CAPSULE_MASS = Math.PI*CAPSULE_RADIUS*CAPSULE_RADIUS*CAPSULE_LENGTH;

    private DBody body;
    private DGeom geom;

    public ODECapsule() {
        this("ODE Capsule");
    }

    public ODECapsule(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        body = OdeHelper.createBody(physics.getODEWorld());
        geom = OdeHelper.createCapsule(physics.getODESpace(),CAPSULE_RADIUS, CAPSULE_LENGTH);
        geom.setBody(body);

        DMass mass = OdeHelper.createMass();
        mass.setCapsuleTotal(CAPSULE_MASS, 3, CAPSULE_RADIUS, CAPSULE_LENGTH);
        body.setMass(mass);
        body.setPosition(0, 0, CAPSULE_LENGTH * 5);

        // set a random orientation
        Matrix4d rx = new Matrix4d();
        Matrix4d ry = new Matrix4d();
        rx.rotX(Math.toRadians(Math.random()*90));
        ry.rotY(Math.toRadians(Math.random()*90));
        Matrix4d mat = new Matrix4d();
        mat.mul(ry, rx);
        body.setRotation(ODE4JHelper.convertRotationToODE(mat));

        Pose cylinder = new Pose("Cylinder");
        // add a Node with a MeshInstance to represent the ball.
        MeshInstance meshInstance = new MeshInstance();
        meshInstance.setMesh(new Cylinder(CAPSULE_LENGTH, CAPSULE_RADIUS, CAPSULE_RADIUS));
        cylinder.addChild(meshInstance);
        addChild(cylinder);

        Pose b1 = new Pose("Ball1");
        meshInstance = new MeshInstance();
        meshInstance.setMesh(new Sphere((float)CAPSULE_RADIUS));
        b1.addChild(meshInstance);
        b1.setPosition(new Vector3d(0, 0, (float)CAPSULE_LENGTH/2));
        addChild(b1);

        Pose b2 = new Pose("Ball2");
        meshInstance = new MeshInstance();
        meshInstance.setMesh(new Sphere((float)CAPSULE_RADIUS));
        b2.addChild(meshInstance);
        b2.setPosition(new Vector3d(0, 0, -(float)CAPSULE_LENGTH/2));
        addChild(b2);

        // add a Material with random diffuse color
        Material material = new Material();
        material.setDiffuseColor(new Color(
                (int)(Math.random()*255.0),
                (int)(Math.random()*255.0),
                (int)(Math.random()*255.0)));

        cylinder.addChild(material);
        b1.addChild(material);
        b2.addChild(material);
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
