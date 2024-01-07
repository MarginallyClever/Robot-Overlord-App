package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.ApproximateJacobian;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.ApproximateJacobianFiniteDifferences;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

/**
 * {@link LimbSolver} calculates <a href="https://en.wikipedia.org/wiki/Inverse_kinematics">Inverse Kinematics</a> for
 * a {@link Limb}.  Given a target and a linear velocity, {@link LimbSolver} will calculate and apply the
 * joint velocities required to move the end effector towards the target in a straight line.
 */
public class LimbSolver extends Node {
    private static final Logger logger = LoggerFactory.getLogger(LimbSolver.class);

    private final NodePath<Limb> limb = new NodePath<>(this,Limb.class);
    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);
    private double linearVelocity = 0;

    public LimbSolver() {
        this("LimbSolver");
    }

    public LimbSolver(String name) {
        super(name);
    }

    public Pose getTarget() {
        return target.getSubject();
    }

    public void setTarget(Pose target) {
        this.target.setRelativePath(this,target);
    }

    public void update(double dt) {
        if(dt==0) return;
        moveTowardsTarget();
    }

    public Limb getLimb() {
        return limb.getSubject();
    }

    public void setLimb(Limb limb) {
        this.limb.setRelativePath(this,limb);
    }

    private Pose getEndEffector() {
        Limb limb = getLimb();
        return limb!=null ? limb.getEndEffector() : null;
    }

    private void moveTowardsTarget() {
        if(getLimb()==null || getEndEffector()==null || getTarget()==null ) {
            // no limb, no end effector, or no target.  Do nothing.
            return;
        }
        if(linearVelocity<0.0001) {
            // no velocity.  Make sure the arm doesn't drift.
            getLimb().setAllJointVelocities(new double[getLimb().getNumJoints()]);
            return;
        }
        double[] cartesianVelocity = MatrixHelper.getCartesianBetweenTwoMatrices(
                getEndEffector().getWorld(),
                getTarget().getWorld());
        scaleVectorToMagnitude(cartesianVelocity,linearVelocity);
        moveEndEffectorInCartesianDirection(cartesianVelocity);
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
        addLabelAndComponent(pane, "Target to EE", targetToEE,gbc);
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
        addNodeSelector(pane, "Target", target, Pose.class, gbc);

        gbc.gridy++;
        gbc.gridwidth=1;
        addNodeSelector(pane, "Limb", limb, Limb.class, gbc);

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
}
