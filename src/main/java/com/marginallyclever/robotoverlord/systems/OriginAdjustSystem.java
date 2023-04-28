package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.OriginAdjustComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

public class OriginAdjustSystem implements ROSystem {
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

    private void decorateOriginAdjust(ComponentPanelFactory view, Component component) {
        OriginAdjustComponent adj = (OriginAdjustComponent)component;
        view.addButton("Adjust me").addActionEventListener( e -> adjustOne(component.getEntity()) );
    }

    public void adjustOne(Entity entity) {
        PoseComponent myPose = entity.findFirstComponent(PoseComponent.class);
        PoseComponent parentPose = entity.findFirstComponentInParents(PoseComponent.class);
        if(myPose==null || parentPose==null) return;
        Matrix4d parentMat = parentPose.getWorld();
        Matrix4d mat = myPose.getWorld();
        parentMat.invert();
        myPose.setLocalMatrix4(parentMat);
    }
    public void adjustEntireTree(Entity root) {
        List<Entity> toProcess = new ArrayList<>();
        toProcess.add(root);
        while(!toProcess.isEmpty()) {
            Entity entity = toProcess.remove(0);
            toProcess.addAll(entity.getChildren());

            OriginAdjustComponent adj = entity.findFirstComponent(OriginAdjustComponent.class);
            if(adj!=null) adjustOne(entity);
        }
    }
}
