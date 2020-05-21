package com.marginallyclever.robotOverlord;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.marginallyclever.convenience.StringHelper;

public class StringHelperTest {
	@Test
	public void testDoubleToBytes() {
		assertArrayEquals(new byte[] { 63, -116, -52, -51}, StringHelper.floatToByteArray(1.1f));
	}

	@Test
	public void testBytesToDouble() {
		assertEquals(1.1f, StringHelper.byteArrayToFloat(new byte[] { 63, -116, -52, -51}), 0);
	}
}
