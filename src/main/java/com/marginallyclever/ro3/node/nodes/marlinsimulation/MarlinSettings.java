package com.marginallyclever.ro3.node.nodes.marlinsimulation;

public class MarlinSettings {
    public static final int MAX_ACCELERATION = 1;
    public static final int SEGMENTS_PER_SECOND = 2;
    public static final int MIN_SEGMENT_LENGTH = 3;
    public static final int BLOCK_BUFFER_SIZE = 4;
    public static final int MIN_SEG_TIME = 5;
    public static final int MINIMUM_PLANNER_SPEED = 6;
    public static final int HANDLE_SMALL_SEGMENTS = 7;
    public static final int MAX_FEEDRATE = 8;

    public double getDouble(int key) {
        return switch (key) {
            case MAX_ACCELERATION -> 3000;
            case MAX_FEEDRATE -> 5;
            case MIN_SEGMENT_LENGTH -> 0.5;
            case MINIMUM_PLANNER_SPEED -> 0.05;
            default -> 0;
        };
    }

    public int getInteger(int key) {
        return switch (key) {
            case SEGMENTS_PER_SECOND -> 5;
            case BLOCK_BUFFER_SIZE -> 16;
            case MIN_SEG_TIME -> 100;
            default -> 0;
        };
    }

    public boolean getBoolean(int key) {
        if (key == HANDLE_SMALL_SEGMENTS) {
            return false;
        }
        return false;
    }
}
