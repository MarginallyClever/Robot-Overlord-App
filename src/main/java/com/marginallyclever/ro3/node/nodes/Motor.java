package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONObject;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Motor} is a {@link Node} that can be attached to a {@link HingeJoint}.  It will then drive the joint
 * according to the motor's settings.
 */
public class Motor extends Node {
    private final NodePath<HingeJoint> hinge = new NodePath<>(this,HingeJoint.class);

    public Motor() {
        this("Motor");
    }

    public Motor(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(hinge.getSubject()!=null) {
            // change Hinge values to affect the Pose.
            // TODO DC motors, alter the hinge to apply force to the joint.
            // TODO Stepper motors, simulate moving in fixed steps.
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MotorPanel(this));
        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",2);
        if(hinge.getSubject()!=null) json.put("hinge",hinge.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if(from.has("hinge")) {
            String s = from.getString("hinge");
            if(version==1) {
                hinge.setSubject(this.findNodeByPath(s,HingeJoint.class));
            } else if(version==0 || version==2) {
                hinge.setUniqueID(s);
            }
        }
    }

    public HingeJoint getHinge() {
        return hinge.getSubject();
    }

    /**
     * Set the hinge this motor will drive.  the hinge must be in the same node tree as this motor.
     * @param hinge the hinge this motor will drive.
     */
    public void setHinge(HingeJoint hinge) {
        this.hinge.setSubject(hinge);
    }

    public boolean hasHinge() {
        return getHinge()!=null;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-motor-16.png")));
    }
}
