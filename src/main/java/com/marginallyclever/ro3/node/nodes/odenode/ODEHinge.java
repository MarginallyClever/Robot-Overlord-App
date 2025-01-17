package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
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
import java.util.Objects;

/**
 * <p>Wrapper for a hinge joint in ODE4J.  If one side of the hinge is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this {@link Pose} will adjust the position and orientation
 * as well as its relation to the attached parts.  If the simulation is NOT paused then the hinge
 * will behave as normal.</p>
 * <p>The hinge pivots on its local Z axis.</p>
 */
public class ODEHinge extends ODEJoint {
    private static final Logger logger = LoggerFactory.getLogger(ODEHinge.class);
    private DHingeJoint hinge;
    double top = Double.POSITIVE_INFINITY;
    double bottom = Double.NEGATIVE_INFINITY;

    public ODEHinge() {
        this(ODEHinge.class.getSimpleName());
    }

    public ODEHinge(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEHingePanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        createHinge();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroyHinge();
    }

    private void createHinge() {
        hinge = OdeHelper.createHingeJoint(Registry.getPhysics().getODEWorld(), null);
        connectInternal();
    }

    private void destroyHinge() {
        if(hinge!=null) {
            try {
                hinge.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            hinge = null;
        }
    }

    public DHingeJoint getHinge() {
        return hinge;
    }

    /**
     * Tell the physics engine who is connected to this hinge.
     */
    @Override
    protected void connect(DBody a, DBody b) {
        if(hinge==null) return;
        //logger.debug("{} connect {} {}",getAbsolutePath(),a,b);
        hinge.attach(a, b);
        setAngleMax(top);
        setAngleMin(bottom);
        updateHingePose();
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
            updatePhysicsFromPose();
        }
    }

    private void updatePhysicsFromPose() {
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
        if(!Registry.getPhysics().isPaused()) {
            updatePoseFromPhysics();
        }
    }

    private void updatePoseFromPhysics() {
        if(hinge==null) return;

        var anchor = new DVector3();
        var axis = new DVector3();
        hinge.getAnchor(anchor);
        hinge.getAxis(axis);
        // use axis and anchor to set the world matrix.
        Matrix3d m3 = MatrixHelper.lookAt(
                new Vector3d(),  // from
                new Vector3d(axis.get0(),axis.get1(),axis.get2())  // to
        );
        setWorld(new Matrix4d(m3,new Vector3d(anchor.get0(),anchor.get1(),anchor.get2()),1));
    }


    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        double v = getAngleMax();        if(!Double.isInfinite(v)) json.put("hiStop1",v);
               v = getAngleMin();        if(!Double.isInfinite(v)) json.put("loStop1",v);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("hiStop1")) setAngleMax(from.getDouble("hiStop1"));
        if(from.has("loStop1")) setAngleMin(from.getDouble("loStop1"));
        connectInternal();
    }

    public void addTorque(double value) {
        if(hinge==null) return;
        if(!Registry.getPhysics().isPaused()) {
            hinge.addTorque(value);
        }
    }

    /**
     * @return angle in degrees
     */
    public double getAngleMax() {
        return top;
    }

    /**
     * @return angle in degrees
     */
    public double getAngleMin() {
        return bottom;
    }

    /**
     * @param angle in degrees
     */
    public void setAngleMax(double angle) {
        top = angle;
        if(hinge==null) return;
        hinge.setParamHiStop(Math.toRadians(angle));
    }

    /**
     * @param angle in degrees
     */
    public void setAngleMin(double angle) {
        bottom = angle;
        if(hinge==null) return;
        hinge.setParamLoStop(Math.toRadians(angle));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/odenode/icons8-angle-16.png")));
    }

    /**
     * @return angle in degrees
     */
    public double getAngle() {
        if(hinge==null) return 0;
        return Math.toDegrees(hinge.getAngle());
    }

    /**
     * @return angle velocity in degrees/sec
     */
    public double getAngleVelocity() {
        if(hinge==null) return 0;
        return Math.toDegrees(hinge.getAngleRate());
    }
}
