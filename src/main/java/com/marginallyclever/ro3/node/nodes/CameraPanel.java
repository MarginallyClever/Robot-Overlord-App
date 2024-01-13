package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.NodePanelHelper;

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

        JCheckBox ortho = new JCheckBox();
        ortho.addActionListener(e -> {
            camera.setDrawOrthographic(ortho.isSelected());
            var drawOrthographic = camera.getDrawOrthographic();
            farZSpinner.setEnabled(!drawOrthographic);
            nearZSpinner.setEnabled(!drawOrthographic);
            fovSpinner.setEnabled(!drawOrthographic);
        });
        NodePanelHelper.addLabelAndComponent(this,"Orthographic",ortho);

        fovSpinner.addChangeListener(e -> {
            camera.setFovY( (double) fovSpinner.getValue() );
        });
        NodePanelHelper.addLabelAndComponent(this,"FOV",fovSpinner);

        nearZSpinner.addChangeListener(e -> {
            camera.setNearZ( (double)nearZSpinner.getValue() );
            var nearZ = camera.getNearZ();
            farZModel.setMinimum(nearZ + 1);
            if (camera.getFarZ() <= nearZ) {
                farZSpinner.setValue(nearZ + 1);
            }
        });
        NodePanelHelper.addLabelAndComponent(this,"Near",nearZSpinner);

        farZSpinner.addChangeListener(e -> {
            camera.setFarZ( (double) farZSpinner.getValue() );
        });
        NodePanelHelper.addLabelAndComponent(this,"Far",farZSpinner);

        addLookAtComponents();
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

        NodePanelHelper.addLabelAndComponent(this, "Look at", new JLabel());
        NodePanelHelper.addLabelAndComponent(this, "X", tx);
        NodePanelHelper.addLabelAndComponent(this, "Y", ty);
        NodePanelHelper.addLabelAndComponent(this, "Z", tz);
        NodePanelHelper.addLabelAndComponent(this, "", button);
    }
}
