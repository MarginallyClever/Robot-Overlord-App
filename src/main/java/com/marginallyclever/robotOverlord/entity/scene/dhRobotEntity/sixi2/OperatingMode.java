package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

public enum OperatingMode {
	LIVE(0,"LIVE"),
	SIM(1,"SIM");

	private int modeNumber;
	private String modeName;
	private OperatingMode(int n,String s) {
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
		OperatingMode[] allModes = OperatingMode.values();
		String[] labels = new String[allModes.length];
		for(int i=0;i<labels.length;++i) {
			labels[i] = allModes[i].toString();
		}
		return labels;
	}
}