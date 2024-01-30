package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * <p>a {@link HingeJoint} is a joint that can rotate around the local Z axis.</p>
 * <p>a {@link HingeJoint} should be attached to a child {@link Pose} referenced as the axle.  In this way the axle's
 * parent {@link Pose} can be thought of as the initial pose.  This helps prevent drift over time.</p>
 */
public class HingeJoint extends Node {
    private double angle = 0;  // degrees
    private double minAngle = 0;  // degrees
    private double maxAngle = 360;  // degrees
    private double velocity = 0;  // degrees/s
    private double acceleration = 0;  // degrees/s/s
    private final NodePath<Pose> axle = new NodePath<>(this,Pose.class);

    public HingeJoint() {
        this("HingeJoint");
    }

    public HingeJoint(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new HingeJointPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        velocity += acceleration * dt;
        setAngle(angle + velocity * dt);

        if(axle.getSubject()!=null) {
            // set the axle's location in space.
            axle.getSubject().getLocal().rotZ(Math.toRadians(angle));
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("angle",angle);
        json.put("minAngle",minAngle);
        json.put("maxAngle",maxAngle);
        json.put("velocity",velocity);
        json.put("acceleration",acceleration);
        json.put("version",2);
        if(axle.getSubject()!=null) json.put("axle",axle.getUniqueID());

        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("angle")) angle = from.getDouble("angle");
        if(from.has("minAngle")) minAngle = from.getDouble("minAngle");
        if(from.has("maxAngle")) maxAngle = from.getDouble("maxAngle");
        if(from.has("velocity")) velocity = from.getDouble("velocity");
        if(from.has("acceleration")) acceleration = from.getDouble("acceleration");

        int version = from.has("version") ? from.getInt("version") : 0;
        if(from.has("axle")) {
            String s = from.getString("axle");
            if(version==1) {
                axle.setUniqueIDByNode(this.findNodeByPath(s,Pose.class));
            } else if(version==0 || version==2) {
                axle.setUniqueID(s);
            }
        }
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double degrees) {
        angle = degrees;

        if(maxAngle!=360 && minAngle!=0) {
            if (angle > maxAngle) angle = maxAngle;
            if (angle < minAngle) angle = minAngle;
        }
    }

    public double getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(double v) {
        minAngle = v;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(double v) {
        maxAngle = v;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public Pose getAxle() {
        return axle.getSubject();
    }

    public void setAxle(Pose subject) {
        axle.setUniqueIDByNode(subject);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/hinge-16.png")));
    }
}