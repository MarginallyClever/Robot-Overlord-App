package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of entities selected in the editor.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class SelectedItems {
    /**
     * The list of entities selected in the editor.
     */
    private final List<Entity> entities = new ArrayList<>();

    /**
     * The world pose of each entity in the selection at the moment it was selected.
     */
    private final Map<Entity, Matrix4d> entityWorldPoses = new HashMap<>();

    public SelectedItems() {
        super();
    }

    public SelectedItems(List<Entity> list) {
        super();

        for(Entity e : list) {
            addEntity(e);
        }
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        setEntityWorldPose(entity);
    }

    private void setEntityWorldPose(Entity entity) {
        PoseComponent poseComponent = entity.getComponent(PoseComponent.class);
        if (poseComponent != null) {
            entityWorldPoses.put(entity, poseComponent.getWorld());
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        entityWorldPoses.remove(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Matrix4d getWorldPoseAtStart(Entity entity) {
        return entityWorldPoses.get(entity);
    }

    public Matrix4d getWorldPoseNow(Entity entity) {
        PoseComponent poseComponent = entity.getComponent(PoseComponent.class);
        if (poseComponent != null) {
            return poseComponent.getWorld();
        }
        return MatrixHelper.createIdentityMatrix4();
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    public void clear() {
        entities.clear();
        entityWorldPoses.clear();
    }

    public void savePose() {
        for(Entity e : entities) {
            setEntityWorldPose(e);
        }
    }
}
