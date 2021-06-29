package com.marginallyclever.robotOverlord.moveTool;

public enum SlideDirection {
	SLIDE_XPOS(0,"X+"),
	SLIDE_XNEG(1,"X-"),
	SLIDE_YPOS(2,"Y+"),
	SLIDE_YNEG(3,"Y-");
	
	private int number;
	private String name;
	private SlideDirection(int n,String s) {
		number=n;
		name=s;
	}
	public int toInt() {
		return number;
	}
	public String toString() {
		return name;
	}
	static public String [] getAll() {
		SlideDirection[] allModes = SlideDirection.values();
		String[] labels = new String[allModes.length];
		for(int i=0;i<labels.length;++i) {
			labels[i] = allModes[i].toString();
		}
		return labels;
	}
}