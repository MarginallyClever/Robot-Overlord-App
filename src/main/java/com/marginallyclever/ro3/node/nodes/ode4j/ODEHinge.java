package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DHingeJoint;
import org.ode4j.ode.OdeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * <p>Wrapper for a hinge joint in ODE4J.  If one side of the hinge is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this {@link Pose} will adjust the position and orientation
 * of the hinge, as well as it's relation to the attached parts.  If the simulation is NOT paused then the hinge
 * will behave as normal.</p>
 */
public class ODEHinge extends ODENode {
    private static final Logger logger = LoggerFactory.getLogger(ODEHinge.class);
    private DHingeJoint hinge;
    private final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    private final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);

    public ODEHinge() {
        this("ODE Hinge");
    }

    public ODEHinge(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEHingePanel(this));
        super.getComponents(list);
    }

    /**
     * Called once at the start of the first {@link #update(double)}
     */
    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        createHinge();
    }

    @Override
    protected void onReady() {
        super.onReady();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroyHinge();
    }

    private void createHinge() {
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        hinge = OdeHelper.createHingeJoint(physics.getODEWorld(), null);
        connect();
    }

    private void destroyHinge() {
        if(hinge!=null) {
            try {
                hinge.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            hinge = null;
        }
    }

    public NodePath<ODEBody> getPartA() {
        return partA;
    }

    public NodePath<ODEBody> getPartB() {
        return partB;
    }

    public DHingeJoint getHinge() {
        return hinge;
    }

    public void setPartA(ODEBody subject) {
        partA.setUniqueIDByNode(subject);
        connect();
    }

    public void setPartB(ODEBody subject) {
        partB.setUniqueIDByNode(subject);
        connect();
    }

    /**
     * Tell the physics engine who is connected to this hinge.
     */
    private void connect() {
        if(hinge==null) return;

        var as = partA.getSubject();
        var bs = partB.getSubject();
        DBody a = as == null ? null : as.getODEBody();
        DBody b = bs == null ? null : bs.getODEBody();
        logger.debug(this.getName()+" connect "+(as==null?"null":as.getName())+" to "+(bs==null?"null":bs.getName()));
        hinge.attach(a, b);
        updatePhysicsFromWorld();
    }

    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateHingePose();
    }

    private void updateHingePose() {
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        // only let the user move the hinge if the physics simulation is paused.
        if(physics.isPaused()) {
            // set the hinge reference point and axis.
            updatePhysicsFromWorld();
        }
    }

    private void updatePhysicsFromWorld() {
        if(hinge==null) return;

        var mat = getWorld();
        var pos = MatrixHelper.getPosition(mat);
        hinge.setAnchor(pos.x, pos.y, pos.z);
        var axis = MatrixHelper.getZAxis(mat);
        if(axis.length()<0.001) {
            logger.error("Hinge axis zero length?");
        }
        hinge.setAxis(axis.x, axis.y, axis.z);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        if(!physics.isPaused()) {
            // if the physics simulation is running then the hinge will behave as normal.
            DVector3 anchor = new DVector3();
            DVector3 axis = new DVector3();
            hinge.getAnchor(anchor);
            hinge.getAxis(axis);
            // use axis and anchor to set the world matrix.
            Matrix3d m3 = MatrixHelper.lookAt(
                    new Vector3d(0,0,0),
                    new Vector3d(axis.get0(),axis.get1(),axis.get2())
            );
            Matrix4d m4 = new Matrix4d();
            m4.set(m3);
            m4.setTranslation(new Vector3d(anchor.get0(),anchor.get1(),anchor.get2()));
            setWorld(m4);
        }
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("partA",partA.getUniqueID());
        json.put("partB",partB.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("partA")) partA.setUniqueID(from.getString("partA"));
        if(from.has("partB")) partB.setUniqueID(from.getString("partB"));
        updatePhysicsFromWorld();
        connect();
        updateHingePose();
    }

    public void addTorque(double value) {
        if(hinge==null) return;
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        if(!physics.isPaused()) {
            hinge.addTorque(value);
        }
    }
}
