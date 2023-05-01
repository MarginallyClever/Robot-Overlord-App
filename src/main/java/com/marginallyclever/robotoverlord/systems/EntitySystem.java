package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

/**
 * {@link EntitySystem} are Systems in an Entity-Component-System pattern.
 * They are equivalent to Controllers in a Model-View-Controller pattern.
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
    void decorate(ComponentPanelFactory view, Component component);

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    void update(double dt);
}
