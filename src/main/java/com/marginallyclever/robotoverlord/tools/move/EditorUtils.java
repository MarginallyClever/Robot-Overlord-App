package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PoseMoveEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.util.List;

public class EditorUtils {
    public static Matrix4d getLastItemSelectedMatrix(SelectedItems selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return null;
        }

        List<Entity> list = selectedItems.getEntities();
        Entity lastEntity = list.get(list.size() - 1);
        return selectedItems.getWorldPoseNow(lastEntity);
    }

    public static Plane getXYPlane(Matrix4d pivot) {
        return new Plane(
                MatrixHelper.getPosition(pivot),
                MatrixHelper.getZAxis(pivot)
        );
    }

    /**
     * Looks through the camera's viewport and returns the point on the translationPlane, if any.
     * @param x the x coordinate of the viewport, in screen coordinates [-1,1]
     * @param y the y coordinate of the viewport, in screen coordinates [-1,1]
     * @return the point on the translationPlane, or null if no intersection
     */
    public static Point3d getPointOnPlane(Plane translationPlane, Viewport viewport, double x, double y) {
        // get ray from camera through viewport
        Ray ray = viewport.getRayThroughPoint(x, y);

        // get intersection of ray with translationPlane
        double distance = IntersectionHelper.rayPlane(ray, translationPlane);
        if(distance == Double.MAX_VALUE) {
            return null;
        }
        return new Point3d(ray.getPoint(distance));
    }

    public static void updateUndoState(Object src,SelectedItems selectedItems) {
        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d before = selectedItems.getWorldPoseAtStart(entity);
            Matrix4d after = selectedItems.getWorldPoseNow(entity);
            entity.findFirstComponent(PoseComponent.class).setWorld(before);
            UndoSystem.addEvent(src, new PoseMoveEdit(entity, before, after, Translator.get("MoveTool.editName")));
        }
    }
}
