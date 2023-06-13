package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;

/**
 * Adjustable form of rotary stewart platform.  All dimensions can be tweaked from the control panel.
 * @author Dan Royer
 * @since 2022-06-27
 */
@Deprecated
public class RotaryStewartPlatformAdjustable extends RotaryStewartPlatform {
	public RotaryStewartPlatformAdjustable() {
		super();
	}

	@Override
	public void update(double dt) {
		calculateEndEffectorPointsOneTime();
		calculateMotorAxlePointsOneTime();

		super.update(dt);
	}
}
