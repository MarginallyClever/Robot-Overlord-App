package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Vector3d;
import java.awt.*;

public class PosePanel extends JPanel {
    private final Pose pose;

    public PosePanel() {
        this(new Pose());
    }

    public PosePanel(Pose pose) {
        super(new GridBagLayout());
        this.pose = pose;
        this.setName(Pose.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        var formatter = NumberFormatHelper.getNumberFormatter();
        addTranslationComponents(formatter,gbc);
        gbc.gridy++;
        addRotationComponents(formatter,gbc);
    }

    private void addTranslationComponents(NumberFormatter formatter,GridBagConstraints gbc) {
        var local = pose.getLocal();
        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(local.m03);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(local.m13);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(local.m23);
        tx.setToolTipText("translate x");
        ty.setToolTipText("translate y");
        tz.setToolTipText("translate z");

        tx.addPropertyChangeListener("value", e -> local.m03 = ((Number) tx.getValue()).doubleValue() );
        ty.addPropertyChangeListener("value", e -> local.m13 = ((Number) ty.getValue()).doubleValue() );
        tz.addPropertyChangeListener("value", e -> local.m23 = ((Number) tz.getValue()).doubleValue() );

        gbc.gridx=0;        this.add(new JLabel("Translation"),gbc);
        gbc.gridx=1;        this.add(tx,gbc);
        gbc.gridx=2;        this.add(ty,gbc);
        gbc.gridx=3;        this.add(tz,gbc);
    }

    private void addRotationComponents(NumberFormatter formatter,GridBagConstraints gbc) {
        var rotationIndex = pose.getRotationIndex();
        Vector3d r = pose.getRotationEuler(rotationIndex);

        JFormattedTextField rx = new JFormattedTextField(formatter);        rx.setValue(r.x);
        JFormattedTextField ry = new JFormattedTextField(formatter);        ry.setValue(r.y);
        JFormattedTextField rz = new JFormattedTextField(formatter);        rz.setValue(r.z);
        rx.setToolTipText("rotate x");
        ry.setToolTipText("rotate y");
        rz.setToolTipText("rotate z");

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

        gbc.gridx=0;        this.add(new JLabel("Rotation"),gbc);
        gbc.gridx=1;        this.add(rx,gbc);
        gbc.gridx=2;        this.add(ry,gbc);
        gbc.gridx=3;        this.add(rz,gbc);

        gbc.gridy++;
        gbc.gridx=2;        this.add(new JLabel("Type"),gbc);
        gbc.gridx=3;        this.add(rotationType,gbc);

        gbc.gridx=0;
    }
}
