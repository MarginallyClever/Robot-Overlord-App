package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodeselector.NodeSelector;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.ApproximateJacobian;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link MarlinRobotArm} converts the state of a robot arm into GCode and back.
 */
public class MarlinRobotArm extends Node {
    private static final Logger logger = LoggerFactory.getLogger(MarlinRobotArm.class);
    public static final int MAX_JOINTS = 6;
    private final List<Motor> motors = new ArrayList<>();
    private Pose endEffector;
    private Pose target;
    private double linearVelocity=0;

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
        for(Motor motor : motors) jointArray.put(motor == null ? JSONObject.NULL : motor.getNodeID());
        json.put("motors",jointArray);
        if(endEffector!=null) json.put("endEffector",endEffector.getNodeID());
        if(target!=null) json.put("target",target.getNodeID());
        json.put("linearVelocity",linearVelocity);
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
        if(from.has("endEffector")) endEffector = Registry.findNodeByID(from.getString("endEffector"),Pose.class);
        if(from.has("target")) target = Registry.findNodeByID(from.getString("target"),Pose.class);
        if(from.has("linearVelocity")) linearVelocity = from.getDouble("linearVelocity");
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

        NodeSelector<Pose> endEffectorSelector = new NodeSelector<>(Pose.class, endEffector);
        endEffectorSelector.addPropertyChangeListener("subject",(e)-> endEffector = (Pose)e.getNewValue());
        addLabelAndComponent(pane, "End Effector", endEffectorSelector);

        NodeSelector<Pose> targetSelector = new NodeSelector<>(Pose.class, target);
        targetSelector.addPropertyChangeListener("subject",(e)-> target = (Pose)e.getNewValue());
        addLabelAndComponent(pane, "Target", targetSelector);

        //TODO add a slider to control linear velocity
        JSlider slider = new JSlider(0,30,(int)linearVelocity);
        slider.addChangeListener(e-> linearVelocity = slider.getValue());
        addLabelAndComponent(pane, "Linear Vel", slider);

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
        logger.info("heard "+gcode);

        if(gcode.startsWith("G0")) {  // fast non-linear move (FK)
            // parse gcode for motor names and angles
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
        } else if(gcode.equals("pos")) {
            if(endEffector==null) {
                logger.error("no end effector");
                return "Error: no end effector";
            }
            double [] cartesian = getCartesianFromWorld(endEffector.getWorld());
            int i=0;
            String response = "G1"
                    +" X"+StringHelper.formatDouble(cartesian[i++])
                    +" Y"+StringHelper.formatDouble(cartesian[i++])
                    +" Z"+StringHelper.formatDouble(cartesian[i++])
                    +" U"+StringHelper.formatDouble(cartesian[i++])
                    +" V"+StringHelper.formatDouble(cartesian[i++])
                    +" W"+StringHelper.formatDouble(cartesian[i++]);
            logger.info(response);
            return "Ok: "+response;
        } else if(gcode.equals("aj")) {
            ApproximateJacobianFiniteDifferences jacobian = new ApproximateJacobianFiniteDifferences(this);
            logger.debug(jacobian.toString());
            return "Ok";
        } else if(gcode.startsWith("G1")) {  // linear move
            // parse gcode for names and values.  names are XYZ for linear, UVW for angular.
            // They set the new target position for the end effector.
            String [] parts = gcode.split("\\s+");
            double [] cartesian = getCartesianFromWorld(endEffector.getWorld());
            logger.debug("before: "+cartesian[0]+","+cartesian[1]+","+cartesian[2]+","+cartesian[3]+","+cartesian[4]+","+cartesian[5]);
            for(String p : parts) {
                if(p.startsWith("X")) cartesian[0] = Double.parseDouble(p.substring(1));
                else if(p.startsWith("Y")) cartesian[1] = Double.parseDouble(p.substring(1));
                else if(p.startsWith("Z")) cartesian[2] = Double.parseDouble(p.substring(1));
                else if(p.startsWith("U")) cartesian[3] = Double.parseDouble(p.substring(1));
                else if(p.startsWith("V")) cartesian[4] = Double.parseDouble(p.substring(1));
                else if(p.startsWith("W")) cartesian[5] = Double.parseDouble(p.substring(1));
            }
            logger.debug("after: "+cartesian[0]+","+cartesian[1]+","+cartesian[2]+","+cartesian[3]+","+cartesian[4]+","+cartesian[5]);
            // set the target position relative to the base of the robot arm
            if(target==null) {
                logger.error("no target");
                return "Error: no target";
            }
            target.setLocal(getReverseCartesianFromWorld(cartesian));
            return "Ok";
        }
        logger.error("unknown command");
        return "Error: unknown command";
    }

    /**
     *
     * @param cartesian XYZ translation and UVW rotation of the end effector.  UVW is in degrees.
     * @return the matrix that represents the given cartesian position.
     */
    private Matrix4d getReverseCartesianFromWorld(double[] cartesian) {
        Matrix4d local = new Matrix4d();
        Vector3d rot = new Vector3d(cartesian[3],cartesian[4],cartesian[5]);
        rot.scale(Math.PI/180);
        local.set(MatrixHelper.eulerToMatrix(rot));
        local.setTranslation(new Vector3d(cartesian[0],cartesian[1],cartesian[2]));
        return local;
    }

    /**
     * @param world the matrix to convert
     * @return the XYZ translation and UVW rotation of the given matrix.  UVW is in degrees.
     */
    private double[] getCartesianFromWorld(Matrix4d world) {
        Vector3d rotate = MatrixHelper.matrixToEuler(world);
        rotate.scale(180/Math.PI);
        Vector3d translate = new Vector3d();
        world.get(translate);
        return new double[] {translate.x,translate.y,translate.z,rotate.x,rotate.y,rotate.z};
    }

    /**
     * @return the number of non-null motors.
     */
    public int getNumJoints() {
        return (int)motors.stream().filter(Objects::nonNull).count();
    }

    public double[] getAllJointValues() {
        double[] result = new double[getNumJoints()];
        int i=0;
        for(Motor motor : motors) {
            if(motor!=null) {
                result[i++] = motor.getAxle().getAngle();
            }
        }
        return result;
    }

    public void setAllJointValues(double[] values) {
        if(values.length!=getNumJoints()) {
            logger.error("setAllJointValues: one value for every motor");
            return;
        }
        int i=0;
        for(Motor motor : motors) {
            if(motor!=null) {
                HingeJoint axle = motor.getAxle();
                if(axle!=null) {
                    axle.setAngle(values[i++]);
                    axle.update(0);
                }
            }
        }
    }

    public Pose getEndEffector() {
        return endEffector;
    }

    public Pose getTarget() {
        return target;
    }

    public void setTarget(Pose target) {
        this.target = target;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(endEffector==null || target==null || linearVelocity<0.0001) return;

        double[] cartesianVelocity = MatrixHelper.getCartesianBetweenTwoMatrices(endEffector.getWorld(), target.getWorld());
        capVectorToMagnitude(cartesianVelocity,linearVelocity*dt);
        applyCartesianForceToEndEffector(cartesianVelocity);
    }

    /**
     * Make sure the given vector's length does not exceed maxLen.  It can be less than the given magnitude.
     * Store the results in the original array.
     * @param vector the vector to cap
     * @param maxLen the max length of the vector.
     */
    public static void capVectorToMagnitude(double[] vector, double maxLen) {
        // get the length of the vector
        double len = 0;
        for (double v : vector) {
            len += v * v;
        }
        len = Math.sqrt(len);
        if(len < maxLen) return;  // already smaller, nothing to do.

        // scale the vector down
        double scale = maxLen / len;
        for(int i=0;i<vector.length;i++) {
            vector[i] *= scale;
        }
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    public void applyCartesianForceToEndEffector(double[] cartesianVelocity) {
        double sum = sumCartesianVelocityComponents(cartesianVelocity);
        if(sum<0.0001) return;
        if(sum <= 1) {
            applySmallCartesianForceToEndEffector(cartesianVelocity);
            return;
        }

        // split the big move in to smaller moves.
        int total = (int) Math.ceil(sum);
        // allocate a new buffer so that we don't smash the original.
        double[] cartesianVelocityUnit = new double[cartesianVelocity.length];
        for (int i = 0; i < cartesianVelocity.length; ++i) {
            cartesianVelocityUnit[i] = cartesianVelocity[i] / total;
        }
        for (int i = 0; i < total; ++i) {
            applySmallCartesianForceToEndEffector(cartesianVelocityUnit);
        }
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void applySmallCartesianForceToEndEffector(double[] cartesianVelocity) {
        ApproximateJacobian aj = new ApproximateJacobianFiniteDifferences(this);
        //ApproximateJacobian aj = new ApproximateJacobianScrewTheory(robotComponent);
        try {
            double[] jointVelocity = aj.getJointForceFromCartesianForce(cartesianVelocity);  // uses inverse jacobian
            // do not make moves for impossible velocities
            if(impossibleVelocity(jointVelocity)) return;

            double[] angles = getAllJointValues();  // # dof long
            for (int i = 0; i < angles.length; ++i) {
                // TODO: set desired velocity in joint motor component, let motor system handle the rest.
                // TODO: get next derivative and set acceleration?
                angles[i] += jointVelocity[i];
            }
            setAllJointValues(angles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jointVelocity the joint velocity to check
     * @return true if the given joint velocity is impossible.
     */
    private boolean impossibleVelocity(double[] jointVelocity) {
        double maxV = 100; // RPM*60 TODO: get from robot per joint
        for(double v : jointVelocity) {
            if(Double.isNaN(v) || Math.abs(v) > maxV) return true;
        }
        return false;
    }

    private double sumCartesianVelocityComponents(double [] cartesianVelocity) {
        double sum = 0;
        for (double v : cartesianVelocity) {
            sum += Math.abs(v);
        }
        return sum;
    }
}
