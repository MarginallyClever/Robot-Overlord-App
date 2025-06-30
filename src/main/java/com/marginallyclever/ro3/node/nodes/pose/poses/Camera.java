package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

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
    private static final Logger logger = LoggerFactory.getLogger(Camera.class);
    private boolean drawOrthographic = false;
    private double fovY = 60;
    private double nearZ = 1;
    private double farZ = 1000;
    private double orbitRadius = 50;
    private boolean canTranslate = true;
    private boolean canRotate = true;

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
     * Translate relative to camera's current orientation if canTranslate is true.
     * @param delta distance to travel.  Positive is up.
     */
    public void pedestal(double delta) {
        translate(MatrixHelper.getYAxis(getWorld()),delta);
    }

    /**
     * Translate relative to camera's current orientation if canTranslate is true.
     * @param delta distance to travel.  Positive is right.
     */
    public void truck(double delta) {
        translate(MatrixHelper.getXAxis(getWorld()),delta);
    }

    /**
     * Translate relative to camera's current orientation if canTranslate is true.
     * @param delta distance to travel.  Positive is forward.
     */
    public void dolly(double delta) {
        translate(MatrixHelper.getZAxis(getWorld()),delta);
    }

    /**
     * Translate relative to camera's current orientation if canTranslate is true.
     * @param direction direction to travel in world space
     * @param delta distance to travel
     */
    private void translate(Vector3d direction,double delta) {
        if(!canTranslate) return;
        var w = getWorld();
        Vector3d t = new Vector3d();
        w.get(t);
        direction.scale(delta);
        t.add(direction);
        w.setTranslation(t);
        setWorld(w);
    }

    /**
     * Rotate relative to camera's current orientation if canRotate is true.
     * @param delta degrees to rotate.  Positive is left.
     */
    public void pan(double delta) {
        rotate(MatrixHelper.getYAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation if canRotate is true.
     * @param delta degrees to rotate.  Positive is up.
     */
    public void tilt(double delta) {
        rotate(MatrixHelper.getXAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation if canRotate is true.
     * @param delta degrees to rotate.  Positive is counter-clockwise.
     */
    public void roll(double delta) {
        rotate(MatrixHelper.getZAxis(this.getLocal()),delta);
    }

    /**
     * Rotate relative to camera's current orientation if canRotate is true.
     * @param axis axis to rotate around
     * @param delta degrees to rotate.  Positive is clockwise.
     */
    private void rotate(Vector3d axis,double delta) {
        if(!canRotate) return;
        Matrix3d m = MatrixHelper.getMatrixFromAxisAndRotation(axis,delta);
        Matrix4d m4 = new Matrix4d();
        m4.set(m);
        var local = getLocal();
        local.mul(m4);
        setLocal(local);
    }

    /**
     * Set the pan and tilt values such that the camera is looking at the target.
     * Set the orbit distance to the distance between the camera and the target.
     * @param target the point to look at.
     */
    public void lookAt(Vector3d target) {
        var m = this.getWorld();
        var position = MatrixHelper.getPosition(m);
        var viewMatrix = MatrixHelper.lookAt(target,position);
        var diff = new Vector3d();
        diff.sub(target,position);
        if(diff.length()<1e-6) {
            throw new InvalidParameterException("target is too close to camera.");
        }
        m.set(viewMatrix,position,1);
        // adjust the orbit radius to match the distance to the target.
        orbitRadius = diff.length();
        this.setWorld(m);
    }

    public Matrix4d getPerspectiveFrustum(int width,int height) {
        double aspect = (double)width / (double)height;
        return MatrixHelper.getPerspectiveMatrix4d(this.getFovY(),aspect,getNearZ(),getFarZ());
    }

    /**
     * Render the scene in orthographic projection.
     */
    public Matrix4d getOrthographicMatrix(int width,int height) {
        double h = height/2.0;
        double w = width/2.0;
        return MatrixHelper.orthographicMatrix4d(-w,w,-h,h,getNearZ(),getFarZ());
    }

    public Matrix4d getChosenProjectionMatrix(int width,int height) {
        return drawOrthographic ? getOrthographicMatrix(width,height) : getPerspectiveFrustum(width,height);
    }

    public Matrix4d getViewMatrix(boolean originShift) {
        Matrix4d inverseCamera = this.getWorld();
        if(originShift) {
            inverseCamera.setTranslation(new Vector3d());
        }
        inverseCamera.invert();
        return inverseCamera;
    }

    /**
     * @return the absolute point around which the camera is orbiting.
     */
    public Vector3d getOrbitPoint() {
        Matrix4d m = getWorld();
        Vector3d position = MatrixHelper.getPosition(m);
        // z axis points away from the direction the camera is facing.
        Vector3d zAxis = MatrixHelper.getZAxis(m);
        zAxis.scale(-orbitRadius);
        position.add(zAxis);
        return position;
    }

    /**
     * Change the distance from the camera to the orbit point.  The orbit point does not move.  In effect the camera
     * is performing a dolly in/out.
     * @param newRadius new radius.  Must be >=1.
     */
    public void moveToNewRadius(double newRadius) {
        var w = this.getWorld();
        var point = getOrbitPoint();
        orbitRadius = Math.max(1,newRadius);

        //logger.debug("wheel "+dz + " orbitRadius=" + orbitRadius);
        Vector3d orbitVector = MatrixHelper.getZAxis(w);
        orbitVector.scaleAdd(orbitRadius,point);
        w.setTranslation(orbitVector);
        setWorld(w);
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
        if(!canRotate || !canTranslate) return;

        Vector3d orbitPoint = getOrbitPoint();
        //logger.debug("before {}",orbitPoint);

        double [] panTiltAngles = getPanTiltFromMatrix(getWorld());
        // range limit around
        panTiltAngles[0] = (panTiltAngles[0] + dx+360) % 360;
        // tilt limit
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + dy));

        var panTilt = buildPanTiltMatrix(panTiltAngles);
        var m = new Matrix4d();
        m.set(panTilt);
        Vector3d orbitVector = MatrixHelper.getZAxis(m);
        orbitVector.scaleAdd(getOrbitRadius(),orbitPoint);
        m.setTranslation(orbitVector);
        setWorld(m);
        //logger.debug("after {}",getOrbitPoint());
    }

    public static double[] getPanTiltFromMatrix(Matrix4d matrix) {
        Vector3d v = MatrixHelper.matrixToEuler(matrix, MatrixHelper.EulerSequence.YXZ);
        double pan = Math.toDegrees(-v.z);
        double tilt = Math.toDegrees(v.x);
        return new double[]{ pan, tilt };
    }

    /**
     * @param panTiltAngles [0] = pan, [1] = tilt
     * @return a matrix that rotates the camera by the given pan and tilt angles.
     */
    public static Matrix3d buildPanTiltMatrix(double [] panTiltAngles) {
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
        if(!canRotate) return;

        var w = getWorld();
        var t = new Vector3d();
        w.get(t);
        double [] panTiltAngles = getPanTiltFromMatrix(w);
        panTiltAngles[0] = (panTiltAngles[0] + panDegrees+360) % 360;
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + tiltDegrees));
        Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
        w.set(panTilt,t,1);
        setWorld(w);
    }

    public boolean getCanTranslate() {
        return canTranslate;
    }

    public void setCanTranslate(boolean canTranslate) {
        this.canTranslate = canTranslate;
    }

    public boolean getCanRotate() {
        return canRotate;
    }

    public void setCanRotate(boolean canRotate) {
        this.canRotate = canRotate;
    }

    /**
     * Move towards or away from the orbit point if canTranslate is true.
     * @param scale relative to the current orbit distance.
     */
    public void orbitDolly(double scale) {
        if(!canTranslate) return;
        moveToNewRadius(orbitRadius*scale);
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("drawOrthographic",drawOrthographic);
        json.put("fovY",fovY);
        json.put("nearZ",nearZ);
        json.put("farZ",farZ);
        json.put("orbitRadius",orbitRadius);
        json.put("canTranslate",canTranslate);
        json.put("canRotate",canRotate);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        drawOrthographic = json.optBoolean("drawOrthographic",drawOrthographic);
        fovY = json.optDouble("fovY",fovY);
        nearZ = json.optDouble("nearZ",nearZ);
        farZ = json.optDouble("farZ",farZ);
        orbitRadius = json.optDouble("orbitRadius",orbitRadius);
        canTranslate = json.optBoolean("canTranslate",canTranslate);
        canRotate = json.optBoolean("canRotate",canRotate);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-movie-camera-16.png")));
    }

    /**
     * Set the distance from the camera to the orbit point.
     * @param radius new radius.  Must be >=1.
     */
    public void setOrbitRadius(double radius) {
        if(radius<1) throw new InvalidParameterException("target is too close to camera.");
        orbitRadius = radius;
    }
}
