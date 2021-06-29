package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;

import org.junit.Test;

public class Matrix4dTest {
	@Test
	public void testQuatFail() {
		Matrix4d A = new Matrix4d(
				6.123233995736766E-17, 6.123233995736766E-17, 1.0, 44.5,
				-6.123233995736766E-17, -1.0, 6.123233995736766E-17, 1.3949339365687892E-16,
				1.0, -6.123233995736766E-17, -6.123233995736766E-17, 61.967099999999995,
				0.0, 0.0, 0.0, 1.0);
		Quat4d B= new Quat4d();
		A.get(B);
		assert(Double.isNaN(B.x));
		assert(Double.isNaN(B.y));
		assert(Double.isNaN(B.z));
		assert(Double.isNaN(B.w));
	}
}
