package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;

public interface Demo {
	void execute(Scene scene);

	String getName();
}
