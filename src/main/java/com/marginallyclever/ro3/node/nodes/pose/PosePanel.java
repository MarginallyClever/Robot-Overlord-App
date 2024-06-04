package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.beans.PropertyChangeListener;

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

        addTranslationComponents(gbc);
        gbc.gridy++;
        addRotationComponents(gbc);
    }

    private void addTranslationComponents(GridBagConstraints gbc) {
        var local = pose.getPosition();

        JFormattedTextField tx = addTranslateField("translate x",local.x, (e)-> {
            var p = pose.getPosition();
            p.x = ((Number) e.getNewValue()).doubleValue();
            pose.setPosition(p);
        });
        JFormattedTextField ty = addTranslateField("translate y",local.y, (e)-> {
            var p = pose.getPosition();
            p.y = ((Number) e.getNewValue()).doubleValue();
            pose.setPosition(p);
        });
        JFormattedTextField tz = addTranslateField("translate z",local.z, (e)-> {
            var p = pose.getPosition();
            p.z = ((Number) e.getNewValue()).doubleValue();
            pose.setPosition(p);
        });

        gbc.gridx=0;        this.add(new JLabel("Translation"),gbc);
        gbc.gridx=1;        this.add(tx,gbc);
        gbc.gridx=2;        this.add(ty,gbc);
        gbc.gridx=3;        this.add(tz,gbc);
    }

    private JFormattedTextField addTranslateField(String label, double value, PropertyChangeListener listener) {
        var formatter = NumberFormatHelper.getNumberFormatter();
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(value);
        field.setToolTipText(label);
        field.addPropertyChangeListener("value", listener );

        return field;
    }

    private void addRotationComponents(GridBagConstraints gbc) {
        var rotationIndex = pose.getRotationIndex();
        Vector3d r = pose.getRotationEuler(rotationIndex);

        String [] names = new String[MatrixHelper.EulerSequence.values().length];
        int i=0;
        for(MatrixHelper.EulerSequence s : MatrixHelper.EulerSequence.values()) {
            names[i++] = "Euler "+s.toString();
        }
        JComboBox<String> rotationType = new JComboBox<>(names);
        rotationType.setSelectedIndex(rotationIndex.ordinal());
        rotationType.addActionListener( e -> {
            pose.setRotationIndex( MatrixHelper.EulerSequence.values()[rotationType.getSelectedIndex()] );
        });

        JFormattedTextField rx = addRotation("rotate x",r.x);
        JFormattedTextField ry = addRotation("rotate y",r.y);
        JFormattedTextField rz = addRotation("rotate z",r.z);

        rx.addPropertyChangeListener((e)->updateRotation(rx,ry,rz));
        ry.addPropertyChangeListener((e)->updateRotation(rx,ry,rz));
        rz.addPropertyChangeListener((e)->updateRotation(rx,ry,rz));

        gbc.gridx=0;        this.add(new JLabel("Rotation"),gbc);
        gbc.gridx=1;        this.add(rx,gbc);
        gbc.gridx=2;        this.add(ry,gbc);
        gbc.gridx=3;        this.add(rz,gbc);
        gbc.gridy++;
        gbc.gridx=2;        this.add(new JLabel("Type"),gbc);
        gbc.gridx=3;        this.add(rotationType,gbc);

        gbc.gridx=0;
    }

    private void updateRotation(JFormattedTextField rx, JFormattedTextField ry, JFormattedTextField rz) {
        Vector3d r = pose.getRotationEuler(pose.getRotationIndex());
        r.x = ((Number)rx.getValue()).doubleValue();
        r.y = ((Number)ry.getValue()).doubleValue();
        r.z = ((Number)rz.getValue()).doubleValue();
        pose.setRotationEuler(r, pose.getRotationIndex());
    }

    private JFormattedTextField addRotation(String label, double value) {
        var formatter = NumberFormatHelper.getNumberFormatter();
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(value);
        field.setToolTipText(label);
        return field;
    }
}
