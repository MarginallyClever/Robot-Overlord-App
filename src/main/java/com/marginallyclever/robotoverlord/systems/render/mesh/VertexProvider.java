package com.marginallyclever.robotoverlord.systems.render.mesh;

import javax.vecmath.Vector3d;

public interface VertexProvider {
    Vector3d provideVertex(int index);
    Vector3d provideNormal(int index);
    int provideCount();
}