package com.marginallyclever.robotoverlord.systems.robot.wheel;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.WheeledRobotComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 * A system to manage robot crabs.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class WheeledRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public WheeledRobotSystem(EntityManager entityManager) {
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
        if (component instanceof WheeledRobotComponent)
            decorateWheeled(view, component);
    }

    public void decorateWheeled(ComponentPanelFactory view, Component component) {
        WheeledRobotComponent robot = (WheeledRobotComponent) component;

        ViewElementButton bMake = view.addButton("Edit Wheeled Robot");
        // bMake.addActionEventListener((evt) -> makeCrab(bMake, crab, "Edit Wheeled
        // Robot"));
    }

    @Override
    public void update(double dt) {

        List<Entity> list = new LinkedList<>(entityManager.getEntities());

        while (!list.isEmpty()) {
            Entity e = list.remove(0);
            WheeledRobotComponent crab = e.getComponent(WheeledRobotComponent.class);
            if (crab != null) {
            //    updateRobot(crab, dt);
            }

            list.addAll(e.getChildren());
        }
    }

}
