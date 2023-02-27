package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.robots.robotarm.ApproximateJacobian2;
import com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.DHTable;
import com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.RobotArmInterface;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * RobotComponent is attached to the root of a robotic arm.
 * It requires that each link in the arm has a {@link PoseComponent} and a {@link DHComponent}.  The end effector
 * must have an {@link ArmEndEffectorComponent}.
 * @author Dan Royer
 * @since 2022-09-14
 */
public class RobotComponent extends Component implements Robot {
    private int activeJoint;
    private final List<DHComponent> bones = new ArrayList<>();

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);

        findBones();

        ViewElementButton bOpen = view.addButton("Open control panel");
        bOpen.addActionEventListener((evt)-> {
            Entity e = getEntity().getRoot();
            final JFrame parentFrame = (e instanceof RobotOverlord) ? ((RobotOverlord)e).getMainFrame() : null;
            final Robot me = this;

            new Thread(() -> {
                JDialog frame = new JDialog(parentFrame,"Control panel");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.add(new RobotArmInterface(me));
                frame.pack();
                frame.setLocationRelativeTo(parentFrame);
                frame.setVisible(true);
            }).start();
        });

        ViewElementButton bDHTable = view.addButton("Open DH Table");
        bDHTable.addActionEventListener((evt)-> {
            Entity e = getEntity().getRoot();
            final JFrame parentFrame = (e instanceof RobotOverlord) ? ((RobotOverlord)e).getMainFrame() : null;
            final RobotComponent me = this;

            JDialog frame = new JDialog(parentFrame,"DH Table");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new DHTable(me));
            frame.pack();
            frame.setLocationRelativeTo(parentFrame);
            frame.setVisible(true);
        });
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
            Entity e = queue.poll();
            DHComponent c = e.findFirstComponent(DHComponent.class);
            if(c!=null) bones.add(c);
            queue.addAll(e.getChildren());
        }
    }

    @Override
    public Object get(int property) {
        switch(property) {
            case NAME: return getName();
            case NUM_JOINTS: return getNumBones();
            case ACTIVE_JOINT: return activeJoint;
            case JOINT_NAME: return getBone(activeJoint).getEntity().getName();
            case JOINT_VALUE: return getBone(activeJoint).getTheta();
            case JOINT_RANGE_MAX: return getBone(activeJoint).getThetaMax();
            case JOINT_RANGE_MIN: return getBone(activeJoint).getThetaMin();
            case JOINT_HAS_RANGE_LIMITS: return true;
            case JOINT_PRISMATIC: return false;
            case END_EFFECTOR: return getEndEffector();
            case END_EFFECTOR_TARGET: return getEndEffectorTargetPose();
            case TOOL_CENTER_POINT: return getToolCenterPoint();
            case POSE: return getPoseWorld();
            case JOINT_POSE: {
                Matrix4d m = new Matrix4d();
                m.setIdentity();
                for(int i=0;i<=activeJoint;++i) {
                    m.mul(getBone(i).getLocal());
                }
                return m;
            }
            case JOINT_HOME: return getBone(activeJoint).getThetaHome();
            default : return null;
        }
    }

    @Override
    public void set(int property, Object value) {
        switch (property) {
            case ACTIVE_JOINT -> activeJoint = Math.max(0, Math.min(getNumBones(), (int) value));
            case JOINT_VALUE -> updateJointValue((double) value);
            case END_EFFECTOR_TARGET -> setEndEffectorTargetPose((Matrix4d) value);
            case TOOL_CENTER_POINT -> setToolCenterPointOffset((Matrix4d) value);
            case POSE -> setPoseWorld((Matrix4d) value);
            case JOINT_HOME -> getBone(activeJoint).setThetaHome((double) value);
            default -> { }
        }
    }

    private Matrix4d getToolCenterPoint() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;
        Matrix4d m = ee.getToolCenterPoint();
        Matrix4d base = getPoseWorld();
        base.invert();
        m.mul(base);
        return m;
    }

    private void setToolCenterPointOffset(Matrix4d value) {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return;
        Matrix4d base = getPoseWorld();
        value.mul(base);
        ee.setToolCenterPoint(base);
    }

    private Matrix4d getEndEffectorTargetPose() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;
        PoseComponent pose = ee.getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        Matrix4d m = pose.getWorld();
        Matrix4d base = getPoseWorld();
        base.invert();
        m.mul(base);
        return m;
    }

    private void setEndEffectorTargetPose(Matrix4d mat) {
        Matrix4d m0 = this.getEndEffector();
        double[] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, mat);
        // Log.message("cartesianDistance="+Arrays.toString(cartesianDistance));
        ApproximateJacobian2 aj = new ApproximateJacobian2(this);
        try {
            double[] jointDistance = aj.getJointFromCartesian(cartesianDistance);
            double[] angles = this.getAngles();
            for (int i = 0; i < angles.length; ++i) {
                angles[i] += jointDistance[i];
            }
            this.setAngles(angles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double[] getAngles() {
        double[] angles = new double[getNumBones()];
        for(int i=0;i<getNumBones();++i) {
            angles[i] = getBone(i).getTheta();
        }
        return angles;
    }

    public void setAngles(double[] angles) {
        Matrix4d eeOld = getEndEffector();
        boolean changed = false;

        for(int i=0;i<getNumBones();++i) {
            DHComponent bone = getBone(i);
            double t = bone.getTheta();
            bone.setTheta(angles[i]);
            changed |= (t!=angles[i]);
        }
        if(changed) {
            Matrix4d eeNew = getEndEffector();
            notifyPropertyChangeListeners(new PropertyChangeEvent(this, "ee", eeOld, eeNew));
        }
    }

    /**
     * @return The pose of the end effector relative to the robot's base.
     */
    public Matrix4d getEndEffector() {
        ArmEndEffectorComponent ee = getEntity().findFirstComponentRecursive(ArmEndEffectorComponent.class);
        if(ee==null) return null;
        PoseComponent pose = ee.getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        Matrix4d m = pose.getWorld();
        Matrix4d base = getPoseWorld();
        base.invert();
        m.mul(base);
        return m;
    }

    /**
     * @return The pose of the robot's base relative to the world.
     */
    private Matrix4d getPoseWorld() {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return null;
        return pose.getWorld();
    }

    /**
     * @param pose The pose of the robot's base relative to the world.
     */
    private void setPoseWorld(Matrix4d pose) {
        PoseComponent p = getEntity().findFirstComponent(PoseComponent.class);
        if(p==null) return;
        p.setWorld(pose);
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

    private void notifyPropertyChangeListeners(PropertyChangeEvent ee) {
        for(PropertyChangeListener p : listeners) {
            p.propertyChange(ee);
        }
    }

    /**
     * Change one joint angle and update the end effector pose.
     * @param value the new angle for the active joint, in degrees.
     */
    private void updateJointValue(double value) {
        Matrix4d eeOld = getEndEffector();
        getBone(activeJoint).setAngleWRTLimits(value);
        Matrix4d eeNew = getEndEffector();

        notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
    }
}
