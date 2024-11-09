package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
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

        JFormattedTextField tx = PanelHelper.addNumberField("translate x",local.x);
        JFormattedTextField ty = PanelHelper.addNumberField("translate y",local.y);
        JFormattedTextField tz = PanelHelper.addNumberField("translate z",local.z);

        gbc.gridx=0;        this.add(new JLabel("Translation"),gbc);
        gbc.gridx=1;        this.add(tx,gbc);
        gbc.gridx=2;        this.add(ty,gbc);
        gbc.gridx=3;        this.add(tz,gbc);

        // these have to be after creating the fields because they reference each other,
        // and should be after adding the panel to reduce the number of property change calls.
        tx.addPropertyChangeListener("value",(e)->updateTranslation(tx,ty,tz));
        ty.addPropertyChangeListener("value",(e)->updateTranslation(tx,ty,tz));
        tz.addPropertyChangeListener("value",(e)->updateTranslation(tx,ty,tz));
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

        JFormattedTextField rx = PanelHelper.addNumberField("rotate x",r.x);
        JFormattedTextField ry = PanelHelper.addNumberField("rotate y",r.y);
        JFormattedTextField rz = PanelHelper.addNumberField("rotate z",r.z);

        gbc.gridx=0;        this.add(new JLabel("Rotation"),gbc);
        gbc.gridx=1;        this.add(rx,gbc);
        gbc.gridx=2;        this.add(ry,gbc);
        gbc.gridx=3;        this.add(rz,gbc);
        gbc.gridy++;
        gbc.gridx=0;        this.add(new JLabel("Type"),gbc);
        gbc.gridwidth=3;
        gbc.gridx=1;        this.add(rotationType,gbc);
        gbc.gridwidth=1;
        gbc.gridx=0;

        // these have to be after creating the fields because they reference each other,
        // and should be after adding the panel to reduce the number of property change calls.
        rx.addPropertyChangeListener("value",(e)->updateRotation(rx,ry,rz));
        ry.addPropertyChangeListener("value",(e)->updateRotation(rx,ry,rz));
        rz.addPropertyChangeListener("value",(e)->updateRotation(rx,ry,rz));
    }

    private void updateTranslation(JFormattedTextField tx, JFormattedTextField ty, JFormattedTextField tz) {
        var p = pose.getPosition();
        p.x = ((Number)tx.getValue()).doubleValue();
        p.y = ((Number)ty.getValue()).doubleValue();
        p.z = ((Number)tz.getValue()).doubleValue();
        pose.setPosition(p);
    }

    private void updateRotation(JFormattedTextField rx, JFormattedTextField ry, JFormattedTextField rz) {
        Vector3d r = pose.getRotationEuler(pose.getRotationIndex());
        r.x = ((Number)rx.getValue()).doubleValue();
        r.y = ((Number)ry.getValue()).doubleValue();
        r.z = ((Number)rz.getValue()).doubleValue();
        pose.setRotationEuler(r, pose.getRotationIndex());
    }
}
