package com.marginallyclever.robotoverlord.systems.robot;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;

public class DogRobotSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof DogRobotComponent) decorateDog(view,component);
    }

    public void decorateDog(ComponentPanelFactory view,Component component) {
        DogRobotComponent dog = (DogRobotComponent)component;
        view.add(dog.standingRadius);
        view.add(dog.standingHeight);
        view.add(dog.turningStrideLength);
        view.add(dog.strideLength);
        view.add(dog.strideHeight);

        view.addComboBox(dog.modeSelector, DogRobotComponent.MODE_NAMES);
        view.add(dog.speedScale);
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
