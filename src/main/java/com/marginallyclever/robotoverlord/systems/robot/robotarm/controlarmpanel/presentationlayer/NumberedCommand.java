package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer;

/**
 * A command with a line number and checksum.  Used by {@link PresentationLayer} to send commands to the robot
 * without flooding the serial port.
 *
 * @author Dan Royer
 */
public class NumberedCommand {
	// for quick retrieval
	public int lineNumber;
	// the complete command with line number and checksum
	public String command;

	public NumberedCommand(int number, String str) {
		lineNumber = number;
		command=str;
	}
}
