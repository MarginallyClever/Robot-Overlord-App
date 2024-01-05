package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    private HingeJoint addHinge() {
        HingeJoint hinge = new HingeJoint("Motor Hinge");
        Node parent = getParent();
        if(parent!=null) parent.addChild(hinge);
        return hinge;
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(Motor.class.getSimpleName());

        NodeSelector<HingeJoint> selector = new NodeSelector<>(HingeJoint.class,hinge.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->{
            hinge.setRelativePath(this,selector.getSubject());
        });
        addLabelAndComponent(pane, "Hinge", selector);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",1);
        if(hinge.getSubject()!=null) json.put("hinge",hinge.getPath());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if(from.has("hinge")) {
            if(version==1) {
                hinge.setPath(from.getString("hinge"));
            } else if(version==0) {
                HingeJoint joint = this.getRootNode().findNodeByID(from.getString("hinge"), HingeJoint.class);
                hinge.setRelativePath(this, joint);
            }
        }
    }

    public HingeJoint getHinge() {
        return hinge.getSubject();
    }

    public void setAxle(HingeJoint hinge) {
        this.hinge.setRelativePath(this, hinge);
    }

    public boolean hasAxle() {
        return hinge.getSubject()!=null;
    }
}
