package com.marginallyclever.robotOverlord.textInterfaces;

import java.util.Arrays;

public class ProgramEvent {
	public static final String NAME = "ProgramEvent:";
	
	private double [] myAngles;
	
	public ProgramEvent(double [] angles) {
		myAngles = Arrays.copyOf(angles,angles.length);
	}
	
	public double [] getAngles() {
		return myAngles;
	}
	
	@Override
	public String toString() {
		return NAME+Arrays.toString(myAngles);
	}

	public static ProgramEvent valueOf(String line) {
		line = line.substring(NAME.length());
		String [] parts = line.split(" ");
		double [] angles = new double[parts.length];
		for(int i=0;i<parts.length;++i) {
			angles[i] = Double.valueOf(parts[i]);
		}

		return new ProgramEvent(angles);
	}
}
