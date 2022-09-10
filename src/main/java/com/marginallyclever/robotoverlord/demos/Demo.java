package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.RobotOverlord;

public interface Demo {
	void execute(RobotOverlord ro);

	String getName();
}
