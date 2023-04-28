package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

public class CameraSystem implements ROSystem {
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if (component instanceof CameraComponent) decorateCamera(view,component);
    }

    private void decorateCamera(ComponentPanelFactory view,Component component) {
        CameraComponent camera = (CameraComponent)component;
        view.add(camera.orbitDistance).setReadOnly(true);
        view.add(camera.pan).setReadOnly(true);
        view.add(camera.tilt).setReadOnly(true);
    }
}
