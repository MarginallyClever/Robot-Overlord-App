package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
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
        Pose p = findParent(Pose.class);
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

        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(false);

        JFormattedTextField x = new JFormattedTextField(formatter);
        x.setValue(local.m03);
        JFormattedTextField y = new JFormattedTextField(formatter);
        y.setValue(local.m13);
        JFormattedTextField z = new JFormattedTextField(formatter);
        z.setValue(local.m23);

        pane.setLayout(new GridLayout(0,2));
        pane.add(new JLabel("X"));
        pane.add(x);
        pane.add(new JLabel("Y"));
        pane.add(y);
        pane.add(new JLabel("Z"));
        pane.add(z);

        x.addPropertyChangeListener("value", e -> {
            local.m03 = ((Number)x.getValue()).doubleValue();
        } );
        y.addPropertyChangeListener("value", e -> {
            local.m13 = ((Number)y.getValue()).doubleValue();
        } );
        z.addPropertyChangeListener("value", e -> {
            local.m23 = ((Number)z.getValue()).doubleValue();
        } );

        super.getComponents(list);
    }

    public Vector3d getPosition() {
        return new Vector3d(local.m03,local.m13,local.m23);
    }

    public void setPosition(Vector3d p) {
        local.m03 = p.x;
        local.m13 = p.y;
        local.m23 = p.z;
    }
}
