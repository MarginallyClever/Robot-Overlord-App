package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.OriginAdjustComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This system adds a button to the component manager panel that allows the user to adjust the origin of a
 * {@link Component}.  The model is assumed to have an origin at the center of the universe.  Given an entity
 * heirarchy</p>
 *
 * <pre>Entity A > Entity Mesh > ShapeComponent</pre>
 *
 * <p>This system will adjust the Mesh's {@link PoseComponent} to compensate for Entity A's {@link PoseComponent}.</p>
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class OriginAdjustSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if(component instanceof OriginAdjustComponent) decorateOriginAdjust(view, component);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}

    private void decorateOriginAdjust(ComponentSwingViewFactory view, Component component) {
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
