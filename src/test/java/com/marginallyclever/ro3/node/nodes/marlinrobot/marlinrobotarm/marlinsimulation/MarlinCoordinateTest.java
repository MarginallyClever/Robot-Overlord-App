package com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.marlinsimulation;

import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.marlinsimulation.MarlinCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarlinCoordinateTest {
    @Test
    public void constructor() {
        MarlinCoordinate c = new MarlinCoordinate();
        for (double d : c.p) {
            assert (d == 0);
        }

        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            c.p[i] = i;
        }

        MarlinCoordinate c2 = new MarlinCoordinate(c);
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            assert(c2.p[i] == i);
        }
    }

    @Test
    public void maths() {
        MarlinCoordinate a = new MarlinCoordinate();
        MarlinCoordinate b = new MarlinCoordinate();
        MarlinCoordinate c = new MarlinCoordinate();

        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            a.p[i] = i;
            b.p[i] = i;
            c.p[i] = i;
        }

        a.add(b);
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            assert(a.p[i] == 2*i);
        }

        a.sub(b);
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            assert(a.p[i] == i);
        }

        a.sub(b,c);
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            assert(a.p[i] == 0);
        }

        a.set(b);
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            assert(a.p[i] == i);
        }

        a.normalize();
        var blen = b.length();
        for(int i=0;i<MarlinCoordinate.SIZE;++i) {
            Assertions.assertEquals(a.p[i], b.p[i]/blen,1e-5);
        }

        assert(a.length() == 1);

        a.scale(2);
        assert(a.length() == 2);
    }
}
