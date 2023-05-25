package com.marginallyclever.robotoverlord.systems.robot;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RobotGripperComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementComboBox;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.RayPickSystem;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.Comparator;
import java.util.List;

public class RobotGripperSystem implements EntitySystem {
    private final EntityManager entityManager;

    public RobotGripperSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof RobotGripperComponent) decorateGripper(view,component);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {}

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

    public void doGrab(RobotGripperComponent gripper) {
        List<Point3d> points = gripper.getPoints();
        if (points.size()!=2) return;

        Point3d p0 = points.get(0);
        Point3d p1 = points.get(1);
        Vector3d diff = new Vector3d(p1);
        diff.sub(p0);
        diff.normalize();

        // cast a ray between the two points
        Ray ray = new Ray(p0,diff,gripper.openDistance.get());
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

        // close the gripper
        // TODO until it touches the object.
        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());
        Vector3d dMove = new Vector3d(diff);
        dMove.scale(distance / 2.0);
        moveJaws(gripper, dMove);

        // remember grip direction for later
        Matrix4d gripperWorld = gripper.getEntity().getComponent(PoseComponent.class).getWorld();
        gripperWorld.setTranslation(new Vector3d());
        gripperWorld.invert();
        gripperWorld.transform(diff); // diff now in local space.
        gripper.setGripDirection(diff);
        System.out.println("grab="+diff);
    }

    public void doRelease(RobotGripperComponent gripper) {
        // release the object
        List<Entity> children = gripper.getEntity().getChildren();
        // assumes two jaws and only one item being held.
        if(children.size()!=3) return;
        Entity entityBeingGrabbed = children.get(2);
        // move the entity to the world
        Matrix4d entityWorld = entityBeingGrabbed.getComponent(PoseComponent.class).getWorld();
        entityManager.addEntityToParent(entityBeingGrabbed,entityManager.getRoot());
        entityBeingGrabbed.getComponent(PoseComponent.class).setWorld(entityWorld);

        // change state to "open"
        gripper.mode.set(RobotGripperComponent.MODE_OPEN);

        // open the gripper
        Vector3d diff = gripper.getGripDirection();  // diff now in local space.
        Matrix4d gripperWorld = gripper.getEntity().getComponent(PoseComponent.class).getWorld();
        gripperWorld.setTranslation(new Vector3d());
        gripperWorld.transform(diff);  // diff now in world space.

        double distance = (gripper.openDistance.get() - gripper.closeDistance.get());
        diff.scale(-distance / 2.0);
        moveJaws(gripper, diff);
        System.out.println("release="+diff);
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
