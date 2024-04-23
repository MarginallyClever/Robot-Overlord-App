package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.DHParameterPanel;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.ode.DSphere;

import javax.swing.*;
import javax.vecmath.Matrix4d;

import java.awt.*;
import java.util.List;

import static org.ode4j.ode.OdeHelper.*;

/**
 * Wrapper for a ODE4J Sphere.
 */
public class ODESphere extends ODEBody {
    private double radius = 2.5;
    private double massQty = radius *5;

    public ODESphere() {
        this("ODE Sphere");
    }

    public ODESphere(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODESpherePanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = createSphere(physics.getODESpace(), radius);
        geom.setBody(body);

        addChild(new MeshInstance());
        addChild(new Material());
        setRadius(radius);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if(radius<=0) throw new IllegalArgumentException("Radius must be greater than zero.");
        this.radius = radius;

        mass.setSphereTotal(massQty, radius);
        body.setMass(mass);

        ((DSphere)geom).setRadius(radius);
        geom.setBody(body);

        var meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Sphere((float) radius));
        }
    }
}
