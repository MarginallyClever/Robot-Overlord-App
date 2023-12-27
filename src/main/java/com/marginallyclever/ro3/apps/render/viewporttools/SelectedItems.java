package com.marginallyclever.ro3.apps.render.viewporttools;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.Node;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of {@link Node}s selected in the {@link com.marginallyclever.ro3.apps.render.Viewport}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class SelectedItems {
    /**
     * The list of entities selected in the editorpanel.
     */
    private final List<Node> entities = new ArrayList<>();

    /**
     * The world pose of each node in the selection at the moment it was selected.
     */
    private final Map<Node, Matrix4d> entityWorldPoses = new HashMap<>();

    public SelectedItems() {
        super();
    }

    public SelectedItems(List<Node> list) {
        super();

        for(Node e : list) {
            addEntity(e);
        }
    }

    public void addEntity(Node node) {
        entities.add(node);
        setEntityWorldPose(node);
    }

    private void setEntityWorldPose(Node node) {
        if(node instanceof Pose pose) {
            entityWorldPoses.put(node, pose.getWorld());
        }
    }

    public void removeEntity(Node node) {
        entities.remove(node);
        entityWorldPoses.remove(node);
    }

    public List<Node> getEntities() {
        return entities;
    }

    public Matrix4d getWorldPoseAtStart(Node node) {
        return entityWorldPoses.get(node);
    }

    public Matrix4d getWorldPoseNow(Node node) {
        if(node instanceof Pose pose) {
            return pose.getWorld();
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
        for(Node e : entities) {
            setEntityWorldPose(e);
        }
    }
}
