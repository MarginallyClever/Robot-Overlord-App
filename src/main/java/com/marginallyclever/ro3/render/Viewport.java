package com.marginallyclever.ro3.render;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.nodes.Camera;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;

/**
 * Connects a {@link Camera} to an {@link OpenGLPanel}.
 */
public class Viewport extends OpenGLPanel implements GLEventListener {
    private Camera camera;
    private double fovY = 60;
    private double nearZ = 1;
    private double farZ = 1000;
    private boolean drawOrthographic = false;
    private int canvasWidth, canvasHeight;

    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final SpinnerNumberModel farZModel = new SpinnerNumberModel(farZ, 0, 10000, 1);
    private final JSpinner farZSpinner = new JSpinner(farZModel);
    private final JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(nearZ, 0, 10000, 1));
    private final JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(fovY, 1, 180, 1));

    public Viewport() {
        super("Viewport");
    }

    public Viewport(String tabText) {
        super(tabText);
        add(toolBar, BorderLayout.NORTH);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addCameraSelector();
        addFovSpinner();
        addNearSpinner();
        addFarSpinner();
        addOrthographicCheckbox();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
    }

    private void addCamera(Camera camera) {
        cameraListModel.addElement(camera);
    }

    private void removeCamera(Camera camera) {
        cameraListModel.removeElement(camera);
    }

    private void addCameraSelector() {
        JComboBox<Camera> cameraSelector = new JComboBox<>();
        cameraSelector.setModel(cameraListModel);
        cameraSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Camera) {
                    setText(((Camera) value).getName());
                }
                return this;
            }
        });

        cameraListModel.addAll(Registry.cameras.getList());

        cameraSelector.addItemListener(e -> {
            camera = (Camera) e.getItem();
        });
        cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
    }

    private void addOrthographicCheckbox() {
        JCheckBox ortho = new JCheckBox("Orthographic");
        toolBar.add(ortho);
        ortho.addActionListener(e -> {
            drawOrthographic = ortho.isSelected();
            farZSpinner.setEnabled(!drawOrthographic);
            nearZSpinner.setEnabled(!drawOrthographic);
            fovSpinner.setEnabled(!drawOrthographic);
        });
    }

    private void addFovSpinner() {
        fovSpinner.addChangeListener(e -> {
            fovY = (double) fovSpinner.getValue();
        });
        toolBar.add(new JLabel("FOV"));
        toolBar.add(fovSpinner);

        Dimension d = fovSpinner.getPreferredSize();
        d.width = 50; // Set the width you want
        fovSpinner.setPreferredSize(d);
    }

    private void addNearSpinner() {
        nearZSpinner.addChangeListener(e -> {
            nearZ = (double) nearZSpinner.getValue();
            farZModel.setMinimum(nearZ + 1);
            if (farZ <= nearZ) {
                farZSpinner.setValue(nearZ + 1);
            }
        });
        toolBar.add(new JLabel("Near"));
        toolBar.add(nearZSpinner);
    }

    private void addFarSpinner() {
        farZSpinner.addChangeListener(e -> {
            farZ = (double) farZSpinner.getValue();
        });
        toolBar.add(new JLabel("Far"));
        toolBar.add(farZSpinner);
    }

    public Matrix4d getPerspectiveFrustum() {
        double nearVal = nearZ;
        double farVal = farZ;
        double aspect = (double)canvasWidth / (double)canvasHeight;

        return MatrixHelper.perspectiveMatrix4d(fovY,aspect,nearVal,farVal);
    }

    /**
     * Render the scene in orthographic projection.
     * @param zoom the zoom factor
     */
    public Matrix4d getOrthographicMatrix(double zoom) {
        double w = canvasWidth/2.0f;
        double h = canvasHeight/2.0f;

        double left = -w/zoom;
        double right = w/zoom;
        double bottom = -h/zoom;
        double top = h/zoom;
        double nearVal = nearZ;
        double farVal = farZ;

        return MatrixHelper.orthographicMatrix4d(left,right,bottom,top,nearVal,farVal);
    }

    public Matrix4d getChosenProjectionMatrix() {
        return drawOrthographic ? getOrthographicMatrix(1.0) : getPerspectiveFrustum();
    }

    private Matrix4d getViewMatrix() {
        Matrix4d inverseCamera = camera.getWorld();
        inverseCamera.invert();
        return inverseCamera;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        super.init(glAutoDrawable);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        super.dispose(glAutoDrawable);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        super.display(glAutoDrawable);
        if (camera == null) return;

        // set the view matrix
        Matrix4d v = getViewMatrix();

        // set the projection matrix
        Matrix4d p = getChosenProjectionMatrix();
    }
}
