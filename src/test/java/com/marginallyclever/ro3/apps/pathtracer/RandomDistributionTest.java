package com.marginallyclever.ro3.apps.pathtracer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RandomDistributionTest {
    @Test
    @Disabled
    public void testRandomDistribution() {
        int [] counts = new int[10];
        int samples = 10000000;
        for (int i = 0; i < samples; i++) {
            int value = (int) (Math.random() * 10);
            counts[value]++;
        }
        for (int count : counts) {
            double percentage = (count / (double) samples) * 100;
            System.out.printf("Value: %d, Count: %d, Percentage: %.5f%%%n", count, count, percentage);
        }
    }
}
