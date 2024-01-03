package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link MarlinRobotArm} converts the state of a robot arm into GCode and back.</p>
 * <p>In order to work it requires references to:</p>
 * <ul>
 *     <li>five or six {@link Motor}s, with names matching those in Marlin;</li>
 *     <li>a {@link Pose} end effector to obtain the inverse kinematic pose.  The end effector should be at the end of
 *     the kinematic chain.</li>
 *     <li>a {@link Pose} target that the end effector will try to match.  It will only do so when the linear velocity
 *     is greater than zero.</li>
 *     <li>an optional {@link Motor} for the tool on arm.</li>
 * </ul>
 */
public class MarlinRobotArm extends Node {
    private static final Logger logger = LoggerFactory.getLogger(MarlinRobotArm.class);
    public static final int MAX_JOINTS = 6;
    private final List<Motor> motors = new ArrayList<>();
    private Pose endEffector;
    private Pose target;
    private double linearVelocity=0;
    private Motor gripperMotor;

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
        if(gripperMotor!=null) json.put("gripperMotor",gripperMotor.getNodeID());
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
                    motors.set(i,this.getRootNode().findNodeByID(motorArray.getString(i),Motor.class));
                }
            }
        }
        Node root = this.getRootNode();
        if(from.has("endEffector")) endEffector = root.findNodeByID(from.getString("endEffector"),Pose.class);
        if(from.has("target")) target = root.findNodeByID(from.getString("target"),Pose.class);
        if(from.has("gripperMotor")) gripperMotor = root.findNodeByID(from.getString("gripperMotor"),Motor.class);
        if(from.has("linearVelocity")) linearVelocity = from.getDouble("linearVelocity");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(MarlinRobotArm.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        // add a selector for each motor
        var motorSelector = new NodeSelector[MAX_JOINTS];
        for(int i=0;i<MAX_JOINTS;++i) {
            motorSelector[i] = new NodeSelector<>(Motor.class, motors.get(i));
            int j = i;
            motorSelector[i].addPropertyChangeListener("subject",(e)-> motors.set(j,(Motor)e.getNewValue()));
            addLabelAndComponent(pane, "Motor "+i, motorSelector[i],gbc);
        }

        // add a selector for the end effector
        NodeSelector<Pose> endEffectorSelector = new NodeSelector<>(Pose.class, endEffector);
        endEffectorSelector.addPropertyChangeListener("subject",(e)-> endEffector = (Pose)e.getNewValue());
        addLabelAndComponent(pane, "End Effector", endEffectorSelector,gbc);

        // add a selector for the target
        NodeSelector<Pose> targetSelector = new NodeSelector<>(Pose.class, target);
        targetSelector.addPropertyChangeListener("subject",(e)-> target = (Pose)e.getNewValue());
        addLabelAndComponent(pane, "Target", targetSelector,gbc);

        // add a selector for the target
        NodeSelector<Motor> gripperMotorSelector = new NodeSelector<>(Motor.class, gripperMotor);
        gripperMotorSelector.addPropertyChangeListener("subject",(e)-> gripperMotor = (Motor)e.getNewValue());
        addLabelAndComponent(pane, "Gripper motor", gripperMotorSelector,gbc);

        // add a slider to control linear velocity
        JSlider slider = new JSlider(0,20,(int)linearVelocity);
        slider.addChangeListener(e-> linearVelocity = slider.getValue());
        addLabelAndComponent(pane, "Linear Vel", slider,gbc);

        JButton M114 = new JButton("M114");
        M114.addActionListener(e-> sendGCode("M114"));
        addLabelAndComponent(pane, "Get state", M114,gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        pane.add(getReceiver(),gbc);
        gbc.gridy++;
        pane.add(getSender(),gbc);

        super.getComponents(list);
    }

    // Add a text field that will be sent to the robot arm.
    private JPanel getSender() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField input = new JTextField();
        input.addActionListener(e-> sendGCode(input.getText()) );
        inputPanel.add(input,BorderLayout.CENTER);
        // Add a button to send the text field to the robot arm.
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e-> sendGCode(input.getText()) );

        inputPanel.add(sendButton,BorderLayout.LINE_END);
        return inputPanel;
    }

    // Add a text field to receive messages from the arm.
    private JPanel getReceiver() {
        JPanel outputPanel = new JPanel(new BorderLayout());
        JLabel outputLabel = new JLabel("Output");
        JTextField output = new JTextField();
        output.setEditable(false);
        outputLabel.setLabelFor(output);
        outputLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        outputPanel.add(output,BorderLayout.CENTER);
        outputPanel.add(outputLabel,BorderLayout.LINE_START);
        addMarlinListener(output::setText);
        return outputPanel;
    }

    /**
     * Build a string from the current angle of each motor hinge, aka the
     * <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a> of the robot arm.
     * @return GCode command
     */
    private String getM114() {
        return "M114"+getMotorsAndFeedrateAsString();
    }

    private String getMotorsAndFeedrateAsString() {
        StringBuilder sb = new StringBuilder();
        for(Motor motor : motors) {
            if(motor!=null && motor.hasAxle()) {
                sb.append(" ")
                        .append(motor.getName())
                        .append(StringHelper.formatDouble(motor.getAxle().getAngle()));
            }
        }
        // gripper motor
        if(gripperMotor!=null && gripperMotor.hasAxle()) {
            sb.append(" ")
                    .append(gripperMotor.getName())
                    .append(StringHelper.formatDouble(gripperMotor.getAxle().getAngle()));
        }
        // feedrate
        sb.append(" F").append(StringHelper.formatDouble(linearVelocity));
        return sb.toString();
    }

    /**
     * <p>Send a single gcode command to the robot arm.  It will reply by firing a
     * {@link MarlinListener#messageFromMarlin} event with the String response.</p>
     * @param gcode GCode command
     */
    public void sendGCode(String gcode) {
        logger.debug("heard "+gcode);

        if(gcode.startsWith("G0")) {  // fast non-linear move (FK)
            fireMarlinMessage( parseG0(gcode) );
            return;
        } else if(gcode.equals("M114")) {
            String response = getM114();
            fireMarlinMessage( "Ok: "+response );
            return;
        } else if(gcode.equals("ik")) {
            fireMarlinMessage(getEndEffectorIK());
        } else if(gcode.equals("aj")) {
            ApproximateJacobianFiniteDifferences jacobian = new ApproximateJacobianFiniteDifferences(this);
            fireMarlinMessage( "Ok: "+jacobian );
            return;
        } else if(gcode.startsWith("G1")) {
            fireMarlinMessage( parseG1(gcode) );
            return;
        }
        fireMarlinMessage( "Error: unknown command" );
    }

    /**
     * <p>G0 rapid non-linear move.</p>
     * <p>Parse gcode for motor names and angles, then set the associated joint values directly.</p>
     * @param gcode GCode command
     * @return response from robot arm
     */
    private String parseG0(String gcode) {
        String [] parts = gcode.split("\\s+");
        try {
            for (Motor motor : motors) {
                if (motor != null && motor.hasAxle()) {
                    for (String p : parts) {
                        if (p.startsWith(motor.getName())) {
                            // TODO check new value is in range.
                            motor.getAxle().setAngle(Double.parseDouble(p.substring(motor.getName().length())));
                            break;
                        }
                    }
                }
            }
            // gripper motor
            if (gripperMotor != null && gripperMotor.hasAxle()) {
                for (String p : parts) {
                    if (p.startsWith(gripperMotor.getName())) {
                        // TODO check new value is in range.
                        gripperMotor.getAxle().setAngle(Double.parseDouble(p.substring(gripperMotor.getName().length())));
                        break;
                    }
                }
            }
            // else ignore unused parts
        } catch( NumberFormatException e ) {
            logger.error("Number format exception: "+e.getMessage());
            return "Error: "+e.getMessage();
        }

        return "Ok: G0"+getMotorsAndFeedrateAsString();
    }

    /**
     * <p>G1 Linear move.</p>
     * <p>Parse gcode for names and values, then set the new target position.  Names are XYZ for linear, UVW for
     * angular. Angular values should be in degrees.</p>
     * <p>Movement will occur on {@link #update(double)} provided the {@link #linearVelocity} and the update time are
     * greater than zero.</p>
     * @param gcode GCode command
     * @return response from robot arm
     */
    private String parseG1(String gcode) {
        if(target==null) {
            logger.error("no target");
            return "Error: no target";
        }

        String [] parts = gcode.split("\\s+");
        double [] cartesian = getCartesianFromWorld(endEffector.getWorld());
        for(String p : parts) {
            if(p.startsWith("F")) setLinearVelocity(Double.parseDouble(p.substring(1)));
            if(p.startsWith("X")) cartesian[0] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("Y")) cartesian[1] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("Z")) cartesian[2] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("U")) cartesian[3] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("V")) cartesian[4] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("W")) cartesian[5] = Double.parseDouble(p.substring(1));
            else logger.warn("unknown G1 command: "+p);
        }
        // set the target position relative to the base of the robot arm
        target.setLocal(getReverseCartesianFromWorld(cartesian));
        return "Ok";
    }

    private String getEndEffectorIK() {
        if(endEffector==null) {
            return ( "Error: no end effector" );
        }
        double [] cartesian = getCartesianFromWorld(endEffector.getWorld());
        int i=0;
        String response = "G1"
                +" F"+StringHelper.formatDouble(linearVelocity)
                +" X"+StringHelper.formatDouble(cartesian[i++])
                +" Y"+StringHelper.formatDouble(cartesian[i++])
                +" Z"+StringHelper.formatDouble(cartesian[i++])
                +" U"+StringHelper.formatDouble(cartesian[i++])
                +" V"+StringHelper.formatDouble(cartesian[i++])
                +" W"+StringHelper.formatDouble(cartesian[i++]);
        return ( "Ok: "+response );
    }

    /**
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

    public double[] getAllJointAngles() {
        double[] result = new double[getNumJoints()];
        int i=0;
        for(Motor motor : motors) {
            if(motor!=null) {
                result[i++] = motor.getAxle().getAngle();
            }
        }
        return result;
    }

    public void setAllJointAngles(double[] values) {
        if(values.length != getNumJoints()) {
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

    public void setAllJointVelocities(double[] values) {
        if(values.length!=getNumJoints()) {
            logger.error("setAllJointValues: one value for every motor");
            return;
        }
        int i=0;
        for(Motor motor : motors) {
            if(motor!=null) {
                HingeJoint axle = motor.getAxle();
                if(axle!=null) {
                    axle.setVelocity(values[i++]);
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
        if(dt==0) return;
        moveTowardsTarget(dt);
    }

    private void moveTowardsTarget(double dt) {
        if(endEffector==null || target==null || linearVelocity<0.0001) {
            double[] jointAnglesOriginal = getAllJointAngles();
            Arrays.fill(jointAnglesOriginal, 0);
            setAllJointVelocities(jointAnglesOriginal);
            return;
        }
        double[] cartesianVelocity = MatrixHelper.getCartesianBetweenTwoMatrices(endEffector.getWorld(), target.getWorld());
        scaleVectorToMagnitude(cartesianVelocity,linearVelocity);
        moveEndEffectorInCartesianDirection(cartesianVelocity);
    }

    /**
     * Make sure the given vector's length does not exceed maxLen.  It can be less than the given magnitude.
     * Store the results in the original array.
     * @param vector the vector to cap
     * @param maxLen the max length of the vector.
     */
    public static void scaleVectorToMagnitude(double[] vector, double maxLen) {
        // get the length of the vector
        double len = 0;
        for (double v : vector) {
            len += v * v;
        }

        len = Math.sqrt(len);
        if(maxLen>len) maxLen=len;

        // scale the vector
        double scale = len==0? 0 : maxLen / len;  // catch len==0
        for(int i=0;i<vector.length;i++) {
            vector[i] *= scale;
        }
    }

    /**
     * Attempts to move the robot arm such that the end effector travels in the direction of the cartesian velocity.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    public void moveEndEffectorInCartesianDirection(double[] cartesianVelocity) {
        // is it a tiny move?
        double sum = sumCartesianVelocityComponents(cartesianVelocity);
        if(sum<0.0001) return;
        if(sum <= 1) {
            setMotorVelocitiesFromCartesianVelocity(cartesianVelocity);
            return;
        }

        // set motor velocities.
        setMotorVelocitiesFromCartesianVelocity(cartesianVelocity);
    }

    /**
     * <p>Attempts to move the robot arm such that the end effector travels in the cartesian direction.  This is
     * achieved by setting the velocity of the motors.</p>
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void setMotorVelocitiesFromCartesianVelocity(double[] cartesianVelocity) {
        ApproximateJacobian aj = getJacobian();
        try {
            double[] jointVelocity = aj.getJointFromCartesian(cartesianVelocity);  // uses inverse jacobian
            if(impossibleVelocity(jointVelocity)) return;  // TODO: throw exception instead?
            setAllJointVelocities(jointVelocity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApproximateJacobian getJacobian() {
        // option 1, use finite differences
        return new ApproximateJacobianFiniteDifferences(this);
        // option 2, use screw theory
        //ApproximateJacobian aj = new ApproximateJacobianScrewTheory(robotComponent);
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

    public void addMarlinListener(MarlinListener editorPanel) {
        listeners.add(MarlinListener.class,editorPanel);
    }

    public void removeMarlinListener(MarlinListener editorPanel) {
        listeners.remove(MarlinListener.class,editorPanel);
    }

    private void fireMarlinMessage(String message) {
        //logger.info(message);

        for(MarlinListener listener : listeners.getListeners(MarlinListener.class)) {
            listener.messageFromMarlin(message);
        }
    }

    public double getLinearVelocity() {
        return linearVelocity;
    }

    public void setLinearVelocity(double linearVelocity) {
        if(linearVelocity<0) throw new IllegalArgumentException("linearVelocity must be >= 0");
        this.linearVelocity = linearVelocity;
    }
}
