package com.marginallyclever.ro3.nodes;

import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.List;

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

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Pose.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.add(new JLabel("Pose stuff goes here"));

        super.getComponents(list);
    }
}
