package com.marginallyclever.ro3.physics;

import org.json.JSONObject;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Shared methods for working with ODE4J physics.
 */
public class ODE4JHelper {
    private static final Logger logger = LoggerFactory.getLogger(ODE4JHelper.class);

    /**
     * Convert an ODE4J translation/rotation pair to a Java3D matrix.
     * @param translation the translation component.
     * @param rotation the rotation matrix
     * @return the Java3D matrix.
     */
    public static Matrix4d convertODEtoMatrix(DVector3C translation, DMatrix3C rotation) {
        // assemble matrix from translation and rotation.
        Matrix4d m = new Matrix4d();
        m.m00 = rotation.get00();
        m.m01 = rotation.get01();
        m.m02 = rotation.get02();
        m.m03 = translation.get0();
        m.m10 = rotation.get10();
        m.m11 = rotation.get11();
        m.m12 = rotation.get12();
        m.m13 = translation.get1();
        m.m20 = rotation.get20();
        m.m21 = rotation.get21();
        m.m22 = rotation.get22();
        m.m23 = translation.get2();
        m.m30 = 0;
        m.m31 = 0;
        m.m32 = 0;
        m.m33 = 1;
        return m;
    }

    /**
     * receive a 4x4 matrix.  extract the rotation component and store it in a DMatrix3C.
     * @param mat the 4x4 matrix.
     * @return the rotation component as a 3x3 matrix.
     */
    public static DMatrix3C convertRotationToODE(Matrix4d mat) {
        return new DMatrix3(
                mat.m00, mat.m01, mat.m02,
                mat.m10, mat.m11, mat.m12,
                mat.m20, mat.m21, mat.m22
        );
    }

    public static double volumeCylinder(double radius, double length) {
        return Math.PI * radius * radius * length;
    }

    public static double volumeCapsule(double radius, double length) {
        return volumeCylinder(radius,length) + volumeSphere(radius);
    }

    public static double volumeBox(double x, double y, double z) {
        return x * y * z;
    }

    public static double volumeSphere(double radius) {
        return 4.0 / 3.0 * Math.PI * radius * radius * radius;
    }

    public static JSONObject vector3ToJSON(Vector3d vector) {
        JSONObject json = new JSONObject();
        json.put("x", vector.x);
        json.put("y", vector.y);
        json.put("z", vector.z);
        return json;
    }

    public static Vector3d jsonToVector3(JSONObject vector) {
        return new Vector3d(
                vector.getDouble("x"),
                vector.getDouble("y"),
                vector.getDouble("z"));
    }
}
