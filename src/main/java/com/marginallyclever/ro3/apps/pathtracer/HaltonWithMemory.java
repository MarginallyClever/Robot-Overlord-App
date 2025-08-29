package com.marginallyclever.ro3.apps.pathtracer;

import java.util.Random;

public class HaltonWithMemory {
    private final int [] memory = new int[8];

    public HaltonWithMemory() {
        for(int i=0;i<8;i++) {
            memory[i]=0;
        }
    }

    public void resetMemory(long seed) {
        Random rand = new Random(seed);
        for(int i=0;i<8;i++) {
            memory[i] = rand.nextInt();
        }
    }

    public double nextDouble(int channel) {
        if(channel>=memory.length) {
            throw new IllegalArgumentException("Channel too high, not enough memory.");
        }
        int index = Math.abs(memory[channel]++);
        return Halton.sample(index,channel);
    }
}
