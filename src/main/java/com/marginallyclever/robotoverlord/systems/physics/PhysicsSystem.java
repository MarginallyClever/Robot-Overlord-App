package com.marginallyclever.robotoverlord.systems.physics;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;

/**
 * Decorates {@link PoseComponent} with a position, rotation, and scale.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class PhysicsSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if(component instanceof PoseComponent) decoratePose(view,component);
    }

    private void decoratePose(ComponentSwingViewFactory view, Component component) {
        PoseComponent pose = (PoseComponent)component;
        view.add(pose.position);
        view.add(pose.rotation);
        //view.add(pose.scale);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}
}
