package com.marginallyclever.ro3.node.nodes.crab;

/**
 * Enum representing the different phases of a crab leg's movement.
 * Each phase indicates the current state of the leg during its motion.
 */
public enum CrabLegPhase {
    // at rest between movements
    REST,
    // lifting the leg up
    RISE,
    // moving the leg towards the next point of contact
    SWING,
    // placing the leg down
    FALL
}
