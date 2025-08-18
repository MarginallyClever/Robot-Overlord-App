package com.marginallyclever.ro3.apps.pathtracer;

import javax.vecmath.Vector3d;
import java.util.Random;
import java.util.SplittableRandom;

public class PathTracerHelper {
    /**
     * @return a random vector on the unit sphere
     */
    public static Vector3d getRandomUnitVector(Random random) {
        double t1 = random.nextDouble() * 2.0 * Math.PI;
        var y = (random.nextDouble() - 0.5) * 2.0;
        double t2 = Math.sqrt(1.0 - y*y);
        var x = t2 * Math.cos(t1);
        var z = t2 * Math.sin(t1);
        return new Vector3d(x, y, z);
    }

    public static Vector3d getRandomCosineWeightedHemisphere(SplittableRandom random, Vector3d normal) {
        // Sample random numbers
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();

        // Convert to polar coordinates (cosine-weighted)
        double r = Math.sqrt(u1);
        double theta = 2 * Math.PI * u2;

        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        double z = Math.sqrt(1 - u1);

        // Create local coordinate system (normal, tangent, bitangent)
        Vector3d tangent = new Vector3d();
        if (Math.abs(normal.x) > 0.1) {
            tangent.set(0, 1, 0);
        } else {
            tangent.set(1, 0, 0);
        }
        tangent.cross(tangent, normal);
        tangent.normalize();

        Vector3d bitangent = new Vector3d();
        bitangent.cross(normal, tangent);

        // Transform local sample to world space
        Vector3d direction = new Vector3d();
        direction.scale(x, tangent);
        direction.scaleAdd(y, bitangent, direction);
        direction.scaleAdd(z, normal, direction);
        direction.normalize();

        return direction;
    }

    /**
     * <p>Get a random direction on the hemisphere defined by the normal.</p>
     * <p>This is used for diffuse reflection.</p>
     * @param normal the normal of the surface at the hit point
     * @return a random direction on the hemisphere defined by the normal
     */
    public static Vector3d getRandomUnitHemisphere(Random random,Vector3d normal) {
        Vector3d newDir = getRandomUnitVector(random);
        // if the random direction is facing the wrong way, flip it.
        if( newDir.dot(normal) < 0) {
            newDir.negate();
        }
        return newDir;
    }
}
