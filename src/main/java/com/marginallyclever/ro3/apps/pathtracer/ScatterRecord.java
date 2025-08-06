package com.marginallyclever.ro3.apps.pathtracer;

import javax.vecmath.Vector3d;

/**
 * Represents a record of scattering events in a path tracer.
 * This class is currently empty and serves as a placeholder for future implementation.
 */
public class ScatterRecord {
    public enum ScatterType {
        NONE, // path stops
        EXPLICIT, // explicit scatter
        RANDOM, // random scatter
    }
    ScatterType type;
    Vector3d direction;
    double p;  // direciton probability
    ColorDouble attenuation; // attenuation factor

    public ScatterRecord() {
        this(ScatterType.NONE, new Vector3d(), 0.0, new ColorDouble(0,0,0,0));
    }

    public ScatterRecord(ScatterType type, Vector3d direction, double p, ColorDouble attenuation) {
        this.type = type;
        this.direction = new Vector3d(direction);
        this.p = p;
        this.attenuation = new ColorDouble(attenuation);
    }

    public ScatterRecord(Vector3d direction, ColorDouble attenuation) {
        this(ScatterType.EXPLICIT, direction, 1.0, attenuation);
    }

    public ScatterRecord(Vector3d direction, double p, ColorDouble attenuation) {
        this(ScatterType.EXPLICIT, direction, p, attenuation);
    }
}
