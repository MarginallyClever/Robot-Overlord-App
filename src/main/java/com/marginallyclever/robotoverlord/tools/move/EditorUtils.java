package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.PoseMoveEdit;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.util.List;

/**
 * Convenience methods for the rotate and translate tools.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class EditorUtils {
    /**
     * Returns the last selected item's world pose, or null if no items are selected.
     * @param selectedItems the list of selected items
     * @return the last selected item's world pose, or null if no items are selected.
     */
    public static Matrix4d getLastItemSelectedMatrix(SelectedItems selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return null;
        }

        List<Entity> list = selectedItems.getEntities();
        Entity lastEntity = list.get(list.size() - 1);
        return selectedItems.getWorldPoseNow(lastEntity);
    }

    /**
     * Looks through the camera's viewport and returns the point on the translationPlane, if any.
     * @param x the x coordinate of the viewport, in screen coordinates [-1,1]
     * @param y the y coordinate of the viewport, in screen coordinates [-1,1]
     * @return the point on the translationPlane, or null if no intersection
     */
    public static Point3d getPointOnPlaneFromCursor(Plane translationPlane, Viewport viewport, double x, double y) {
        // get ray from camera through viewport
        Ray ray = viewport.getRayThroughPoint(x, y);

        // get intersection of ray with translationPlane
        double distance = IntersectionHelper.rayPlane(ray, translationPlane);
        if(distance == Double.MAX_VALUE) {
            return null;
        }
        return new Point3d(ray.getPoint(distance));
    }

    public static void updateUndoState(SelectedItems selectedItems) {
        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d before = selectedItems.getWorldPoseAtStart(entity);
            Matrix4d after = selectedItems.getWorldPoseNow(entity);
            entity.getComponent(PoseComponent.class).setWorld(before);
            UndoSystem.addEvent(new PoseMoveEdit(entity, before, after));
        }
    }

    public static Matrix4d getPivotMatrix(int frameOfReference,Viewport viewport,SelectedItems selectedItems) {
        Matrix4d m;
        switch(frameOfReference) {
            case EditorTool.FRAME_WORLD -> {
                m = MatrixHelper.createIdentityMatrix4();
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m.setTranslation(MatrixHelper.getPosition(lis));
            }
            case EditorTool.FRAME_LOCAL -> {
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m = lis;
            }
            case EditorTool.FRAME_CAMERA -> {
                m = viewport.getViewMatrix();
                m.invert();
                Matrix4d lis = getLastItemSelectedMatrix(selectedItems);
                assert lis!=null;
                m.setTranslation(MatrixHelper.getPosition(lis));
            }
            default -> throw new RuntimeException("Unknown frame of reference: " + frameOfReference);
        }

        if(m==null) {
            m = MatrixHelper.createIdentityMatrix4();
        }
        return m;
    }
}
