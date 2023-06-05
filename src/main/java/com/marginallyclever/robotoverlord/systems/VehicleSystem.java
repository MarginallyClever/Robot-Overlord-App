package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;

import java.util.LinkedList;
import java.util.List;

/**
 * A system that manages all vehicles.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class VehicleSystem implements EntitySystem {
    private final EntityManager entityManager;

    public VehicleSystem(EntityManager entityManager) {
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
        if(component instanceof CarComponent) decorateCar(view, (CarComponent)component);
        if(component instanceof WheelComponent) decorateWheel(view, (WheelComponent)component);
    }

    private void decorateCar(ComponentPanelFactory view, CarComponent component) {
        // TODO: how to manage a list?

        ViewElementButton bDrive = view.addButton("Drive");
        bDrive.addActionEventListener(evt -> {
            // TODO: open panel to drive the car
        });
    }

    private void decorateWheel(ComponentPanelFactory view, WheelComponent component) {
        view.addComboBox(component.type, WheelComponent.wheelTypeNames);
        view.add(component.diameter);
        view.add(component.width);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while (!list.isEmpty()) {
            Entity e = list.remove(0);
            list.addAll(e.getChildren());

            CarComponent found = e.getComponent(CarComponent.class);
            if (found != null) updateCar(found, dt);
        }
    }

    private void updateCar(CarComponent found, double dt) {
        if(found.wheels.size()==0) return;  // nothing to do


    }
}
