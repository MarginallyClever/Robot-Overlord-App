package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.ode.DCylinder;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import java.util.List;

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
    public void getComponents(List<JPanel> list) {
        list.add(new ODECylinderPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = OdeHelper.createCylinder(physics.getODESpace(), radius, length);
        geom.setBody(body);

        mass.setCylinderTotal(massQty, 3, radius, length);
        body.setMass(mass);

        addChild(new MeshInstance());
        addChild(new Material());
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
        ((DCylinder)geom).setParams(radius, length);
        geom.setBody(body);

        mass.setCylinderTotal(massQty, 3, radius, length);
        body.setMass(mass);

        MeshInstance meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Cylinder(length, radius, radius));
        }
    }
}
