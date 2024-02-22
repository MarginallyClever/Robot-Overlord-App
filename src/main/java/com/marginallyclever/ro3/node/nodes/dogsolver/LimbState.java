package com.marginallyclever.ro3.node.nodes.dogsolver;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;

import javax.vecmath.Vector3d;

public class LimbState {
    public final String name;
    public final NodePath<LimbSolver> limb;
    public final Vector3d lastFloorContact = new Vector3d();
    public LegAction action = LegAction.NONE;

    public LimbState(Node owner, String name) {
        this.name = name;
        limb = new NodePath<>(owner, LimbSolver.class);
    }
}
