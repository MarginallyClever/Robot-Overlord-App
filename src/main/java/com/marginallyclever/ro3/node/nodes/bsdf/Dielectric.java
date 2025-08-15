package com.marginallyclever.ro3.node.nodes.bsdf;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.ScatterRecord;
import com.marginallyclever.ro3.raypicking.RayHit;

import java.util.Random;

/**
 * {@link Dielectric} is a material that simulates the refractive properties of transparent materials like glass or
 * water.  It handles light transmission and reflection based on the index of refraction.
 */
public class Dielectric implements BSDF {
    public ScatterRecord scatter(Ray ray, RayHit hitRecord, Random random) {
        return new ScatterRecord();
    }
}
