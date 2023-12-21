package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodeselector.NodeSelector;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;

/**
 * {@link HingeJoint} is a joint that can rotate around the local Z axis.
 */
public class HingeJoint extends Node {
    private double angle = 0;  // degrees
    private double minAngle = 0;  // degrees
    private double maxAngle = 360;  // degrees
    private double velocity = 0;  // degrees/s
    private double acceleration = 0;  // degrees/s/s
    private Pose axle;

    public HingeJoint() {
        this("HingeJoint");
    }

    public HingeJoint(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(HingeJoint.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);

        JFormattedTextField angleField = new JFormattedTextField(formatter);
        angleField.setValue(angle);
        angleField.addPropertyChangeListener("value", (evt) ->{
            angle = ((Number) angleField.getValue()).doubleValue();
        });

        JFormattedTextField maxAngleField = new JFormattedTextField(formatter);
        maxAngleField.setValue(maxAngle);
        maxAngleField.addPropertyChangeListener("value", (evt) ->{
            maxAngle = ((Number) maxAngleField.getValue()).doubleValue();
        });

        JFormattedTextField minAngleField = new JFormattedTextField(formatter);
        minAngleField.setValue(minAngle);
        minAngleField.addPropertyChangeListener("value", (evt) ->{
            minAngle = ((Number) minAngleField.getValue()).doubleValue();
        });

        JFormattedTextField velocityField = new JFormattedTextField(formatter);
        velocityField.setValue(velocity);
        velocityField.addPropertyChangeListener("value", (evt) ->{
            velocity = ((Number) velocityField.getValue()).doubleValue();
        });

        JFormattedTextField accelerationField = new JFormattedTextField(formatter);
        accelerationField.setValue(acceleration);
        accelerationField.addPropertyChangeListener("value", (evt) ->{
            acceleration = ((Number) accelerationField.getValue()).doubleValue();
        });

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class);
        selector.setSubject(axle);
        selector.addPropertyChangeListener("subject", (evt) ->{
            axle = selector.getSubject();
        });

        addLabelAndComponent(pane, "Axle",selector);
        addLabelAndComponent(pane, "Angle",angleField);
        addLabelAndComponent(pane, "Min",minAngleField);
        addLabelAndComponent(pane, "Max",maxAngleField);
        addLabelAndComponent(pane, "Velocity",velocityField);
        addLabelAndComponent(pane, "Acceleration",accelerationField);

        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        velocity += acceleration * dt;
        angle += velocity * dt;

        if(axle!=null) {
            // set the axle's location in space.
            Matrix3d rotZ = new Matrix3d();
            rotZ.rotZ(Math.toRadians(angle));
            Matrix4d local = new Matrix4d(axle.getLocal());
            local.set(rotZ);
            axle.setLocal(local);
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("angle",angle);
        json.put("minAngle",minAngle);
        json.put("maxAngle",maxAngle);
        if(axle!=null) json.put("axle",axle.getNodeID());
        json.put("velocity",velocity);
        json.put("acceleration",acceleration);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("axle")) {
            axle = Registry.findNodeByID(from.getString("axle"),Pose.class);
        }
        if(from.has("angle")) angle = from.getDouble("angle");
        if(from.has("minAngle")) minAngle = from.getDouble("minAngle");
        if(from.has("maxAngle")) maxAngle = from.getDouble("maxAngle");
        if(from.has("velocity")) velocity = from.getDouble("velocity");
        if(from.has("acceleration")) acceleration = from.getDouble("acceleration");
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double degrees) {
        angle = degrees;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getMaxAngle() {
        return maxAngle;
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
}