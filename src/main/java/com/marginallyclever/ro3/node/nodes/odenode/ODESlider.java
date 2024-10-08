package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DHingeJoint;
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
public class ODESlider extends ODENode {
    private static final Logger logger = LoggerFactory.getLogger(ODESlider.class);
    private DSliderJoint sliderJoint;
    private final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    private final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);
    double top = Double.POSITIVE_INFINITY;
    double bottom = Double.NEGATIVE_INFINITY;

    public ODESlider() {
        this("ODE Slider Joint");
    }

    public ODESlider(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODESliderPanel(this));
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
        sliderJoint = OdeHelper.createSliderJoint(Registry.getPhysics().getODEWorld(), null);
        connect();
        setDistanceMax(top);
        setDistanceMin(bottom);
    }

    private void destroyHinge() {
        if(sliderJoint !=null) {
            try {
                sliderJoint.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            sliderJoint = null;
        }
    }

    public NodePath<ODEBody> getPartA() {
        return partA;
    }

    public NodePath<ODEBody> getPartB() {
        return partB;
    }

    public DSliderJoint getSliderJoint() {
        return sliderJoint;
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
        if(sliderJoint ==null) return;

        var as = partA.getSubject();
        var bs = partB.getSubject();
        DBody a = as == null ? null : as.getODEBody();
        DBody b = bs == null ? null : bs.getODEBody();
        if(a==null) {
            a=b;
            b=null;
        }
        logger.debug(this.getName()+" connect "+(as==null?"null":as.getName())+" to "+(bs==null?"null":bs.getName()));
        sliderJoint.attach(a, b);
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
        if(sliderJoint==null) return;

        var mat = getWorld();
        var zAxis = MatrixHelper.getZAxis(mat);
        if(zAxis.length()<0.001) {
            logger.error("Slider zAxis zero length?");
        }
        // When the zAxis is set, the current position of the attached bodies is considered the new zero position.
        // there is no sliderJoint.setPosition().  This attempts to trick the system by fiddling with the position of
        // the bodies to make the distance between them zero as setAxis is being called.
        // first we measure the distance between this.Pose and the car along zAxis.
        double distance = 0;
        Pose p = partA.getSubject();
        Matrix4d pOldWorld = null;
        if(p==null) p = partB.getSubject();
        if(p!=null) {
            logger.debug("swizzling position to set zero distance.");
            // p position
            var pWorld = p.getWorld();
            // a copy of p position
            pOldWorld = new Matrix4d(pWorld);
            // the distance between p and this.pose along zAxis
            var pPos = MatrixHelper.getPosition(pWorld);
            var worldPos = MatrixHelper.getPosition(mat);
            pPos.sub(worldPos);
            distance = pPos.dot(zAxis);
            // set the p.position to this.pose so that the distance is zero.
            pWorld.setTranslation(worldPos);
            p.setWorld(pWorld);
        }
        // now we adjust the axis
        sliderJoint.setAxis(zAxis.x, zAxis.y, zAxis.z);

        // make an update(0) to make the physics engine aware of the new position of the bodies.
        var physics = Registry.getPhysics();
        var paused = physics.isPaused();
        physics.setPaused(false);
        physics.update(0);
        physics.setPaused(paused);

        if(p!=null) {
            // move p to its old position like nothing happened.
            p.setWorld(pOldWorld);
            var compare = sliderJoint.getPosition();
            // the distance should be very close to compare.
            logger.debug("swizzle complete.  distance="+distance+" compare="+compare);
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!Registry.getPhysics().isPaused()) {
            // if the physics simulation is running then the hinge will behave as normal.
            DVector3 anchor = new DVector3();
            DVector3 axis = new DVector3();
            sliderJoint.getAxis(axis);
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
        double v = getDistanceMax();
        if(!Double.isInfinite(v)) {
            json.put("hiStop1",v);
        }
        v = getDistanceMin();
        if(!Double.isInfinite(v)) {
            json.put("loStop1",v);
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("partA")) partA.setUniqueID(from.getString("partA"));
        if(from.has("partB")) partB.setUniqueID(from.getString("partB"));
        if(from.has("hiStop1")) {
            setDistanceMax(from.getDouble("hiStop1"));
        }
        if(from.has("loStop1")) {
            setDistanceMin(from.getDouble("loStop1"));
        }
        updatePhysicsFromWorld();
        connect();
        updateHingePose();
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
        if(sliderJoint ==null) return;
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
