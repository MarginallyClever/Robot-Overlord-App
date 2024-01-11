package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    public Pose getEndEffector() {
        return endEffector.getSubject() == null ? null : endEffector.getSubject();
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

    public Motor getJoint(int i) {
        return motors.get(i).getSubject();
    }

    public double[] getAllJointAngles() {
        double[] result = new double[getNumJoints()];
        int i=0;
        for(NodePath<Motor> paths : motors) {
            Motor motor = paths.getSubject();
            if(motor!=null) {
                result[i++] = motor.getHinge().getAngle();
            }
        }
        return result;
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
                    axle.setAngle(values[i++]);
                    axle.update(0);
                }
            }
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
        json.put("version",1);

        for(var motor : motors) {
            jointArray.put(motor == null ? JSONObject.NULL : motor.getPath());
        }
        json.put("motors",jointArray);
        if(endEffector.getSubject()!=null) json.put("endEffector",endEffector.getPath());
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
                    motors.get(i).setPath(null);
                } else {
                    if(version==1) {
                        motors.get(i).setPath(motorArray.getString(i));
                    } else if(version==0) {
                        Motor motor = this.getRootNode().findNodeByID(motorArray.getString(i), Motor.class);
                        motors.get(i).setRelativePath(this,motor);
                    }
                }
            }
        }
        Node root = this.getRootNode();
        if(from.has("endEffector")) {
            String s = from.getString("endEffector");
            if(version==1) {
                endEffector.setPath(s);
            } else if(version==0) {
                Pose goal = root.findNodeByID(s,Pose.class);
                endEffector.setRelativePath(this,goal);
            }
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(Limb.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;
        NodePanelHelper.addNodeSelector(pane, "End Effector", endEffector, Pose.class, gbc,this);

        gbc.gridx=0;
        gbc.gridwidth=2;
        gbc.gridy++;
        pane.add(createFKDials(),gbc);

        gbc.gridy++;
        pane.add(addMotorPanel(),gbc);

        super.getComponents(list);
    }

    private JComponent createFKDials() {
        var containerPanel = new CollapsiblePanel("Forward Kinematics");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridLayout(0,3));

        int count=0;
        for(int i=0;i<getNumJoints();++i) {
            Motor motor = motors.get(i).getSubject();
            if(motor==null) continue;
            outerPanel.add(createOneFKDial(motor));
            count++;
        }
        count = 3-(count%3);
        for(int i=0;i<count;++i) {
            outerPanel.add(new JPanel());
        }

        return containerPanel;
    }

    private JPanel createOneFKDial(final Motor motor) {
        JPanel panel = new JPanel(new BorderLayout());
        Dial dial = new Dial();
        dial.addActionListener(e -> {
            if(motor.hasHinge()) {
                motor.getHinge().setAngle(dial.getValue());
                dial.setValue(motor.getHinge().getAngle());
            }
        });
        // TODO subscribe to motor.getAxle().getAngle(), then dial.setValue() without triggering an action event.

        JLabel label = new JLabel(motor.getName());
        label.setLabelFor(dial);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label,BorderLayout.PAGE_START);
        panel.add(dial,BorderLayout.CENTER);
        dial.setPreferredSize(new Dimension(80,80));
        if(motor.hasHinge()) {
            dial.setValue(motor.getHinge().getAngle());
        }
        return panel;
    }

    private JComponent addMotorPanel() {
        var containerPanel = new CollapsiblePanel("Motors");
        containerPanel.setCollapsed(true);
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        // add a selector for each motor
        var motorSelector = new NodeSelector[MAX_JOINTS];
        for(int i=0;i<MAX_JOINTS;++i) {
            motorSelector[i] = new NodeSelector<>(Motor.class, motors.get(i).getSubject());
            int j = i;
            motorSelector[i].addPropertyChangeListener("subject",(e)-> {
                motors.get(j).setRelativePath(this,(Motor)e.getNewValue());
            });
            NodePanelHelper.addLabelAndComponent(outerPanel, "Motor "+i, motorSelector[i],gbc);
        }
        return containerPanel;
    }
}
