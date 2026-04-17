package com.marginallyclever.ro3.apps.pathtracer.halton;

import java.util.SplittableRandom;

/**
 * <p>{@link HaltonWithMemoryOld} is a {@link HaltonSequence} with memory for multiple channels.  Each channel has its
 * own index counter stored in memory.  This is useful for path tracing where different dimensions (channels)
 * need to be sampled independently but consistently across multiple samples.</p>
 * <p>In practice I get better results with just random numbers.  This class is only here for reference.</p>
 */
@Deprecated
public class HaltonWithMemoryOld {

    private final SplittableRandom [] memory = new SplittableRandom[8];

    public HaltonWithMemoryOld() {
        for(int i=0;i<memory.length;i++) {
            memory[i] = new SplittableRandom();
        }
    }

    public void resetMemory(long seed) {
        var source = new SplittableRandom(seed);
        for(int i=0;i<memory.length;i++) {
            memory[i] = new SplittableRandom(source.nextLong());
        }
    }

    public double nextDouble(int channel) {
        if(channel>=memory.length) {
            throw new IllegalArgumentException("Channel too high, not enough memory.");
        }
        return memory[channel].nextDouble();
    }
}
