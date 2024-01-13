package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;

/**
 * <p>A {@link Pose} is a {@link Node} that has a position and rotation in space.</p>
 */
public class Pose extends Node {
    private final Matrix4d local = MatrixHelper.createIdentityMatrix4();
    private MatrixHelper.EulerSequence rotationIndex = MatrixHelper.EulerSequence.YXZ;

    public Pose() {
        this("Pose");
    }

    public Pose(String name) {
        super(name);
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

    public void setWorld(Matrix4d world) {
        // search up the tree to find the world transform.
        Pose parent = findParent(Pose.class);
        if(parent==null) {
            local.set(world);
            return;
        }
        Matrix4d inverseParentWorld = parent.getWorld();
        inverseParentWorld.invert();
        local.mul(inverseParentWorld,world);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(Pose.class.getSimpleName());

        var formatter = NumberFormatHelper.getNumberFormatter();
        addTranslationComponents(pane,formatter);
        addRotationComponents(pane,formatter);

        super.getComponents(list);
    }

    private void addTranslationComponents(JPanel pane, NumberFormatter formatter) {
        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(local.m03);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(local.m13);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(local.m23);

        tx.addPropertyChangeListener("value", e -> local.m03 = ((Number) tx.getValue()).doubleValue() );
        ty.addPropertyChangeListener("value", e -> local.m13 = ((Number) ty.getValue()).doubleValue() );
        tz.addPropertyChangeListener("value", e -> local.m23 = ((Number) tz.getValue()).doubleValue() );

        NodePanelHelper.addLabelAndComponent(pane, "Translation", new JLabel());
        NodePanelHelper.addLabelAndComponent(pane, "X", tx);
        NodePanelHelper.addLabelAndComponent(pane, "Y", ty);
        NodePanelHelper.addLabelAndComponent(pane, "Z", tz);
    }

    private void addRotationComponents(JPanel pane, NumberFormatter formatter) {
        Vector3d r = getRotationEuler(rotationIndex);

        JFormattedTextField rx = new JFormattedTextField(formatter);        rx.setValue(r.x);
        JFormattedTextField ry = new JFormattedTextField(formatter);        ry.setValue(r.y);
        JFormattedTextField rz = new JFormattedTextField(formatter);        rz.setValue(r.z);

        String [] names = new String[MatrixHelper.EulerSequence.values().length];
        int i=0;
        for(MatrixHelper.EulerSequence s : MatrixHelper.EulerSequence.values()) {
            names[i++] = "Euler "+s.toString();
        }
        JComboBox<String> rotationType = new JComboBox<>(names);
        rotationType.setSelectedIndex(rotationIndex.ordinal());
        rotationType.addActionListener( e -> {
            rotationIndex = MatrixHelper.EulerSequence.values()[rotationType.getSelectedIndex()];
        });

        rx.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler(rotationIndex);
            r2.x = ((Number) rx.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });
        ry.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler(rotationIndex);
            r2.y = ((Number) ry.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });
        rz.addPropertyChangeListener("value", e -> {
            Vector3d r2 = getRotationEuler(rotationIndex);
            r2.z = ((Number) rz.getValue()).doubleValue();
            setRotationEuler(r2, rotationIndex);
        });

        NodePanelHelper.addLabelAndComponent(pane, "Rotation", new JLabel());
        NodePanelHelper.addLabelAndComponent(pane, "Type", rotationType);
        NodePanelHelper.addLabelAndComponent(pane, "X", rx);
        NodePanelHelper.addLabelAndComponent(pane, "Y", ry);
        NodePanelHelper.addLabelAndComponent(pane, "Z", rz);
    }

    /**
     * @return the rotation of this pose using Euler angles in degrees.
     */
    public Vector3d getRotationEuler(MatrixHelper.EulerSequence orderOfRotation) {
        Vector3d r = MatrixHelper.matrixToEuler(local,orderOfRotation);
        r.scale(180.0/Math.PI);
        return r;
    }

    /**
     * Set the rotation of this pose using Euler angles.
     *
     * @param r Euler angles in degrees.
     * @param orderOfRotation the order of rotation.
     */
    public void setRotationEuler(Vector3d r, MatrixHelper.EulerSequence orderOfRotation) {
        System.out.println("setRotationEuler("+r+","+orderOfRotation+")");
        Vector3d p = getPosition();
        Vector3d rRad = new Vector3d(r);
        rRad.scale(Math.PI/180.0);
        local.set(MatrixHelper.eulerToMatrix(rRad, orderOfRotation));
        setPosition(p);
    }

    /**
     * @return the local position of this pose.
     */
    public Vector3d getPosition() {
        return new Vector3d(local.m03,local.m13,local.m23);
    }

    /**
     * set the local position of this pose.
     * @param p the new position.
     */
    public void setPosition(Vector3d p) {
        local.m03 = p.x;
        local.m13 = p.y;
        local.m23 = p.z;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        double[] localArray = BigMatrixHelper.matrix4dToArray(local);
        json.put("local", new JSONArray(localArray));
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("local")) {
            JSONArray localArray = from.getJSONArray("local");
            double[] localData = new double[16];
            for (int i = 0; i < 16; i++) {
                localData[i] = localArray.getDouble(i);
            }
            local.set(localData);
        }
    }
}
