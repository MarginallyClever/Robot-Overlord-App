package com.marginallyclever.robotoverlord.components.path;

import javax.vecmath.Point3d;

public interface WalkablePath<T> {
    double getDistanceMeasured();
    T get(double d);
}
