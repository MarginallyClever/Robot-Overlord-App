package com.marginallyclever.ro3.node.nodes.stewartplatform.linear;

import org.junit.jupiter.api.Test;

import javax.vecmath.Vector2d;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LinearStewartPlatform2Test {

    //@Test  // not a unit test
    public void testSearchBestScaleRunsAndRestoresOffsets() {
        LinearStewartPlatform2 platform = new LinearStewartPlatform2();

        // attach to create top/bottom and initial shape
        platform.onAttach();

        // record original offsets
        Vector2d origTop = platform.getTopOffset();
        Vector2d origBottom = platform.getBottomOffset();

        // capture stdout
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos)) {
            System.setOut(ps);

            // run the grid search (should complete without throwing)
            platform.searchBestScale(0.6, 1.4, 25);

            ps.flush();
        } finally {
            System.setOut(oldOut);
        }

        String out = baos.toString();
        // method prints a summary line prefixed with "searchBestScale result"
        assertTrue(out.contains("searchBestScale result"));

        System.out.println(out);

        // offsets should be restored to originals
        Vector2d afterTop = platform.getTopOffset();
        Vector2d afterBottom = platform.getBottomOffset();
        double eps = 1e-9;
        assertEquals(origTop.x, afterTop.x, eps);
        assertEquals(origTop.y, afterTop.y, eps);
        assertEquals(origBottom.x, afterBottom.x, eps);
        assertEquals(origBottom.y, afterBottom.y, eps);
    }


    //@Test  // not a unit test
    public void testSearchBestOffsetsByGradientDescentRunsAndRestoresOffsets() {
        LinearStewartPlatform2 platform = new LinearStewartPlatform2();

        // attach to create top/bottom and initial shape
        platform.onAttach();

        // record original offsets
        Vector2d origTop = platform.getTopOffset();
        Vector2d origBottom = platform.getBottomOffset();

        // capture stdout
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos)) {
            System.setOut(ps);

            // run the gradient descent search (should complete without throwing)
            platform.searchBestOffsetsByGradientDescent(1e-1, 1000, 1e-6);

            ps.flush();
        } finally {
            System.setOut(oldOut);
        }

        String out = baos.toString();
        // method prints a summary line prefixed with "searchBestOffsetsByGradientDescent result"
        assertTrue(out.contains("result"));

        System.out.println(out);

        // offsets should be restored to originals
        Vector2d afterTop = platform.getTopOffset();
        Vector2d afterBottom = platform.getBottomOffset();
        double eps = 1e-9;
        assertEquals(origTop.x, afterTop.x, eps);
        assertEquals(origTop.y, afterTop.y, eps);
        assertEquals(origBottom.x, afterBottom.x, eps);
        assertEquals(origBottom.y, afterBottom.y, eps);
    }
}