package com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.marlinInterface;

public class MarlinCommand {
	// for quick retrieval
	public int lineNumber;
	// the complete command with line number and checksum
	public String command;

	public MarlinCommand(int number, String str) {
		lineNumber = number;
		command=str;
	}
}
