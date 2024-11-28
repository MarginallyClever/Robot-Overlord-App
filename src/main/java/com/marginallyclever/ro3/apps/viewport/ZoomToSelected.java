package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Move the active camera to within a "good" distance of the last selected Pose.
 * Does not change the camera's orientation.
 */
public class ZoomToSelected extends AbstractAction {
    private final Viewport viewport;

    public ZoomToSelected(Viewport viewport) {
        super("Zoom to Selected");
        this.viewport = viewport;
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/shared/icons8-search-16.png"))));
        putValue(Action.SHORT_DESCRIPTION,"Zoom to the last selected Pose.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Pose lastFound = null;
        for(Node node : Registry.selection.getList()) {
            if(node instanceof Pose pose) {
                lastFound = pose;
            }
        }
        if(lastFound==null) return;

        double distance = 0;
        Camera camera = viewport.getActiveCamera();

        // use the bounds of lastFound to determine the zoom distance.
        var mi = lastFound.findFirstChild(MeshInstance.class);
        if( mi != null) {
            // if the last selected pose has a mesh, use the mesh's bounds to determine the zoom distance.
            var box = mi.getMesh().getBoundingBox();
            var diameter = new Vector3d(box.getBoundsTop());
            diameter.sub(box.getBoundsBottom());
            distance = diameter.length() * 0.6;  // diameter + add a little padding
        }

        // look at the last selected pose
        Matrix4d m = camera.getWorld();
        var cameraPosition = MatrixHelper.getPosition(m);
        var lastFoundPosition = MatrixHelper.getPosition(lastFound.getWorld());

        if(mi!=null) {
            // knowing the distance to the last selected pose, adjust the camera position to be that distance away.
            Vector3d diff = new Vector3d(cameraPosition);
            diff.sub(lastFoundPosition);
            diff.normalize();
            diff.scale(distance);
            cameraPosition.set(lastFoundPosition);
            cameraPosition.add(diff);
        } else {
            // adjust the camera orbit distance so that orbiting will be around the lastFound position.
            Vector3d diff = new Vector3d(cameraPosition);
            diff.sub(lastFoundPosition);
            distance = Math.max(1, diff.length());
        }

        m.setTranslation(cameraPosition);
        camera.setWorld(m);
        camera.setOrbitRadius(distance);
    }
}