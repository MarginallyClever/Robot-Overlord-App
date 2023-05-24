package com.marginallyclever.convenience.helpers;

import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MathHelperTest {
    private static final Logger logger = LoggerFactory.getLogger(MathHelperTest.class);
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}

    @Test
    @Disabled
    @Deprecated
    public void testWrapDegrees() {
        logger.info("testWrapDegrees start");
        for (double i = -360 * 2; i <= 360 * 2; i += 10) {
            double v = MathHelper.wrapDegrees(i);
            logger.info(i + "\t" + v);
            //assert(v>=-180 && v<=180);
        }
        logger.info("testWrapDegrees end");
    }
}
