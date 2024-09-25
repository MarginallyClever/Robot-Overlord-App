package com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.marlinsimulation;

import java.util.Arrays;

/**
 * {@link MarlinCoordinate} is a vector used to represent the simultaneous motion of motors in a
 * {@link MarlinSimulationBlock}.
 */
public class MarlinCoordinate {
    public static final int SIZE=6;
    public final double [] p = new double[SIZE];

    public MarlinCoordinate() {}

    public MarlinCoordinate(MarlinCoordinate other) {
        set(other);
    }

    public void add(MarlinCoordinate other) {
        for(int i=0;i<SIZE;++i) {
            p[i] += other.p[i];
        }
    }

    public void sub(MarlinCoordinate other) {
        for(int i=0;i<SIZE;++i) {
            p[i] -= other.p[i];
        }
    }

    public void scale(double s) {
        for(int i=0;i<SIZE;++i) {
            p[i] *= s;
        }
    }

    public void set(MarlinCoordinate other) {
        System.arraycopy(other.p, 0, p, 0, SIZE);
    }

    @Override
    public String toString() {
        return super.toString() + Arrays.toString(p);
    }

    public void normalize() {
        double len = length();
        if(len==0) return;
        scale(1.0/len);
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public void sub(MarlinCoordinate a, MarlinCoordinate b) {
        for(int i=0;i<SIZE;++i) {
            this.p[i] = a.p[i] - b.p[i];
        }
    }

    public double dot(MarlinCoordinate b) {
        double sum=0;
        for(int i=0;i<SIZE;++i) {
            sum += p[i] * b.p[i];
        }
        return sum;
    }
}
