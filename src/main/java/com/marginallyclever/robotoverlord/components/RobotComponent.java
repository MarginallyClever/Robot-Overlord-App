package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * RobotComponent is attached to the root of a robotic arm.
 * It requires that each link in the arm has a {@link PoseComponent} and a {@link DHComponent}.  The end effector
 * must have an {@link ArmEndEffectorComponent}.
 * @author Dan Royer
 * @since 2022-09-14
 */
@ComponentDependency(components = {PoseComponent.class})
public class RobotComponent extends Component implements Robot, ComponentWithReferences {
    private static final Logger logger = LoggerFactory.getLogger(RobotComponent.class);
    public static final String TARGET_NAME = "target";

    private int activeJoint;
    private final List<DHComponent> bones = new ArrayList<>();
    public final ReferenceParameter gcodePath = new ReferenceParameter("Path");

    public DoubleParameter desiredLinearVelocity = new DoubleParameter("Desired Linear Velocity (cm/s)",1);

    @Override
    public void onAttach() {
        findBones();
    }

    public void goHome() {
        double [] homeValues = new double[getNumBones()];
        for(int i=0;i<getNumBones();++i) {
            homeValues[i] = getBone(i).getJointHome();
        }
        setAllJointValues(homeValues);
        set(Robot.END_EFFECTOR_TARGET,get(Robot.END_EFFECTOR));
    }

    public int getNumBones() {
        return bones.size();
    }

    public DHComponent getBone(int index) {
        return bones.get(index);
    }

    public void findBones() {
        bones.clear();

        // recursively add all DHComponents to the list.
        Queue<Entity> queue = new LinkedList<>();
        queue.add(getEntity());
        while(!queue.isEmpty()) {
            Entity entity = queue.poll();
            queue.addAll(entity.getChildren());

            DHComponent c = entity.getComponent(DHComponent.class);
            if(c!=null) bones.add(c);
        }
    }

    @Override
    public Object get(int property) {
        switch(property) {
            case NAME: return getName();
            case NUM_JOINTS: return getNumBones();
            case ACTIVE_JOINT: return activeJoint;
            case JOINT_NAME: return getBone(activeJoint).getEntity().getName();
            case JOINT_VALUE: return getActiveJointValue();
            case JOINT_RANGE_MAX: return getBone(activeJoint).getJointMax();
            case JOINT_RANGE_MIN: return getBone(activeJoint).getJointMin();
            case JOINT_HAS_RANGE_LIMITS: return true;
            case JOINT_PRISMATIC: return false;
            case END_EFFECTOR: return getEndEffectorPose();
            case END_EFFECTOR_TARGET: return getEndEffectorTargetPose();
            case END_EFFECTOR_TARGET_POSITION: return getEndEffectorTargetPosition();
            case TOOL_CENTER_POINT: return getToolCenterPoint();
            case POSE: return getPoseWorld();
            case JOINT_POSE: return getActiveJointPose();
            case JOINT_HOME: return getBone(activeJoint).getJointHome();
            case ALL_JOINT_VALUES: return getAllJointValues();
            case DESIRED_LINEAR_VELOCITY: return desiredLinearVelocity.get();
            default : {
                logger.warn("invalid get() property {}", property);
                return null;
            }
        }
    }

    private Object getActiveJointValue() {
        DHComponent b = getBone(activeJoint);
        return b.getJointValue();
    }

    private Object getActiveJointPose() {
        Matrix4d m = new Matrix4d();
        m.setIdentity();
        for(int i=0;i<=activeJoint;++i) {
            m.mul(getBone(i).getLocal());
        }
        return m;
    }

    public double[] getAllJointValues() {
        double[] result = new double[getNumBones()];
        for(int i=0;i<getNumBones();++i) {
            result[i] = getBone(i).getJointValue();
        }
        return result;
    }

    @Override
    public void set(int property, Object value) {
        switch (property) {
            case ACTIVE_JOINT -> activeJoint = Math.max(0, Math.min(getNumBones(), (int) value));
            case JOINT_VALUE -> setActiveJointValue((double) value);
            case END_EFFECTOR_TARGET -> setEndEffectorTargetPose((Matrix4d) value);
            case END_EFFECTOR_TARGET_POSITION -> setEndEffectorTargetPosition((Point3d) value);
            case TOOL_CENTER_POINT -> setToolCenterPointOffset((Matrix4d) value);
            case POSE -> setPoseWorld((Matrix4d) value);
            case JOINT_HOME -> getBone(activeJoint).setJointHome((double) value);
            case ALL_JOINT_VALUES -> setAllJointValues((double[]) value);
            case DESIRED_LINEAR_VELOCITY -> desiredLinearVelocity.set((double) value);
            default -> {
                logger.warn("invalid set() property {}", property);
            }
        }
    }

    private Matrix4d inBaseFrameOfReference(Matrix4d m) {
        Matrix4d base = getPoseWorld();
        assert base != null;
        base.invert();
        m.mul(base,m);
        return m;
    }

    private Matrix4d getToolCenterPoint() {
        ArmEndEffectorComponent ee = getEndEffector();
        if(ee==null) return null;

        Matrix4d m = ee.getToolCenterPoint();
        return inBaseFrameOfReference(m);
    }

    private void setToolCenterPointOffset(Matrix4d value) {
        ArmEndEffectorComponent ee = getEndEffector();
        if(ee==null) return;
        Matrix4d base = getPoseWorld();
        assert base != null;

        value.mul(base);
        ee.setToolCenterPoint(base);
    }

    /**
     * Returns the end effector's target pose relative to the robot's base.
     * @return the end effector's target pose relative to the robot's base.
     */
    private Matrix4d getEndEffectorTargetPose() {
        Entity target = getChildTarget();
        if(target==null) return getEndEffectorPose();
        Matrix4d m = target.getComponent(PoseComponent.class).getWorld();
        return inBaseFrameOfReference(m);
    }

    public Entity getChildTarget() {
        return getEntity().findChildNamed(TARGET_NAME);
    }

    /**
     * Returns the end effector's position relative to the robot's base.
     * @return the end effector's position relative to the robot's base.
     */
    private Point3d getEndEffectorTargetPosition() {
        Matrix4d m = getEndEffectorTargetPose();
        return new Point3d(m.m03, m.m13, m.m23);
    }

    /**
     * Sets the end effector target pose and immediately attempts to move the robot to that pose.
     * @param newPose the target pose relative to the robot's base.
     * @throws RuntimeException if the robot cannot be moved to the target pose.
     */
    private void setEndEffectorTargetPose(Matrix4d newPose) {
        Entity target = getChildTarget();
        if(target==null) return;
        PoseComponent targetPose = target.getComponent(PoseComponent.class);
        newPose.mul(getPoseWorld(),newPose);
        targetPose.setWorld(newPose);
    }

    /**
     * Sets the end effector target position and immediately attempts to move the robot to that position.
     * Intended for 3 axis robots only.
     * @param targetPosition the target position relative to the robot's base.
     */
    private void setEndEffectorTargetPosition(Point3d targetPosition) {
        Matrix4d m = getEndEffectorTargetPose();
        m.m03 = targetPosition.x;
        m.m13 = targetPosition.y;
        m.m23 = targetPosition.z;
        setEndEffectorTargetPose(m);
    }

    public ArmEndEffectorComponent getEndEffector() {
        return getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
    }

    /**
     * @return The pose of the end effector relative to the robot's base.
     */
    private Matrix4d getEndEffectorPose() {
        ArmEndEffectorComponent ee = getEndEffector();
        if(ee==null) return null;
        PoseComponent endEffectorPose = ee.getEntity().getComponent(PoseComponent.class);
        if(endEffectorPose==null) return null;
        Matrix4d m = endEffectorPose.getWorld();
        return inBaseFrameOfReference(m);
    }

    /**
     * @return The pose of the robot's base relative to the world.
     */
    private Matrix4d getPoseWorld() {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        if(pose==null) return MatrixHelper.createIdentityMatrix4();
        return pose.getWorld();
    }

    /**
     * @param m The pose of the robot's base relative to the world.
     */
    private void setPoseWorld(Matrix4d m) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        if(pose==null) return;
        pose.setWorld(m);
    }

    private final List<PropertyChangeListener> listeners = new ArrayList<>();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        listeners.add(p);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        listeners.remove(p);
    }

    private void firePropertyChangeEvent(PropertyChangeEvent ee) {
        for(PropertyChangeListener p : listeners) {
            p.propertyChange(ee);
        }
    }

    /**
     * Change one joint angle and update the end effector pose.
     * @param value the new angle for the active joint, in degrees.
     */
    private void setActiveJointValue(double value) {
        Matrix4d eeOld = getEndEffectorPose();
        getBone(activeJoint).setJointValueWRTLimits(value);
        Matrix4d eeNew = getEndEffectorPose();

        firePropertyChangeEvent(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
    }

    public void setAllJointValues(double[] angles) {
        assert angles.length == getNumBones();

        Matrix4d eeOld = getEndEffectorPose();
        boolean changed = false;
        for(int i=0;i<getNumBones();++i) {
            DHComponent bone = getBone(i);
            double t = bone.getJointValue();
            bone.setJointValueWRTLimits(angles[i]);
            changed |= (t!=angles[i]);
        }
        if(changed) {
            Matrix4d eeNew = getEndEffectorPose();
            firePropertyChangeEvent(new PropertyChangeEvent(this, "ee", eeOld, eeNew));
        }
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);

        jo.put("gcodepath", gcodePath.toJSON(context));

        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);

        if(jo.has("gcodepath")) gcodePath.parseJSON(jo.getJSONObject("gcodepath"),context);
    }

    public String getGCodePathEntityUUID() {
        return gcodePath.get();
    }


    @Override
    public void updateReferences(Map<String, String> oldToNewIDMap) {
        gcodePath.updateReferences(oldToNewIDMap);
    }
}
