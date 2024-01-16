package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * <p>The Camera class is a subclass of the Pose class and is used by a Viewport to viewport the scene in a 3D graphics or game engine. This class provides several functionalities:</p>
 * <ul>
 *     <li>It can be set to viewport in orthographic projection.</li>
 *     <li>It has a vertical field of view, a near and far clipping plane for perspective rendering.</li>
 *     <li>It can translate and rotate relative to its current orientation.</li>
 *     <li>It can look at a specific point in the scene.</li>
 *     <li>It can orbit around a point at a certain radius.</li>
 *     <li>It can get the point it is orbiting around.</li>
 *     <li>It can change the distance from itself to the orbit point.</li>
 *     <li>It can get the distance from itself to the orbit point.</li>
 *     <li>It can get the perspective frustum and orthographic matrix.</li>
 *     <li>It can get the chosen projection matrix based on whether it is set to draw in orthographic or not.</li>
 *     <li>It can get the view matrix.</li>
 *     <li>It can build a Swing Component that represents itself.</li>
 * </ul>
 *
 */
public class Camera extends Pose {
    private boolean drawOrthographic = false;
    private double fovY = 60;
    private double nearZ = 1;
    private double farZ = 1000;
    private double orbitRadius = 50;

    public Camera() {
        super("Camera");
    }

    public Camera(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        Registry.cameras.add(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        Registry.cameras.remove(this);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JPanel> list) {
        list.add(new CameraPanel(this));
        super.getComponents(list);
    }

    public boolean getDrawOrthographic() {
        return drawOrthographic;
    }

    public void setDrawOrthographic(boolean selected) {
        drawOrthographic = selected;
    }

    public double getFovY() {
        return fovY;
    }

    public void setFovY(double fovY) {
        this.fovY = fovY;
    }

    public double getNearZ() {
        return nearZ;
    }

    public void setNearZ(double nearZ) {
        this.nearZ = nearZ;
    }

    public double getFarZ() {
        return farZ;
    }

    public void setFarZ(double farZ) {
        this.farZ = farZ;
    }

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is up.
     */
    public void pedestal(double delta) {
        translate(MatrixHelper.getYAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is right.
     */
    public void truck(double delta) {
        translate(MatrixHelper.getXAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is forward.
     */
    public void dolly(double delta) {
        translate(MatrixHelper.getZAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param direction direction to travel
     * @param delta distance to travel
     */
    private void translate(Vector3d direction,double delta) {
        Matrix4d local = this.getLocal();
        Vector3d t = new Vector3d();
        local.get(t);
        direction.scale(delta);
        t.add(direction);
        local.setTranslation(t);
        this.setLocal(local);
    }

    /**
     * Rotate relative to camera's current orientation
     * @param delta degrees to rotate.  Positive is left.
     */
    public void pan(double delta) {
        rotate(MatrixHelper.getYAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation
     * @param delta degrees to rotate.  Positive is up.
     */
    public void tilt(double delta) {
        rotate(MatrixHelper.getXAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation
     * @param delta degrees to rotate.  Positive is counter-clockwise.
     */
    public void roll(double delta) {
        rotate(MatrixHelper.getZAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation
     * @param axis axis to rotate around
     * @param delta degrees to rotate.  Positive is clockwise.
     */
    private void rotate(Vector3d axis,double delta) {
        Matrix3d m = MatrixHelper.getMatrixFromAxisAndRotation(axis,delta);
        Matrix4d m4 = new Matrix4d();
        m4.set(m);
        getLocal().mul(m4);
    }

    /**
     * Set the pan and tilt values such that the camera is looking at the target.
     * Set the orbit distance to the distance between the camera and the target.
     * @param target the point to look at.
     */
    public void lookAt(Vector3d target) {
        Matrix4d local = this.getLocal();
        Vector3d position = MatrixHelper.getPosition(local);
        Matrix3d viewMatrix = MatrixHelper.lookAt(target,position);
        Vector3d diff = new Vector3d();
        diff.sub(target,position);
        if(diff.length()<0.0001) {
            throw new InvalidParameterException("target is too close to camera.");
        }
        local.set(viewMatrix);
        local.setTranslation(position);
        this.setLocal(local);
    }

    public Matrix4d getPerspectiveFrustum(int width,int height) {
        double nearVal = this.getNearZ();
        double farVal = this.getFarZ();
        double aspect = (double)width / (double)height;

        return MatrixHelper.perspectiveMatrix4d(this.getFovY(),aspect,nearVal,farVal);
    }

    /**
     * Render the scene in orthographic projection.
     * @param zoom the zoom factor
     */
    public Matrix4d getOrthographicMatrix(double zoom,int width,int height) {
        double w = width/2.0f;
        double h = height/2.0f;

        double left = -w/zoom;
        double right = w/zoom;
        double bottom = -h/zoom;
        double top = h/zoom;
        double nearVal = this.getNearZ();
        double farVal = this.getFarZ();

        return MatrixHelper.orthographicMatrix4d(left,right,bottom,top,nearVal,farVal);
    }

    public Matrix4d getChosenProjectionMatrix(int width,int height) {
        return drawOrthographic ? getOrthographicMatrix(1.0,width,height) : getPerspectiveFrustum(width,height);
    }

    public Matrix4d getViewMatrix() {
        Matrix4d inverseCamera = this.getWorld();
        inverseCamera.invert();
        inverseCamera.transpose();
        return inverseCamera;
    }

    /**
     * @return the point that the camera is orbiting around.
     */
    public Vector3d getOrbitPoint() {
        Matrix4d local = getLocal();
        Vector3d position = MatrixHelper.getPosition(local);
        // z axis points away from the direction the camera is facing.
        Vector3d zAxis = MatrixHelper.getZAxis(local);
        zAxis.scale(-orbitRadius);
        position.add(zAxis);
        return position;
    }

    /**
     * Change the distance from the camera to the orbit point.  The orbit point does not move.  In effect the camera
     * is performing a dolly in/out.
     * @param newRadius new radius.  Must be >=1.
     */
    public void setOrbitRadius(double newRadius) {
        Matrix4d local = getLocal();
        var point = getOrbitPoint();
        orbitRadius = Math.max(1,newRadius);
        //logger.debug("wheel "+dz + " orbitRadius=" + orbitRadius);
        Vector3d orbitVector = MatrixHelper.getZAxis(local);
        orbitVector.scaleAdd(orbitRadius,point);
        local.setTranslation(orbitVector);
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    /**
     * Orbit the camera around a point orbitRadius ahead of the camera.
     * @param dx change in x
     * @param dy change in y
     */
    public void orbit(double dx,double dy) {
        Vector3d orbitPoint = getOrbitPoint();
        //logger.debug("before {}",orbitPoint);
        double [] panTiltAngles = getPanTiltFromMatrix(getLocal());
        panTiltAngles[0] = (panTiltAngles[0] + dx+360) % 360;
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + dy));
        Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
        Matrix4d newLocal = new Matrix4d();
        newLocal.set(panTilt);
        Vector3d orbitVector = MatrixHelper.getZAxis(newLocal);
        orbitVector.scaleAdd(getOrbitRadius(),orbitPoint);
        newLocal.setTranslation(orbitVector);
        setLocal(newLocal);
        //logger.debug("after {}",getOrbitPoint());
    }

    double[] getPanTiltFromMatrix(Matrix4d matrix) {
        Vector3d v = MatrixHelper.matrixToEuler(matrix, MatrixHelper.EulerSequence.YXZ);
        double pan = Math.toDegrees(-v.z);
        double tilt = Math.toDegrees(v.x);
        return new double[]{ pan, tilt };
    }

    /**
     * @param panTiltAngles [0] = pan, [1] = tilt
     * @return a matrix that rotates the camera by the given pan and tilt angles.
     */
    Matrix3d buildPanTiltMatrix(double [] panTiltAngles) {
        Matrix3d a = new Matrix3d();
        a.rotZ(Math.toRadians(panTiltAngles[0]));

        Matrix3d b = new Matrix3d();
        b.rotX(Math.toRadians(-panTiltAngles[1]));

        Matrix3d c = new Matrix3d();
        c.mul(b,a);
        c.transpose();
        return c;
    }

    /**
     * Combination pan and tilt.
     * @param panDegrees pan angle in degrees
     * @param tiltDegrees tilt angle in degrees
     */
    public void panTilt(double panDegrees, double tiltDegrees) {
        Matrix4d local = getLocal();
        Vector3d t = new Vector3d();
        local.get(t);
        double [] panTiltAngles = getPanTiltFromMatrix(local);
        panTiltAngles[0] = (panTiltAngles[0] + panDegrees+360) % 360;
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + tiltDegrees));
        Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
        local.set(panTilt);
        local.setTranslation(t);
        setLocal(local);
    }
}
