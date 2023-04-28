package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

public class PhysicsSystem implements ROSystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if(component instanceof PoseComponent) decoratePose(view,component);
    }

    private void decoratePose(ComponentPanelFactory view, Component component) {
        PoseComponent pose = (PoseComponent)component;
        view.add(pose.position);
        view.add(pose.rotation);
        view.add(pose.scale);
    }
}
