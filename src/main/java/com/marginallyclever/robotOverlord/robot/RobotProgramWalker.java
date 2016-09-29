package com.marginallyclever.robotOverlord.robot;

import java.util.ArrayList;
import java.util.List;

/**
 * RobotProgramWalker keeps track of the robot's current position in a RobotProgram.  Several robots might be following the same program.
 * This keeps them from stepping on each other.
 * @author danroyer
 *
 */
public class RobotProgramWalker {
	RobotProgram program;
	int index;
	
	public RobotProgramWalker(RobotProgram program) {
		this.program=program;
		index=0;
	}
	
	public void jumpAbs(int index) {
		this.index=index;
	}
	
	public void jumpRel(int offset) {
		index+=offset;
	}
	public RobotInstruction getInstruction() {
		if(program==null) return null;
		return program.get(index);
	}
}
