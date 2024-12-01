package com.marginallyclever.ro3.apps.viewport.renderpass;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class RenderPassHelper {
    public static Matrix4d getOriginShiftedMatrix(Matrix4d m, Vector3d cameraWorldPos) {
        var m2 = new Matrix4d(m);
        var t = new Vector3d();
        m2.get(t);
        t.x -= cameraWorldPos.x;
        t.y -= cameraWorldPos.y;
        t.z -= cameraWorldPos.z;
        m2.setTranslation(t);

        return m2;
    }
}
