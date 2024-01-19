package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link Limb} is a linear chain of bones, joints, and muscles.  Bones are represented by {@link Pose}s.  Joints are
 * represented by {@link HingeJoint}s.  Muscles are represented by {@link Motor}s.  The end of the chain is a
 * {@link Pose} called the <i>end effector</i>.</p>
 * <p>{@link Limb}s only perform <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a> -
 * given the angle of each joint, they calculate the world space position of the end effector.  For more sophisticated
 * behavior, use a {@link LimbSolver}.</p>
 * <p>{@link Limb} is designed to handle six joints or less.</p>
 */
public class Limb extends Pose {
    public static final int MAX_JOINTS = 6;
    private final List<NodePath<Motor>> motors = new ArrayList<>();
    private final NodePath<Pose> endEffector = new NodePath<>(this,Pose.class);

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
                    axle.update(0);
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
        json.put("version",2);

        for(var motor : motors) {
            jointArray.put(motor == null ? JSONObject.NULL : motor.getUniqueID());
        }
        json.put("motors",jointArray);
        if(endEffector.getSubject()!=null) json.put("endEffector",endEffector.getUniqueID());
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
            } else if(version==0||version==2) {
                endEffector.setUniqueID(s);
            }
        }
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
}
