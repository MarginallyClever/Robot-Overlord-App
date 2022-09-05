package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.RobotOverlord;

public abstract interface Demo {
	public abstract void execute(RobotOverlord ro);

	public abstract String getName();
}
