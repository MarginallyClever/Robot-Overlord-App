package com.marginallyclever.robotoverlord.systems.render.mesh;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

@Deprecated
public interface VertexProvider {
    Point3d provideVertex(int index);
    Vector3d provideNormal(int index);
    int provideCount();
}