package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.log.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class MathHelperTest {
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}

    @Test
    @Deprecated
    public void testWrapDegrees() {
        Log.message("testWrapDegrees start");
        for (double i = -360 * 2; i <= 360 * 2; i += 10) {
            double v = MathHelper.wrapDegrees(i);
            Log.message(i + "\t" + v);
            //assert(v>=-180 && v<=180);
        }
        Log.message("testWrapDegrees end");
    }
}
