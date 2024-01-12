package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.limbsolver.ApproximateJacobianFiniteDifferences;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link MarlinRobotArm} converts the state of a robot arm into GCode and back.</p>
 * <p>In order to work it requires references to:</p>
 * <ul>
 *     <li>a {@link Limb} of no more than six {@link Motor}s, whose names match those in Marlin;</li>
 *     <li>a {@link LimbSolver} to calculate the inverse kinematics;</li>
 *     <li>an optional {@link Motor} for the tool on arm.</li>
 * </ul>
 */
public class MarlinRobotArm extends Node {
    private static final Logger logger = LoggerFactory.getLogger(MarlinRobotArm.class);
    public final NodePath<Limb> limb = new NodePath<>(this,Limb.class);
    public final NodePath<LimbSolver> solver = new NodePath<>(this,LimbSolver.class);
    private final NodePath<Motor> gripperMotor = new NodePath<>(this,Motor.class);
    private double reportInterval=1.0;

    public MarlinRobotArm() {
        this("MarlinRobotArm");
    }

    public MarlinRobotArm(String name) {
        super(name);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",2);
        if(gripperMotor.getSubject()!=null) json.put("gripperMotor",gripperMotor.getPath());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if(version<2) {
            var toRemove = new ArrayList<Node>();
            while(!getChildren().isEmpty()) {
                removeChild(getChildren().get(0));
            }

            // limb
            Limb limb1 = new Limb();
            limb1.fromJSON(from);
            limb1.getChildren().stream().filter(n -> n.getName().equals("target")).forEach(toRemove::add);
            for(Node n : toRemove) limb1.removeChild(n);
            toRemove.clear();
            limb1.setName("Limb");
            getParent().addChild(limb1);
            limb.setRelativePath(this,limb1);

            // solver
            LimbSolver solver1 = new LimbSolver();
            solver1.fromJSON(from);
            solver1.getChildren().stream().filter(n -> !n.getName().equals("target")).forEach(toRemove::add);
            for(Node n : toRemove) solver1.removeChild(n);
            solver1.setName("LimbSolver");
            getParent().addChild(solver1);
            solver.setRelativePath(this,solver1);
            solver1.setLimb(limb1);

            // gripper
            Node root = this.getRootNode();
            if (from.has("gripperMotor")) {
                String s = from.getString("gripperMotor");
                if (version == 1) {
                    gripperMotor.setPath(s);
                } else if (version == 0) {
                    Motor goal = root.findNodeByID(s, Motor.class);
                    gripperMotor.setRelativePath(this, goal);
                }
            }

            limb.setRelativePath(this,limb1);
            solver.setRelativePath(this,solver1);
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(MarlinRobotArm.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        NodePanelHelper.addNodeSelector(pane, "Limb", limb, Limb.class, gbc,this);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(pane, "Solver", solver, LimbSolver.class, gbc,this);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(pane, "Gripper motor", gripperMotor, Motor.class, gbc,this);
        gbc.gridy++;
        JButton M114 = new JButton("M114");
        M114.addActionListener(e-> sendGCode("M114"));
        NodePanelHelper.addLabelAndComponent(pane, "Get state", M114,gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        pane.add(getReceiver(),gbc);
        gbc.gridy++;
        pane.add(getSender(),gbc);
        gbc.gridy++;
        pane.add(createReportInterval(),gbc);

        super.getComponents(list);
    }

    private JComponent createReportInterval() {
        var containerPanel = new CollapsiblePanel("Report");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridBagLayout());

        var label = new JLabel("interval (s)");
        // here i need an input - time interval (positive float, seconds)
        var formatter = NumberFormatHelper.getNumberFormatter();
        var secondsField = new JFormattedTextField(formatter);
        secondsField.setValue(getReportInterval());

        // then a toggle to turn it on and off.
        JToggleButton toggle = new JToggleButton("Start");
        toggle.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/icons8-stopwatch-16.png"))));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setMaximum((int) (getReportInterval() * 1000)); // Assuming interval is in seconds

        Timer timer = new Timer(100, null);
        ActionListener timerAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int value = progressBar.getValue() + 100;
                if (value >= progressBar.getMaximum()) {
                    value = 0;
                    sendGCode("G0"); // Send G0 command when progress bar is full
                }
                progressBar.setValue(value);
            }
        };

        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                toggle.setText("Stop");
                timer.addActionListener(timerAction);
                timer.start();
            } else {
                toggle.setText("Start");
                progressBar.setValue(0); // Reset progress bar when toggle is off
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        toggle.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !toggle.isShowing()) {
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        // Add components to the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        outerPanel.add(label, gbc);
        gbc.gridx++;
        outerPanel.add(secondsField, gbc);
        gbc.gridy++;
        gbc.gridx=0;
        outerPanel.add(toggle, gbc);
        gbc.gridx++;
        outerPanel.add(progressBar, gbc);
        gbc.gridy++;

        return containerPanel;
    }

    private double getReportInterval() {
        return reportInterval;
    }

    private void setReportInterval(double seconds) {
        reportInterval = Math.max(0.1,seconds);
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
        output.setMaximumSize(new Dimension(100, output.getPreferredSize().height));
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

    public Limb getLimb() {
        return limb.getSubject();
    }

    public LimbSolver getSolver() {
        return solver.getSubject();
    }

    private String getMotorsAndFeedrateAsString() {
        if(getLimb()==null || getSolver()==null) return "";
        StringBuilder sb = new StringBuilder();
        for(NodePath<Motor> paths : getLimb().getMotors()) {
            Motor motor = paths.getSubject();
            if(motor!=null && motor.hasHinge()) {
                sb.append(" ")
                        .append(motor.getName())
                        .append(StringHelper.formatDouble(motor.getHinge().getAngle()));
            }
        }
        // gripper motor
        Motor gripperMotor = this.gripperMotor.getSubject();
        if(gripperMotor!=null && gripperMotor.hasHinge()) {
            sb.append(" ")
                    .append(gripperMotor.getName())
                    .append(StringHelper.formatDouble(gripperMotor.getHinge().getAngle()));
        }
        // feedrate
        sb.append(" F").append(StringHelper.formatDouble(getSolver().getLinearVelocity()));
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
            ApproximateJacobianFiniteDifferences jacobian = new ApproximateJacobianFiniteDifferences(getLimb());
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
        if(getLimb()==null) {
            logger.warn("no limb");
            return "Error: no limb";
        }
        String [] parts = gcode.split("\\s+");
        try {
            for (NodePath<Motor> paths : getLimb().getMotors()) {
                Motor motor = paths.getSubject();
                if (motor != null && motor.hasHinge()) {
                    for (String p : parts) {
                        if (p.startsWith(motor.getName())) {
                            // TODO check new value is in range.
                            motor.getHinge().setAngle(Double.parseDouble(p.substring(motor.getName().length())));
                            break;
                        }
                    }
                }
            }
            // gripper motor
            Motor gripperMotor = this.gripperMotor.getSubject();
            if (gripperMotor != null && gripperMotor.hasHinge()) {
                for (String p : parts) {
                    if (p.startsWith(gripperMotor.getName())) {
                        // TODO check new value is in range.
                        gripperMotor.getHinge().setAngle(Double.parseDouble(p.substring(gripperMotor.getName().length())));
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
     * <p>Movement will occur on {@link #update(double)} provided the {@link LimbSolver} linear velocity and the update
     * time are greater than zero.</p>
     * @param gcode GCode command
     * @return response from robot arm
     */
    private String parseG1(String gcode) {
        Limb myLimb = getLimb();
        if(myLimb==null) {
            logger.warn("no limb");
            return "Error: no limb";
        }
        LimbSolver mySolver = getSolver();
        if(mySolver==null) {
            logger.warn("no solver");
            return "Error: no solver";
        }
        if(mySolver.getTarget()==null) {
            logger.warn("no target");
            return "Error: no target";
        }
        if(myLimb.getEndEffector()==null) {
            logger.warn("no end effector");
            return "Error: no end effector";
        }
        String [] parts = gcode.split("\\s+");
        double [] cartesian = getCartesianFromWorld(myLimb.getEndEffector().getWorld());
        for(String p : parts) {
            if(p.startsWith("F")) mySolver.setLinearVelocity(Double.parseDouble(p.substring(1)));
            if(p.startsWith("X")) cartesian[0] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("Y")) cartesian[1] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("Z")) cartesian[2] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("U")) cartesian[3] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("V")) cartesian[4] = Double.parseDouble(p.substring(1));
            else if(p.startsWith("W")) cartesian[5] = Double.parseDouble(p.substring(1));
            else logger.warn("unknown G1 command: "+p);
        }
        // set the target position relative to the base of the robot arm
        mySolver.getTarget().setLocal(getReverseCartesianFromWorld(cartesian));
        return "Ok";
    }

    private String getEndEffectorIK() {
        Limb myLimb = getLimb();
        if(myLimb==null) {
            logger.warn("no limb");
            return "Error: no limb";
        }
        LimbSolver mySolver = getSolver();
        if(mySolver==null) {
            logger.warn("no solver");
            return "Error: no solver";
        }
        if(myLimb.getEndEffector()==null) {
            return ( "Error: no end effector" );
        }
        double [] cartesian = getCartesianFromWorld(myLimb.getEndEffector().getWorld());
        int i=0;
        String response = "G1"
                +" F"+StringHelper.formatDouble(mySolver.getLinearVelocity())
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
     * @return the target pose or null if not set.
     */
    public Pose getTarget() {
        LimbSolver solver = getSolver();
        return solver == null ? null : solver.getTarget();
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

    /**
     * Set the limb to be controlled by this instance.
     * limb must be in the same node tree as this instance.
     * @param limb the limb to control
     */
    public void setLimb(Limb limb) {
        this.limb.setRelativePath(this,limb);
    }

    /**
     * Set the solver to be used by this instance.
     * solver must be in the same node tree as this instance.
     * @param solver the solver to use
     */
    public void setSolver(LimbSolver solver) {
        this.solver.setRelativePath(this, solver);
    }
}
