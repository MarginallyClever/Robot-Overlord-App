package com.marginallyclever.ro3.apps.render.viewporttools.move;

import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.viewporttools.SelectedItems;
import com.marginallyclever.ro3.apps.render.viewporttools.ViewportTool;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.apps.render.Viewport;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * Convenience methods for the rotate and translate viewporttools.
 *
 * @author Dan Royer
 * @since 2.5.0
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
     * Looks through the camera's viewport and returns the point on the translationPlane, if any.
     * @param x the x coordinate of the viewport, in screen coordinates [-1,1]
     * @param y the y coordinate of the viewport, in screen coordinates [-1,1]
     * @return the point on the translationPlane, or null if no intersection
     */
    public static Point3d getPointOnPlaneFromCursor(Plane translationPlane, Viewport viewport, double x, double y) {
        Camera cam = Registry.getActiveCamera();
        assert cam != null;
        // get ray from camera through viewport
        Ray ray = viewport.getRayThroughPoint(cam, x, y);

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
     * @param frameOfReference the frame of reference to use
     * @param selectedItems the list of selected items
     * @return the pivot matrix of the selected items, in world space.
     */
    public static Matrix4d getPivotMatrix(int frameOfReference, SelectedItems selectedItems) {
        Matrix4d m;
        switch(frameOfReference) {
            case ViewportTool.FRAME_WORLD -> {
                m = MatrixHelper.createIdentityMatrix4();
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m.setTranslation(MatrixHelper.getPosition(lis));
            }
            case ViewportTool.FRAME_LOCAL -> {
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m = lis;
            }
            case ViewportTool.FRAME_CAMERA -> {
                Camera cam = Registry.getActiveCamera();
                assert cam != null;
                m = cam.getViewMatrix();
                m.invert();
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m.setTranslation(MatrixHelper.getPosition(lis));
            }
            default -> throw new InvalidParameterException("Unknown frame of reference: " + frameOfReference);
        }

        return m;
    }
}
