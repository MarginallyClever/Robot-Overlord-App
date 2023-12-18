package com.marginallyclever.ro3.render;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
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
 * Connects a {@link Camera} to an {@link OpenGLPanel}.
 */
public class Viewport extends OpenGLPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(Viewport.class);
    private Camera camera;
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final JPopupMenu overlayMenu = new JPopupMenu();
    private final List<Boolean> buttonPressed = new ArrayList<>();
    private int mx, my;


    public Viewport() {
        super();
        add(toolBar, BorderLayout.NORTH);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addCameraSelector();
        addRenderPassSelection();

        for(int i=0;i<MouseInfo.getNumberOfButtons();++i) {
            buttonPressed.add(false);
        }
    }

    private void addRenderPassSelection() {
        JButton button = new JButton("Render");
        toolBar.add(button);

        overlayMenu.removeAll();

        // Add an ActionListener to the JButton to show the JPopupMenu when clicked
        button.addActionListener(e -> overlayMenu.show(button, button.getWidth()/2, button.getHeight()/2));

        for(RenderPass renderPass : Registry.renderPasses.getList()) {
            addOverlayInternal(renderPass);
        }
    }

    private void addOverlayInternal(RenderPass renderPass) {
        JCheckBox checkBox = new JCheckBox(renderPass.getName());
        checkBox.setSelected(renderPass.getActiveStatus() == RenderPass.ALWAYS);
        checkBox.addActionListener(e -> {
            renderPass.setActiveStatus(checkBox.isSelected() ? RenderPass.ALWAYS : RenderPass.NEVER);
        });
        overlayMenu.add(checkBox);
    }

    private void removeOverlayInternal(RenderPass renderPass) {
        for(Component c : overlayMenu.getComponents()) {
            if(c instanceof JCheckBox checkBox) {
                if(checkBox.getText().equals(renderPass.getName())) {
                    overlayMenu.remove(c);
                    return;
                }
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addGLEventListener(this);
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
        Registry.renderPasses.addItemAddedListener(this::addOverlay);
        Registry.renderPasses.addItemRemovedListener(this::removeOverlay);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        removeGLEventListener(this);
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
        Registry.renderPasses.removeItemAddedListener(this::addOverlay);
        Registry.renderPasses.removeItemRemovedListener(this::removeOverlay);
    }

    private void addOverlay(RenderPass renderPass) {
        addOverlayInternal(renderPass);
        //addGLEventListener(overlay);
    }

    private void removeOverlay(RenderPass renderPass) {
        removeOverlayInternal(renderPass);
        //removeGLEventListener(overlay);
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
        });
        if(cameraListModel.getSize()>0) cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
    }

    public Matrix4d getPerspectiveFrustum() {
        double nearVal = camera.getNearZ();
        double farVal = camera.getFarZ();
        double aspect = (double)canvasWidth / (double)canvasHeight;

        return MatrixHelper.perspectiveMatrix4d(camera.getFovY(),aspect,nearVal,farVal);
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
        double nearVal = camera.getNearZ();
        double farVal = camera.getFarZ();

        return MatrixHelper.orthographicMatrix4d(left,right,bottom,top,nearVal,farVal);
    }

    public Matrix4d getChosenProjectionMatrix() {
        return camera.getDrawOrthographic() ? getOrthographicMatrix(1.0) : getPerspectiveFrustum();
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
        super.reshape(glAutoDrawable,x,y,width,height);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        double dt = 0.03;
        updateAllNodes(dt);

        if (camera == null) return;
        if (shaderDefault == null) return;

        Matrix4d w = camera.getWorld();
        Vector3d cameraWorldPos = MatrixHelper.getPosition(w);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shaderDefault.use(gl3);
        Matrix4d viewMatrix = getViewMatrix();
        viewMatrix.transpose();
        shaderDefault.setMatrix4d(gl3,"viewMatrix",viewMatrix);
        Matrix4d projectionMatrix = getChosenProjectionMatrix();
        shaderDefault.setMatrix4d(gl3,"projectionMatrix",projectionMatrix);
        shaderDefault.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shaderDefault.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        OpenGLHelper.checkGLError(gl3,logger);

        // render all passes
        super.display(glAutoDrawable);
    }

    private void updateAllNodes(double dt) {
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            node.update(dt);
            toScan.addAll(node.getChildren());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

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
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        int px = e.getX();
        int dx = px - mx;
        mx = px;

        int py = e.getY();
        int dy = py - my;
        my = py;

        if(buttonPressed.get(MouseEvent.BUTTON1)) {}
        if(buttonPressed.get(MouseEvent.BUTTON2)) {
            // middle button
            Matrix4d local = camera.getLocal();
            double [] panTiltAngles = getPanTiltFromMatrix(local);
            double beforePan = (panTiltAngles[0]+360) % 360;
            double beforeTilt = ((panTiltAngles[1]+90) % 360) -90;
            panTiltAngles[0] = beforePan + dx;
            panTiltAngles[1] = beforeTilt + dy;
            panTiltAngles[1] = Math.max(0,Math.min(180,panTiltAngles[1]));
            System.out.println("before= "+beforePan+","+beforeTilt+"\tafter=" + panTiltAngles[0] + "," + panTiltAngles[1] + "\tdiff="+dx+","+dy);
            Matrix3d panTilt = buildPanTiltMatrix(panTiltAngles);
            Vector3d t = new Vector3d();
            local.get(t);
            local.set(panTilt);
            local.setTranslation(t);
            camera.setLocal(local);
        }
        if(buttonPressed.get(MouseEvent.BUTTON3)) {
            // right button
            camera.truck(-dx);
            camera.dolly(dy);
        }
    }

    public double[] getPanTiltFromMatrix(Matrix4d matrix) {
        Vector3d v = MatrixHelper.matrixToEuler(matrix);
        double pan = Math.toDegrees(-v.z);
        double tilt = Math.toDegrees(v.x);
        return new double[]{ pan, tilt };
    }

    /**
     * @param panTiltAngles [0] = pan, [1] = tilt
     * @return a matrix that rotates the camera by the given pan and tilt angles.
     */
    public Matrix3d buildPanTiltMatrix(double [] panTiltAngles) {
        Matrix3d a = new Matrix3d();
        a.rotZ(Math.toRadians(panTiltAngles[0]));

        Matrix3d b = new Matrix3d();
        b.rotX(Math.toRadians(-panTiltAngles[1]));

        Matrix3d c = new Matrix3d();
        c.mul(b,a);
        c.transpose();
        return c;
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}
}
