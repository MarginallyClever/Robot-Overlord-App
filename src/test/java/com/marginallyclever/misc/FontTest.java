package com.marginallyclever.misc;

import com.marginallyclever.convenience.log.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class FontTest {
    private static final Logger logger = LoggerFactory.getLogger(FontTest.class);

    @Test
    public void testCompatibleFonts() {
    	Log.start();
        String s = "\u23EF";
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        logger.info("Total fonts: \t" + fonts.length);
        Assertions.assertNotEquals(0,Arrays.stream(fonts).filter(font -> font.canDisplayUpTo(s) < 0).count());
    }
}
