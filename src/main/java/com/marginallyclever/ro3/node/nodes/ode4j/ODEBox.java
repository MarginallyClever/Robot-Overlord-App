package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Box;
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
 * Wrapper for a ODE4J Box.
 */
public class ODEBox extends Pose {
    private static final double CUBE_SIDE_LENGTH = 5.0;
    private static final double CUBE_MASS = 23.0;

    private DBody body;
    private DGeom geom;

    public ODEBox() {
        this("ODE Box");
    }

    public ODEBox(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        // add scene elements
        body = OdeHelper.createBody(physics.getODEWorld());
        geom = createBox(physics.getODESpace(), CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH);
        geom.setBody(body);

        DMass mass = OdeHelper.createMass();
        mass.setBoxTotal(CUBE_MASS, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH);
        body.setMass(mass);
        body.setPosition(0, 0, CUBE_SIDE_LENGTH * 5);

        Matrix4d rx = new Matrix4d();
        Matrix4d ry = new Matrix4d();
        rx.rotX(Math.toRadians(Math.random()*90));
        ry.rotY(Math.toRadians(Math.random()*90));
        Matrix4d mat = new Matrix4d();
        mat.mul(ry, rx);
        body.setRotation(ODE4JHelper.convertRotationToODE(mat));

        // add a Node with a MeshInstance to represent the cube.
        MeshInstance cubeMesh = new MeshInstance();
        addChild(cubeMesh);
        cubeMesh.setMesh(new Box());
        mat = cubeMesh.getLocal();
        mat.setScale(CUBE_SIDE_LENGTH);
        cubeMesh.setLocal(mat);

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
