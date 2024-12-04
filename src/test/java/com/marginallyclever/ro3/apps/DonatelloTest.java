package com.marginallyclever.ro3.apps;

import com.marginallyclever.ro3.apps.donatello.Donatello;
import com.marginallyclever.ro3.apps.donatello.GraphViewPanel;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Random;

public class DonatelloTest {
    @Test
    public void testWorldToScreenAndBack() {
        final int range = (int)1e6;
        GraphViewPanel donatello = new GraphViewPanel();

        for(int i=0;i<200;++i) {
            Point p = new Point((int)( Math.random()*range*2 - range),
                                (int)( Math.random()*range*2 - range));
            Point p2 = donatello.screenToWorld(donatello.worldToScreen(p));
            System.out.println("p="+p+" p2="+p2);
            assert p.equals(p2);
        }
    }
}
