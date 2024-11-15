package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DSliderJoint;
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
 * <p>Wrapper for a slider joint in ODE4J.  If one side of the hinge is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this {@link Pose} will adjust the position and orientation
 * of the hinge, as well as it's relation to the attached parts.  If the simulation is NOT paused then the hinge
 * will behave as normal.</p>
 * <p>If partA and partB are connected then sliderjoint will allow relative linear motion along the specified axis.</p>
 * <p>If only one of the two parts is connected then the slider joint will allow the connected part to move freely
 * along the specified axis while the other end remains fixed.</p>
 */
public class ODESlider extends ODEJoint {
    private static final Logger logger = LoggerFactory.getLogger(ODESlider.class);
    private DSliderJoint sliderJoint;
    private double top = Double.POSITIVE_INFINITY;
    private double bottom = Double.NEGATIVE_INFINITY;

    public ODESlider() {
        this(ODESlider.class.getSimpleName());
    }

    public ODESlider(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODESliderPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        createSlider();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroySlider();
    }

    private void createSlider() {
        sliderJoint = OdeHelper.createSliderJoint(Registry.getPhysics().getODEWorld(), null);
        connectInternal();
    }

    private void destroySlider() {
        if(sliderJoint !=null) {
            try {
                sliderJoint.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            sliderJoint = null;
        }
    }

    /**
     * Tell the physics engine who is connected to this hinge.
     */
    @Override
    protected void connect(DBody a, DBody b) {
        if(sliderJoint == null) return;
        logger.debug("{} connect {} {}",getAbsolutePath(),a,b);
        sliderJoint.attach(a, b);
        setDistanceMax(top);
        setDistanceMin(bottom);
        updateSliderPose();
    }

    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateSliderPose();
    }

    private void updateSliderPose() {
        // only let the user move the hinge if the physics simulation is paused.
        if(Registry.getPhysics().isPaused()) {
            // set the hinge reference point and axis.
            updatePhysicsFromPose();
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!Registry.getPhysics().isPaused()) {
            updatePoseFromPhysics();
        }
    }

    private void updatePhysicsFromPose() {
        if(sliderJoint==null) return;
        var zAxis = MatrixHelper.getZAxis(getWorld());
        sliderJoint.setAxis(zAxis.x, zAxis.y, zAxis.z);
        logger.debug("{} setAxis {}",getAbsolutePath(), StringHelper.printTuple3d(zAxis));
    }

    private void updatePoseFromPhysics() {
        if(sliderJoint==null) return;

        var axis = new DVector3();
        sliderJoint.getAxis(axis);
        Vector3d to = new Vector3d(axis.get0(), axis.get1(), axis.get2());
        Matrix3d m3 = MatrixHelper.lookAt(
                new Vector3d(),  // from
                to // to
        );
        setWorld(new Matrix4d(m3,MatrixHelper.getPosition(getWorld()),1));
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        double v = getDistanceMax();
        if(!Double.isInfinite(v)) json.put("hiStop1",v);
        v = getDistanceMin();
        if(!Double.isInfinite(v)) json.put("loStop1",v);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("hiStop1")) setDistanceMax(from.getDouble("hiStop1"));
        if(from.has("loStop1")) setDistanceMin(from.getDouble("loStop1"));
        updatePhysicsFromPose();
        connectInternal();
        updateSliderPose();
    }

    /**
     * @return distance.  can be +infinity.
     */
    public double getDistanceMax() {
        return top;
    }

    /**
     * @return distance.  can be -infinity.
     */
    public double getDistanceMin() {
        return bottom;
    }

    public double getDistance() {
        if(sliderJoint ==null) return 0;
        return sliderJoint.getPosition();
    }

    /**
     * @param distance can be +infinity.
     */
    public void setDistanceMax(double distance) {
        top = distance;
        if(sliderJoint == null) return;
        sliderJoint.setParamHiStop(distance);
    }

    /**
     * @param distance can be -infinity.
     */
    public void setDistanceMin(double distance) {
        bottom = distance;
        if(sliderJoint ==null) return;
        sliderJoint.setParamLoStop(distance);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/odenode/icons8-slider-16.png")));
    }
}
