package com.marginallyclever.ro3.node.nodes.bsdf;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.ScatterRecord;
import com.marginallyclever.ro3.raypicking.RayHit;

import java.util.Random;

/**
 * <p>{@link Lambertian} is a simple diffuse material that scatters light uniformly in all directions.</p>
 * <p>It is often used to simulate matte surfaces.</p>
 */
public class Lambertian implements BSDF {
    public ScatterRecord scatter(Ray ray, RayHit hitRecord, Random random) {
        return new ScatterRecord();
    }
}
