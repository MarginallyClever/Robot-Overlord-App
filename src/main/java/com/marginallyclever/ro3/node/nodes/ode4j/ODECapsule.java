package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.ode.DCapsule;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * Wrapper for a ODE4J capsule.
 */
public class ODECapsule extends ODEBody {
    private double radius = 2.5;
    private double length = 5.0;
    private double massQty = Math.PI * radius * radius * length;

    public ODECapsule() {
        this("ODE Capsule");
    }

    public ODECapsule(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODECapsulePanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = OdeHelper.createCapsule(physics.getODESpace(), radius, length);
        geom.setBody(body);

        mass.setCapsuleTotal(massQty, 3, radius, length);
        body.setMass(mass);

        // add a Node with a MeshInstance to represent the ball.
        MeshInstance meshInstance = new MeshInstance();
        meshInstance.setMesh(new Cylinder(length, radius, radius));
        addChild(meshInstance);

        Pose b1 = new Pose("Ball1");
        addChild(b1);
        b1.addChild(new MeshInstance());

        Pose b2 = new Pose("Ball2");
        addChild(b2);
        b2.addChild(new MeshInstance());

        Material material = new Material();
        addChild(material);
        b1.addChild(material);
        b2.addChild(material);

        updateSize();
    }

    public double getRadius() {
        return radius;
    }

    public double getLength() {
        return length;
    }

    public void setRadius(double radius) {
        if(radius<=0) throw new IllegalArgumentException("Radius must be greater than zero.");
        this.radius = radius;
        updateSize();
    }

    public void setLength(double length) {
        if(length<=0) throw new IllegalArgumentException("Length must be greater than zero.");
        this.length = length;
        updateSize();
    }

    private void updateSize() {
        ((DCapsule)geom).setParams(radius, length);
        geom.setBody(body);

        mass.setCapsuleTotal(massQty, 3, radius, length);
        body.setMass(mass);

        MeshInstance meshInstance = findFirstChild(MeshInstance.class);
        if(null != meshInstance) {
            meshInstance.setMesh(new Cylinder(length, radius, radius));
        }

        MeshInstance b1 = findNodeByPath("Ball1/MeshInstance",MeshInstance.class);
        if(null != b1) {
            b1.setMesh(new Sphere((float) radius));
            b1.setPosition(new Vector3d(0, 0, (float) length /2));
        }

        MeshInstance b2 = findNodeByPath("Ball2/MeshInstance",MeshInstance.class);
        if(null != b2) {
            b2.setMesh(new Sphere((float) radius));
            b2.setPosition(new Vector3d(0, 0, -(float) length /2));
        }
    }
}
