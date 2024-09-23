package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBallJoint;
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
import java.util.Objects;

/**
 * <p>Wrapper for a ball joint in ODE4J.  If one side of the hinge is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this {@link Pose} will adjust the position and orientation
 * of the hinge, as well as it's relation to the attached parts.  If the simulation is NOT paused then the hinge
 * will behave as normal.</p>
 */
public class ODEBallSocket extends ODENode {
    private static final Logger logger = LoggerFactory.getLogger(ODEBallSocket.class);
    private DBallJoint ballJoint;
    private final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    private final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);
    public ODEBallSocket() {
        this("ODE Ball Socket Joint");
    }

    public ODEBallSocket(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEBallSocketPanel(this));
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
        ballJoint = OdeHelper.createBallJoint(Registry.getPhysics().getODEWorld(), null);
        connect();
    }

    private void destroyHinge() {
        if(ballJoint !=null) {
            try {
                ballJoint.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            ballJoint = null;
        }
    }

    public NodePath<ODEBody> getPartA() {
        return partA;
    }

    public NodePath<ODEBody> getPartB() {
        return partB;
    }

    public DBallJoint getBallJoint() {
        return ballJoint;
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
        if(ballJoint ==null) return;

        var as = partA.getSubject();
        var bs = partB.getSubject();
        DBody a = as == null ? null : as.getODEBody();
        DBody b = bs == null ? null : bs.getODEBody();
        if(a==null) {
            a=b;
            b=null;
        }
        logger.debug(this.getName()+" connect "+(as==null?"null":as.getName())+" to "+(bs==null?"null":bs.getName()));
        ballJoint.attach(a, b);
        updatePhysicsFromWorld();
    }

    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateHingePose();
    }

    private void updateHingePose() {
        // only let the user move the hinge if the physics simulation is paused.
        if(Registry.getPhysics().isPaused()) {
            // set the hinge reference point and axis.
            updatePhysicsFromWorld();
        }
    }

    private void updatePhysicsFromWorld() {
        if(ballJoint ==null) return;

        var mat = getWorld();
        var pos = MatrixHelper.getPosition(mat);
        ballJoint.setAnchor(pos.x, pos.y, pos.z);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!Registry.getPhysics().isPaused()) {
            // if the physics simulation is running then the hinge will behave as normal.
            DVector3 anchor = new DVector3();
            ballJoint.getAnchor(anchor);
            // use anchor to set the world position.
            Matrix4d m4 = this.getWorld();
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
}
