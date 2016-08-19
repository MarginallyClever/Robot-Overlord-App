package com.marginallyclever.robotOverlord.robot;

/**
 * Base class for instructions in a robot program.  Instructions should closely mirror Scratch.
 * - wait for signal X
 * - send signal Y
 * - move to position
 * - send custom instruction to robot
 * - loop over sub-instructions
 * - branch condition
 * - etc...
 * @author danroyer
 *
 */
public class RobotInstructionControlBlockForever implements RobotInstruction {
	public String getName() {
		return "Forever";
	}
	
}
