package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
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
import java.awt.*;
import java.util.Objects;

/**
 * Wrapper for a ODE4J cylinder.
 */
public class ODECylinder extends ODEBody {
    private double radius = 2.5;
    private double length = 5.0;
    private double massQty = Math.PI * radius * radius * length;

    public ODECylinder() {
        this("ODE Cylinder");
    }

    public ODECylinder(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = OdeHelper.createCylinder(physics.getODESpace(),radius, length);
        geom.setBody(body);

        mass.setCylinderTotal(massQty, 3, radius, length);
        body.setMass(mass);

        // add a Node with a MeshInstance to represent the ball.
        MeshInstance meshInstance = new MeshInstance();
        meshInstance.setMesh(new Cylinder(length, radius, radius));
        addChild(meshInstance);

        addChild(new Material());
    }
}
