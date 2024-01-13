package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Vector3d;
import java.awt.*;

public class PosePanel extends JPanel {
    private final Pose pose;

    public PosePanel(Pose pose) {
        super(new GridLayout(0,2));
        this.pose = pose;
        this.setName(Pose.class.getSimpleName());

        var formatter = NumberFormatHelper.getNumberFormatter();
        addTranslationComponents(formatter);
        addRotationComponents(formatter);
    }

    private void addTranslationComponents(NumberFormatter formatter) {
        var local = pose.getLocal();
        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(local.m03);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(local.m13);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(local.m23);

        tx.addPropertyChangeListener("value", e -> local.m03 = ((Number) tx.getValue()).doubleValue() );
        ty.addPropertyChangeListener("value", e -> local.m13 = ((Number) ty.getValue()).doubleValue() );
        tz.addPropertyChangeListener("value", e -> local.m23 = ((Number) tz.getValue()).doubleValue() );

        PanelHelper.addLabelAndComponent(this, "Translation", new JLabel());
        PanelHelper.addLabelAndComponent(this, "X", tx);
        PanelHelper.addLabelAndComponent(this, "Y", ty);
        PanelHelper.addLabelAndComponent(this, "Z", tz);
    }

    private void addRotationComponents(NumberFormatter formatter) {
        var rotationIndex = pose.getRotationIndex();
        Vector3d r = pose.getRotationEuler(rotationIndex);

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
            pose.setRotationIndex( MatrixHelper.EulerSequence.values()[rotationType.getSelectedIndex()] );;
        });

        rx.addPropertyChangeListener("value", e -> {
            Vector3d r2 = pose.getRotationEuler(rotationIndex);
            r2.x = ((Number) rx.getValue()).doubleValue();
            pose.setRotationEuler(r2, rotationIndex);
        });
        ry.addPropertyChangeListener("value", e -> {
            Vector3d r2 = pose.getRotationEuler(rotationIndex);
            r2.y = ((Number) ry.getValue()).doubleValue();
            pose.setRotationEuler(r2, rotationIndex);
        });
        rz.addPropertyChangeListener("value", e -> {
            Vector3d r2 = pose.getRotationEuler(rotationIndex);
            r2.z = ((Number) rz.getValue()).doubleValue();
            pose.setRotationEuler(r2, rotationIndex);
        });

        PanelHelper.addLabelAndComponent(this, "Rotation", new JLabel());
        PanelHelper.addLabelAndComponent(this, "Type", rotationType);
        PanelHelper.addLabelAndComponent(this, "X", rx);
        PanelHelper.addLabelAndComponent(this, "Y", ry);
        PanelHelper.addLabelAndComponent(this, "Z", rz);
    }
}
