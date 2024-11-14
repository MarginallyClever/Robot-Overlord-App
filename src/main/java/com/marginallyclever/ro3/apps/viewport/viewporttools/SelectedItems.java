package com.marginallyclever.ro3.apps.viewport.viewporttools;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of {@link Node}s selected in the {@link com.marginallyclever.ro3.apps.viewport.Viewport}.
 *
 */
public class SelectedItems {
    /**
     * The list of entities selected in the editorpanel.
     */
    private final List<Node> nodes = new ArrayList<>();

    /**
     * The world pose of each node in the selection at the moment it was selected.
     */
    private final Map<Node, Matrix4d> worldPoses = new HashMap<>();

    public SelectedItems() {
        super();
    }

    public SelectedItems(List<Node> list) {
        super();

        for(Node e : list) {
            addNode(e);
        }
    }

    public void addNode(Node node) {
        nodes.add(node);
        setEntityWorldPose(node);
    }

    private void setEntityWorldPose(Node node) {
        if(node instanceof Pose pose) {
            worldPoses.put(node, pose.getWorld());
        }
    }

    public void removeEntity(Node node) {
        nodes.remove(node);
        worldPoses.remove(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Matrix4d getWorldPoseAtStart(Node node) {
        return worldPoses.get(node);
    }

    public Matrix4d getWorldPoseNow(Node node) {
        if(node instanceof Pose pose) {
            return pose.getWorld();
        }
        return MatrixHelper.createIdentityMatrix4();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void clear() {
        nodes.clear();
        worldPoses.clear();
    }

    public void savePose() {
        for(Node e : nodes) {
            setEntityWorldPose(e);
        }
    }
}
