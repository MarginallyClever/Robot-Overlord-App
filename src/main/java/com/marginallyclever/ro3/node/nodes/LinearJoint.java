 package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * <p>a {@link LinearJoint} is a joint that can translate along the local Z axis.</p>
 * <p>a {@link LinearJoint} should be attached to a child {@link Pose} referenced as the car.  In this way the car's
 * parent {@link Pose} can be thought of as the initial pose at zero mm.  This helps prevent drift over time.</p>
 */
public class LinearJoint extends Node {
    private double position = 0;  // cm
    private double minPosition = 0;  // cm
    private double maxPosition = 100;  // cm
    private double velocity = 0;  // cm/s
    private double acceleration = 0;  // cm/s/s
    private final NodePath<Pose> car = new NodePath<>(this,Pose.class);

    public LinearJoint() {
        this("LinearJoint");
    }

    public LinearJoint(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LinearJointPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        velocity += acceleration * dt;
        setPosition(position + velocity * dt);

        if(car.getSubject()!=null) {
            // set the axle's location in space.
            var subject = car.getSubject();
            var m = subject.getLocal();
            m.m23 = position;
            subject.setLocal(m);
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("position", position);
        json.put("minPosition", minPosition);
        json.put("maxPosition", maxPosition);
        json.put("velocity",velocity);
        json.put("acceleration",acceleration);
        json.put("car", car.getUniqueID());

        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("position")) position = from.getDouble("position");
        if(from.has("minPosition")) minPosition = from.getDouble("minPosition");
        if(from.has("maxPosition")) maxPosition = from.getDouble("maxPosition");
        if(from.has("velocity")) velocity = from.getDouble("velocity");
        if(from.has("acceleration")) acceleration = from.getDouble("acceleration");
        if(from.has("car")) car.setUniqueID(from.getString("car"));
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double degrees) {
        position = degrees;
        if (position > maxPosition) position = maxPosition;
        if (position < minPosition) position = minPosition;
    }

    public double getMinPosition() {
        return minPosition;
    }

    public void setMinPosition(double v) {
        minPosition = v;
    }

    public double getMaxPosition() {
        return maxPosition;
    }

    public void setMaxPosition(double v) {
        maxPosition = v;
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

    public Pose getCar() {
        return car.getSubject();
    }

    public void setCar(Pose subject) {
        car.setUniqueIDByNode(subject);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-slider-bar-16.png")));
    }
}