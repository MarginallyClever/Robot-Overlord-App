package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DAMotorJoint;
import org.ode4j.ode.DBody;
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
 * <p>Wrapper for an angular motor joint in ODE4J.  If one side of the motor is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this {@link Pose} will adjust the position and orientation
 * as well as its relation to the attached parts.  If the simulation is NOT paused then the motor
 * will behave as normal.</p>
 * <p>The motor applies force on its local Z axis.</p>
 */
public class ODEAngularMotor extends ODEJoint {
    private static final Logger logger = LoggerFactory.getLogger(ODEAngularMotor.class);
    private DAMotorJoint motor;
    private double top = Double.POSITIVE_INFINITY;
    private double bottom = Double.NEGATIVE_INFINITY;
    private double forceMax = Double.POSITIVE_INFINITY;

    public ODEAngularMotor() {
        this(ODEAngularMotor.class.getSimpleName());
    }

    public ODEAngularMotor(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEAngularMotorPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        createMotor();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroyMotor();
    }

    private void createMotor() {
        motor = OdeHelper.createAMotorJoint(Registry.getPhysics().getODEWorld(), null);
        connectInternal();
    }

    private void destroyMotor() {
        if(motor!=null) {
            try {
                motor.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
            motor = null;
        }
    }

    public DAMotorJoint getMotor() {
        return motor;
    }

    /**
     * Tell the physics engine who is connected to this motor.
     */
    @Override
    protected void connect(DBody a, DBody b) {
        if(motor==null) return;
        logger.debug("{} connect {} {}",getAbsolutePath(),a,b);
        motor.attach(a, b);
        motor.setNumAxes(3);
        setAngleMax(top);
        setAngleMin(bottom);
        setForceMax(forceMax);
        updateMotorPose();
    }

    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateMotorPose();
    }

    private void updateMotorPose() {
        // only let the user move the motor if the physics simulation is paused.
        if(Registry.getPhysics().isPaused()) {
            // set the motor reference point and axis.
            updatePhysicsFromWorld();
        }
    }

    private void updatePhysicsFromWorld() {
        if(motor==null) return;

        var mat = getWorld();
        int rel = 0;  // world space
        var xAxis = MatrixHelper.getXAxis(mat);        motor.setAxis(0,rel,xAxis.x, xAxis.y, xAxis.z);
        var yAxis = MatrixHelper.getYAxis(mat);        motor.setAxis(1,rel,yAxis.x, yAxis.y, yAxis.z);
        var zAxis = MatrixHelper.getZAxis(mat);        motor.setAxis(2,rel,zAxis.x, zAxis.y, zAxis.z);
        logger.debug("{} setAxis {}",getAbsolutePath(),zAxis);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (!Registry.getPhysics().isPaused()) {
            updateWorldFromPhysics();
        }
    }

    private void updateWorldFromPhysics() {
        if(motor==null) return;

        DBody body = motor.getBody(0);
        if(body==null) body = motor.getBody(1);
        if(body==null) return;

        var anchor = body.getPosition();
        var axis = new DVector3();
        motor.getAxis(2,axis);
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
        double v = getAngleMax();   if(!Double.isInfinite(v)) json.put("hiStop1",v);
               v = getAngleMin();   if(!Double.isInfinite(v)) json.put("loStop1",v);
               v = getForceMax();   if(!Double.isInfinite(v)) json.put("fMax",v);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("hiStop1")) setAngleMax(from.getDouble("hiStop1"));
        if(from.has("loStop1")) setAngleMin(from.getDouble("loStop1"));
        if(from.has("fMax")) setForceMax(from.getDouble("fMax"));
        connectInternal();
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

    public void setForceMax(double force) {
        forceMax = force;
        if(motor==null) return;
        motor.setParamFMax(force);
        motor.setParamFMax2(force);
        motor.setParamFMax3(force);
    }

    public double getForceMax() {
        return forceMax;
    }

    /**
     * @param angle in degrees
     */
    public void setAngleMax(double angle) {
        top = angle;
        if(motor==null) return;
        //motor.setParamHiStop(Math.toRadians(angle));
        //motor.setParamHiStop2(Math.toRadians(angle));
        motor.setParamHiStop3(Math.toRadians(angle));
    }

    /**
     * @param angle in degrees
     */
    public void setAngleMin(double angle) {
        bottom = angle;
        if(motor==null) return;
        //motor.setParamLoStop(Math.toRadians(angle));
        //motor.setParamLoStop2(Math.toRadians(angle));
        motor.setParamLoStop3(Math.toRadians(angle));
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-motor-16.png")));
    }

    public void addTorque(double qty) {
        if(motor==null) return;
        System.out.println("addTorque "+qty);
        //motor.addTorques(0,0,qty);
        motor.setParamVel3(qty);//+motor.getParamVel3());
    }
}
