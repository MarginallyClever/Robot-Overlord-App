package com.marginallyclever.misc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {
    @Test
    public void testLogger() {
        Logger logger = LoggerFactory.getLogger(LoggerTest.class);
        logger.info("This is a test.");
    }
}
