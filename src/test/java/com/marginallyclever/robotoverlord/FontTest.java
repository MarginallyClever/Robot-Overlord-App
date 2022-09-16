package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import com.marginallyclever.convenience.log.Log;

import java.awt.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class FontTest {

    @Test
    public void testCompatibleFonts() {
    	Log.start();
        String s = "\u23EF";
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Log.message("Total fonts: \t" + fonts.length);
        Assertions.assertNotEquals(0,Arrays.stream(fonts).filter(font -> font.canDisplayUpTo(s) < 0).count());
    }

}
