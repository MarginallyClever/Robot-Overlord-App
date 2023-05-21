package com.marginallyclever.convenience.swing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DialTest {
    @Test
    public void testDial() {
        Dial d = new Dial();
        d.setValue(10);
        Assertions.assertEquals(10, d.getValue());
        d.setChange(-5);
        Assertions.assertEquals(-5, d.getChange());
        Assertions.assertEquals(5, d.getValue());
    }

    @Test
    public void testDial2() {
        Dial d = new Dial();
        d.setChange(5);
        Assertions.assertEquals(5, d.getChange());
        Assertions.assertEquals(5, d.getValue());
        d.setValue(-10);
        Assertions.assertEquals(-15, d.getChange());
        Assertions.assertEquals(350, d.getValue());
        d.setValue(360);
        Assertions.assertEquals(0, d.getValue());
    }
}
