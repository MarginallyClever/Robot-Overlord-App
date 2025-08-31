package com.marginallyclever.ro3.apps.pathtracer;

import javax.vecmath.Vector3d;
import java.util.Random;
import java.util.SplittableRandom;

public class PathTracerHelper {
    /**
     * @return a random vector on the unit sphere
     */
    public static Vector3d getRandomUnitVector(HaltonWithMemory halton) {
        double t1 = halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_U) * 2.0 * Math.PI;
        double y = (halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_V) - 0.5) * 2.0;
        double t2 = Math.sqrt(1.0 - y*y);
        var x = t2 * Math.cos(t1);
        var z = t2 * Math.sin(t1);
        return new Vector3d(x, y, z);
    }

    /**
     * Get a random direction on the hemisphere defined by the normal, with cosine-weighted distribution.
     * This is used for diffuse reflection.
     * @param halton
     * @param normal
     * @return
     */
    public static Vector3d getRandomCosineWeightedHemisphere(HaltonWithMemory halton, Vector3d normal) {
        // Sample random numbers
        double u1 = halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_U);
        double u2 = halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_V);

        // Convert to polar coordinates (cosine-weighted)
        double r = Math.sqrt(u1);
        double theta = 2 * Math.PI * u2;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        double z = Math.sqrt(1 - u1);

        // Build orthonormal basis (tangent, bitangent, normal)
        Vector3d n = new Vector3d(normal);
        n.normalize();

        Vector3d tangent;
        if (Math.abs(n.z) < 0.999) {
            tangent = new Vector3d(-n.y, n.x, 0.0); // perpendicular
        } else {
            tangent = new Vector3d(0.0, 1.0, 0.0);
            tangent.cross(tangent, n);
        }
        tangent.normalize();

        Vector3d bitangent = new Vector3d();
        bitangent.cross(normal, tangent);

        // Transform local sample to world space
        Vector3d direction = new Vector3d();
        direction.scale(x, tangent);
        direction.scaleAdd(y, bitangent, direction);
        direction.scaleAdd(z, normal, direction);

        return direction;
    }

    /**
     * <p>Get a random direction on the hemisphere defined by the normal.</p>
     * <p>This is used for diffuse reflection.</p>
     * @param normal the normal of the surface at the hit point
     * @return a random direction on the hemisphere defined by the normal
     */
    public static Vector3d getRandomUnitHemisphere(HaltonWithMemory halton,Vector3d normal) {
        Vector3d newDir = getRandomUnitVector(halton);
        // if the random direction is facing the wrong way, flip it.
        if( newDir.dot(normal) < 0) {
            newDir.negate();
        }
        return newDir;
    }
}
