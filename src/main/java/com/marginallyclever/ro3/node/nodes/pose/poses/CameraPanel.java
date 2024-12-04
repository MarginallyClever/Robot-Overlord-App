package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.security.InvalidParameterException;

public class CameraPanel extends JPanel {
    private final Camera camera;
    private final JFormattedTextField lookAtx = PanelHelper.addNumberField("x",0);
    private final JFormattedTextField lookAty = PanelHelper.addNumberField("y",0);
    private final JFormattedTextField lookAtz = PanelHelper.addNumberField("z",-1);

    public CameraPanel() {
        this(new Camera());
    }

    public CameraPanel(Camera camera) {
        super(new GridBagLayout());
        this.camera = camera;
        this.setName(Camera.class.getSimpleName());

        var v = camera.getOrbitPoint();
        lookAtx.setValue(v.x);
        lookAty.setValue(v.y);
        lookAtz.setValue(v.z);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        SpinnerNumberModel farZModel = new SpinnerNumberModel(camera.getFarZ(), 0, 1e10, 1);
        SpinnerNumberModel nearZModel = new SpinnerNumberModel(camera.getNearZ(), 0, 1e10, 1);
        JSpinner farZSpinner = new JSpinner(farZModel);
        JSpinner nearZSpinner = new JSpinner(nearZModel);
        JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(camera.getFovY(), 1, 180, 1));

        // orthographic?
        JCheckBox ortho = new JCheckBox();
        ortho.addActionListener(e -> {
            camera.setDrawOrthographic(ortho.isSelected());
            var drawOrthographic = camera.getDrawOrthographic();
            farZSpinner.setEnabled(!drawOrthographic);
            nearZSpinner.setEnabled(!drawOrthographic);
            fovSpinner.setEnabled(!drawOrthographic);
        });
        ortho.setSelected(camera.getDrawOrthographic());
        PanelHelper.addLabelAndComponent(this,"Orthographic",ortho,gbc);
        gbc.gridy++;

        // fov
        fovSpinner.setValue(camera.getFovY());
        fovSpinner.addChangeListener(e -> {
            camera.setFovY( (double) fovSpinner.getValue() );
        });
        fovSpinner.setToolTipText("degrees");
        PanelHelper.addLabelAndComponent(this,"FOV",fovSpinner,gbc);
        gbc.gridy++;

        // near z
        nearZSpinner.addChangeListener(e -> {
            camera.setNearZ( (double)nearZSpinner.getValue() );
            var nearZ = camera.getNearZ();
            farZModel.setMinimum(nearZ + 1);
            if (camera.getFarZ() <= nearZ) {
                farZSpinner.setValue(nearZ + 1);
            }
        });
        nearZSpinner.setValue(camera.getNearZ());
        nearZSpinner.setToolTipText("cm");
        PanelHelper.addLabelAndComponent(this,"Near",nearZSpinner,gbc);
        gbc.gridy++;

        // far z
        farZSpinner.setValue(camera.getFarZ());
        farZSpinner.addChangeListener(e -> {
            camera.setFarZ( (double) farZSpinner.getValue() );
            var farZ = camera.getFarZ();
            nearZModel.setMaximum(farZ - 1);
            if(camera.getNearZ() >= farZ) {
                nearZSpinner.setValue(farZ - 1);
            }
        });
        farZSpinner.setToolTipText("cm");
        PanelHelper.addLabelAndComponent(this,"Far",farZSpinner,gbc);
        gbc.gridy++;

        // can rotate
        JToggleButton canRotate = new JToggleButton("Yes");
        canRotate.addActionListener(e -> {
            camera.setCanRotate(canRotate.isSelected());
            updateRotateButton(canRotate);
        });
        canRotate.setSelected(camera.getCanRotate());
        updateRotateButton(canRotate);
        PanelHelper.addLabelAndComponent(this,"Can rotate",canRotate,gbc);
        gbc.gridy++;

        // can translate
        JToggleButton canTranslate = new JToggleButton("Yes");
        canTranslate.addActionListener(e -> {
            camera.setCanTranslate(canTranslate.isSelected());
            updateTranslateButton(canTranslate);
        });
        canTranslate.setSelected(camera.getCanTranslate());
        updateTranslateButton(canTranslate);
        PanelHelper.addLabelAndComponent(this,"Can translate",canTranslate,gbc);
        gbc.gridy++;

        addLookAtComponents(gbc);
    }

    private void updateTranslateButton(JToggleButton canTranslate) {
        canTranslate.setText(camera.getCanTranslate() ? "Yes" : "No");
        canTranslate.setToolTipText(camera.getCanTranslate() ? "Click to deny" : "Click to allow");
    }

    private void updateRotateButton(JToggleButton canRotate) {
        canRotate.setText(camera.getCanRotate() ? "Yes" : "No");
        canRotate.setToolTipText(camera.getCanRotate() ? "Click to deny" : "Click to allow");
    }

    private void addLookAtComponents(GridBagConstraints gbc) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridy=0;
        panel.add(lookAtx,c);
        panel.add(lookAty,c);
        panel.add(lookAtz,c);

        PanelHelper.addLabelAndComponent(this, "Look at", panel,gbc);
        lookAtx.addPropertyChangeListener(e->lookAt());
        lookAty.addPropertyChangeListener(e->lookAt());
        lookAtz.addPropertyChangeListener(e->lookAt());
        gbc.gridy++;
    }

    private void lookAt() {
        try {
            var lookAt = new Vector3d(
                    ((Number)lookAtx.getValue()).doubleValue(),
                    ((Number)lookAty.getValue()).doubleValue(),
                    ((Number)lookAtz.getValue()).doubleValue());
            camera.lookAt(lookAt);
        } catch (InvalidParameterException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
