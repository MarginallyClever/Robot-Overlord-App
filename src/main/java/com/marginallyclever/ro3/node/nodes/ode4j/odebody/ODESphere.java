package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
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
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = createSphere(physics.getODESpace(), radius);
        geom.setBody(body);

        mass.setSphereTotal(ODE4JHelper.volumeSphere(radius), radius);
        body.setMass(mass);

        if(findFirstChild(MeshInstance.class)==null) addChild(new MeshInstance());
        if(findFirstChild(Material.class)==null) addChild(new Material());
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
        if(geom==null) return;

        ((DSphere)geom).setRadius(radius);
        geom.setBody(body);

        mass.setSphereTotal(mass.getMass(), radius);
        body.setMass(mass);

        var meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Sphere((float) radius));
        }
    }

    @Override
    public JSONObject toJSON() {
        var json= super.toJSON();
        json.put("radius", radius);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("radius")) radius = json.getDouble("radius");
        updateSize();
    }
}
