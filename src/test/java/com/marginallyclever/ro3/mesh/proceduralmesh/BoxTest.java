package com.marginallyclever.ro3.mesh.proceduralmesh;

import org.junit.jupiter.api.Test;

public class BoxTest {
    @Test
    public void constructor() {
        var box = new Box();
        assert(box.height == 1);
        assert(box.width == 1);
        assert(box.length == 1);

        box = new Box(2,3,4);
        assert(box.width == 2);
        assert(box.length == 3);
        assert(box.height == 4);
    }
}
