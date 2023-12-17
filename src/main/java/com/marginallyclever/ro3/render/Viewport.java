package com.marginallyclever.ro3.render;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Connects a {@link Camera} to an {@link OpenGLPanel}.
 */
public class Viewport extends OpenGLPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(Viewport.class);
    private Camera camera;
    private double fovY = 60;
    private double nearZ = 1;
    private double farZ = 1000;
    private boolean drawOrthographic = false;

    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final SpinnerNumberModel farZModel = new SpinnerNumberModel(farZ, 0, 10000, 1);
    private final JSpinner farZSpinner = new JSpinner(farZModel);
    private final JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(nearZ, 0, 10000, 1));
    private final JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(fovY, 1, 180, 1));
    private final JPopupMenu overlayMenu = new JPopupMenu();


    private final Vector3d moveCamera = new Vector3d();

    public Viewport() {
        super();
        add(toolBar, BorderLayout.NORTH);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addCameraSelector();
        addFovSpinner();
        addNearSpinner();
        addFarSpinner();
        addOrthographicCheckbox();
        addOverlaySelection();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W: moveCamera.x=+1;  break;
                    case KeyEvent.VK_S: moveCamera.x=-1;  break;
                    case KeyEvent.VK_A: moveCamera.y=+1;  break;
                    case KeyEvent.VK_D: moveCamera.y=-1;  break;
                    case KeyEvent.VK_Q: moveCamera.z=+1;  break;
                    case KeyEvent.VK_E: moveCamera.z=-1;  break;
                    default: break;
                }
                System.out.println("Press "+e.getKeyCode()+" "+e.getKeyChar()+" "+e.getKeyLocation()+" "+e.isActionKey()+" "+e.isAltDown()+" "+e.isAltGraphDown()+" "+e.isConsumed()+" "+e.isControlDown()+" "+e.isMetaDown()+" "+e.isShiftDown());
                super.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W: moveCamera.x=0;  break;
                    case KeyEvent.VK_S: moveCamera.x=0;  break;
                    case KeyEvent.VK_A: moveCamera.y=0;  break;
                    case KeyEvent.VK_D: moveCamera.y=0;  break;
                    case KeyEvent.VK_Q: moveCamera.z=0;  break;
                    case KeyEvent.VK_E: moveCamera.z=0;  break;
                    default: break;
                }
                System.out.println("Release "+e.getKeyCode()+" "+e.getKeyChar()+" "+e.getKeyLocation()+" "+e.isActionKey()+" "+e.isAltDown()+" "+e.isAltGraphDown()+" "+e.isConsumed()+" "+e.isControlDown()+" "+e.isMetaDown()+" "+e.isShiftDown());
                super.keyReleased(e);
            }
        });
        this.setFocusable(true);
    }

    private void addOverlaySelection() {
        JButton button = new JButton("...");
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
        super.reshape(glAutoDrawable,x,y,width,height);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        double dt = 0.03;
        updateAllNodes(dt);

        if (camera == null) return;
        if (shaderDefault == null) return;

        Vector3d p = camera.getPosition();
        p.add(moveCamera);
        camera.setPosition(p);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shaderDefault.use(gl3);
        shaderDefault.setMatrix4d(gl3,"viewMatrix",getViewMatrix());
        shaderDefault.setMatrix4d(gl3,"projectionMatrix",getChosenProjectionMatrix());
        shaderDefault.setVector3d(gl3,"cameraPos",camera.getPosition());  // Camera position in world space
        shaderDefault.setVector3d(gl3,"lightPos",camera.getPosition());  // Light position in world space
        OpenGLHelper.checkGLError(gl3,logger);

        // render all passes
        super.display(glAutoDrawable);
    }

    private void updateAllNodes(double dt) {
        List<Node> toScan = new ArrayList<>(Registry.scene.getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            node.update(dt);
            toScan.addAll(node.getChildren());
        }
    }
}
