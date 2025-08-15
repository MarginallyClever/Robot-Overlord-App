package com.marginallyclever.ro3.node.nodes.bsdf;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.ScatterRecord;
import com.marginallyclever.ro3.raypicking.RayHit;

import java.util.Random;

/**
 * {@link Metal} is a material that simulates the reflective properties of metals.
 * It reflects light in a specular manner, often with a shiny appearance.
 */
public class Metal implements BSDF {
    public ScatterRecord scatter(Ray ray, RayHit hitRecord, Random random) {
        return new ScatterRecord();
    }
}