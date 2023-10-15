package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;

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
    public void update(double dt) {
        calculateBasePointsOneTime();
        calculateEndEffectorPointsOneTime();

        super.update(dt);
    }

    @Deprecated
    public void getView(ComponentSwingViewFactory view) {
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
