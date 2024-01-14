package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.limbsolver.ApproximateJacobianFiniteDifferences;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

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
        if(limb.getSubject()!=null) json.put("limb",limb.getUniqueID());
        if(solver.getSubject()!=null) json.put("solver",solver.getUniqueID());
        if(gripperMotor.getSubject()!=null) json.put("gripperMotor",gripperMotor.getUniqueID());
        json.put("reportInterval",reportInterval);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if(version==2) {
            if(from.has("limb")) {
                limb.setUniqueID(from.getString("limb"));
            }
            if(from.has("solver")) {
                solver.setUniqueID(from.getString("solver"));
            }
            if(from.has("gripperMotor")) {
                gripperMotor.setUniqueID(from.getString("gripperMotor"));
            }
            if(from.has("reportInterval")) {
                reportInterval = from.getDouble("reportInterval");
            }
        }
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
            limb.setUniqueIDByNode(limb1);

            // solver
            LimbSolver solver1 = new LimbSolver();
            solver1.fromJSON(from);
            solver1.getChildren().stream().filter(n -> !n.getName().equals("target")).forEach(toRemove::add);
            for(Node n : toRemove) solver1.removeChild(n);
            solver1.setName("LimbSolver");
            getParent().addChild(solver1);
            solver.setUniqueIDByNode(solver1);
            solver1.setLimb(limb1);

            // gripper
            if (from.has("gripperMotor")) {
                String s = from.getString("gripperMotor");
                if (version == 1) {
                    Motor m = this.findNodeByPath(s, Motor.class);
                    gripperMotor.setUniqueIDByNode(m);
                } else if (version == 0) {
                    gripperMotor.setUniqueID(s);
                }
            }

            limb.setUniqueIDByNode(limb1);
            solver.setUniqueIDByNode(solver1);
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MarlinRobotArmPanel(this));
        super.getComponents(list);
    }

    /**
     * Build a string from the current angle of each motor hinge, aka the
     * <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a> of the robot arm.
     * @return GCode command
     */
    private String getM114() {
        return "M114"+getMotorsAndFeedrateAsString();
    }

    public NodePath<Limb> getLimb() {
        return limb;
    }

    public NodePath<LimbSolver> getSolver() {
        return solver;
    }

    private String getMotorsAndFeedrateAsString() {
        var myLimb = limb.getSubject();
        if(myLimb==null) return "";
        StringBuilder sb = new StringBuilder();
        for(NodePath<Motor> paths : myLimb.getMotors()) {
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

        if(getSolver()!=null) {
            // feedrate
            sb.append(" F")
              .append(StringHelper.formatDouble(getSolver().getSubject().getLinearVelocity()));
        }

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
            ApproximateJacobianFiniteDifferences jacobian = new ApproximateJacobianFiniteDifferences(getLimb().getSubject());
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
            for (NodePath<Motor> paths : getLimb().getSubject().getMotors()) {
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
        Limb myLimb = getLimb().getSubject();
        if(myLimb==null) {
            logger.warn("no limb");
            return "Error: no limb";
        }
        LimbSolver mySolver = getSolver().getSubject();
        if(mySolver==null) {
            logger.warn("no solver");
            return "Error: no solver";
        }
        if(mySolver.getTarget().getSubject()==null) {
            logger.warn("no target");
            return "Error: no target";
        }
        if(myLimb.getEndEffector().getSubject()==null) {
            logger.warn("no end effector");
            return "Error: no end effector";
        }
        String [] parts = gcode.split("\\s+");
        double [] cartesian = getCartesianFromWorld(myLimb.getEndEffector().getSubject().getWorld());
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
        mySolver.getTarget().getSubject().setLocal(getReverseCartesianFromWorld(cartesian));
        return "Ok";
    }

    private String getEndEffectorIK() {
        Limb myLimb = getLimb().getSubject();
        if(myLimb==null) {
            logger.warn("no limb");
            return "Error: no limb";
        }
        LimbSolver mySolver = getSolver().getSubject();
        if(mySolver==null) {
            logger.warn("no solver");
            return "Error: no solver";
        }
        if(myLimb.getEndEffector()==null) {
            return ( "Error: no end effector" );
        }
        double [] cartesian = getCartesianFromWorld(myLimb.getEndEffector().getSubject().getWorld());
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
        local.set(MatrixHelper.eulerToMatrix(rot, MatrixHelper.EulerSequence.YXZ));
        local.setTranslation(new Vector3d(cartesian[0],cartesian[1],cartesian[2]));
        return local;
    }

    /**
     * @param world the matrix to convert
     * @return the XYZ translation and UVW rotation of the given matrix.  UVW is in degrees.
     */
    private double[] getCartesianFromWorld(Matrix4d world) {
        Vector3d rotate = MatrixHelper.matrixToEuler(world, MatrixHelper.EulerSequence.YXZ);
        rotate.scale(180/Math.PI);
        Vector3d translate = new Vector3d();
        world.get(translate);
        return new double[] {translate.x,translate.y,translate.z,rotate.x,rotate.y,rotate.z};
    }

    /**
     * @return the target pose or null if not set.
     */
    public Pose getTarget() {
        LimbSolver solver = getSolver().getSubject();
        return solver == null ? null : solver.getTarget().getSubject();
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
        this.limb.setUniqueIDByNode(limb);
    }

    /**
     * Set the solver to be used by this instance.
     * solver must be in the same node tree as this instance.
     * @param solver the solver to use
     */
    public void setSolver(LimbSolver solver) {
        this.solver.setUniqueIDByNode(solver);
    }

    public double getReportInterval() {
        return reportInterval;
    }

    public void setReportInterval(double seconds) {
        reportInterval = Math.max(0.1,seconds);
    }

    public NodePath<Motor> getGripperMotor() {
        return gripperMotor;
    }
}