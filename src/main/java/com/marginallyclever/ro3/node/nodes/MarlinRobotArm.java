package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodeselector.NodeSelector;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link MarlinRobotArm} converts the state of a robot arm into GCode and back.
 */
public class MarlinRobotArm extends Node {
    public static final int MAX_JOINTS = 6;
    private final List<Motor> motors = new ArrayList<>();

    public MarlinRobotArm() {
        this("MarlinRobotArm");
    }

    public MarlinRobotArm(String name) {
        super(name);
        for(int i=0;i<MAX_JOINTS;++i) {
            motors.add(null);
        }
    }

    @Override
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(MarlinRobotArm.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

        var motorSelector = new NodeSelector[MAX_JOINTS];
        for(int i=0;i<MAX_JOINTS;++i) {
            motorSelector[i] = new NodeSelector<>(Motor.class, motors.get(i));
            int j = i;
            motorSelector[i].addPropertyChangeListener("subject",(e)-> motors.set(j,(Motor)e.getNewValue()));
            addLabelAndComponent(pane, "Joint "+i, motorSelector[i]);
        }

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        JSONArray jointArray = new JSONArray();
        for(Motor motor : motors) {
            jointArray.put(motor == null ? JSONObject.NULL : motor.getNodeID());
        }
        json.put("motors",jointArray);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("motors")) {
            JSONArray motorArray = from.getJSONArray("motors");
            for(int i=0;i<motorArray.length();++i) {
                if(motorArray.isNull(i)) {
                    motors.set(i,null);
                } else {
                    motors.set(i,Registry.findNodeByID(motorArray.getString(i),Motor.class));
                }
            }
        }
    }
}
