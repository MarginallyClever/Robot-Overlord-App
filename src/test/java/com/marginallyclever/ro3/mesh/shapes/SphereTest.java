package com.marginallyclever.ro3.mesh.shapes;

import org.junit.jupiter.api.Test;

public class SphereTest {
    @Test
    public void constructor() {
        var sphere = new Sphere();
        assert(sphere.vertexArray.size() == ((sphere.detail-2) * (sphere.detail*2)+2)*3 );
    }
}
