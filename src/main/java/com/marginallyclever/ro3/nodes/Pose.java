package com.marginallyclever.ro3.nodes;

import javax.vecmath.Matrix4d;

public class Pose extends Node {
    private final Matrix4d local = new Matrix4d();

    public Pose() {
        super("Pose");
    }

    public Pose(String name) {
        super(name);
        local.setIdentity();
    }

    public Matrix4d getLocal() {
        return local;
    }

    public void setLocal(Matrix4d m) {
        local.set(m);
    }

    public Matrix4d getWorld() {
        // search up the tree to find the world transform.
        Pose p = (Pose)findParent(Pose.class);
        if(p==null) {
            return new Matrix4d(local);
        }
        Matrix4d parentWorld = p.getWorld();
        parentWorld.mul(local);
        return parentWorld;
    }
}
