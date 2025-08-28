package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.raypicking.RayHit;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RayXY} is used to track the average color of each pixel as the path traced image is generated.
 */
public class RayXY {
    public int x;
    public int y;
    public int samples = 0;
    public final ColorDouble colorSum = new ColorDouble(0, 0, 0);
    public final ColorDouble colorAverage = new ColorDouble(0, 0, 0);
    public double depth;  // depth of first hit in the scene.
    public Vector3d normal;
    // history of paths traced for this pixel (for debugging)
    public boolean debug = false;
    public Map<Ray, RayHit> rays = new HashMap<>();

    public RayXY(int x, int y) {
        this.x = x;
        this.y = y;
        this.depth = Double.POSITIVE_INFINITY;
    }

    public void addRayRecord(Ray ray, RayHit hit) {
        if(debug && samples<1) {
            rays.put(ray, hit);
        }
    }

    /**
     * Add the results of a path trace to this pixel and recalculate the tone mapped average.
     * @param traceResult the result of the path trace.
     */
    public void add(ColorDouble traceResult) {
        colorSum.add(traceResult);
        samples++;
        // recalculate the average.
        colorAverage.set(colorSum);
        colorAverage.scale(1.0/samples);
        // tone map the result.
        toneMap(colorAverage);
    }

    private void toneMap(ColorDouble d) {
        //acesApprox(d);
        shlickUniformRationalQuantization(d,2.0);
    }

    private void acesApprox(ColorDouble v) {
        v.scale(0.6);
        double a = 2.51;
        double b = 0.03;
        double c = 2.43;
        double d = 0.59;
        double e = 0.14;
        v.r = Math.max(0,Math.min(1, (v.r * (a*v.r+b)) / (v.r * (c*v.r+d)+e) ));
        v.g = Math.max(0,Math.min(1, (v.g * (a*v.g+b)) / (v.g * (c*v.g+d)+e) ));
        v.b = Math.max(0,Math.min(1, (v.b * (a*v.b+b)) / (v.b * (c*v.b+d)+e) ));
    }


    // sRGB to XYZ (D65)
    private static final double[][] M_RGB2XYZ = {
            {0.4124564, 0.3575761, 0.1804375},
            {0.2126729, 0.7151522, 0.0721750},
            {0.0193339, 0.1191920, 0.9503041}
    };

    // XYZ to sRGB (D65)
    private static final double[][] M_XYZ2RGB = {
            { 3.2404542, -1.5371385, -0.4985314},
            {-0.9692660,  1.8760108,  0.0415560},
            { 0.0556434, -0.2040259,  1.0572252}
    };

    // Multiply a 3x3 matrix by a vector
    private static double[] mul(double[][] m, double[] v) {
        return new double[] {
                m[0][0]*v[0] + m[0][1]*v[1] + m[0][2]*v[2],
                m[1][0]*v[0] + m[1][1]*v[1] + m[1][2]*v[2],
                m[2][0]*v[0] + m[2][1]*v[1] + m[2][2]*v[2]
        };
    }

    // Schlick luminance warp
    private static double warpLuminance(double Y, double b, double Ymax) {
        return (b * Y) / ((b - 1.0) * Y + Ymax);
    }

    /**
     * Convert the color to sRGB and apply a uniform rational quantization.
     * @param v the color to quantize.
     */
    private void shlickUniformRationalQuantization(ColorDouble v,double b) {
        // Step 1: RGB → XYZ
        double[] xyz = mul(M_RGB2XYZ, new double[]{v.r, v.g, v.b});

        // Step 2: Warp luminance (Y)
        double Y = xyz[1];
        double Ymax = 1.0; // normalized
        double Yp = warpLuminance(Y, b, Ymax);

        // Step 3: Scale X and Z to preserve chromaticity
        double scale = (Y == 0.0) ? 0.0 : (Yp / Y);
        xyz[0] *= scale;
        xyz[1] = Yp;
        xyz[2] *= scale;

        // Step 4: XYZ → RGB
        double[] rgb = mul(M_XYZ2RGB, xyz);

        v.set(rgb[0], rgb[1], rgb[2], v.a);
    }

    public int getSamples() {
        return samples;
    }
}
