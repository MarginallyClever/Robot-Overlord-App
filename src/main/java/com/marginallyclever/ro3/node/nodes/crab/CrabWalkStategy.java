package com.marginallyclever.ro3.node.nodes.crab;

/**
 * Enum representing different strategies for crab walking.
 * Each strategy corresponds to a specific walking behavior or movement pattern.
 */
public enum CrabWalkStategy {
    GO_LIMP("GO_LIMP"),
    HOME_POSITION("HOME_POSITION"),
    SIT_DOWN("SIT_DOWN"),
    STAND_UP("STAND_UP"),
    TAP_ONE_TOE("TAP_ONE_TOE"),
    WALK_RIPPLE("WALK_RIPPLE"),
    WALK_WAVE("WALK_WAVE"),
    WALK_THREE_AT_ONCE("WALK_THREE_AT_ONCE");

    public final String name;

    CrabWalkStategy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
