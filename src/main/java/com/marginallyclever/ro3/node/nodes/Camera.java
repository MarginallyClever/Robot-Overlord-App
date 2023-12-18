package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
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

        JCheckBox ortho = new JCheckBox();
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

        addLabelAndComponent(pane,"Orthographic",ortho);
        addLabelAndComponent(pane,"FOV",fovSpinner);
        addLabelAndComponent(pane,"Near",nearZSpinner);
        addLabelAndComponent(pane,"Far",farZSpinner);

        addLookAtComponents(pane);

        super.getComponents(list);
    }

    private void addLookAtComponents(JPanel pane) {
        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);

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
                lookAt(target);
            } catch (InvalidParameterException ex) {
                JOptionPane.showMessageDialog(pane, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addLabelAndComponent(pane, "Look at", new JLabel());
        addLabelAndComponent(pane, "X", tx);
        addLabelAndComponent(pane, "Y", ty);
        addLabelAndComponent(pane, "Z", tz);
        addLabelAndComponent(pane, "", button);
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

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is up.
     */
    public void pedestal(double delta) {
        translate(MatrixHelper.getYAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is right.
     */
    public void truck(double delta) {
        translate(MatrixHelper.getXAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param delta distance to travel.  Positive is forward.
     */
    public void dolly(double delta) {
        translate(MatrixHelper.getZAxis(this.getLocal()),delta);
    }

    /**
     * Translate relative to camera's current orientation
     * @param direction direction to travel
     * @param delta distance to travel
     */
    private void translate(Vector3d direction,double delta) {
        Matrix4d local = this.getLocal();
        Vector3d t = new Vector3d();
        local.get(t);
        direction.scale(delta);
        t.add(direction);
        local.setTranslation(t);
        this.setLocal(local);
    }

    /**
     * Set the pan and tilt values such that the camera is looking at the target.
     * Set the orbit distance to the distance between the camera and the target.
     * @param target the point to look at.
     */
    public void lookAt(Vector3d target) {
        Matrix4d local = this.getLocal();
        Vector3d position = MatrixHelper.getPosition(local);
        Matrix3d viewMatrix = MatrixHelper.lookAt(target,position);
        Vector3d diff = new Vector3d();
        diff.sub(target,position);
        if(diff.length()<0.0001) {
            throw new InvalidParameterException("target is too close to camera.");
        }
        local.set(viewMatrix);
        local.setTranslation(position);
        this.setLocal(local);
    }
}
