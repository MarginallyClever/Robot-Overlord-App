package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.approximatejacobian.ApproximateJacobian;
import com.marginallyclever.convenience.approximatejacobian.ApproximateJacobianFiniteDifferences;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link Limb} is a linear chain of bones driven by joints powered by muscles.  {@link Limb} is designed to handle six joints or less.</p>
 * <ul>
 *     <li>Bones are represented by {@link Pose}s.</li>
 *     <li>Joints are represented by {@link HingeJoint}s.</li>
 *     <li>Muscles are represented by {@link Motor}s.</li>
 *     <li>The end of the chain - at the wrist - is a {@link Pose} called the <i>end effector</i>.</li>
 *     <li>The target is a {@link Pose} that the end effector is trying to reach at a given linear velocity.</li>
 * </ul>
 * <p>{@link Limb}s perform both <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a> and
 * <a href="https://en.wikipedia.org/wiki/Inverse_kinematics">Inverse kinematics</a>.  In earlier versions of the app
 * the concerns were separated, but this made it difficult to tell who was in charge and led to cyclic, jittery behavior.</p>
 */
public class Limb extends Pose {
    private static final Logger logger = LoggerFactory.getLogger(Limb.class);

    public static final int MAX_JOINTS = 6;
    public static final double DEFAULT_LINEAR_VELOCITY = 0.0;  // cm/s
    public static final double DEFAULT_GOAL_MARGIN_OF_ERROR = 0.1;  // not degrees or mm.  Just a number.

    private final List<NodePath<Motor>> motors = new ArrayList<>();
    private final NodePath<Pose> endEffector = new NodePath<>(this,Pose.class);
    private final EventListenerList listenerList = new EventListenerList();

    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);
    private double linearVelocity = DEFAULT_LINEAR_VELOCITY;
    private double distanceToTarget = 0;
    private double goalMarginOfError = DEFAULT_GOAL_MARGIN_OF_ERROR; // not degrees or mm.  Just a number.
    private final double[] cartesianDistance = new double[6];  // 3 linear, 3 angular
    private final double[] cartesianVelocity = new double[cartesianDistance.length];
    private boolean isAtGoal = false;

    public Limb() {
        this("Limb");
    }

    public Limb(String name) {
        super(name);

        for(int i=0;i<MAX_JOINTS;++i) {
            motors.add(new NodePath<>(this,Motor.class));
        }
    }

    /**
     * @return the end effector pose or null if not set.
     */
    public NodePath<Pose> getEndEffector() {
        return endEffector;
    }

    public void setEndEffector(Pose pose) {
        endEffector.setUniqueIDByNode(pose);
    }

    public List<NodePath<Motor>> getMotors() {
        return motors;
    }

    public int getNumJoints() {
        int count=0;
        for(NodePath<Motor> paths : motors) {
            if(paths.getSubject()!=null) count++;
        }
        return count;
    }

    /**
     * Get the motor at the given index.
     * @param index the index of the motor to get.
     * @return the motor at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public Motor getJoint(int index) {
        return motors.get(index).getSubject();
    }

    public double[] getAllJointAngles() {
        double[] result = new double[getNumJoints()];
        int i=0;
        for(NodePath<Motor> paths : motors) {
            Motor motor = paths.getSubject();
            if(motor!=null) {
                if(motor.hasHinge()) {
                    result[i++] = motor.getHinge().getAngle();
                } else {
                    result[i++] = 0;
                }
            }
        }
        return result;
    }

    /**
     * Get the motor at the given index.
     * @param index the index of the motor to get.
     * @param newValue the new motor to set.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public void setJoint(int index, Motor newValue) {
        motors.get(index).setUniqueIDByNode(newValue);
    }

    /**
     * Set all joint angles at once while respecting the joint range limits.
     * @param values the angles to set each joint to.
     */
    public void setAllJointAngles(double[] values) {
        if(values.length != getNumJoints()) {
            throw new IllegalArgumentException("setAllJointValues: one value for every motor");
        }
        int i=0;
        for(NodePath<Motor> paths : motors) {
            Motor motor = paths.getSubject();
            if(motor!=null) {
                HingeJoint axle = motor.getHinge();
                if(axle!=null) {
                    axle.setAngle(values[i]);
                    axle.updateAxleLocationInSpace();
                }
            }
            i++;
        }
    }

    /**
     * Set all joint angles at once without respecting the joint range limits.
     * @param values the angles to set each joint to.
     */
    public void setAllJointAnglesUnsafe(double[] values) {
        if(values.length != getNumJoints()) {
            throw new IllegalArgumentException("setAllJointValues: one value for every motor");
        }
        int i=0;
        for(NodePath<Motor> paths : motors) {
            Motor motor = paths.getSubject();
            if(motor!=null) {
                HingeJoint axle = motor.getHinge();
                if(axle!=null) {
                    axle.setAngleUnsafe(values[i]);
                    axle.updateAxleLocationInSpace();
                }
            }
            i++;
        }
    }

    public void setAllJointVelocities(double[] values) {
        if(values.length != getNumJoints()) {
            throw new IllegalArgumentException("One value for every motor");
        }
        int i=0;
        for(NodePath<Motor> paths : motors) {
            Motor motor = paths.getSubject();
            if(motor!=null) {
                HingeJoint axle = motor.getHinge();
                if(axle!=null) {
                    axle.setVelocity(values[i++]);
                }
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        JSONArray jointArray = new JSONArray();
        json.put("version",3);

        for(var motor : motors) {
            jointArray.put(motor == null ? JSONObject.NULL : motor.getUniqueID());
        }
        json.put("motors",jointArray);
        if(endEffector.getSubject()!=null) json.put("endEffector",endEffector.getUniqueID());
        if(getTarget()!=null) json.put("target",target.getUniqueID());
        json.put("linearVelocity",linearVelocity);
        json.put("goalMarginOfError",goalMarginOfError);
        json.put("isAtGoal",isAtGoal);

        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;

        if(from.has("motors")) {
            JSONArray motorArray = from.getJSONArray("motors");
            for(int i=0;i<motorArray.length();++i) {
                if(motorArray.isNull(i)) {
                    motors.get(i).setUniqueID(null);
                } else {
                    String s = motorArray.getString(i);
                    if(version==1) {
                        motors.get(i).setUniqueIDByNode(this.findNodeByPath(s,Motor.class));
                    } else if(version==0||version==2) {
                        motors.get(i).setUniqueID(s);
                    }
                }
            }
        }

        if(from.has("endEffector")) {
            String s = from.getString("endEffector");
            if(version==1) {
                endEffector.setUniqueIDByNode(this.findNodeByPath(s,Pose.class));
            } else if(version==0||version>=2) {
                endEffector.setUniqueID(s);
            }
        }
        if(from.has("target")) {
            String s = from.getString("target");
            if(version==1||version==2) {
                target.setUniqueIDByNode(this.findNodeByPath(s,Pose.class));
            } else if(version==0 || version==3) {
                target.setUniqueID(s);
            }
        }

        linearVelocity = from.optDouble("linearVelocity",DEFAULT_LINEAR_VELOCITY);
        goalMarginOfError = from.optDouble("goalMarginOfError",DEFAULT_GOAL_MARGIN_OF_ERROR);
        isAtGoal = from.optBoolean("isAtGoal",false);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LimbPanel(this));
        super.getComponents(list);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-mechanical-arm-16.png")));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class,listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class,listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this,propertyName,oldValue,newValue);
        for(PropertyChangeListener listener : listenerList.getListeners(PropertyChangeListener.class)) {
            listener.propertyChange(event);
        }
    }

    /**
     * Move a motor to a specific angle and notify listeners that the pose has changed.
     * @param motor the motor to move
     * @param angle the angle to move the motor to
     */
    public void setMotorAngle(Motor motor, double angle) {
        if(!motor.hasHinge()) return;
        var hinge = motor.getHinge();
        hinge.setAngle(angle);
        hinge.updateAxleLocationInSpace();
        moveTargetToEndEffector();
        firePropertyChange("poseChanged",null,this);
    }

    public NodePath<Pose> getTarget() {
        return target;
    }

    /**
     * Set the target to move towards.
     * target must be in the same node tree as this instance.
     * @param target the target to move towards
     */
    public void setTarget(Pose target) {
        this.target.setUniqueIDByNode(target);
    }

    public void update(double dt) {
        super.update(dt);
        if(dt==0) return;

        moveTowardsTarget();

        if(areWeThereYet()) {
            if(!isAtGoal) {
                isAtGoal = true;
                fireArrivedAtGoal();
            }
        } else {
            isAtGoal = false;
        }
    }

    // are we there yet?
    private boolean areWeThereYet() {
        distanceToTarget = sumCartesianVelocityComponents(cartesianDistance);
        return distanceToTarget < goalMarginOfError;
    }

    /**
     * @return true if the solver has a limb, an end effector, and a target.  Does not guarantee that a solution exists.
     */
    public boolean readyToSolve() {
        return endEffector.getSubject() != null
                && getTarget().getSubject() != null;
    }

    private void moveTowardsTarget() {
        if(!readyToSolve()) {
            return;
        }

        if(Math.abs(linearVelocity) < 0.0001) {
            // no velocity.  Make sure the arm doesn't drift.
            this.setAllJointVelocities(new double[this.getNumJoints()]);
            return;
        }
        // find direction to move
        MatrixHelper.getCartesianBetweenTwoMatrices(
                Objects.requireNonNull(endEffector.getSubject()).getWorld(),
                getTarget().getSubject().getWorld(),
                cartesianDistance);
        // limit the velocity
        System.arraycopy(cartesianDistance,0,cartesianVelocity,0,cartesianDistance.length);
        scaleVectorToMagnitude(cartesianVelocity,linearVelocity);
        // set motor velocities.
        setMotorVelocitiesFromCartesianVelocity(cartesianVelocity);
    }

    /**
     * <p>Attempts to move the robot arm such that the end effector travels in the cartesian direction.  This is
     * achieved by setting the velocity of the motors.</p>
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void setMotorVelocitiesFromCartesianVelocity(double[] cartesianVelocity) {
        if(getNumJoints()==0) return;

        ApproximateJacobian aj = getJacobian();
        double[] jointVelocity = null;
        try {
            jointVelocity = aj.getJointFromCartesian(cartesianVelocity);  // uses inverse jacobian
        } catch (Exception e) {
            logger.warn(e.getMessage());
            // set velocity to zero
            jointVelocity = new double[this.getNumJoints()];
        }
        if(impossibleVelocity(jointVelocity)) return;  // TODO throw exception instead?
        this.setAllJointVelocities(jointVelocity);
    }

    private ApproximateJacobian getJacobian() {
        // option 1, use finite differences
        return new ApproximateJacobianFiniteDifferences(this);
        // option 2, use screw theory
        //ApproximateJacobian aj = new ApproximateJacobianScrewTheory(this);
    }

    /**
     * @param jointVelocity the joint velocity to check
     * @return true if the given joint velocity is impossible.
     */
    private boolean impossibleVelocity(double[] jointVelocity) {
        double maxV = 100; // RPM*60 TODO: get from robot per joint
        for(double v : jointVelocity) {
            if(Double.isNaN(v) || Math.abs(v) > maxV) return true;
        }
        return false;
    }


    /**
     * <p>Make sure the given vector's length does not exceed linearVelocity.  This means as the limb approaches the
     * target the velocity will slow down.</p>
     * <p>Store the results in the original array.</p>
     * @param vector the vector to cap
     * @param maxLen the max length of the vector.
     */
    public static void scaleVectorToMagnitude(double[] vector, double maxLen) {
        // get the length of the vector
        double len = 0;
        for (double v : vector) {
            len += v * v;
        }
        len = Math.sqrt(len);

        var linearMagnitude = Math.abs(maxLen);
        if(linearMagnitude>len) maxLen = Math.signum(maxLen) * len;

        // scale the vector
        double scale = (len == 0) ? 0 : maxLen / len;  // catch len==0
        for(int i=0;i<vector.length;i++) {
            vector[i] *= scale;
        }
    }

    private double sumCartesianVelocityComponents(double [] cartesianVelocity) {
        double sum = 0;
        for (double v : cartesianVelocity) {
            sum += Math.abs(v);
        }
        return sum;
    }

    /**
     * Move the target to the end effector's current pose, stopping all motion of the arm (because it is now at the target).
     */
    public void moveTargetToEndEffector() {
        var ee = endEffector.getSubject();
        if(getTarget().getSubject()!=null && ee!=null) {
            getTarget().getSubject().setWorld(ee.getWorld());
        }
    }

    public double getLinearVelocity() {
        return linearVelocity;
    }

    /**
     * Set the linear velocity of the end effector in cm/s.
     * @param linearVelocity must be >= 0
     */
    public void setLinearVelocity(double linearVelocity) {
        this.linearVelocity = linearVelocity;
    }

    /**
     * @return the distance to the target that is a combination of linear and angular distances.
     */
    public double getDistanceToTarget() {
        return distanceToTarget;
    }

    /**
     * @return the distance to the target that is a combination of linear and angular distances.
     */
    public double getGoalMarginOfError() {
        return goalMarginOfError;
    }

    /**
     * @param goalMarginOfError the distance to the target that is a combination of linear and angular distances.
     */
    public void setGoalMarginOfError(double goalMarginOfError) {
        if(goalMarginOfError<0) throw new IllegalArgumentException("goalMarginOfError must be >= 0");
        this.goalMarginOfError = goalMarginOfError;
    }
    public void addActionListener(ActionListener listener) {
        listeners.add(ActionListener.class,listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(ActionListener.class,listener);
    }

    /**
     * Fire the "arrivedAtGoal" event to any {@link ActionListener} subscribed to this node.
     */
    private void fireArrivedAtGoal() {
        logger.debug("Arrived at goal.");
        ActionEvent e = new ActionEvent(this,0,"arrivedAtGoal");
        // Dispatch the event to the listeners
        for (ActionListener listener : listeners.getListeners(ActionListener.class)) {
            listener.actionPerformed(e);
        }
    }

    public void setIsAtGoal(boolean isAtGoal) {
        this.isAtGoal = isAtGoal;
    }

    public boolean getIsAtGoal() {
        return isAtGoal;
    }

}
