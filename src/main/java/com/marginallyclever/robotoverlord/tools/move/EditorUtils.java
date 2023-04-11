package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class EditorUtils {

    public static Matrix4d getFirstItemSelectedMatrix(SelectedItems selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return null;
        }

        Entity firstEntity = selectedItems.getEntities().get(0);
        return selectedItems.getWorldPose(firstEntity);
    }

    public static Plane getXYPlane(Matrix4d pivot) {
        Vector3d zAxis = MatrixHelper.getZAxis(pivot);
        Vector3d point = MatrixHelper.getPosition(pivot);
        return new Plane(point, zAxis);
    }
}
