package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.Matrix4d;
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

    public static Plane getYZPlane(Matrix4d pivot) {
        return new Plane(
                MatrixHelper.getPosition(pivot),
                MatrixHelper.getXAxis(pivot)
        );
    }
}
