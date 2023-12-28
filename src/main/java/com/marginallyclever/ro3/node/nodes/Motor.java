package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A {@link Motor} is a {@link Node} that can be attached to a {@link HingeJoint}.  It will then drive the joint
 * according to the motor's settings.
 */
public class Motor extends Node {
    private HingeJoint hinge;

    public Motor() {
        this("Motor");
    }

    public Motor(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(hinge!=null) {
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

        NodeSelector<HingeJoint> selector = new NodeSelector<>(HingeJoint.class);
        selector.setSubject(hinge);
        selector.addPropertyChangeListener("subject", (evt) ->{
            hinge = selector.getSubject();
        });
        addLabelAndComponent(pane, "Hinge", selector);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        if(hinge!=null) json.put("hinge",hinge.getNodeID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("hinge")) hinge = this.getRootNode().findNodeByID(from.getString("hinge"),HingeJoint.class);
    }

    public HingeJoint getAxle() {
        return hinge;
    }

    public void setAxle(HingeJoint hinge) {
        this.hinge = hinge;
    }

    public boolean hasAxle() {
        return hinge!=null;
    }
}
