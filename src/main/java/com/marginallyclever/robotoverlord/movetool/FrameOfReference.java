package com.marginallyclever.robotoverlord.movetool;

public enum FrameOfReference {
	SUBJECT(0,"SUBJECT"),
	CAMERA(1,"CAMERA"),
	WORLD(2,"WORLD");
	
	private int number;
	private String name;
	private FrameOfReference(int n,String s) {
		number=n;
		name=s;
	}
	public int toInt() {
		return number;
	}
	@Override
	public String toString() {
		return name;
	}
	static public String [] getAll() {
		FrameOfReference[] allModes = FrameOfReference.values();
		String[] labels = new String[allModes.length];
		for(int i=0;i<labels.length;++i) {
			labels[i] = allModes[i].toString();
		}
		return labels;
	}
}