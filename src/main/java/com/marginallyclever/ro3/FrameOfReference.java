package com.marginallyclever.ro3;

/**
 * Used for determining the frame of reference for a given operation in 3D space.
 */
public enum FrameOfReference {
    WORLD,
    LOCAL, // aka object space
    CAMERA,
}
