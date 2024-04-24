package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.ode.DSphere;

import javax.swing.*;
import java.util.List;

import static org.ode4j.ode.OdeHelper.createSphere;

/**
 * Wrapper for a ODE4J Sphere.
 */
public class ODESphere extends ODEBody {
    private double radius = 2.5;

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

        mass.setSphereTotal(Math.PI * radius*radius*radius * 4.0/3.0, radius);
        body.setMass(mass);

        addChild(new MeshInstance());
        addChild(new Material());
        updateSize();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("Radius must be greater than zero.");
        this.radius = radius;
        updateSize();
    }

    private void updateSize() {
        ((DSphere)geom).setRadius(radius);
        geom.setBody(body);

        mass.setSphereTotal(mass.getMass(), radius);
        body.setMass(mass);

        var meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Sphere((float) radius));
        }
    }
}
