package com.marginallyclever.robotOverlord.demos;

import com.marginallyclever.robotOverlord.RobotOverlord;

public abstract interface Demo {
	public abstract void execute(RobotOverlord ro);

	public abstract String getName();
}
