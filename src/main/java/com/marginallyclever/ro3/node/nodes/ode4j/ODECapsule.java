package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.ode.OdeHelper;

import javax.vecmath.Vector3d;

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
        meshInstance = new MeshInstance();
        meshInstance.setMesh(new Sphere((float) radius));
        b1.addChild(meshInstance);
        b1.setPosition(new Vector3d(0, 0, (float) length /2));
        addChild(b1);

        Pose b2 = new Pose("Ball2");
        meshInstance = new MeshInstance();
        meshInstance.setMesh(new Sphere((float) radius));
        b2.addChild(meshInstance);
        b2.setPosition(new Vector3d(0, 0, -(float) length /2));
        addChild(b2);

        // add a Material
        Material material = new Material();
        addChild(material);
        b1.addChild(material);
        b2.addChild(material);
    }
}
