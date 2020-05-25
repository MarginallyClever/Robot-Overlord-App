package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

public enum ControlMode {
	RECORD(0,"RECORD"),
	PLAYBACK(1,"PLAYBACK");

	private int modeNumber;
	private String modeName;
	private ControlMode(int n,String s) {
		modeNumber=n;
		modeName=s;
	}
	public int toInt() {
		return modeNumber;
	}
	public String toString() {
		return modeName;
	}
	static public String [] getAll() {
		ControlMode[] allModes = ControlMode.values();
		String[] labels = new String[allModes.length];
		for(int i=0;i<labels.length;++i) {
			labels[i] = allModes[i].toString();
		}
		return labels;
	}
}