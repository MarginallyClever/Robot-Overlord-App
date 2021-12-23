package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.programInterface;

import java.io.Serializable;
import java.util.Arrays;

import com.marginallyclever.convenience.StringHelper;

public class ProgramEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "ProgramEvent:";
	private double [] angles;
	private String nickname="";
	
	public ProgramEvent(double [] m) {
		angles = Arrays.copyOf(m, m.length);
	}

	public ProgramEvent(ProgramEvent p) {
		this(p.angles);
		this.nickname = new String(p.nickname);
	}
	
	public double [] getAngles() {
		return angles;
	}
	
	@Override
	public String toString() {
		return NAME+Arrays.toString(angles) + (nickname.isBlank()?"":" ")+nickname;
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
		return NAME+"["+s+"]" + (nickname.isBlank()?"":" ")+nickname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
