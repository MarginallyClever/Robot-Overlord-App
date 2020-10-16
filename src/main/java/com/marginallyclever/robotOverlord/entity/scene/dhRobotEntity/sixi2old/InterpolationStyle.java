package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2old;

public enum InterpolationStyle {
	LINEAR_FK(0,"Linear FK"),
	LINEAR_IK(1,"Linear IK"),
	JACOBIAN(2,"Jacobian IK");
	
	private int number;
	private String name;
	private InterpolationStyle(int n,String s) {
		number=n;
		name=s;
	}
	final public int toInt() {
		return number;
	}
	final public String toString() {
		return name;
	}
	static public String [] getAll() {
		InterpolationStyle[] allModes = InterpolationStyle.values();
		String[] labels = new String[allModes.length];
		for(int i=0;i<labels.length;++i) {
			labels[i] = allModes[i].toString();
		}
		return labels;
	}
}