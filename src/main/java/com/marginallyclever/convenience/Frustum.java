package com.marginallyclever.convenience;

import javax.vecmath.Matrix4d;

/**
 * A Frustum (often misspelled as frustrum) is the portion of a solid, such as a cone or pyramid, that remains after
 * the top part is cut off by a plane parallel to the base, resulting in two parallel ends.
 */
public class Frustum {
    double zNear, zFar;
    double width, height;
    double fov;
    double aspectRatio;

    /**
     * Constructs a Frustum object, defined by its near and far clipping planes, width, height,
     * field of view, and aspect ratio. A Frustum represents a truncated pyramid used to describe
     * a 3D perspective view volume.
     *
     * @param zNear the distance to the near clipping plane from the camera position.
     * @param zFar the distance to the far clipping plane from the camera position.
     * @param width the width of the view at the near clipping plane.
     * @param height the height of the view at the near clipping plane.
     * @param fov the field of view angle, in degrees.
     * @param aspectRatio the aspect ratio of the view (ratio of width to height).
     */
    public Frustum(double zNear, double zFar, double width, double height, double fov, double aspectRatio) {
        this.zNear = zNear;
        this.zFar = zFar;
        this.width = width;
        this.height = height;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
    }

    public void setFOV(double fov) {
        this.fov = fov;
    }

    public double getFov() {
        return fov;
    }

    public double getZNear() {
        return zNear;
    }

    public void setZNear(double zNear) {
        this.zNear = zNear;
    }

    public double getZFar() {
        return zFar;
    }

    public void setZFar(double zFar) {
        this.zFar = zFar;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    /**
     * Computes and returns the perspective projection matrix representing this frustum.
     * This matrix is typically used for projecting 3D coordinates into a 2D perspective view.
     *
     * @return a {@link Matrix4d} object representing the frustum's perspective projection matrix.
     */
    public Matrix4d getMatrix() {
        Matrix4d mat = new Matrix4d();
        double f = 1.0 / Math.tan(Math.toRadians(fov) / 2.0);

        mat.m00 = f / aspectRatio;
        mat.m11 = f;
        mat.m22 = -(zFar + zNear) / (zFar - zNear);
        mat.m32 = -1.0;
        mat.m23 = -(2.0 * zFar * zNear) / (zFar - zNear);
        mat.m33 = 0.0;

        return mat;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
