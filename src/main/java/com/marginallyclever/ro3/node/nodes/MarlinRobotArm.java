package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodeselector.NodeSelector;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.EventListenerList;
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
            addLabelAndComponent(pane, "Motor "+i, motorSelector[i]);
        }

        // Add a text field to send a position to the robot arm.
        JTextField output = new JTextField();
        output.setEditable(false);
        pane.add(output);

        // Add a button that displays gcode to the output.
        JButton getFKButton = new JButton("Get");
        getFKButton.addActionListener(e-> output.setText(getFKAsGCode()) );
        pane.add(getFKButton);

        // Add a text field that will be sent to the robot arm.
        JTextField input = new JTextField();
        pane.add(input);
        // Add a button to send the text field to the robot arm.
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e-> output.setText(sendGCode(input.getText())) );
        pane.add(sendButton);

        super.getComponents(list);
    }

    /**
     * Build a string from the current angle of each motor hinge, aka the
     * <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a> of the robot arm.
     * @return GCode command
     */
    public String getFKAsGCode() {
        StringBuilder sb = new StringBuilder("G0");
        for(Motor motor : motors) {
            if(motor!=null) {
                sb.append(" ")
                    .append(motor.getName())
                    .append(motor.getAxle().getAngle());
            }
        }
        return sb.toString();
    }

    /**
     * Send gcode to robot arm.
     * @param gcode GCode command
     * @return response from robot arm
     */
    public String sendGCode(String gcode) {
        if(gcode.startsWith("G0")) {
            // parse gcode and set motor angles
            String [] parts = gcode.split("\\s+");
            for(Motor motor : motors) {
                if(motor!=null) {
                    for(String p : parts) {
                        if(p.startsWith(motor.getName())) {
                            motor.getAxle().setAngle(Double.parseDouble(p.substring(motor.getName().length())));
                        }
                    }
                }
            }
            return "Ok";
        }
        return "Error: unknown command";
    }
}
