package com.marginallyclever.ro3.apps.pathtracer.halton;

import java.util.SplittableRandom;
import java.util.Arrays;

/**
 * <p>In practice I get better results with just random numbers.</p>
 * <p>{@link HaltonWithMemory} is a {@link HaltonSequence} with memory for multiple channels.  Each channel has its
 * own index counter stored in memory.  This is useful for path tracing where different dimensions (channels)
 * need to be sampled independently but consistently across multiple samples.</p>
 */
public class HaltonWithMemory {
    // counters for each channel; these are sample indices into the Halton sequence
    private final int[] indices = new int[8];

    public HaltonWithMemory() {
        Arrays.fill(indices, 0);
    }

    public void resetMemory(long seed) {
        var source = new SplittableRandom(seed);
        for (int i = 0; i < indices.length; i++) {
            // initialize each channel to a different starting index produced from the seed
            indices[i] = source.nextInt() & Integer.MAX_VALUE; // ensure non-negative
        }
    }

    public double nextDouble(int channel) {
        if (channel >= indices.length) {
            throw new IllegalArgumentException("Channel too high, not enough memory.");
        }
        int idx = indices[channel]++;
        return HaltonSequence.sample(idx, channel);
    }
}
