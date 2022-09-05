package com.marginallyclever.robotoverlord.robots;

import javax.vecmath.Matrix4d;
import java.beans.PropertyChangeListener;

/**
 * An attempt to unify the interface for all robots.
 * @author Dan Royer
 * @since 2022-05-12
 */
public interface Robot {
    int NAME=0;
    int NUM_JOINTS = 1;
    int ACTIVE_JOINT = 2;
    int JOINT_NAME = 3;
    int JOINT_VALUE = 4;
    int JOINT_RANGE_MAX = 5;
    int JOINT_RANGE_MIN = 6;
    int JOINT_HAS_RANGE_LIMITS = 7;
    int JOINT_PRISMATIC = 8;
    int END_EFFECTOR = 9;
    int END_EFFECTOR_TARGET = 10;
    int TOOL_CENTER_POINT = 11;
    int POSE = 12;
    int JOINT_POSE = 13;
    int JOINT_HOME = 14;

    /**
     * @param property can be any one of the following:
     * <ul>
     *      <li>{@link #NAME}: returns a string</li>
     *      <li>{@link #NUM_JOINTS}: returns an int</li>
     *      <li>{@link #ACTIVE_JOINT}: returns an int</li>
     *      <li>{@link #JOINT_NAME}: returns a string</li>
     *      <li>{@link #JOINT_VALUE}: returns a double representing the degrees or millimeters of the active joint</li>
     *      <li>{@link #JOINT_RANGE_MAX}: returns a double representing degrees</li>
     *      <li>{@link #JOINT_RANGE_MIN}: returns a double representing degrees</li>
     *      <li>{@link #JOINT_HAS_RANGE_LIMITS}: returns true if there are limits</li>
     *      <li>{@link #JOINT_PRISMATIC}: returns true if the joint is prismatic (linear mm) and false if it is angular (rotation degrees)</li>
     *      <li>{@link #END_EFFECTOR}: returns a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #END_EFFECTOR_TARGET}: returns a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #TOOL_CENTER_POINT}: returns a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #POSE}: returns a {@link Matrix4d} </li>
     *      <li>{@link #JOINT_POSE}: returns a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #JOINT_HOME}: returns a double representing the degrees or millimeters of the active joint at its home position</li>
     * </ul>
     * @return the requested property or null.
     */
    Object get(int property);

    /**
     * @param property see value for valid properties
     * @param value is based on the property flag:
     * <ul>
     *      <li>{@link #ACTIVE_JOINT}: an int in the range 0 ... <code>get(NUM_JOINTS)</code></li>
     *      <li>{@link #JOINT_VALUE}: a double representing the degrees or millimeters of the active joint</li>
     *      <li>{@link #END_EFFECTOR_TARGET}: a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #TOOL_CENTER_POINT}: a {@link Matrix4d} relative to the origin of this robot</li>
     *      <li>{@link #POSE}: a {@link Matrix4d} </li>
     *      <li>{@link #JOINT_POSE}: a {@link Matrix4d} relative to the origin of this robot</li>
     * </ul>
     * @return the requested property or null.
     */
    void set(int property, Object value);

    void addPropertyChangeListener(PropertyChangeListener p);

    void removePropertyChangeListener(PropertyChangeListener p);
}
