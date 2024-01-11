package com.marginallyclever.robotoverlord.systems.render.gcodepath;

/**
 * A path that can be walked along.
 * @param <T> the type of point in the path.
 *
 */
public interface WalkablePath<T> {
    double getDistanceMeasured();
    T get(double d);
}
