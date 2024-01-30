package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.renderpasses.*;
import com.marginallyclever.ro3.apps.viewport.viewporttools.Compass3D;
import com.marginallyclever.ro3.apps.viewport.viewporttools.SelectionTool;
import com.marginallyclever.ro3.apps.viewport.viewporttools.ViewportTool;
import com.marginallyclever.ro3.apps.viewport.viewporttools.move.RotateToolMulti;
import com.marginallyclever.ro3.apps.viewport.viewporttools.move.TranslateToolMulti;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * {@link Viewport} is an {@link OpenGLPanel} that uses a set of {@link RenderPass}es to draw the
 * {@link Registry#getScene()} from the perspective of a {@link Registry#getActiveCamera()}.
 */
public class Viewport extends OpenGLPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(Viewport.class);
    public ListWithEvents<RenderPass> renderPasses = new ListWithEvents<>();
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final JPopupMenu renderPassMenu = new JPopupMenu();
    private final List<Boolean> buttonPressed = new ArrayList<>();
    private int mx, my;
    private double orbitChangeFactor = 1.1;  // must always be greater than 1
    private int canvasWidth, canvasHeight;
    private final List<ViewportTool> viewportTools = new ArrayList<>();
    private int activeToolIndex = -1;
    private ShaderProgram toolShader;
    private double userMovementScale = 1.0;
    private final JButton frameOfReferenceButton = new JButton();
    private final JPopupMenu frameOfReferenceMenu = new JPopupMenu();

    public Viewport() {
        super();
        add(toolBar, BorderLayout.NORTH);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));

        addRenderPasses();
        addCameraSelector();
        addRenderPassSelection();
        addCopyCameraAction();
        addViewportTools();
        allocateButtonMemory();
    }

    private void addViewportTools() {
        var selectionTool = new SelectionTool();
        var translateToolMulti = new TranslateToolMulti();
        var rotateToolMulti = new RotateToolMulti();
        var compass3D = new Compass3D();
        viewportTools.add(selectionTool);
        viewportTools.add(translateToolMulti);
        viewportTools.add(rotateToolMulti);
        viewportTools.add(compass3D);

        for(ViewportTool tool : viewportTools) {
            tool.setViewport(this);
        }

        JToggleButton select = new JToggleButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Select");
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-select-16.png"))));
                putValue(Action.SHORT_DESCRIPTION,"Select items in the scene.");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setActiveToolIndex(viewportTools.indexOf(selectionTool));
            }
        });

        JToggleButton move = new JToggleButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Move");
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-move-16.png"))));
                putValue(Action.SHORT_DESCRIPTION,"Move the selected items.");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setActiveToolIndex(viewportTools.indexOf(translateToolMulti));
            }
        });

        JToggleButton rotate = new JToggleButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Rotate");
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-rotate-16.png"))));
                putValue(Action.SHORT_DESCRIPTION,"Rotate the selected items.");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setActiveToolIndex(viewportTools.indexOf(rotateToolMulti));
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(select);
        group.add(move);
        group.add(rotate);

        toolBar.add(select);
        toolBar.add(move);
        toolBar.add(rotate);
        toolBar.add(createFrameOfReferenceSelection());
        toolBar.add(new JSeparator());

        var lookAtLastSelected = new JButton(new AbstractAction() {
            {
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-look-16.png"))));
                putValue(Action.SHORT_DESCRIPTION,"Look at the last selected Pose.");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                Pose lastFound = null;
                for(Node node : Registry.selection.getList()) {
                    if(node instanceof Pose pose) {
                        lastFound = pose;
                    }
                }
                if(lastFound==null) return;

                // look at the last selected pose
                Camera camera = Registry.getActiveCamera();
                Matrix4d m = camera.getWorld();
                var cameraPosition = MatrixHelper.getPosition(m);
                var lastFoundPosition = MatrixHelper.getPosition(lastFound.getWorld());
                var lookAt = MatrixHelper.lookAt(lastFoundPosition,cameraPosition);
                m.set(lookAt);
                m.setTranslation(cameraPosition);
                camera.setWorld(m);
                // adjust the camera orbit to be the distance from the camera to the last selected pose.
                Vector3d diff = new Vector3d(cameraPosition);
                diff.sub(lastFoundPosition);
                double distance = Math.max(1,diff.length());
                camera.setOrbitRadius(distance);
            }
        });

        toolBar.add(lookAtLastSelected);
    }

    private void addRenderPasses() {
        renderPasses.add(new DrawBackground());
        renderPasses.add(new DrawGroundPlane());
        renderPasses.add(new DrawMeshes());
        renderPasses.add(new DrawBoundingBoxes());
        renderPasses.add(new DrawCameras());
        renderPasses.add(new DrawDHParameters());
        renderPasses.add(new DrawJoints());
        renderPasses.add(new DrawPoses());
    }

    /**
     * Load the viewport pass state from the {@link java.util.prefs.Preferences}.
     */
    private void loadRenderPassState() {
        logger.debug("load RenderPass State");
        Preferences pref = Preferences.userNodeForPackage(this.getClass());

        for (RenderPass renderPass : renderPasses.getList()) {
            String key = renderPass.getClass().getSimpleName();
            int activeState = pref.getInt(key, RenderPass.ALWAYS);
            renderPass.setActiveStatus(activeState);
        }
    }

    /**
     * Save the viewport pass state to the {@link java.util.prefs.Preferences}.
     */
    public void saveRenderPassState() {
        logger.debug("save RenderPass State");
        Preferences pref = Preferences.userNodeForPackage(this.getClass());

        for (RenderPass renderPass : renderPasses.getList()) {
            String key = renderPass.getClass().getSimpleName();
            int activeState = renderPass.getActiveStatus();
            pref.putInt(key, activeState);
        }
    }

    private void allocateButtonMemory() {
        // initialize mouse button states
        for(int i=0;i<MouseInfo.getNumberOfButtons();++i) {
            buttonPressed.add(false);
        }
    }

    private void addCopyCameraAction() {
        JButton button = new JButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Copy to Scene");
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-add-16.png"))));
                putValue(Action.SHORT_DESCRIPTION,"Copy the current camera to the root of the scene.");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("copy camera");
                Camera oldCamera = Registry.getActiveCamera();
                assert oldCamera != null;
                Camera newCamera = new Camera();
                newCamera.fromJSON(oldCamera.toJSON());
                newCamera.witnessProtection();
                Registry.getScene().addChild(newCamera);
            }
        });
        toolBar.add(button);
    }

    private void addRenderPassSelection() {
        JButton button = new JButton("Render");
        toolBar.add(button);
        // Add an ActionListener to the JButton to show the JPopupMenu when clicked
        button.addActionListener(e -> renderPassMenu.show(button, button.getWidth()/2, button.getHeight()/2));
        button.setToolTipText("Select the viewport passes to use.");

        updateRenderPassMenu();
        renderPassMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                saveRenderPassState();
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
    }

    private JButton createFrameOfReferenceSelection() {
        var world = new JMenuItem(new AbstractAction() {
            private final ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-world-16.png")));

            {
                putValue(Action.NAME,"World");
                putValue(Action.SHORT_DESCRIPTION,"Use the world frame of reference.");
                putValue(Action.SMALL_ICON, icon);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setFrameOfReference(FrameOfReference.WORLD);
                frameOfReferenceButton.setIcon(icon);
            }
        });
        var camera = new JMenuItem(new AbstractAction() {
            private final ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-movie-camera-16.png")));

            {
                putValue(Action.NAME,"Camera");
                putValue(Action.SHORT_DESCRIPTION,"Use the camera frame of reference.");
                putValue(Action.SMALL_ICON, icon);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setFrameOfReference(FrameOfReference.CAMERA);
                frameOfReferenceButton.setIcon(icon);
            }
        });
        var local = new JMenuItem(new AbstractAction() {
            private final ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-eye-16.png")));

            {
                putValue(Action.NAME,"Local");
                putValue(Action.SHORT_DESCRIPTION,"Use the local frame of reference.");
                putValue(Action.SMALL_ICON, icon);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                setFrameOfReference(FrameOfReference.LOCAL);
                frameOfReferenceButton.setIcon(icon);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(world);
        group.add(camera);
        group.add(local);
        frameOfReferenceMenu.add(world);
        frameOfReferenceMenu.add(camera);
        frameOfReferenceMenu.add(local);

        frameOfReferenceButton.setIcon(world.getIcon());
        frameOfReferenceButton.addActionListener(e -> frameOfReferenceMenu.show(frameOfReferenceButton, frameOfReferenceButton.getWidth()/2, frameOfReferenceButton.getHeight()/2));
        frameOfReferenceButton.setToolTipText("Select the frame of reference.");
        return frameOfReferenceButton;
    }

    private void setFrameOfReference(FrameOfReference frameOfReference) {
        for( var t : viewportTools) t.setFrameOfReference(frameOfReference);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadRenderPassState();

        for(RenderPass renderPass : renderPasses.getList()) {
            addRenderPass(this,renderPass);
        }

        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
        Registry.selection.addItemAddedListener(this::selectionChanged);
        Registry.selection.addItemRemovedListener(this::selectionChanged);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        saveRenderPassState();
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
        Registry.selection.removeItemAddedListener(this::selectionChanged);
        Registry.selection.removeItemRemovedListener(this::selectionChanged);
    }

    private void addRenderPass(Object source,RenderPass renderPass) {
        addGLEventListener(renderPass);
        updateRenderPassMenu();
    }

    private void removeRenderPass(Object source,RenderPass renderPass) {
        removeGLEventListener(renderPass);
        updateRenderPassMenu();
    }

    /**
     * Refreshes the entire contents of the viewport pass menu.
     */
    private void updateRenderPassMenu() {
        renderPassMenu.removeAll();
        for(RenderPass renderPass : renderPasses.getList()) {
            renderPassMenu.add(createRenderPassMenuItem(renderPass));
        }
    }

    private JPanel createRenderPassMenuItem(RenderPass renderPass) {
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
        return panel;
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

    private void addCamera(Object source,Camera camera) {
        if(cameraListModel.getIndexOf(camera) == -1) {
            cameraListModel.addElement(camera);
        }
    }

    private void removeCamera(Object source,Camera camera) {
        cameraListModel.removeElement(camera);
    }

    private void addCameraSelector() {
        JComboBox<Camera> cameraSelector = new JComboBox<>();
        cameraSelector.setModel(cameraListModel);
        cameraListModel.addAll(Registry.cameras.getList());
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
        cameraSelector.setToolTipText("Select the active camera.");
        cameraSelector.addItemListener(e -> {
            Registry.setActiveCamera((Camera) e.getItem());
        });
        cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        super.init(glAutoDrawable);
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            toolShader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.vert"),
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.frag"));
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
        for(ViewportTool tool : viewportTools) tool.init(gl3);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        super.dispose(glAutoDrawable);
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        toolShader.delete(gl3);
        for(ViewportTool tool : viewportTools) tool.dispose(gl3);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        super.reshape(glAutoDrawable,x,y,width,height);
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        double dt = 1.0 / (double)this.getFPS();
        for(ViewportTool tool : viewportTools) tool.update(dt);
        updateAllNodes(dt);
        renderAllPasses();
        renderViewportTools();
    }

    private void renderViewportTools() {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        toolShader.use(gl3);
        toolShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix());
        toolShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        toolShader.setVector3d(gl3, "cameraPos", cameraWorldPos);  // Camera position in world space
        toolShader.setVector3d(gl3, "lightPos", cameraWorldPos);  // Light position in world space

        toolShader.setColor(gl3, "lightColor", Color.WHITE);
        toolShader.setColor(gl3, "diffuseColor", Color.WHITE);
        toolShader.setColor(gl3, "specularColor", Color.WHITE);
        toolShader.setColor(gl3,"ambientColor",Color.BLACK);

        toolShader.set1i(gl3,"useTexture",0);
        toolShader.set1i(gl3,"useLighting",0);
        toolShader.set1i(gl3,"useVertexColor",0);

        gl3.glDisable(GL3.GL_DEPTH_TEST);
        for(ViewportTool tool : viewportTools) {
            tool.render(gl3,toolShader);
        }
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    private void renderAllPasses() {
        // renderPasses that are always on
        for(RenderPass pass : renderPasses.getList()) {
            if(pass.getActiveStatus()==RenderPass.NEVER) continue;
            pass.draw(this);
        }
    }

    private void updateAllNodes(double dt) {
        Registry.getScene().update(dt);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        buttonPressed.set(e.getButton(),true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        buttonPressed.set(e.getButton(),false);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);

        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        int px = e.getX();
        double dx = px - mx;
        mx = px;

        int py = e.getY();
        double dy = py - my;
        my = py;

        Camera camera = Registry.getActiveCamera();
        assert camera != null;

        // scale based on orbit distance - smaller orbits need smaller movements
        double scale = camera.getOrbitRadius() * userMovementScale / 50d;
        dx *= scale;
        dy *= scale;

        boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;

        //if(buttonPressed.get(MouseEvent.BUTTON1)) {}
        if(buttonPressed.get(MouseEvent.BUTTON2)) {  // middle button
            if(!shift) {
                camera.panTilt(dx, dy);
            } else {
                camera.dolly(dy);
            }
        }
        if(buttonPressed.get(MouseEvent.BUTTON3)) {  // right button
            if(!shift) {
                camera.orbit(dx,dy);
            } else {
                camera.truck(-dx);
                camera.pedestal(dy);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        int dz = e.getWheelRotation();
        changeOrbitRadius(dz);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        mx = e.getX();
        my = e.getY();
        //logger.debug("mouse {},{}",e.getX(),e.getY());
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);
    }

    /**
     * Change the distance from the camera to the orbit point.  The orbit point does not move.  In effect the camera
     * is performing a dolly in/out.
     * @param dz mouse wheel movement
     */
    private void changeOrbitRadius(int dz) {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        camera.orbitDolly(dz > 0 ? orbitChangeFactor : 1.0 / orbitChangeFactor);
    }

    public double getOrbitChangeFactor() {
        return orbitChangeFactor;
    }

    /**
     * @param amount a value greater than one.
     */
    public void setOrbitChangeFactor(double amount) {
        if( amount <= 1 ) throw new InvalidParameterException("orbit change factor must be greater than 1.");
        orbitChangeFactor = amount;
    }

    /**
     * <p>Return the ray coming through the viewport in the current projection.  Remember that in OpenGL the
     * camera -Z=forward, +X=right, +Y=up</p>
     * @param x the cursor position in screen coordinates [-1,1]
     * @param y the cursor position in screen coordinates [-1,1]
     * @return the ray coming through the viewport in the current projection.
     */
    public Ray getRayThroughPoint(Camera camera,double x,double y) {
        Point3d origin;
        Vector3d direction;

        if(camera.getDrawOrthographic()) {
            // orthographic projection
            origin = new Point3d(
                    x*canvasWidth/10,
                    y*canvasHeight/10,
                    0);
            direction = new Vector3d(0,0,-1);
            Matrix4d m2 = camera.getWorld();
            m2.transform(direction);
            m2.transform(origin);
        } else {
            // perspective projection
            Vector3d cursorUnitVector = getCursorAsNormalized();
            double t = Math.tan(Math.toRadians(camera.getFovY()/2));
            direction = new Vector3d((cursorUnitVector.x)*t*getAspectRatio(),
                    (cursorUnitVector.y)*t,
                    -1);
            // adjust the ray by the camera world pose.
            Matrix4d m2 = camera.getWorld();
            m2.transform(direction);
            origin = new Point3d(MatrixHelper.getPosition(m2));
            //logger.debug("origin {} direction {}",origin,direction);
        }

        return new Ray(origin,direction);
    }

    /**
     * @return the cursor position as values from -1...1.
     */
    public Vector3d getCursorAsNormalized() {
        return new Vector3d((2.0*mx/canvasWidth)-1.0,
                1.0-(2.0*my/canvasHeight),
                0);
    }

    public Point2d getCursorPosition() {
        return new Point2d(mx,my);
    }

    public double getAspectRatio() {
        return (double)canvasWidth/(double)canvasHeight;
    }

    private void deactivateAllTools() {
        for(ViewportTool tool : viewportTools) {
            tool.deactivate();
        }
    }

    public int getNumTools() {
        return viewportTools.size();
    }

    /**
     * Set the active tool by index.
     * @param index the index of the tool to activate.
     */
    public void setActiveToolIndex(int index) {
        if(index < 0 || index >= viewportTools.size()) throw new InvalidParameterException("activeToolIndex out of range.");

        deactivateAllTools();
        // if we reselect the current tool, toggle off.
        activeToolIndex = (activeToolIndex == index) ? -1 : index;

        if(activeToolIndex >= 0) {
            viewportTools.get(activeToolIndex).activate(Registry.selection.getList());
        }
    }

    private void selectionChanged(Object source,Object item) {
        if(activeToolIndex >= 0) {
            viewportTools.get(activeToolIndex).activate(Registry.selection.getList());
        }
    }

    public double getUserMovementScale() {
        return userMovementScale;
    }

    /**
     * Set the scale of user movement.  This is used to scale the mouse movement to the camera movement.
     * @param scale a value greater than zero.
     */
    public void setUserMovementScale(double scale) {
        if(scale<=0) throw new InvalidParameterException("scale must be greater than zero.");
        userMovementScale=scale;
    }
}