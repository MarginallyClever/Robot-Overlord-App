package com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.shapes.Cylinder;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.physics.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
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
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        geom = OdeHelper.createCylinder(Registry.getPhysics().getODESpace(), radius, length);
        geom.setBody(body);

        mass.setCylinderTotal(getMassQty(), 3, radius, length);
        body.setMass(mass);

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
        if(geom==null) return;

        ((DCylinder)geom).setParams(radius, length);
        geom.setBody(body);

        mass.setCylinderTotal(getMassQty(), 3, radius, length);
        body.setMass(mass);

        MeshInstance meshInstance = findFirstChild(MeshInstance.class);
        if(null != meshInstance) {
            var mesh = meshInstance.getMesh();
            if(mesh==null || mesh instanceof Cylinder) {
                meshInstance.setMesh(new Cylinder(length, radius, radius));
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        var json= super.toJSON();
        json.put("radius", radius);
        json.put("length", length);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("radius")) radius = json.getDouble("radius");
        if(json.has("length")) length = json.getDouble("length");
        updateSize();
    }
}
