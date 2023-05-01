package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.OriginAdjustComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

public class OriginAdjustSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if(component instanceof OriginAdjustComponent) decorateOriginAdjust(view, component);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}

    private void decorateOriginAdjust(ComponentPanelFactory view, Component component) {
        OriginAdjustComponent adj = (OriginAdjustComponent)component;
        view.addButton("Adjust me").addActionEventListener( e -> OriginAdjustSystem.adjustOne(component.getEntity()) );
    }

    public static void adjustOne(Entity entity) {
        PoseComponent myPose = entity.getComponent(PoseComponent.class);
        PoseComponent parentPose = entity.findFirstComponentInParents(PoseComponent.class);
        if(myPose==null || parentPose==null) return;
        Matrix4d parentMat = parentPose.getWorld();
        Matrix4d mat = myPose.getWorld();
        parentMat.invert();
        myPose.setLocalMatrix4(parentMat);
    }
    public static void adjustEntireTree(Entity root) {
        List<Entity> toProcess = new ArrayList<>();
        toProcess.add(root);
        while(!toProcess.isEmpty()) {
            Entity entity = toProcess.remove(0);
            toProcess.addAll(entity.getChildren());

            OriginAdjustComponent adj = entity.getComponent(OriginAdjustComponent.class);
            if(adj!=null) adjustOne(entity);
        }
    }
}
