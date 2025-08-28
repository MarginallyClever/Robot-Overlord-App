package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;

import javax.vecmath.Vector3d;

/**
 * Represents a record of scattering events in a path tracer.
 * This class is currently empty and serves as a placeholder for future implementation.
 */
public class ScatterRecord {
    public enum ScatterType {
        DIFFUSE, SPECULAR, REFRACTIVE
    };
    public ScatterType type;
    public Ray ray; // scattered ray
    public final ColorDouble attenuation; // attenuation factor
    public double pdf;
    public boolean isSpecular;

    public ScatterRecord(Ray ray, ColorDouble attenuation, double pdf, boolean isSpecular) {
        this.ray = ray;
        this.attenuation = attenuation;
        this.pdf = pdf;
        this.isSpecular = isSpecular;
    }
}
