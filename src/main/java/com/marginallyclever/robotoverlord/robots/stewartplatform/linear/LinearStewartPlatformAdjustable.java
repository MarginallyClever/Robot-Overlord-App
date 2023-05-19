package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

/**
 * A linear stewart platform is a 6DOF robot that can move in X, Y, Z, and rotate around X, Y, Z.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class LinearStewartPlatformAdjustable extends LinearStewartPlatformCore {
    public LinearStewartPlatformAdjustable() {
        super();
    }

    @Override
    public void render(GL2 gl2) {
        PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

        gl2.glPushMatrix();
        MatrixHelper.applyMatrix(gl2,myPose.getLocal());

        material.render(gl2);
        renderBase(gl2);
        renderTopPlate(gl2);
        drawDebugArms(gl2);
        gl2.glPopMatrix();

        super.render(gl2);
    }

    private void renderTopPlate(GL2 gl2) {
        double x = EE_X.get();
        double y = EE_Y.get();
        double z = EE_Z.get();
        double r = Math.sqrt(x*x + y*y);

        gl2.glPushMatrix();
        MatrixHelper.applyMatrix(gl2,getEndEffectorPose());
        PrimitiveSolids.drawCylinderAlongZ(gl2,z,r);
        gl2.glPopMatrix();
    }

    private void renderBase(GL2 gl2) {
        double x = BASE_X.get();
        double y = BASE_Y.get();
        double z = BASE_Z.get();
        double r = Math.sqrt(x * x + y * y);

        gl2.glColor3d(1, 1, 1);
        PrimitiveSolids.drawCylinderAlongZ(gl2, z, r);
    }

    @Override
    public void update(double dt) {
        calculateBasePointsOneTime();
        calculateEndEffectorPointsOneTime();

        super.update(dt);
    }

    @Deprecated
    public void getView(ComponentPanelFactory view) {
        view.add(BASE_X);
        view.add(BASE_Y);
        view.add(BASE_Z);
        view.add(EE_X);
        view.add(EE_Y);
        view.add(EE_Z);
        view.add(ARM_LENGTH);
        view.add(SLIDE_TRAVEL);

        super.getView(view);
    }
}
