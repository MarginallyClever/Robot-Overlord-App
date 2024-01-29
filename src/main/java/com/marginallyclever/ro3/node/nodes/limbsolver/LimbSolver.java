package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * {@link LimbSolver} calculates <a href="https://en.wikipedia.org/wiki/Inverse_kinematics">Inverse Kinematics</a> for
 * a {@link Limb}.  Given a target and a linear velocity, {@link LimbSolver} will calculate and apply the
 * joint velocities required to move the end effector towards the target in a straight line.  When the end effector
 * reaches the target (with a margin of error), {@link LimbSolver} will fire an ActionEvent "arrivedAtGoal".
 */
public class LimbSolver extends Node {
    private static final Logger logger = LoggerFactory.getLogger(LimbSolver.class);
    private final NodePath<Limb> limb = new NodePath<>(this,Limb.class);
    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);
    private double linearVelocity = 0;
    private double distanceToTarget = 0;

    private double goalMarginOfError = 0.1; // not degrees or mm.  Just a number.
    private final double[] cartesianDistance = new double[6];  // 3 linear, 3 angular
    private final double[] cartesianVelocity = new double[cartesianDistance.length];
    private boolean isAtGoal = false;

    public LimbSolver() {
        this("LimbSolver");
    }

    public LimbSolver(String name) {
        super(name);
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

    public NodePath<Limb> getLimb() {
        return limb;
    }

    /**
     * Set the limb to be controlled by this instance.
     * limb must be in the same node tree as this instance.
     * @param limb the limb to control
     */
    public void setLimb(Limb limb) {
        this.limb.setUniqueIDByNode(limb);
    }

    private Pose getEndEffector() {
        var limb = getLimb().getSubject();
        if(limb == null) return null;
        return limb.getEndEffector().getSubject();
    }

    /**
     * @return true if the solver has a limb, an end effector, and a target.  Does not guarantee that a solution exists.
     */
    public boolean readyToSolve() {
        return getLimb().getSubject()!=null && getEndEffector()!=null && getTarget().getSubject()!=null;
    }

    private void moveTowardsTarget() {
        if(!readyToSolve()) {
            return;
        }

        var limb = getLimb().getSubject();

        if(Math.abs(linearVelocity) < 0.0001) {
            // no velocity.  Make sure the arm doesn't drift.
            limb.setAllJointVelocities(new double[limb.getNumJoints()]);
            return;
        }
        // find direction to move
        MatrixHelper.getCartesianBetweenTwoMatrices(
                getEndEffector().getWorld(),
                getTarget().getSubject().getWorld(),
                cartesianDistance);
        // theshold the velocity
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
        var myLimb = getLimb().getSubject();
        if(myLimb==null || myLimb.getNumJoints()==0) return;

        ApproximateJacobian aj = getJacobian();
        double[] jointVelocity = null;
        try {
            jointVelocity = aj.getJointFromCartesian(cartesianVelocity);  // uses inverse jacobian
        } catch (Exception e) {
            logger.warn(e.getMessage());
            // set velocity to zero
            jointVelocity = new double[myLimb.getNumJoints()];
        }
        if(impossibleVelocity(jointVelocity)) return;  // TODO: throw exception instead?
        myLimb.setAllJointVelocities(jointVelocity);
    }

    private ApproximateJacobian getJacobian() {
        // option 1, use finite differences
        return new ApproximateJacobianFiniteDifferences(limb.getSubject());
        // option 2, use screw theory
        //ApproximateJacobian aj = new ApproximateJacobianScrewTheory(getLimb());
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

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",3);
        if(getLimb()!=null) json.put("limb",limb.getUniqueID());
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

        if(from.has("limb")) {
            String s = from.getString("limb");
            if(version==1||version==2) {
                limb.setUniqueIDByNode(this.findNodeByPath(s,Limb.class));
            } else if(version==0 || version==3) {
                this.limb.setUniqueID(s);
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
        if(from.has("linearVelocity")) {
            linearVelocity = from.getDouble("linearVelocity");
        }
        if(from.has("goalMarginOfError")) {
            goalMarginOfError = from.getDouble("goalMarginOfError");
        }
        if(from.has("isAtGoal")) {
            isAtGoal = from.getBoolean("isAtGoal");
        }
    }

    /**
     * <p>Make sure the given vector's length does not exceed maxLen.  It can be less than the given magnitude.
     * If the maxLen is greater than the vector length, the vector is unchanged.  This means as the limb approaches
     * the target the velocity will slow down.</p>
     * <p>Store the results in the original array.</p>
     * @param vector the vector to cap
     * @param linearVelocity the max length of the vector.
     */
    public static void scaleVectorToMagnitude(double[] vector, double linearVelocity) {
        // get the length of the vector
        double len = 0;
        for (double v : vector) {
            len += v * v;
        }

        len = Math.sqrt(len);
        var linearMagnitude = Math.abs(linearVelocity);
        if(linearMagnitude>len) linearVelocity = Math.signum(linearVelocity) * len;

        // scale the vector
        double scale = len==0? 0 : linearVelocity / len;  // catch len==0
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

    public void moveTargetToEndEffector() {
        if(getTarget().getSubject()!=null && getEndEffector()!=null) {
            getTarget().getSubject().setWorld(getEndEffector().getWorld());
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LimbSolverPanel(this));
        super.getComponents(list);
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

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-rubik's-cube-16.png")));
    }
}
