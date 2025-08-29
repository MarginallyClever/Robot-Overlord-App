package com.marginallyclever.ro3.apps.pathtracer;

/**
 * Halton sequence generator.
 *
 * Each dimension has its own prime base (2,3,5,...).
 * Call sample(index, dim) to get the dim-th dimension sample
 * for the given index (both zero-based).
 */
public class Halton {
    // Common small primes for dimensions
    private static final int[] PRIMES = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
            31, 37, 41, 43, 47, 53, 59, 61, 67, 71
    };

    /**
     * Return the Halton sequence value for a given index and dimension.
     * @param index sample index (0-based)
     * @param dim   dimension index (0 = base 2, 1 = base 3, ...)
     * @return value in [0,1)
     */
    public static double sample(int index, int dim) {
        if (dim >= PRIMES.length) {
            throw new IllegalArgumentException("Dimension too high, not enough primes.");
        }
        int base = PRIMES[dim];
        double result = 0.0;
        double f = 1.0;
        int i = index + 1; // +1 to avoid returning 0 on the first sample
        while (i > 0) {
            f /= base;
            result += f * (i % base);
            i /= base;
        }
        return result;
    }

    // Convenience method for 2D sample
    public static double[] sample2D(int index) {
        return new double[] { sample(index, 0), sample(index, 1) };
    }

    // Convenience method for 3D sample
    public static double[] sample3D(int index) {
        return new double[] { sample(index, 0), sample(index, 1), sample(index, 2) };
    }

    // Example usage
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            double[] p = sample2D(i);
            System.out.printf("%2d: (%.6f, %.6f)%n", i, p[0], p[1]);
        }
    }
}
