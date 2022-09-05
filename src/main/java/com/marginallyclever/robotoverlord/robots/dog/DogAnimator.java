package com.marginallyclever.robotoverlord.robots.dog;

import com.jogamp.opengl.GL2;

public interface DogAnimator {
	public void walk(DogRobot robot,GL2 gl2);

	public String getName();
}
