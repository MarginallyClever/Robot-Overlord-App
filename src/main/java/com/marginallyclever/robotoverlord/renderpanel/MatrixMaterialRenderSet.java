package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import javax.vecmath.Vector3d;
import javax.vecmath.Matrix4d;
import java.util.*;

/**
 * a {@link MatrixMaterialRenderSet} is a set of lists of {@link MatrixMaterialRender} used to sort items prior to
 * rendering.  The lists are opaque, alpha, no material, and finally items that must be on top.
 * @author Dan Royer
 * @since 2.7.1
 */
public class MatrixMaterialRenderSet {
    // background is always first
    public final List<MatrixMaterialRender> onBottom = new ArrayList<>();
    public final List<MatrixMaterialRender> opaque = new ArrayList<>();
    public final List<MatrixMaterialRender> alpha = new ArrayList<>();
    public final List<MatrixMaterialRender> noMaterial = new ArrayList<>();
    public final List<MatrixMaterialRender> onTop = new ArrayList<>();

    public MatrixMaterialRenderSet(List<Entity> list) {
        super();

        Set<RenderComponent> alreadyAdded = new HashSet<>();

        // collect all entities with a RenderComponent
        Queue<Entity> toRender = new LinkedList<>(list);
        while(!toRender.isEmpty()) {
            Entity entity = toRender.remove();

            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if(renderComponent==null) continue;
            if(alreadyAdded.contains(renderComponent)) continue;
            alreadyAdded.add(renderComponent);

            PoseComponent pose = entity.getComponent(PoseComponent.class);
            Matrix4d m = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
            MatrixMaterialRender mmr = new MatrixMaterialRender(m,
                    entity.getComponent(RenderComponent.class),
                    entity.getComponent(MaterialComponent.class));

            if(mmr.materialComponent==null) noMaterial.add(mmr);
            else if(mmr.materialComponent.drawOnBottom.get()) onBottom.add(mmr);
            else if(mmr.materialComponent.drawOnTop.get()) onTop.add(mmr);
            else if(mmr.materialComponent.isAlpha()) alpha.add(mmr);
            else opaque.add(mmr);
        }
    }

    public void sortAlpha(Vector3d cameraPoint) {
        Vector3d p1 = new Vector3d();
        Vector3d p2 = new Vector3d();
        alpha.sort((o1, o2) -> {
            o1.matrix.get(p1);
            o2.matrix.get(p2);
            p1.sub(cameraPoint);
            p2.sub(cameraPoint);
            double d1 = p1.lengthSquared();
            double d2 = p2.lengthSquared();
            return (int) Math.signum(d2 - d1);
        });
    }
}