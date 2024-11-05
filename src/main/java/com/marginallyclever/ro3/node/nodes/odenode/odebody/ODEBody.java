package com.marginallyclever.ro3.node.nodes.odenode.odebody;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.physics.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.odenode.ODENode;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
import org.ode4j.math.DMatrix3;
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
    private boolean isTouchingSomething = false;

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
        if(findFirstChild(MeshInstance.class)==null) addChild(new MeshInstance());
        if(findFirstChild(Material.class)==null) addChild(new Material());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroyBody();
        destroyGeom();
    }

    protected void destroyBody() {
        if(body != null) {
            body.destroy();
            body = null;
        }
    }

    protected void destroyGeom() {
        if(geom != null) {
            geom.destroy();
            geom = null;
        }
    }

    /**
     * Called once at the start of the first {@link #update(double)}
     */
    protected void onFirstUpdate() {
        body = OdeHelper.createBody(Registry.getPhysics().getODEWorld());
        mass = OdeHelper.createMass();
        mass.setZero();
        updateMass();
        updatePhysicsFromPose();
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!Registry.getPhysics().isPaused()) {
            updatePoseFromPhysics();
        }
    }

    /**
     * Adjust the position of the {@link com.marginallyclever.ro3.node.nodes.pose.Pose} to match the {@link ODEBody}.
     * This will cause the visual representation to match the physics representation.
     */
    protected void updatePoseFromPhysics() {
        if(body == null) return;
        DVector3C translation = body.getPosition();
        DMatrix3C rotation = body.getRotation();
        super.setWorld(ODE4JHelper.convertODEtoMatrix(translation, rotation));
    }

    /**
     * Update the {@link ODEBody} to match the {@link com.marginallyclever.ro3.node.nodes.pose.Pose}.  This will
     * cause the physics representation to match the visual representation.
     */
    protected void updatePhysicsFromPose() {
        if (body == null) return;
        var world = getWorld();
        body.setPosition(world.m03, world.m13, world.m23);
        body.setRotation(ODE4JHelper.convertRotationToODE(world));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/odenode/icons8-mechanics-16.png")));
    }

    public double getMassQty() {
        return massQty;
    }

    /**
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
        if(massQty>0 && mass.check()) {
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
        json.put("avel", ODE4JHelper.vector3ToJSON(angularVel));
        json.put("lvel", ODE4JHelper.vector3ToJSON(linearVel));
        return json;

    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("mass")) setMassQty(from.getDouble("mass"));
        if(from.has("avel")) setAngularVel(ODE4JHelper.jsonToVector3(from.getJSONObject("avel")));
        if(from.has("lvel")) setLinearVel(ODE4JHelper.jsonToVector3(from.getJSONObject("lvel")));
        updatePhysicsFromPose();
    }

    public void setAngularVel(Vector3d angularVel) {
        this.angularVel.set(angularVel);
        if(body!=null) body.setAngularVel(angularVel.x, angularVel.y, angularVel.z);
    }

    public void setLinearVel(Vector3d linearVel) {
        this.linearVel.set(linearVel);
        if(body!=null) body.setLinearVel(linearVel.x, linearVel.y, linearVel.z);
    }

    public DGeom getGeom() {
        return geom;
    }

    public boolean isTouchingSomething() {
        return isTouchingSomething;
    }

    public void setTouchingSomething(boolean isTouchingSomething) {
        this.isTouchingSomething = isTouchingSomething;
    }


    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateBodyPose();
    }

    private void updateBodyPose() {
        // only allow while paused.
        if (Registry.getPhysics().isPaused()) {
            updatePhysicsFromWorld();
        }
    }

    private void updatePhysicsFromWorld() {
        if(body==null) return;

        var w = getWorld();

        // set position part
        var p = MatrixHelper.getPosition(w);
        body.setPosition(p.x,p.y,p.z);

        // set rotation part
        DMatrix3 rotation = new DMatrix3();
        for(int i=0;i<3;++i) {
            for(int j=0;j<3;++j) {
                rotation.set(i,j,w.getElement(i,j));
            }
        }
        body.setRotation(rotation);
    }
}
