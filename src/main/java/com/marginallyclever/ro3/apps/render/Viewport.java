package com.marginallyclever.ro3.apps.render;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.renderpasses.*;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.nodes.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Viewport} is an {@link OpenGLPanel} that uses a set of {@link RenderPass}es to draw the
 * {@link Registry#getScene()} from the perspective of a {@link Registry#getActiveCamera()}.
 */
public class Viewport extends OpenGLPanel implements GLEventListener {
    public ListWithEvents<RenderPass> renderPasses = new ListWithEvents<>();
    private Camera camera;
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final JPopupMenu renderPassMenu = new JPopupMenu();
    private final List<Boolean> buttonPressed = new ArrayList<>();
    private int mx, my;
    private double orbitRadius = 100;
    private final double orbitChangeFactor = 1.1;  // must always be greater than 1


    public Viewport() {
        super();
        add(toolBar, BorderLayout.NORTH);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addRenderPasses();
        addCameraSelector();
        addRenderPassSelection();

        for(int i=0;i<MouseInfo.getNumberOfButtons();++i) {
            buttonPressed.add(false);
        }
    }

    private void addRenderPasses() {
        renderPasses.add(new DrawBackground());
        renderPasses.add(new DrawGroundPlane());
        renderPasses.add(new DrawMeshes());
        renderPasses.add(new DrawBoundingBoxes());
        renderPasses.add(new DrawCameras());
        renderPasses.add(new DrawDHParameters());
        renderPasses.add(new DrawHingeJoints());
        renderPasses.add(new DrawPoses());
    }

    private void addRenderPassSelection() {
        JButton button = new JButton("Render");
        toolBar.add(button);

        renderPassMenu.removeAll();

        // Add an ActionListener to the JButton to show the JPopupMenu when clicked
        button.addActionListener(e -> renderPassMenu.show(button, button.getWidth()/2, button.getHeight()/2));

        for(RenderPass renderPass : renderPasses.getList()) {
            addRenderPass(renderPass);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
        renderPasses.addItemAddedListener(this::addRenderPass);
        renderPasses.addItemRemovedListener(this::removeRenderPass);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
        renderPasses.removeItemAddedListener(this::addRenderPass);
        renderPasses.removeItemRemovedListener(this::removeRenderPass);
    }

    private void addRenderPass(RenderPass renderPass) {
        addRenderPassInternal(renderPass);
        addGLEventListener(renderPass);
    }

    private void removeRenderPass(RenderPass renderPass) {
        removeRenderPassInternal(renderPass);
        removeGLEventListener(renderPass);
    }

    private void addRenderPassInternal(RenderPass renderPass) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2,2,0,2));
        JButton button = new JButton();
        setRenderPassButtonLabel(button, renderPass.getActiveStatus());
        button.addActionListener(e -> {
            renderPass.setActiveStatus((renderPass.getActiveStatus() + 1) % RenderPass.MAX_STATUS );
            setRenderPassButtonLabel(button, renderPass.getActiveStatus());
        });
        panel.add(button, BorderLayout.WEST);
        JLabel label = new JLabel(renderPass.getName());
        label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        panel.add(label, BorderLayout.CENTER);
        renderPassMenu.add(panel);
    }

    void setRenderPassButtonLabel(JButton button, int status) {
        switch(status) {
            case RenderPass.NEVER -> button.setText("N");
            case RenderPass.SOMETIMES -> button.setText("S");
            case RenderPass.ALWAYS -> button.setText("A");
        }
    }

    private void removeRenderPassInternal(RenderPass renderPass) {
        for(Component c : renderPassMenu.getComponents()) {
            if(c instanceof JCheckBox checkBox) {
                if(checkBox.getText().equals(renderPass.getName())) {
                    renderPassMenu.remove(c);
                    return;
                }
            }
        }
    }

    private void addCamera(Camera camera) {
        if(cameraListModel.getIndexOf(camera) == -1) {
            cameraListModel.addElement(camera);
        }
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
            Registry.setActiveCamera(camera);
        });
        if(cameraListModel.getSize()>0) cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
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
        super.reshape(glAutoDrawable,x,y,width,height);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        double dt = 0.03;
        updateAllNodes(dt);
        renderAllPasses();
    }

    private void renderAllPasses() {
        // renderPasses that are always on
        for(RenderPass pass : renderPasses.getList()) {
            if(pass.getActiveStatus()==RenderPass.ALWAYS) {
                pass.draw();
            }
        }
    }

    private void updateAllNodes(double dt) {
        // option 1, recursively
        Registry.getScene().update(dt);
        /*
        // option 2, in a linear way.
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            node.update(dt);
            toScan.addAll(node.getChildren());
        }*/
    }

    @Override
    public void mousePressed(MouseEvent e) {
        buttonPressed.set(e.getButton(),true);
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        buttonPressed.set(e.getButton(),false);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int px = e.getX();
        int dx = px - mx;
        mx = px;

        int py = e.getY();
        int dy = py - my;
        my = py;

        boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;

        //if(buttonPressed.get(MouseEvent.BUTTON1)) {}
        if(buttonPressed.get(MouseEvent.BUTTON2)) {  // middle button
            if(!shift) {
                panTiltCamera(dx, dy);
            } else {
                camera.dolly(dy);
            }
        }
        if(buttonPressed.get(MouseEvent.BUTTON3)) {  // right button
            if(!shift) {
                orbitCamera(dx,dy);
            } else {
                camera.truck(-dx);
                camera.pedestal(dy);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        int dz = e.getWheelRotation();
        changeOrbitRadius(dz);
    }

    /**
     * Change the distance from the camera to the orbit point.  The orbit point does not move.  In effect the camera
     * is performing a dolly in/out.
     * @param dz mouse wheel movement
     */
    private void changeOrbitRadius(int dz) {
        Matrix4d local = camera.getLocal();
        Vector3d orbitPoint = getOrbitPoint();

        orbitRadius = dz > 0 ? orbitRadius * orbitChangeFactor : orbitRadius / orbitChangeFactor;
        orbitRadius = Math.max(1,orbitRadius);
        //logger.debug("wheel "+dz + " orbitRadius=" + orbitRadius);

        Vector3d orbitVector = MatrixHelper.getZAxis(local);
        orbitVector.scaleAdd(orbitRadius,orbitPoint);
        local.setTranslation(orbitVector);
    }

    private void panTiltCamera(int dx, int dy) {
        Matrix4d local = camera.getLocal();
        Vector3d t = new Vector3d();
        local.get(t);
        double [] panTiltAngles = getPanTiltFromMatrix(local);
        panTiltAngles[0] = (panTiltAngles[0] + dx+360) % 360;
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + dy));
        Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
        local.set(panTilt);
        local.setTranslation(t);
        camera.setLocal(local);
    }

    /**
     * Orbit the camera around a point orbitRadius ahead of the camera.
     * @param dx mouse movement in x
     * @param dy mouse movement in y
     */
    void orbitCamera(int dx,int dy) {
        Matrix4d local = camera.getLocal();
        Vector3d orbitPoint = getOrbitPoint();
        //logger.debug("before {}",orbitPoint);
        double [] panTiltAngles = getPanTiltFromMatrix(local);
        panTiltAngles[0] = (panTiltAngles[0] + dx+360) % 360;
        panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1] + dy));
        Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
        Matrix4d newLocal = new Matrix4d();
        newLocal.set(panTilt);
        Vector3d orbitVector = MatrixHelper.getZAxis(newLocal);
        orbitVector.scaleAdd(orbitRadius,orbitPoint);
        newLocal.setTranslation(orbitVector);
        camera.setLocal(newLocal);
        //logger.debug("after {}",getOrbitPoint());
    }

    /**
     * @return the point that the camera is orbiting around.
     */
    Vector3d getOrbitPoint() {
        Matrix4d local = camera.getLocal();
        Vector3d position = MatrixHelper.getPosition(local);
        // z axis points away from the direction the camera is facing.
        Vector3d zAxis = MatrixHelper.getZAxis(local);
        zAxis.scale(-orbitRadius);
        position.add(zAxis);
        return position;
    }

    double[] getPanTiltFromMatrix(Matrix4d matrix) {
        Vector3d v = MatrixHelper.matrixToEuler(matrix);
        double pan = Math.toDegrees(-v.z);
        double tilt = Math.toDegrees(v.x);
        return new double[]{ pan, tilt };
    }

    /**
     * @param panTiltAngles [0] = pan, [1] = tilt
     * @return a matrix that rotates the camera by the given pan and tilt angles.
     */
    Matrix3d buildPanTiltMatrix(double [] panTiltAngles) {
        Matrix3d a = new Matrix3d();
        a.rotZ(Math.toRadians(panTiltAngles[0]));

        Matrix3d b = new Matrix3d();
        b.rotX(Math.toRadians(-panTiltAngles[1]));

        Matrix3d c = new Matrix3d();
        c.mul(b,a);
        c.transpose();
        return c;
    }
}
