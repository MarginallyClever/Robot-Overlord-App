package com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.shapes.Capsule;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.physics.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
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
    private double length = 10.0;

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
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        geom = OdeHelper.createCapsule(Registry.getPhysics().getODESpace(), radius, length);
        geom.setBody(body);

        mass.setCapsuleTotal(ODE4JHelper.volumeCapsule(radius,length), 3, radius, length);
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

        ((DCapsule)geom).setParams(radius, length);
        geom.setBody(body);

        mass.setCapsuleTotal(mass.getMass(), 3, radius, length);
        body.setMass(mass);

        MeshInstance meshInstance = findFirstChild(MeshInstance.class);
        if(null != meshInstance) {
            meshInstance.setMesh(new Capsule(length, radius));
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
