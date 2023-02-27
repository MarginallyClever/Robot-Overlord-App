package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

@ComponentDependency(components={PoseComponent.class})
public class CameraComponent extends RenderComponent {
    private final DoubleEntity pan = new DoubleEntity("Pan",0);
    private final DoubleEntity tilt = new DoubleEntity("Tilt",0);
    private final DoubleEntity orbitDistance = new DoubleEntity("Orbit distance",100);
    private final DoubleEntity snapDeadZone = new DoubleEntity("Snap dead zone",100);
    private final DoubleEntity snapDegrees = new DoubleEntity("Snap degrees",45);

    protected transient boolean hasSnappingStarted=false;
    protected transient double sumDx=0;
    protected transient double sumDy=0;
    protected boolean isCurrentlyMoving=false;

    public double getOrbitDistance() {
        return orbitDistance.get();
    }

    public void setOrbitDistance(double arg0) {
        orbitDistance.set(arg0);
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
        double dz = InputManager.getRawValue(InputManager.Source.MOUSE_Z);
        if(dz!=0) adjustOrbitPoint(dz);

        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Matrix4d myPose = pose.getWorld();

        if (InputManager.isOn(InputManager.Source.MOUSE_MIDDLE)) {
            //Log.message("mouse middle");
            isCurrentlyMoving=true;
            double scale = 1;
            double dx = InputManager.getRawValue(InputManager.Source.MOUSE_X) * scale;
            double dy = InputManager.getRawValue(InputManager.Source.MOUSE_Y) * scale;

            if(dx!=0 || dy!=0) {
                // snap system
                boolean isSnapHappeningNow = InputManager.isOn(InputManager.Source.KEY_LALT)
                                            || InputManager.isOn(InputManager.Source.KEY_RALT);
                if(isSnapHappeningNow) {
                    if(!hasSnappingStarted) {
                        sumDx=0;
                        sumDy=0;
                        hasSnappingStarted=true;
                    }
                }
                hasSnappingStarted = isSnapHappeningNow;
                //Log.message("Snap="+isSnapHappeningNow);

                //
                if( InputManager.isOn(InputManager.Source.KEY_LSHIFT) ||
                        InputManager.isOn(InputManager.Source.KEY_RSHIFT) ) {
                    pedestalCamera(dy);
                    truckCamera(dx);
                } else if(InputManager.isOn(InputManager.Source.KEY_LCONTROL) ||
                        InputManager.isOn(InputManager.Source.KEY_RCONTROL) ) {
                    dollyCamera(dy);
                } else if( isSnapHappeningNow ) {
                    sumDx+=dx;
                    sumDy+=dy;
                    if(Math.abs(sumDx)>snapDeadZone.get() || Math.abs(sumDy)>snapDeadZone.get()) {
                        double degrees = snapDegrees.get();
                        if(Math.abs(sumDx) > Math.abs(sumDy)) {
                            double a=getPan();
                            if(sumDx>0)	a+=degrees;	// snap CCW
                            else		a-=degrees;	// snap CW
                            setPan(Math.round(a/degrees)*degrees);
                        } else {
                            double a=getTilt();
                            if(sumDy>0)	a-=degrees;	// snap down
                            else		a+=degrees;	// snap up
                            setTilt(Math.round(a/degrees)*degrees);
                        }

                        Matrix3d rot = buildPanTiltMatrix(pan.get(),tilt.get());
                        pose.setLocalMatrix3(rot);
                        sumDx=0;
                        sumDy=0;
                    }
                } else {
                    orbitCamera(dx,dy);
                }
            }
        }

        // CONTROLLER
        if(!InputManager.isOn(InputManager.Source.STICK_X) && !InputManager.isOn(InputManager.Source.STICK_CIRCLE)) {
            double rawxl = InputManager.getRawValue(InputManager.Source.STICK_LX);
            double rawyl = InputManager.getRawValue(InputManager.Source.STICK_LY);
            double rawzl = InputManager.getRawValue(InputManager.Source.STICK_L2);

            double rawxr = InputManager.getRawValue(InputManager.Source.STICK_RX);
            double rawyr = InputManager.getRawValue(InputManager.Source.STICK_RY);

            double scale = 50.0*dt;  // TODO something better?
            double dxl = rawxl * -scale;
            double dyl = rawyl * -scale;
            double dzl = rawzl * scale;

            double dxr = rawxr * scale;
            double dyr = rawyr * scale;

            if(dxr!=0 || dyr!=0 || dxl!=0 || dyl!=0 || dzl!=0) {
                //Log.message("stick");
                isCurrentlyMoving=true;
                orbitCamera(dxr,dyr);
                adjustOrbitPoint(dzl);
                truckCamera(dxl);
                pedestalCamera(dyl);
            }
        }
    }

    /**
     *  adjust the camera position to orbit around a point 'zoom' in front of the camera
     *  relies on the Z axis of the matrix BEFORE any rotations are applied.
     * @param dx
     * @param dy
     */
    private void orbitCamera(double dx, double dy) {
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

    // translate relative to camera's current orientation
    private void pedestalCamera(double dy) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d vy = MatrixHelper.getYAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        double zSq = Math.sqrt(orbitDistance.get())*0.01;
        vy.scale(zSq* dy);
        p.add(vy);
        setPosition(p);
    }

    // translate relative to camera's current orientation
    private void truckCamera(double dx) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d vx = MatrixHelper.getXAxis(pose.getWorld());
        Vector3d p = pose.getPosition();
        double zSq = Math.sqrt(orbitDistance.get())*0.01;
        vx.scale(zSq*-dx);
        p.add(vx);
        setPosition(p);
    }

    /**
     * up and down to fly forward and back
     */
    private void dollyCamera(double dy) {
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
     * dolly the camera forward/back relative to the orbit point.
     * The orbit point is 'zoom' distance in front of the camera.
     * @param scale how much to dolly
     */
    private void adjustOrbitPoint(double scale) {
        isCurrentlyMoving=true;
        // get new and old scale
        double oldScale = orbitDistance.get();
        double newScale = oldScale + scale*3.0;

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
        jo.put("snapDegrees",snapDegrees.toJSON());
        jo.put("snapDeadZone",snapDeadZone.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        pan.parseJSON(jo.getJSONObject("pan"));
        tilt.parseJSON(jo.getJSONObject("tilt"));
        orbitDistance.parseJSON(jo.getJSONObject("zoom"));
        snapDegrees.parseJSON(jo.getJSONObject("snapDegrees"));
        snapDeadZone.parseJSON(jo.getJSONObject("snapDeadZone"));
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
    /**
     * A Component may offer one or more {@link AbstractEntity} visual elements for the User to manipulate.
     * it does so by Decorating the given {@link ViewPanel} with these elements.
     *
     * @param view the ViewPanel to decorate.
     */
    public void getView(ViewPanel view) {
        view.pushStack("Camera", true);
        view.add(isVisible);
        view.add(orbitDistance);
        view.popStack();
        super.getView(view);
    }
}
