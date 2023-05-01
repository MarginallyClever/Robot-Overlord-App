package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import javax.vecmath.Point3d;

public interface WalkablePath<T> {
    double getDistanceMeasured();
    T get(double d);
}
