package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;

/**
 * {@link EntitySystem} are Systems in an <a href="https://en.wikipedia.org/wiki/Entity_component_system">
 *     Entity-Component-System</a> pattern.
 * They are equivalent to Controllers in a <a href="https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">
 *     Model-View-Controller</a> pattern.
 * They are responsible for the logic of one or more {@link Component}s.
 *
 * @author Dan Royer
 * @since 2.5.3
 */
public interface EntitySystem {
    /**
     * Get the Swing view of this component.
     * @param view the factory to use to create the panel
     * @param component the component to visualize
     */
    void decorate(ComponentSwingViewFactory view, Component component);

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    void update(double dt);
}
