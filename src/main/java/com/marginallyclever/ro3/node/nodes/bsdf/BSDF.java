package com.marginallyclever.ro3.node.nodes.bsdf;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.ScatterRecord;
import com.marginallyclever.ro3.raypicking.RayHit;

import java.util.Random;

public interface BSDF {
    ScatterRecord scatter(Ray ray, RayHit hitRecord, Random random);
}
