package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.GCodePathComponent;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.ControlArmPanel;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.LinkedList;
import java.util.List;

/**
 * A system to manage robot arms.
 *
 * @author Dan Royer
 * @since 2.5.5
 */
public class ArmRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public ArmRobotSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof RobotComponent ) decorateRobot(view,component);
        if( component instanceof DHComponent ) decorateDH(view,component);
    }

    private void decorateDH(ComponentPanelFactory view, Component component) {
        DHComponent dh = (DHComponent)component;
        view.add(dh.isRevolute).setReadOnly(true);
        view.add(dh.myD).setReadOnly(true);
        view.add(dh.myR).setReadOnly(true);
        view.add(dh.alpha).setReadOnly(true);
        view.add(dh.theta).setReadOnly(true);
        view.add(dh.jointMax).setReadOnly(true);
        view.add(dh.jointMin).setReadOnly(true);
        view.add(dh.jointHome).setReadOnly(true);
    }

    private void decorateRobot(ComponentPanelFactory view, Component component) {
        RobotComponent robotComponent = (RobotComponent)component;

        view.add(robotComponent.desiredLinearVelocity);
        view.add(robotComponent.gcodePath);

        ViewElementButton bMake = view.addButton("Edit Arm");
        bMake.addActionEventListener((evt)-> makeRobotArm6(bMake,robotComponent,"Edit Arm"));

        ViewElementButton bOpenJog = view.addButton(Translator.get("RobotROSystem.controlPanel"));
        bOpenJog.addActionEventListener((evt)-> showControlPanel(bOpenJog,robotComponent));

        ViewElementButton bHome = view.addButton("Go home");
        bHome.addActionEventListener((evt)-> robotComponent.goHome());
    }

    private void makeRobotArm6(JComponent parent, RobotComponent robotComponent,String title) {
        EntitySystemUtils.makePanel(new EditArmPanel(robotComponent.getEntity(), entityManager), parent,title);
    }

    private void showControlPanel(JComponent parent,RobotComponent robotComponent) {
        try {
            EntitySystemUtils.makePanel(new ControlArmPanel(robotComponent, getGCodePath(robotComponent)), parent, Translator.get("RobotROSystem.controlPanel"));
        } catch (Exception e) {
            e.printStackTrace();
            // display error message in a dialog
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(parent),
                    "Failed to open window.  Is robot initialized?",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private GCodePathComponent getGCodePath(RobotComponent robotComponent) {
        Entity entity = entityManager.findEntityByUniqueID(robotComponent.getGCodePathEntityUUID());
        if(entity==null) return null;
        return entity.getComponent(GCodePathComponent.class);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while (!list.isEmpty()) {
            Entity e = list.remove(0);
            list.addAll(e.getChildren());

            RobotComponent found = e.getComponent(RobotComponent.class);
            if (found != null) updateRobotComponent(found, dt);
        }
    }

    private void updateRobotComponent(RobotComponent robotComponent, double dt) {
        Matrix4d startPose = (Matrix4d)robotComponent.get(Robot.END_EFFECTOR);
        Matrix4d targetPose = (Matrix4d)robotComponent.get(Robot.END_EFFECTOR_TARGET);
        if(startPose==null || targetPose==null) return;

        double[] cartesianVelocity = MatrixHelper.getCartesianBetweenTwoMatrices(startPose, targetPose);

        // adjust for desired linear speed
        double linearVelocity = (double)robotComponent.get(Robot.DESIRED_LINEAR_VELOCITY);
        capVectorToMagnitude(cartesianVelocity,linearVelocity*dt);

        // push the robot
        applyCartesianForceToEndEffector(robotComponent,cartesianVelocity);
    }

    private double sumCartesianVelocityComponents(double [] cartesianVelocity) {
        double sum = 0;
        for (double v : cartesianVelocity) {
            sum += Math.abs(v);
        }
        return sum;
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    public void applyCartesianForceToEndEffector(RobotComponent robotComponent,double[] cartesianVelocity) {
        double sum = sumCartesianVelocityComponents(cartesianVelocity);
        if(sum<0.0001) return;
        if(sum <= 1) {
            applySmallCartesianForceToEndEffector(robotComponent,cartesianVelocity);
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
            applySmallCartesianForceToEndEffector(robotComponent,cartesianVelocityUnit);
        }
    }

    /**
     * Applies a cartesian force to the robot, moving it in the direction of the cartesian force.
     * @param cartesianVelocity three linear forces (mm) and three angular forces (degrees).
     * @throws RuntimeException if the robot cannot be moved in the direction of the cartesian force.
     */
    private void applySmallCartesianForceToEndEffector(RobotComponent robotComponent,double[] cartesianVelocity) {
        ApproximateJacobian aj = new ApproximateJacobianScrewTheory(robotComponent);
        try {
            double[] jointVelocity = aj.getJointForceFromCartesianForce(cartesianVelocity);  // uses inverse jacobian
            double[] angles = robotComponent.getAllJointValues();  // # dof long
            for (int i = 0; i < angles.length; ++i) {
                angles[i] += jointVelocity[i];
            }
            robotComponent.setAllJointValues(angles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}
