package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

@ComponentDependency(components={PoseComponent.class})
public class CameraComponent extends RenderComponent {
    public final DoubleParameter pan = new DoubleParameter("Pan",0);
    public final DoubleParameter tilt = new DoubleParameter("Tilt",0);
    public final DoubleParameter orbitDistance = new DoubleParameter("Orbit distance",0);
    protected boolean isCurrentlyMoving=false;

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity!=null) {
            PoseComponent p = entity.findFirstComponent(PoseComponent.class);
            if(p==null) entity.addComponent(new PoseComponent());
        }
    }

    public double getOrbitDistance() {
        return orbitDistance.get();
    }

    public void setOrbitDistance(double arg0) {
        adjustOrbitPoint(arg0 - orbitDistance.get());
    }

    private void setPosition(Vector3d target) {
        PoseComponent p = getEntity().findFirstComponent(PoseComponent.class);
        p.setPosition(target);
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

    public void lookAt(Vector3d target) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);

        Vector3d forward = new Vector3d(target);
        forward.sub(pose.getPosition());
        double xy = Math.sqrt(forward.x*forward.x + forward.y*forward.y);
        setPan(Math.toDegrees(Math.atan2(forward.x,forward.y)));
        setTilt(Math.toDegrees(Math.atan2(xy,forward.z))-90);
        Vector3d dp = new Vector3d();
        dp.sub(target,pose.getPosition());
        this.orbitDistance.set(dp.length());

        pose.setLocalMatrix3(buildPanTiltMatrix(pan.get(),tilt.get()));
    }

    /**
     * Move the camera according to user input.
     */
    @Override
    public void update(double dt) {
        super.update(dt);
    }

    /**
     *  adjust the camera position to orbit around a point 'zoom' in front of the camera
     *  relies on the Z axis of the matrix BEFORE any rotations are applied.
     * @param dx pan amount
     * @param dy tilt amount
     */
    public void orbitCamera(double dx, double dy) {
        double distance = orbitDistance.get();
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d oldZ = MatrixHelper.getZAxis(pose.getWorld());
        oldZ.scale(distance);

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
        Vector3d p = pose.getPosition();
        p.sub(oldZ);
        p.add(newZ);
        setPosition(p);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dy distance to travel.  Positive is up.
     */
    public void pedestalCamera(double dy) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d vy = MatrixHelper.getYAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        double zSq = Math.sqrt(orbitDistance.get())*0.01;
        vy.scale(zSq* dy);
        p.add(vy);
        setPosition(p);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dx distance to travel.  Positive is right.
     */
    public void truckCamera(double dx) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d vx = MatrixHelper.getXAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        double zSq = Math.sqrt(orbitDistance.get())*0.01;
        vx.scale(zSq*-dx);
        p.add(vx);
        setPosition(p);
    }

    /**
     * Translate relative to camera's current orientation
     * @param dy distance to travel.  Positive is forward.
     */
    public void dollyCamera(double dy) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d zAxis = MatrixHelper.getZAxis(pose.getWorld());
        zAxis.scale(dy);
        Vector3d p = pose.getPosition();
        p.add(zAxis);
        setPosition(p);
    }

    protected Matrix3d buildPanTiltMatrix(double panDeg,double tiltDeg) {
        Matrix3d a = new Matrix3d();
        Matrix3d b = new Matrix3d();
        Matrix3d c = new Matrix3d();
        a.rotZ(Math.toRadians(panDeg));
        b.rotX(Math.toRadians(-tiltDeg));
        c.mul(b,a);
        c.transpose();

        return c;
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
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d p = pose.getPosition();
        Vector3d prevOrbit = getOrbitPoint();
        orbitDistance.set(newScale);
        Vector3d newOrbit = getOrbitPoint();
        prevOrbit.sub(newOrbit);
        p.add(prevOrbit);
        setPosition(p);

        // adjust dolly after getting the orbit point.
    }

    public Vector3d getOrbitPoint() {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d p = pose.getPosition();
        // z axis points behind the camera.
        Vector3d zAxis = MatrixHelper.getZAxis(pose.getWorld());
        zAxis.scale(-orbitDistance.get());
        p.add(zAxis);
        return p;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("pan",pan.toJSON());
        jo.put("tilt",tilt.toJSON());
        jo.put("zoom", orbitDistance.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        pan.parseJSON(jo.getJSONObject("pan"));
        tilt.parseJSON(jo.getJSONObject("tilt"));
        orbitDistance.parseJSON(jo.getJSONObject("zoom"));
    }

    @Override
    public void render(GL2 gl2) {
        if (!isCurrentlyMoving) return;
        isCurrentlyMoving = false;
        renderOrbitPoint(gl2);
    }

    private void renderOrbitPoint(GL2 gl2) {
        gl2.glPushMatrix();

        // reset matrix to camera inverse * orbit point
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Matrix4d inverseCamera = pose.getWorld();
        inverseCamera.invert();

        Matrix4d orbitPointMatrix = MatrixHelper.createIdentityMatrix4();
        orbitPointMatrix.setTranslation(getOrbitPoint());

        orbitPointMatrix.mul(inverseCamera,orbitPointMatrix);
        MatrixHelper.applyMatrix(gl2,orbitPointMatrix);

        // draw marker
        PrimitiveSolids.drawStar(gl2,25);

        gl2.glPopMatrix();
    }

    public void setCurrentlyMoving(boolean state) {
        isCurrentlyMoving = state;
    }
}
