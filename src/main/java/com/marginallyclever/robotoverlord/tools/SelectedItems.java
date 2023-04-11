package com.marginallyclever.robotoverlord.tools;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        PoseComponent poseComponent = entity.findFirstComponent(PoseComponent.class);
        if (poseComponent != null) {
            entities.add(entity);
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

    public Matrix4d getWorldPose(Entity entity) {
        return entityWorldPoses.get(entity);
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    public void clear() {
        entities.clear();
        entityWorldPoses.clear();
    }
}
