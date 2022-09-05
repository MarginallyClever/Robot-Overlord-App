package com.marginallyclever.robotOverlord.components;

import com.marginallyclever.robotOverlord.Component;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;

import javax.vecmath.Vector3d;

public class CameraComponent extends Component {
    private final DoubleEntity pan = new DoubleEntity("Pan",0);
    private final DoubleEntity tilt = new DoubleEntity("Tilt",0);
    private final DoubleEntity zoom = new DoubleEntity("Zoom",100);

    /**
     *
     * @return
     */
    public double getZoom() {
        return zoom.get();
    }

    public void setZoom(double arg0) {
        zoom.set(arg0);
    }

    @Deprecated
    public void setPosition(Vector3d target) {
        PoseComponent p = getEntity().getComponent(PoseComponent.class);
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
        PoseComponent p = getEntity().getComponent(PoseComponent.class);

        Vector3d forward = new Vector3d(target);
        forward.sub(p.getPosition());
        double xy = Math.sqrt(forward.x*forward.x + forward.y*forward.y);
        setPan(Math.toDegrees(Math.atan2(forward.x,forward.y)));
        setTilt(Math.toDegrees(Math.atan2(xy,forward.z))-90);
        Vector3d dp = new Vector3d();
        dp.sub(target,p.getPosition());
        this.zoom.set(dp.length());
    }
}
