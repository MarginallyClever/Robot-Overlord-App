package com.marginallyclever.convenience;

public class Units {
	public double mm2inch(double x) {	return x/25.4;	}
	public double inch2mm(double x) {	return x*25.4;	}
	
	// from 0..1 to 0...255
	public double unit2oneByte(double x) {	return x*255;	}
	// from 0...255 to 0..1
	public double oneByte2Unit(double x) {	return x/255;	}
}
