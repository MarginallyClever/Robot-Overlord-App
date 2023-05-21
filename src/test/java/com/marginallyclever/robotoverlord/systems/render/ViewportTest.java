package com.marginallyclever.robotoverlord.systems.render;

import com.marginallyclever.robotoverlord.systems.render.Viewport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ViewportTest {
    private static Viewport var0;
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        var0 = new Viewport();
        var0.setCanvasHeight(600);
        var0.setCanvasWidth(800);
    }

    @Test
    public void testCenter() {
        testShared( 400, 300, 0, 0);
    }

    @Test
    public void testTopLeft() {
        testShared( 0, 0, -1, 1);
    }

    @Test
    public void testBottomRight() {
        testShared( 800, 600, 1, -1);
    }

    @Test
    public void testBottomLeft() {
        testShared( 0, 600, -1, -1);
    }

    @Test
    public void testTopRight() {
        testShared(800,  0,1,1);
    }

    private void testShared(int x, int y, double nx, double ny) {
        var0.setCursor(x, y);
        double[] cursor = var0.getCursor();
        Assertions.assertEquals(x,cursor[0]);
        Assertions.assertEquals(y,cursor[1]);

        double[] cursorNormalized = var0.getCursorAsNormalized();
        Assertions.assertEquals(nx,cursorNormalized[0]);
        Assertions.assertEquals(ny,cursorNormalized[1]);
    }
}
