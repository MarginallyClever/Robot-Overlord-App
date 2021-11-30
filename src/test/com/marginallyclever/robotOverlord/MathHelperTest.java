package com.marginallyclever.robotOverlord;

import org.junit.Test;

import com.marginallyclever.convenience.MathHelper;

public class MathHelperTest {
	@Test
	public void testWrapDegrees() {
		System.out.println("testWrapDegrees start");
		for(double i=-360*2;i<=360*2;i+=10) {
			double v = MathHelper.wrapDegrees(i);
			System.out.println(i+"\t"+v);
			//assert(v>=-180 && v<=180);
		}
		System.out.println("testWrapDegrees end");
	}
}
