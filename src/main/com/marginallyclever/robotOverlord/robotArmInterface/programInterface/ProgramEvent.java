package com.marginallyclever.robotOverlord.robotArmInterface.programInterface;

import java.util.Arrays;

import com.marginallyclever.convenience.StringHelper;

public class ProgramEvent {
	public static final String NAME = "ProgramEvent:";
	
	private double [] angles;
	
	public ProgramEvent(double [] m) {
		angles = Arrays.copyOf(m, m.length);
	}

	public ProgramEvent(ProgramEvent p) {
		this(p.angles);
	}
	
	public double [] getAngles() {
		return angles;
	}
	
	@Override
	public String toString() {
		return NAME+Arrays.toString(angles);
	}

	public static ProgramEvent valueOf(String line) {
		line = line.substring(NAME.length());
		if(line.startsWith("[")) line=line.substring(1);
		if(line.endsWith("]")) line=line.substring(0,line.length()-1);
		String [] parts = line.split(", ");
		double [] angles = new double[parts.length];
		for(int i=0;i<parts.length;++i) {
			angles[i] = Double.valueOf(parts[i]);
		}
		
		return new ProgramEvent(angles);
	}
	
	public String getFormattedDisplay() {
		String s = "";
		String add="";
		for(int i=0;i<angles.length;++i) {
			s+= add+StringHelper.formatDouble(angles[i]);
			add=", ";
		}
		return NAME+"["+s+"]";
	}
}
