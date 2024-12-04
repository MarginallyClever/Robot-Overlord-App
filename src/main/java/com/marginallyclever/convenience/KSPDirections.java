package com.marginallyclever.convenience;

public enum KSPDirections {
    /**
     * Prograde is the direction of travel.  +Z
     */
    PROGRADE,
    /**
     * Retrograde is the opposite direction of travel. -Z
     */
    RETROGRADE,
    /**
     * Normal is the direction of the orbit's normal vector. +Y
     */
    NORMAL,
    /**
     * Antinormal is the opposite direction of the orbit's normal vector. -Y
     */
    ANTINORMAL,
    /**
     * Radial is the direction of the orbit's radial vector. +X
     */
    RADIAL_IN,
    /**
     * Antiradial is the opposite direction of the orbit's radial vector. -X
     */
    RADIAL_OUT,
}
