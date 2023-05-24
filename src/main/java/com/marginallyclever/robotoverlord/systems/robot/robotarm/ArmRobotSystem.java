package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementColor;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementComboBox;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.RayPickSystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.ControlArmPanel;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.Comparator;
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
        if( component instanceof RobotGripperComponent ) decorateGripper(view,component);
    }

    private void decorateGripper(ComponentPanelFactory view, Component component) {
        RobotGripperComponent gripper = (RobotGripperComponent)component;

        view.add(gripper.openDistance);
        view.add(gripper.closeDistance);

        ViewElementComboBox box = (ViewElementComboBox)view.addComboBox(gripper.mode,RobotGripperComponent.names);
        box.setReadOnly(true);

        ViewElementButton bToggleGripper = view.addButton("Grab");
        bToggleGripper.addActionEventListener((evt)-> {
            switch(gripper.mode.get()) {
                case RobotGripperComponent.MODE_OPEN -> doGrab(gripper);
                case RobotGripperComponent.MODE_CLOSED -> doRelease(gripper);
                default -> {}
            }
        });
        gripper.mode.addPropertyChangeListener( e->setGripperButton(bToggleGripper,gripper) );
        setGripperButton(bToggleGripper,gripper);
    }

    private void setGripperButton(ViewElementButton bToggleGripper,RobotGripperComponent gripper) {
        if(gripper.mode.get() == RobotGripperComponent.MODE_OPEN) {
            bToggleGripper.setText("Grab");
            bToggleGripper.setEnabled(true);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_CLOSED) {
            bToggleGripper.setText("Release");
            bToggleGripper.setEnabled(true);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_OPENING) {
            bToggleGripper.setText("Opening");
            bToggleGripper.setEnabled(false);
        } else if(gripper.mode.get() == RobotGripperComponent.MODE_CLOSING) {
            bToggleGripper.setText("Closing");
            bToggleGripper.setEnabled(false);
        }
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
        ApproximateJacobian aj = new ApproximateJacobianFiniteDifferences(robotComponent);
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

    public void doGrab(RobotGripperComponent gripper) {
        List<Vector3d> points = gripper.getPoints();
        if (points.size()!=2) return;

        Vector3d p0 = points.get(0);
        Vector3d p1 = points.get(1);
        Vector3d diff = new Vector3d(p1);
        diff.sub(p0);
        diff.normalize();

        // cast a ray between the two points
        Ray ray = new Ray();
        ray.getOrigin().set(p0);
        ray.getDirection().set(diff);
        RayPickSystem picker = new RayPickSystem(entityManager);
        List<RayHit> hits = picker.findRayIntersections(ray);

        // do not consider the gripper itself or the jaws.
        removeGripperAndJawsFromHits(gripper, hits);

        if (hits.isEmpty()) return;  // no hit, nothing to grab

        // get the nearest item
        hits.sort(Comparator.comparingDouble(o -> o.distance));
        RayHit first = hits.get(0);

        //if object found is outside the gripper's distance, no grab.
        if (first.distance > gripper.openDistance.get()) return;

        // move the entity to the gripper
        Entity entityBeingGrabbed = first.target.getEntity();
        Matrix4d entityWorld = entityBeingGrabbed.getComponent(PoseComponent.class).getWorld();
        entityManager.addEntityToParent(entityBeingGrabbed, gripper.getEntity());
        entityBeingGrabbed.getComponent(PoseComponent.class).setWorld(entityWorld);

        // change state to "closed"
        gripper.mode.set(RobotGripperComponent.MODE_CLOSED);

        // TODO close the gripper until it touches the object.
        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());
        diff.scale(distance / 2.0);
        moveJaws(gripper, diff);
    }

    public void doRelease(RobotGripperComponent gripper) {
        List<Vector3d> points = gripper.getPoints();
        if (points.size()!=2) return;

        // release the object
        List<Entity> children = gripper.getEntity().getChildren();
        // assumes two jaws and then the thing being held.
        if(children.size()!=3) return;
        Entity entityBeingGrabbed = children.get(2);
        // move the entity to the world
        Matrix4d entityWorld = entityBeingGrabbed.getComponent(PoseComponent.class).getWorld();
        entityManager.addEntityToParent(entityBeingGrabbed,entityManager.getRoot());
        entityBeingGrabbed.getComponent(PoseComponent.class).setWorld(entityWorld);

        // change state to "open"
        gripper.mode.set(RobotGripperComponent.MODE_OPEN);

        // TODO open the gripper all the way
        Vector3d p0 = points.get(0);
        Vector3d p1 = points.get(1);
        Vector3d diff = new Vector3d(p1);
        diff.sub(p0);
        diff.normalize();
        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());
        diff.scale(-distance / 2.0);
        moveJaws(gripper, diff);
    }

    private void moveJaws(RobotGripperComponent gripper, Vector3d diff) {
        List<ShapeComponent> jaws = gripper.getJaws();
        moveOneJaw(jaws.get(0).getEntity(), diff);
        diff.negate();
        moveOneJaw(jaws.get(1).getEntity(), diff);
    }

    private void moveOneJaw(Entity jaw, Vector3d diff) {
        PoseComponent pose = jaw.getComponent(PoseComponent.class);
        Matrix4d m = pose.getWorld();
        Vector3d p = MatrixHelper.getPosition(m);
        p.add(diff);
        m.setTranslation(p);
        pose.setWorld(m);
    }

    private void removeGripperAndJawsFromHits(RobotGripperComponent gripper, List<RayHit> hits) {
        List<ShapeComponent> jaws = gripper.getJaws();
        ShapeComponent gripperBody = gripper.getEntity().getComponent(ShapeComponent.class);
        hits.removeIf(hit -> jaws.contains(hit.target) || hit.target == gripperBody);
    }
}
