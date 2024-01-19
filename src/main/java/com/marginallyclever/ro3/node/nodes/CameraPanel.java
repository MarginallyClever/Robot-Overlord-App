package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.security.InvalidParameterException;

public class CameraPanel extends JPanel {
    private final Camera camera;

    public CameraPanel(Camera camera) {
        super(new GridLayout(0,2));
        this.camera = camera;
        this.setName(Camera.class.getSimpleName());

        SpinnerNumberModel farZModel = new SpinnerNumberModel(camera.getFarZ(), 0, 10000, 1);
        JSpinner farZSpinner = new JSpinner(farZModel);
        JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(camera.getNearZ(), 0, 10000, 1));
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
        PanelHelper.addLabelAndComponent(this,"Orthographic",ortho);

        // fov
        fovSpinner.setValue(camera.getFovY());
        fovSpinner.addChangeListener(e -> {
            camera.setFovY( (double) fovSpinner.getValue() );
        });
        PanelHelper.addLabelAndComponent(this,"FOV",fovSpinner);

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
        PanelHelper.addLabelAndComponent(this,"Near",nearZSpinner);

        // far z
        farZSpinner.setValue(camera.getFarZ());
        farZSpinner.addChangeListener(e -> {
            camera.setFarZ( (double) farZSpinner.getValue() );
        });
        PanelHelper.addLabelAndComponent(this,"Far",farZSpinner);

        // can rotate
        JToggleButton canRotate = new JToggleButton("Yes");
        canRotate.addActionListener(e -> {
            camera.setCanRotate(canRotate.isSelected());
            updateRotateButton(canRotate);
        });
        canRotate.setSelected(camera.getCanRotate());
        updateRotateButton(canRotate);
        PanelHelper.addLabelAndComponent(this,"Can rotate",canRotate);

        // can translate
        JToggleButton canTranslate = new JToggleButton("Yes");
        canTranslate.addActionListener(e -> {
            camera.setCanTranslate(canTranslate.isSelected());
            updateTranslateButton(canTranslate);
        });
        canTranslate.setSelected(camera.getCanTranslate());
        updateTranslateButton(canTranslate);
        PanelHelper.addLabelAndComponent(this,"Can translate",canTranslate);

        addLookAtComponents();
    }

    private void updateTranslateButton(JToggleButton canTranslate) {
        canTranslate.setText(camera.getCanTranslate() ? "Yes" : "No");
        canTranslate.setToolTipText(camera.getCanTranslate() ? "Click to deny" : "Click to allow");
    }

    private void updateRotateButton(JToggleButton canRotate) {
        canRotate.setText(camera.getCanRotate() ? "Yes" : "No");
        canRotate.setToolTipText(camera.getCanRotate() ? "Click to deny" : "Click to allow");
    }

    private void addLookAtComponents() {
        var formatter = NumberFormatHelper.getNumberFormatter();

        JFormattedTextField tx = new JFormattedTextField(formatter);        tx.setValue(0);
        JFormattedTextField ty = new JFormattedTextField(formatter);        ty.setValue(0);
        JFormattedTextField tz = new JFormattedTextField(formatter);        tz.setValue(0);

        JButton button = new JButton("Set");
        button.addActionListener(e -> {
            Vector3d target = new Vector3d(
                    ((Number) tx.getValue()).doubleValue(),
                    ((Number) ty.getValue()).doubleValue(),
                    ((Number) tz.getValue()).doubleValue()
            );
            try {
                camera.lookAt(target);
            } catch (InvalidParameterException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        PanelHelper.addLabelAndComponent(this, "Look at", new JLabel());
        PanelHelper.addLabelAndComponent(this, "X", tx);
        PanelHelper.addLabelAndComponent(this, "Y", ty);
        PanelHelper.addLabelAndComponent(this, "Z", tz);
        PanelHelper.addLabelAndComponent(this, "", button);
    }
}
