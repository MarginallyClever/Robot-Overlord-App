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
    private int canvasWidth, canvasHeight;

    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final SpinnerNumberModel farZModel = new SpinnerNumberModel(farZ, 0, 10000, 1);
    private final JSpinner farZSpinner = new JSpinner(farZModel);
    private final JSpinner nearZSpinner = new JSpinner(new SpinnerNumberModel(nearZ, 0, 10000, 1));
    private final JSpinner fovSpinner = new JSpinner(new SpinnerNumberModel(fovY, 1, 180, 1));
    private ShaderProgram shaderDefault;

    private final Vector3d moveCamera = new Vector3d();

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
        logger.info("init");
        super.init(glAutoDrawable);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        // turn on vsync
        gl3.setSwapInterval(GraphicsPreferences.verticalSync.get() ? 1 : 0);

        // make things pretty
        gl3.glEnable(GL3.GL_LINE_SMOOTH);
        gl3.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        // depth testing and culling options
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glDepthFunc(GL3.GL_LESS);
        gl3.glDepthMask(true);
        // Don't draw triangles facing away from camera
        gl3.glEnable(GL3.GL_CULL_FACE);
        gl3.glCullFace(GL3.GL_BACK);
        // default blending option for transparent materials
        gl3.glEnable(GL3.GL_BLEND);
        gl3.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        gl3.glActiveTexture(GL3.GL_TEXTURE0);
        // create the default shader
        shaderDefault = new ShaderProgram(gl3,
                readResource("default_330.vert"),
                readResource("default_330.frag"));
        shaderDefault.use(gl3);
        shaderDefault.setVector3d(gl3,"lightColor",new Vector3d(1,1,1));  // Light color
        shaderDefault.set4f(gl3,"objectColor",1,1,1,1);
        shaderDefault.setVector3d(gl3,"specularColor",new Vector3d(0.5,0.5,0.5));
        shaderDefault.setVector3d(gl3,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
        shaderDefault.set1f(gl3,"useVertexColor",0);
        shaderDefault.set1i(gl3,"useLighting",1);
        shaderDefault.set1i(gl3,"useTexture",0);
        shaderDefault.set1i(gl3,"diffuseTexture",0);
        OpenGLHelper.checkGLError(gl3,logger);
    }

    protected String [] readResource(String resourceName) {
        List<String> lines = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream(resourceName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line+"\n");
            }
        } catch (Exception e) {
            logger.error("Failed to read resource: {}",resourceName,e);
        }
        return lines.toArray(new String[0]);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        logger.info("dispose");
        super.dispose(glAutoDrawable);
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shaderDefault.delete(gl3);
        unloadAllMeshes(gl3);
        Registry.textureFactory.unloadAll();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        logger.info("reshape {}x{}",width,height);
        canvasWidth = width;
        canvasHeight = height;
    }

    private void unloadAllMeshes(GL3 gl3) {
        List<Node> toScan = new ArrayList<>(Registry.scene.getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;
                mesh.unload(gl3);
            }

            toScan.addAll(node.getChildren());
        }
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        super.display(glAutoDrawable);

        updateAllNodes(0.03);

        if (camera == null) return;

        Vector3d p = camera.getPosition();
        p.add(moveCamera);
        camera.setPosition(p);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shaderDefault.use(gl3);
        shaderDefault.setMatrix4d(gl3,"viewMatrix",getViewMatrix());
        shaderDefault.setMatrix4d(gl3,"projectionMatrix",getChosenProjectionMatrix());
        OpenGLHelper.checkGLError(gl3,logger);

        shaderDefault.setVector3d(gl3,"lightPos",camera.getPosition());  // Light position in world space
        shaderDefault.setVector3d(gl3,"cameraPos",camera.getPosition());  // Camera position in world space

        // find all MeshInstance nodes in Registry
        List<Node> toScan = new ArrayList<>(Registry.scene.getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                // if they have a mesh, draw it.
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;
                // set the texture to the first sibling that is a material and has a texture
                meshInstance.getParent().getChildren().stream()
                        .filter(n -> n instanceof Material)
                        .map(n -> (Material) n)
                        .filter(m -> m.getTexture() != null)
                        .findFirst()
                        .ifPresent(m -> {
                            m.getTexture().use(shaderDefault);
                        });

                // set the model matrix
                Matrix4d m = meshInstance.getWorld();
                m.transpose();
                shaderDefault.setMatrix4d(gl3,"modelMatrix",m);
                // draw it
                mesh.render(gl3);

                OpenGLHelper.checkGLError(gl3,logger);
            }

            toScan.addAll(node.getChildren());
        }
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
