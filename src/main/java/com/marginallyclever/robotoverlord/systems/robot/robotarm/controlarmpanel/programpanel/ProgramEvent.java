package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.programpanel;

import com.marginallyclever.convenience.helpers.StringHelper;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A program event is a single point in time in a program.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ProgramEvent implements Serializable {
	public static final String NAME = "ProgramEvent:";
	private final double [] angles;
	private String nickname="";
	
	public ProgramEvent(double [] m) {
		angles = Arrays.copyOf(m, m.length);
	}

	public ProgramEvent(ProgramEvent p) {
		this(p.angles);
		this.nickname = p.nickname;
	}

	public double [] getAngles() {
		return angles;
	}
	
	@Override
	public String toString() {
		return NAME+Arrays.toString(angles) + (nickname.isBlank()?"":" "+nickname);
	}

	public static ProgramEvent valueOf(String line) {
		line = line.substring(NAME.length());
		if(line.startsWith("[")) line=line.substring(1);
		if(line.endsWith("]")) line=line.substring(0,line.length()-1);
		String [] parts = line.split(", ");
		double [] angles = new double[parts.length];
		for(int i=0;i<parts.length;++i) {
			angles[i] = Double.parseDouble(parts[i]);
		}
		
		return new ProgramEvent(angles);
	}
	
	public String getFormattedDisplay() {
		StringBuilder stringBuilder = new StringBuilder(NAME+"[");
		String add="";
		for (double angle : angles) {
			stringBuilder.append(add).append(StringHelper.formatDouble(angle));
			add = ", ";
		}
		stringBuilder.append("]").append(nickname.isBlank() ? "" : " ").append(nickname);

		return stringBuilder.toString();
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
