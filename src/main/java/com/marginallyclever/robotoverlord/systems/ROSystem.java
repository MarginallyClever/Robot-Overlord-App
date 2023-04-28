package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

/**
 * ROSystems are Systems in an Entity-Component-System pattern.
 * They are equivalent to Controllers in a Model-View-Controller pattern.
 * They are responsible for the logic of a component.
 *
 * @author Dan Royer
 * @since 2.5.3
 */
public interface ROSystem {
    /**
     * Get the Swing view of this component.
     * @param view the factory to use to create the panel
     * @param component the component to visualize
     */
    void decorate(ComponentPanelFactory view, Component component);
}
