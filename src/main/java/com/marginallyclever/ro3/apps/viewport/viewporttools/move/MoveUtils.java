package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.apps.viewport.viewporttools.SelectedItems;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * Convenience methods for the rotate and translate viewporttools.
 *
 */
public class MoveUtils {
    /**
     * Returns the last selected item's world pose, or null if no items are selected.
     * @param selectedItems the list of selected items
     * @return the last selected item's world pose, or null if no items are selected.
     */
    public static Matrix4d getLastItemSelectedMatrix(SelectedItems selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return null;
        }

        List<Node> list = selectedItems.getNodes();
        Node lastEntity = null;
        for(Node e : list) {
            if(e instanceof Pose) lastEntity = e;
        }
        return selectedItems.getWorldPoseNow(lastEntity);
    }

    /**
     * Cast a ray through the camera's viewport at the given x,y and find the intersection point on the translationPlane.
     * @param x the x coordinate of the viewport, in screen coordinates [-1,1]
     * @param y the y coordinate of the viewport, in screen coordinates [-1,1]
     * @return the point on the translationPlane, or null if no intersection
     */
    public static Point3d getPointOnPlaneFromCursor(Plane translationPlane, Viewport viewport, double x, double y) {
        Camera cam = viewport.getActiveCamera();
        assert cam != null;
        // get ray from camera through viewport
        var normalizedCoordinates = viewport.getCursorAsNormalized(x,y);
        Ray ray = viewport.getRayThroughPoint(cam, normalizedCoordinates.x, normalizedCoordinates.y);

        // get intersection of ray with translationPlane
        double distance = IntersectionHelper.rayPlane(ray, translationPlane);
        if(distance == Double.MAX_VALUE) {
            return null;
        }
        return new Point3d(ray.getPoint(distance));
    }

    public static void updateUndoState(SelectedItems selectedItems) {
        for (Node node : selectedItems.getNodes()) {
            if(node instanceof Pose pose) {
                Matrix4d before = selectedItems.getWorldPoseAtStart(node);
                Matrix4d after = selectedItems.getWorldPoseNow(node);
                //TODO pose.setWorld(before);
                //TODO UndoSystem.addEvent(new PoseMoveEdit(node, before, after));
            }
        }
    }

    /**
     * Get the pivot matrix of the selected items.  The matrix should be returned in world space.
     * @param frameOfReference the {@link FrameOfReference} to use.
     * @param selectedItems the list of selected items
     * @param camera the active camera
     * @return the pivot matrix of the selected items, in world space.  If no items are selected, returns the
     *         identity matrix.
     */
    public static Matrix4d getPivotMatrix(FrameOfReference frameOfReference, SelectedItems selectedItems,Camera camera) {
        if(selectedItems == null || selectedItems.isEmpty()) {
            return MatrixHelper.createIdentityMatrix4();
        }

        Matrix4d lastItemSelectedMatrix = getLastItemSelectedMatrix(selectedItems);

        Matrix4d m;
        switch(frameOfReference) {
            case WORLD -> m = MatrixHelper.createIdentityMatrix4();
            case LOCAL -> m = lastItemSelectedMatrix;
            case CAMERA -> m = camera.getWorld();
            default -> throw new InvalidParameterException("Unknown frame of reference: " + frameOfReference);
        }
        m.setTranslation(MatrixHelper.getPosition(lastItemSelectedMatrix));

        return m;
    }

    public static boolean listContainsAPose(List<Node> list) {
        boolean valid = false;
        for(Node n : list) {
            if(n instanceof Pose) {
                return true;
            }
        }
        return valid;
    }
}
