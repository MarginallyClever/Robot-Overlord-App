package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A camera {@link Component}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components={PoseComponent.class})
public class CameraComponent extends RenderComponent {
    public final DoubleParameter pan = new DoubleParameter("Pan",0);
    public final DoubleParameter tilt = new DoubleParameter("Tilt",0);
    public final DoubleParameter orbitDistance = new DoubleParameter("Orbit distance",0);
    protected boolean isCurrentlyMoving=false;

    public double getOrbitDistance() {
        return orbitDistance.get();
    }

    public void setOrbitDistance(double arg0) {
        adjustOrbitPoint(arg0 - orbitDistance.get());
    }

    private void setPosition(Vector3d target) {
        PoseComponent p = getEntity().getComponent(PoseComponent.class);
        p.setPosition(target);
    }

    public Vector3d getPosition() {
        PoseComponent p = getEntity().getComponent(PoseComponent.class);
        return p.getPosition();
    }

    public double getPan() {
        return pan.get();
    }

    public double getTilt() {
        return tilt.get();
    }

    public void setPan(double arg0) {
        //arg0 = Math.min(Math.max(arg0, 0), 360);
        pan.set(arg0);
    }

    public void setTilt(double arg0) {
        arg0 = Math.min(Math.max(arg0, 1), 179);
        tilt.set(arg0);
    }

    public Vector3d getOrbitPoint() {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Vector3d p = pose.getPosition();
        // z axis points away from the direction the camera is facing.
        Vector3d zAxis = MatrixHelper.getZAxis(pose.getWorld());
        zAxis.scale(-orbitDistance.get());
        p.add(zAxis);
        return p;
    }

    /**
     * Set the pan and tilt values such that the camera is looking at the target.
     * Set the orbit distance to the distance between the camera and the target.
     * @param target the point to look at.
     */
    public void lookAt(Vector3d target) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Matrix3d viewMatrix = MatrixHelper.lookAt(target,pose.getPosition());

        Vector3d zAxis = new Vector3d();
        zAxis.sub(target,pose.getPosition());
        double distance = zAxis.length();

        pose.setLocalMatrix3(viewMatrix);
        setPanTiltFromMatrix(viewMatrix);
        this.orbitDistance.set(distance);
    }

    protected void setPanTiltFromMatrix(Matrix3d matrix) {
        double [] v =  getPanTiltFromMatrix(matrix);
        setPan(v[0]);
        setTilt(v[1]);
    }

    public double[] getPanTiltFromMatrix(Matrix3d matrix) {
        Vector3d v = MatrixHelper.matrixToEuler(matrix);
        double pan = Math.toDegrees(-v.z);
        double tilt = Math.toDegrees(v.x);
        return new double[]{ pan, tilt };
    }

    public Matrix3d buildPanTiltMatrix(double panDeg,double tiltDeg) {
        Matrix3d a = new Matrix3d();
        a.rotZ(Math.toRadians(panDeg));

        Matrix3d b = new Matrix3d();
        b.rotX(Math.toRadians(-tiltDeg));

        Matrix3d c = new Matrix3d();
        c.mul(b,a);
        c.transpose();
        return c;
    }

    /**
     *  adjust the camera position to orbit around a point 'zoom' in front of the camera
     *  relies on the Z axis of the matrix BEFORE any rotations are applied.
     * @param dx pan amount
     * @param dy tilt amount
     */
    public void orbitCamera(double dx, double dy) {
        Vector3d p = getOrbitPoint();
        double distance = orbitDistance.get();

        PoseComponent pose = getEntity().getComponent(PoseComponent.class);

        // orbit around the focal point
        setPan(getPan()+dx);
        setTilt(getTilt()-dy);

        // do updateMatrix() but keep the rotation matrix
        Matrix3d rot = buildPanTiltMatrix(pan.get(),tilt.get());
        pose.setLocalMatrix3(rot);

        // get the new Z axis
        Vector3d newZ = new Vector3d(rot.m02,rot.m12,rot.m22);
        newZ.scale(distance);

        // adjust position according to zoom (aka orbit) distance.
        p.add(newZ);
        setPosition(p);
    }

    private void moveInternal(Vector3d p,Vector3d direction,double scale) {
        double d = orbitDistance.get();
        direction.scale(scale*d*0.001);
        p.add(direction);
        setPosition(p);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dy distance to travel.  Positive is up.
     */
    public void pedestalCamera(double dy) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Vector3d vy = MatrixHelper.getYAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        moveInternal(p,vy,dy);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dx distance to travel.  Positive is right.
     */
    public void truckCamera(double dx) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Vector3d vx = MatrixHelper.getXAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        moveInternal(p,vx,-dx);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dz distance to travel.  Positive is forward.
     */
    public void dollyCamera(double dz) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Vector3d p = pose.getPosition();
        Vector3d vz = MatrixHelper.getZAxis(pose.getWorld());
        moveInternal(p,vz,dz);
    }

    /**
     * Dolly the camera forward/back relative to the orbit point.
     * @param scale how much to dolly
     */
    private void adjustOrbitPoint(double scale) {
        isCurrentlyMoving=true;
        // get new and old scale
        double oldScale = orbitDistance.get();
        double newScale = oldScale + scale;

        // don't allow scale too close
        newScale = Math.max(1,newScale);
        if(oldScale==newScale) return;

        // apply change
        Vector3d p = getPosition();
        Vector3d prevOrbit = getOrbitPoint();
        orbitDistance.set(newScale);
        Vector3d newOrbit = getOrbitPoint();
        prevOrbit.sub(newOrbit);
        p.add(prevOrbit);
        setPosition(p);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("pan",pan.toJSON(context));
        jo.put("tilt",tilt.toJSON(context));
        jo.put("zoom", orbitDistance.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        pan.parseJSON(jo.getJSONObject("pan"),context);
        tilt.parseJSON(jo.getJSONObject("tilt"),context);
        orbitDistance.parseJSON(jo.getJSONObject("zoom"),context);
    }

    @Override
    public void render(GL3 gl) {
        if (!isCurrentlyMoving) return;
        isCurrentlyMoving = false;
        renderOrbitPoint(gl);
    }

    private void renderOrbitPoint(GL3 gl) {
        // reset matrix to camera inverse * orbit point
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Matrix4d inverseCamera = pose.getWorld();
        inverseCamera.invert();

        Matrix4d orbitPointMatrix = MatrixHelper.createIdentityMatrix4();
        orbitPointMatrix.setTranslation(getOrbitPoint());
        orbitPointMatrix.mul(inverseCamera,orbitPointMatrix);

        MatrixHelper.drawMatrix(orbitPointMatrix,25);
    }

    public void setCurrentlyMoving(boolean state) {
        isCurrentlyMoving = state;
    }
}
