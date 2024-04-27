package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEBodyPanel;
import com.marginallyclever.ro3.node.nodes.ode4j.ODENode;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import org.json.JSONObject;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Objects;

/**
 * <p>An {@link ODENode} that represents a body with a geometry in the ODE physics engine.  This node is responsible
 * for the {@link DBody}, {@link DMass}, and {@link DGeom}.  Classes which extend this class are responsible for the
 * visual and physical representation of the shape.</p>
 * <p>TODO: They should not be responsible for the visual representation because physical and visual shape don't always match.</p>
 */
public abstract class ODEBody extends ODENode {
    protected DBody body;
    protected DGeom geom;
    protected DMass mass;
    private double massQty=1;
    private final Vector3d angularVel = new Vector3d();
    private final Vector3d linearVel = new Vector3d();

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

    /**
     * Called once at the start of the first {@link #update(double)}
     */
    protected void onFirstUpdate() {
        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        body = OdeHelper.createBody(physics.getODEWorld());
        mass = OdeHelper.createMass();
        updateMass();
        updatePoseFromPhysics();
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
        updatePoseFromPhysics();
    }

    protected void updatePoseFromPhysics() {
        if (body == null) return;
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
        if (massQty < 0) throw new IllegalArgumentException("Mass must be greater than zero.");
        this.massQty = massQty;
        updateMass();
    }

    private void updateMass() {
        if(mass==null || body==null) return;
        mass.setMass(massQty);
        if(mass.check()) {
            body.setMass(mass);
            body.setAngularVel(angularVel.x, angularVel.y, angularVel.z);
            body.setLinearVel(linearVel.x, linearVel.y, linearVel.z);
        }
    }

    public DBody getODEBody() {
        return body;
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("mass", getMassQty());
        if(body!=null) {
            json.put("avel", ODE4JHelper.vector3ToJSON(body.getAngularVel()));
            json.put("lvel", ODE4JHelper.vector3ToJSON(body.getLinearVel()));
        }
        return json;

    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("mass")) setMassQty(from.getDouble("mass"));
        if(from.has("avel")) setAngularVel(ODE4JHelper.jsonToVector3(from.getJSONObject("avel")));
        if(from.has("lvel")) setLinearVel(ODE4JHelper.jsonToVector3(from.getJSONObject("lvel")));
        updatePoseFromPhysics();
    }

    public void setAngularVel(Vector3d angularVel) {
        this.angularVel.set(angularVel);
        if(body!=null) body.setAngularVel(angularVel.x, angularVel.y, angularVel.z);
    }

    public void setLinearVel(Vector3d linearVel) {
        this.linearVel.set(linearVel);
        if(body!=null) body.setLinearVel(linearVel.x, linearVel.y, linearVel.z);
    }
}
