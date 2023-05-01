package com.marginallyclever.robotoverlord.systems.robot;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;

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
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {

    }
}
