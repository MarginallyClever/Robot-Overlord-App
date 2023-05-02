package com.marginallyclever.robotoverlord.systems.robot.crab;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.robot.dog.EditDogPanel;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A system to manage robot crabs.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class CrabRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public CrabRobotSystem(EntityManager entityManager) {
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
        if( component instanceof CrabRobotComponent) decorateCrab(view,component);
    }

    public void decorateCrab(ComponentPanelFactory view,Component component) {
        CrabRobotComponent crab = (CrabRobotComponent)component;
        view.add(crab.standingRadius);
        view.add(crab.standingHeight);
        view.add(crab.turningStrideLength);
        view.add(crab.strideLength);
        view.add(crab.strideHeight);

        view.addComboBox(crab.modeSelector, CrabRobotComponent.MODE_NAMES);
        view.add(crab.speedScale);

        ViewElementButton bMake = view.addButton("Edit Crab");
        bMake.addActionEventListener((evt)-> makeCrab(bMake,crab,"Edit Crab"));
    }

    private void makeCrab(JComponent parent, CrabRobotComponent crab, String title) {
        EntitySystemUtils.makePanel(new EditCrabPanel(crab.getEntity(), entityManager), parent,title);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while(!list.isEmpty()) {
            Entity e = list.remove(0);
            CrabRobotComponent crab = e.getComponent(CrabRobotComponent.class);
            if( crab!=null ) crab.update(dt);
            list.addAll(e.getChildren());
        }
    }
}
