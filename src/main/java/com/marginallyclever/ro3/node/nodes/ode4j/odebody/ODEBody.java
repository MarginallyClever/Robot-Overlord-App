package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEBodyPanel;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.List;
import java.util.Objects;

public abstract class ODEBody extends Pose {
    protected DBody body;
    protected DGeom geom;
    protected DMass mass;


    public ODEBody() {
        this("ODE Body");
    }

    public ODEBody(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEBodyPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        body = OdeHelper.createBody(physics.getODEWorld());
        mass = OdeHelper.createMass();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(body != null) {
            try {
                body.destroy();
            } catch(Exception ignored) {}  // if the worldspace is destroyed first, this will throw an exception.
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
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        if(body == null) return;

        var world = getWorld();
        body.setPosition(world.m03, world.m13, world.m23);
        body.setRotation(ODE4JHelper.convertRotationToODE(world));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/ode4j/icons8-mechanics-16.png")));
    }

    public double getMassQty() {
        return mass.getMass();
    }

    /**
     *
     * @param massQty must be >= 0
     */
    public void setMassQty(double massQty) {
        if(massQty<0) throw new IllegalArgumentException("Mass must be greater than zero.");
        mass.setMass(massQty);
        body.setMass(mass);
    }

    public DBody getODEBody() {
        return body;
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("mass", getMassQty());
        json.put("avel", ODE4JHelper.vector3ToJSON(body.getAngularVel()));
        json.put("lvel", ODE4JHelper.vector3ToJSON(body.getLinearVel()));
        return json;

    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("mass")) setMassQty(from.getDouble("mass"));
        if(from.has("avel")) body.setAngularVel(ODE4JHelper.jsonToVector3(from.getJSONObject("avel")));
        if(from.has("lvel")) body.setLinearVel(ODE4JHelper.jsonToVector3(from.getJSONObject("lvel")));
    }
}
