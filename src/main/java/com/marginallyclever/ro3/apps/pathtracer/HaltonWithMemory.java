package com.marginallyclever.ro3.apps.pathtracer;

import com.github.alexeyr.pcg.Pcg32;

/**
 * Halton sequence with memory for multiple channels.
 * Each channel has its own index counter stored in memory.
 * This is useful for path tracing where different dimensions (channels)
 * need to be sampled independently but consistently across multiple samples.
 */
public class HaltonWithMemory {
    public Pcg32 rand = new Pcg32();
    private final int [] memory = new int[8];

    public HaltonWithMemory() {}

    public void resetMemory(long seed) {
        rand.seed(seed,1);
        for(int i=0;i<memory.length;i++) {
            memory[i] = Math.abs(rand.nextInt());
        }
    }

    public double nextDouble(int channel) {
        if(channel>=memory.length) {
            throw new IllegalArgumentException("Channel too high, not enough memory.");
        }
        int index = Math.abs(memory[channel]++);
        return Halton.sample(channel,index);
    }
}
