package com.marginallyclever.ro3.apps.viewport.renderpass;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class RenderPassHelper {
    public static Matrix4d getOriginShiftedMatrix(Matrix4d m, Vector3d cameraWorldPos) {
        var m2 = new Matrix4d(m);
        m2.m03 -= cameraWorldPos.x;
        m2.m13 -= cameraWorldPos.y;
        m2.m23 -= cameraWorldPos.z;
        return m2;
    }
}
