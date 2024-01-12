package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * {@link LimbSolver} calculates <a href="https://en.wikipedia.org/wiki/Inverse_kinematics">Inverse Kinematics</a> for
 * a {@link Limb}.  Given a target and a linear velocity, {@link LimbSolver} will calculate and apply the
 * joint velocities required to move the end effector towards the target in a straight line.  When the end effector
 * reaches the target (with a margin of error), {@link LimbSolver} will fire an ActionEvent "arrivedAtGoal".
 */
public class LimbSolver extends Node {
    private static final Logger logger = LoggerFactory.getLogger(LimbSolver.class);
    private final NodePath<Limb> limb = new NodePath<>(this,Limb.class);
    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);
    private double linearVelocity = 0;
    private double distanceToTarget = 0;

    private double goalMarginOfError = 0.1; // not degrees or mm.  Just a number.
    private final double[] cartesianDistance = new double[6];  // 3 linear, 3 angular
    private final double[] cartesianVelocity = new double[cartesianDistance.length];
    private boolean isAtGoal = false;

    public LimbSolver() {
        this("LimbSolver");
    }

    public LimbSolver(String name) {
        super(name);
    }

    public Pose getTarget() {
        return target.getSubject();
    }

    /**
     * Set the target to move towards.
     * target must be in the same node tree as this instance.
     * @param target the target to move towards
     */
    public void setTarget(Pose target) {
        this.target.setRelativePath(this,target);
    }

    public void update(double dt) {
        if(dt==0) return;

        moveTowardsTarget();

        if(areWeThereYet()) {
            if(!isAtGoal) {
                isAtGoal = true;
                fireArrivedAtGoal();
            }
        } else {
            isAtGoal = false;
        }
    }

    // are we there yet?
    private boolean areWeThereYet() {
        distanceToTarget = sumCartesianVelocityComponents(cartesianDistance);
        return distanceToTarget < goalMarginOfError;
    }

    public Limb getLimb() {
        return limb.getSubject();
    }

    /**
     * Set the limb to be controlled by this instance.
     * limb must be in the same node tree as this instance.
     * @param limb the limb to control
     */
    public void setLimb(Limb limb) {
        this.limb.setRelativePath(this,limb);
    }

    private Pose getEndEffector() {
        Limb limb = getLimb();
        return limb!=null ? limb.getEndEffector() : null;
    }

    /**
     * @return true if the solver has a limb, an end effector, and a target.  Does not guarantee that a solution exists.
     */
    public boolean readyToSolve() {
        return getLimb()!=null && getEndEffector()!=null && getTarget()!=null;
    }

    private void moveTowardsTarget() {
        if(!readyToSolve()) {
            return;
        }

        if(linearVelocity<0.0001) {
            // no velocity.  Make sure the arm doesn't drift.
            getLimb().setAllJointVelocities(new double[getLimb().getNumJoints()]);
            return;
        }
        // find direction to move
        MatrixHelper.getCartesianBetweenTwoMatrices(
                getEndEffector().getWorld(),
                getTarget().getWorld(),
                cartesianDistance);
        // theshold the velocity
        System.arraycopy(cartesianDistance,0,cartesianVelocity,0,cartesianDistance.length);
        scaleVectorToMagnitude(cartesianVelocity,linearVelocity);
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
        if(getLimb()==null || getLimb().getNumJoints()==0) return;

        ApproximateJacobian aj = getJacobian();
        try {
            double[] jointVelocity = aj.getJointFromCartesian(cartesianVelocity);  // uses inverse jacobian
            if(impossibleVelocity(jointVelocity)) return;  // TODO: throw exception instead?
            getLimb().setAllJointVelocities(jointVelocity);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private ApproximateJacobian getJacobian() {
        // option 1, use finite differences
        return new ApproximateJacobianFiniteDifferences(getLimb());
        // option 2, use screw theory
        //ApproximateJacobian aj = new ApproximateJacobianScrewTheory(getLimb());
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

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("version",2);

        if(getLimb()!=null) json.put("limb",limb.getPath());
        if(getTarget()!=null) json.put("target",target.getPath());
        json.put("linearVelocity",linearVelocity);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;

        Node root = this.getRootNode();

        if(from.has("target")) {
            String s = from.getString("target");
            if(version>0) {
                target.setPath(s);
            } else if(version==0) {
                Pose goal = root.findNodeByID(s,Pose.class);
                target.setRelativePath(this,goal);
            }
        }
        if(from.has("limb")) {
            String s = from.getString("limb");
            if(version>=2) {
                limb.setPath(s);
            } else {
                Limb limb = root.findNodeByID(s, Limb.class);
                this.limb.setRelativePath(this, limb);
            }
        }
        if(from.has("linearVelocity")) {
            linearVelocity = from.getDouble("linearVelocity");
        }
    }

    /**
     * <p>Make sure the given vector's length does not exceed maxLen.  It can be less than the given magnitude.
     * If the maxLen is greater than the vector length, the vector is unchanged.  This means as the limb approaches
     * the target the velocity will slow down.</p>
     * <p>Store the results in the original array.</p>
     * @param vector the vector to cap
     * @param maxLen the max length of the vector.
     */
    public static void scaleVectorToMagnitude(double[] vector, double maxLen) {
        if(maxLen<0) throw new IllegalArgumentException("maxLen must be >= 0");

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

    private double sumCartesianVelocityComponents(double [] cartesianVelocity) {
        double sum = 0;
        for (double v : cartesianVelocity) {
            sum += Math.abs(v);
        }
        return sum;
    }

    private JComponent createVelocitySlider() {
        JPanel container = new JPanel(new BorderLayout());
        // add a slider to control linear velocity
        JSlider slider = new JSlider(0,20,(int)linearVelocity);
        slider.addChangeListener(e-> linearVelocity = slider.getValue());

        // Make the slider fill the available horizontal space
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, slider.getPreferredSize().height));
        slider.setMinimumSize(new Dimension(50, slider.getPreferredSize().height));

        container.add(new JLabel("Linear Vel"), BorderLayout.LINE_START);
        container.add(slider, BorderLayout.CENTER); // Add slider to the center of the container

        return container;
    }

    private void setTargetToEndEffector() {
        if(getTarget()!=null && getEndEffector()!=null) {
            getTarget().setWorld(getEndEffector().getWorld());
        }
    }

    private void addMoveTargetToEndEffector(JPanel pane,GridBagConstraints gbc) {
        // move target to end effector
        JButton targetToEE = new JButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Move");
                putValue(Action.SHORT_DESCRIPTION,"Move the Target Pose to the End Effector.");
                putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/shared/icons8-move-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setTargetToEndEffector();
            }
        });
        NodePanelHelper.addLabelAndComponent(pane, "Target to EE", targetToEE,gbc);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(LimbSolver.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;

        gbc.gridwidth=2;
        pane.add(createVelocitySlider(),gbc);

        gbc.gridy++;
        gbc.gridwidth=1;
        addMoveTargetToEndEffector(pane,gbc);

        gbc.gridy++;
        NodePanelHelper.addNodeSelector(pane, "Target", target, Pose.class, gbc,this);

        gbc.gridy++;
        gbc.gridwidth=1;
        NodePanelHelper.addNodeSelector(pane, "Limb", limb, Limb.class, gbc,this);

        gbc.gridy++;
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        JFormattedTextField marginField = new JFormattedTextField(formatter);
        marginField.setValue(goalMarginOfError);
        marginField.addPropertyChangeListener("value", evt -> {
            goalMarginOfError = ((Number) marginField.getValue()).doubleValue();
        });
        NodePanelHelper.addLabelAndComponent(pane, "Goal Margin", marginField, gbc);

        super.getComponents(list);
    }

    public double getLinearVelocity() {
        return linearVelocity;
    }

    /**
     * Set the linear velocity of the end effector in cm/s.
     * @param linearVelocity must be >= 0
     */
    public void setLinearVelocity(double linearVelocity) {
        if(linearVelocity<0) throw new IllegalArgumentException("linearVelocity must be >= 0");
        this.linearVelocity = linearVelocity;
    }

    /**
     * @return the distance to the target that is a combination of linear and angular distances.
     */
    public double getDistanceToTarget() {
        return distanceToTarget;
    }

    /**
     * @return the distance to the target that is a combination of linear and angular distances.
     */
    public double getGoalMarginOfError() {
        return goalMarginOfError;
    }

    /**
     * @param goalMarginOfError the distance to the target that is a combination of linear and angular distances.
     */
    public void setGoalMarginOfError(double goalMarginOfError) {
        if(goalMarginOfError<0) throw new IllegalArgumentException("goalMarginOfError must be >= 0");
        this.goalMarginOfError = goalMarginOfError;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(ActionListener.class,listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(ActionListener.class,listener);
    }

    /**
     * Fire the "arrivedAtGoal" event to any {@link ActionListener} subscribed to this node.
     */
    private void fireArrivedAtGoal() {
        logger.debug("Arrived at goal.");
        ActionEvent e = new ActionEvent(this,0,"arrivedAtGoal");
        // Dispatch the event to the listeners
        for (ActionListener listener : listeners.getListeners(ActionListener.class)) {
            listener.actionPerformed(e);
        }
    }

    public void setIsAtGoal(boolean isAtGoal) {
        this.isAtGoal = isAtGoal;
    }
}
