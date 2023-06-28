package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.util.*;
import java.util.function.Consumer;

/**
 * Used to sort items prior to rendering.  lists are opaque, alpha, no material, and finally items that must be on top.
 * @author Dan Royer
 * @since 2.7.1
 */
public class MatrixMaterialRenderSet {
    public final List<MatrixMaterialRender> opaque = new ArrayList<>();
    public final List<MatrixMaterialRender> alpha = new ArrayList<>();
    public final List<MatrixMaterialRender> noMaterial = new ArrayList<>();
    public final List<MatrixMaterialRender> onTop = new ArrayList<>();

    public MatrixMaterialRenderSet(List<Entity> list) {
        super();

        // collect all entities with a RenderComponent
        Queue<Entity> toRender = new LinkedList<>(list);
        while(!toRender.isEmpty()) {
            Entity entity = toRender.remove();
            toRender.addAll(entity.getChildren());

            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if(renderComponent==null) continue;

            MatrixMaterialRender mmr = new MatrixMaterialRender();
            mmr.renderComponent = entity.getComponent(RenderComponent.class);
            mmr.materialComponent = entity.getComponent(MaterialComponent.class);
            PoseComponent pose = entity.getComponent(PoseComponent.class);
            if(pose!=null) mmr.matrix.set(pose.getWorld());

            if(mmr.materialComponent==null) noMaterial.add(mmr);
            else if(mmr.materialComponent.drawOnTop.get()) onTop.add(mmr);
            else if(mmr.materialComponent.isAlpha()) alpha.add(mmr);
            else opaque.add(mmr);
        }
    }
}