package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Camera extends Pose {
    private double fovY = 60;
    private double nearZ = 1;
    private double farZ = 1000;
    private boolean drawOrthographic = false;

    public Camera() {
        super("Camera");
    }

    public Camera(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        Registry.cameras.add(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        Registry.cameras.remove(this);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Camera.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();
        pane.setLayout(new GridLayout(0, 2));

        SpinnerNumberModel farZModel = new SpinnerNumberModel(farZ, 0, 10000, 1);
        JSpinner farZSpinner = new JSpinner(farZModel);
        JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(nearZ, 0, 10000, 1));
        JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(fovY, 1, 180, 1));

        JCheckBox ortho = new JCheckBox("Orthographic");
        ortho.addActionListener(e -> {
            drawOrthographic = ortho.isSelected();
            farZSpinner.setEnabled(!drawOrthographic);
            nearZSpinner.setEnabled(!drawOrthographic);
            fovSpinner.setEnabled(!drawOrthographic);
        });

        fovSpinner.addChangeListener(e -> {
            fovY = (double) fovSpinner.getValue();
        });

        nearZSpinner.addChangeListener(e -> {
            nearZ = (double) nearZSpinner.getValue();
            farZModel.setMinimum(nearZ + 1);
            if (farZ <= nearZ) {
                farZSpinner.setValue(nearZ + 1);
            }
        });

        farZSpinner.addChangeListener(e -> {
            farZ = (double) farZSpinner.getValue();
        });

        addLabelAndComponent(pane,"FOV",fovSpinner);
        addLabelAndComponent(pane,"Near",nearZSpinner);
        addLabelAndComponent(pane,"Far",farZSpinner);
        addLabelAndComponent(pane,"Orthographic",ortho);

        super.getComponents(list);
    }

    public boolean getDrawOrthographic() {
        return drawOrthographic;
    }

    public double getFovY() {
        return fovY;
    }

    public double getNearZ() {
        return nearZ;
    }

    public double getFarZ() {
        return farZ;
    }
}
