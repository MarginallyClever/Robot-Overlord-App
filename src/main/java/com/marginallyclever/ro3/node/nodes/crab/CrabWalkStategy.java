package com.marginallyclever.ro3.node.nodes.crab;

public enum CrabWalkStategy {
    GO_LIMP("GO_LIMP"),
    HOME_POSITION("HOME_POSITION"),
    SIT_DOWN("SIT_DOWN"),
    STAND_UP("STAND_UP"),
    TAP_TOE_ONE("TAP_TOE_ONE"),
    WALK_RIPPLE1("WALK_RIPPLE1"),
    WALK_RIPPLE2("WALK_RIPPLE2"),
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
