package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;

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
    public void render(GL3 gl) {
        PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

        gl.glPushMatrix();
        MatrixHelper.applyMatrix(gl,myPose.getLocal());

        material.render(gl);
        renderBase(gl);
        renderTopPlate(gl);
        drawDebugArms(gl);
        gl.glPopMatrix();

        super.render(gl);
    }

    private void renderTopPlate(GL3 gl) {
        double x = EE_X.get();
        double y = EE_Y.get();
        double z = EE_Z.get();
        double r = Math.sqrt(x*x + y*y);

        gl.glPushMatrix();
        MatrixHelper.applyMatrix(gl,getEndEffectorPose());
        PrimitiveSolids.drawCylinderAlongZ(gl,z,r);
        gl.glPopMatrix();
    }

    private void renderBase(GL3 gl) {
        double x = BASE_X.get();
        double y = BASE_Y.get();
        double z = BASE_Z.get();
        double r = Math.sqrt(x * x + y * y);

        gl.glColor3d(1, 1, 1);
        PrimitiveSolids.drawCylinderAlongZ(gl, z, r);
    }

    @Override
    public void update(double dt) {
        calculateBasePointsOneTime();
        calculateEndEffectorPointsOneTime();

        super.update(dt);
    }

    @Deprecated
    public void getView(ViewPanelFactory view) {
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
