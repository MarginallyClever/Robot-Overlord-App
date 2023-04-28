package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

public interface ROSystem {
    /**
     * Get the Swing view of this component.
     * @param view the factory to use to create the panel
     * @param component the component to visualize
     */
    void decorate(ComponentPanelFactory view, Component component);
}
