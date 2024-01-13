package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

/**
 * Adjustable form of rotary stewart platform.  All dimensions can be tweaked from the control panel.
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
