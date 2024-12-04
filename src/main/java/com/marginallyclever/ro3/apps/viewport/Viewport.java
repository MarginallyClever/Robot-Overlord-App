package com.marginallyclever.ro3.apps.viewport;

import ModernDocking.app.Docking;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.apps.viewport.renderpass.*;
import com.marginallyclever.ro3.apps.viewport.viewporttools.Compass3D;
import com.marginallyclever.ro3.apps.viewport.viewporttools.SelectionTool;
import com.marginallyclever.ro3.apps.viewport.viewporttools.ViewportTool;
import com.marginallyclever.ro3.apps.viewport.viewporttools.move.RotateToolMulti;
import com.marginallyclever.ro3.apps.viewport.viewporttools.move.TranslateToolMulti;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * {@link Viewport} is an {@link JPanel} that uses a set of {@link RenderPass}es to draw the
 * {@link Registry#getScene()} from the perspective of the active {@link Camera}.  Mouse actions in the panel
 * can be used to manipulate the active camera.
 */
public class Viewport
        extends JPanel
        implements SceneChangeListener,
        MouseListener,
        MouseMotionListener,
        MouseWheelListener {
    private static final Logger logger = LoggerFactory.getLogger(Viewport.class);
    private Camera activeCamera;
    public ListWithEvents<RenderPass> renderPasses = new ListWithEvents<>();
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private final JPopupMenu renderPassMenu = new JPopupMenu();
    private final List<Boolean> buttonPressed = new ArrayList<>();
    private int mouseX, mouseY;
    private double orbitChangeFactor = 1.1;  // must always be greater than 1
    protected int canvasWidth, canvasHeight;
    protected final List<ViewportTool> viewportTools = new ArrayList<>();
    private int activeToolIndex = -1;
    private double userMovementScale = 1.0;
    private final JButton frameOfReferenceButton = new JButton();
    private final JPopupMenu frameOfReferenceMenu = new JPopupMenu();
    private boolean originShift = true;

    public Viewport() {
        this(new BorderLayout());
    }

    public Viewport(LayoutManager layout) {
        super(layout);
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

        for (ViewportTool tool : viewportTools) {
            tool.setViewport(this);
        }

        String[] toolOptions = {"Select", "Move", "Rotate"};
        ImageIcon[] toolIcons = {
                new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-select-16.png"))),
                new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-move-16.png"))),
                new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-rotate-16.png")))
        };

        JComboBox<String> toolDropdown = new JComboBox<>(toolOptions);
        toolDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index >= 0 && index < toolIcons.length) {
                    setIcon(toolIcons[index]);
                } else if (value != null) {
                    int selectedIndex = toolDropdown.getSelectedIndex();
                    if (selectedIndex >= 0 && selectedIndex < toolIcons.length) {
                        setIcon(toolIcons[selectedIndex]);
                    }
                }
                return this;
            }
        });
        toolDropdown.setToolTipText("Select a tool");

        toolDropdown.addActionListener(e -> {
            switch ((String) Objects.requireNonNull(toolDropdown.getSelectedItem())) {
                default -> setActiveToolIndex(viewportTools.indexOf(selectionTool));
                case "Move" -> setActiveToolIndex(viewportTools.indexOf(translateToolMulti));
                case "Rotate" -> setActiveToolIndex(viewportTools.indexOf(rotateToolMulti));
            }
        });

        toolBar.add(toolDropdown);
        toolBar.add(createFrameOfReferenceSelection());
        toolBar.add(new JSeparator());
        toolBar.add(new LookAtLastSelected(this));
        toolBar.add(new ZoomToSelected(this));
    }

    private void addRenderPasses() {
        renderPasses.add(new DrawBackground());
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
                Camera oldCamera = getActiveCamera();
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

        Registry.addSceneChangeListener(this);
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
        Registry.selection.addItemAddedListener(this::selectionChanged);
        Registry.selection.addItemRemovedListener(this::selectionChanged);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        saveRenderPassState();
        Registry.removeSceneChangeListener(this);
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
        Registry.selection.removeItemAddedListener(this::selectionChanged);
        Registry.selection.removeItemRemovedListener(this::selectionChanged);
    }

    protected void addRenderPass(Object source,RenderPass renderPass) {
        updateRenderPassMenu();
    }

    protected void removeRenderPass(Object source,RenderPass renderPass) {
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
        cameraSelector.addActionListener(e -> {
            setActiveCamera((Camera)cameraSelector.getSelectedItem());
        });
        cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
    }

    public void renderAllPasses() {
        // renderPasses that are always on
        for(RenderPass pass : renderPasses.getList()) {
            if(pass.getActiveStatus()==RenderPass.NEVER) continue;
            pass.draw(this);
        }
    }

    public void updateAllNodes(double dt) {
        Registry.getPhysics().update(dt);
        Registry.getScene().update(dt);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        buttonPressed.set(e.getButton(),true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        buttonPressed.set(e.getButton(),false);
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        int px = e.getX();
        double dx = px - mouseX;
        mouseX = px;

        int py = e.getY();
        double dy = py - mouseY;
        mouseY = py;

        Camera camera = getActiveCamera();
        assert camera != null;

        // scale based on orbit distance - smaller orbits need smaller movements
        double scale = camera.getOrbitRadius() * userMovementScale / 50d;
        boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;

        //if(buttonPressed.get(MouseEvent.BUTTON1)) {}
        if(buttonPressed.get(MouseEvent.BUTTON2)) {  // middle button
            if(!shift) {
                camera.panTilt(dx, dy);
            } else {
                camera.dolly(dy * scale);
            }
        }
        if(buttonPressed.get(MouseEvent.BUTTON3)) {  // right button
            if(!shift) {
                camera.orbit(dx,dy);
            } else {
                camera.truck(-dx * scale);
                camera.pedestal(dy * scale);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);

        int dz = e.getWheelRotation();
        changeOrbitRadius(dz);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        //logger.debug("mouse {},{}",e.getX(),e.getY());
        for(ViewportTool tool : viewportTools) tool.handleMouseEvent(e);
    }

    /**
     * Change the distance from the camera to the orbit point.  The orbit point does not move.  In effect the camera
     * is performing a dolly in/out.
     * @param dz mouse wheel movement
     */
    private void changeOrbitRadius(int dz) {
        Camera camera = getActiveCamera();
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
     * <p>Return the ray, in world space, that starts at the camera and passes through this viewport at (x,y) in the
     * current projection.  x,y should be normalized screen coordinates adjusted for the vertical flip.</p>
     * <p>Remember that in OpenGL the camera -Z=forward, +X=right, +Y=up</p>
     * @param normalizedX the cursor position in screen coordinates [-1,1]
     * @param normalizedY the cursor position in screen coordinates [-1,1]
     * @return the ray coming through the viewport in the current projection.
     */
    public Ray getRayThroughPoint(Camera camera,double normalizedX,double normalizedY) {
        Ray r = getRayThroughPointUntransformed(camera,normalizedX,normalizedY);
        Ray transformedRay = new Ray();
        // adjust by the camera world orientation.
        transformedRay.transform(camera.getWorld(),r);
        return transformedRay;
    }

    /**
     * <p>Return the ray, in camera space, that starts at the origin and passes through this viewport at (x,y) in the
     * current projection.  x,y should be normalized screen coordinates adjusted for the vertical flip.</p>
     * <p>Remember that in OpenGL the camera -Z=forward, +X=right, +Y=up</p>
     * @param normalizedX the cursor position in screen coordinates [-1,1]
     * @param normalizedY the cursor position in screen coordinates [-1,1]
     * @return the ray coming through the viewport in the current projection.
     */
    public Ray getRayThroughPointUntransformed(Camera camera,double normalizedX,double normalizedY) {
        if(camera.getDrawOrthographic()) {
            // orthographic projection
            var origin = new Point3d(
                    normalizedX*canvasWidth/2.0,
                    normalizedY*canvasHeight/2.0,
                    0);
            var direction = new Vector3d(0,0,-1);  // forward in camera space

            return new Ray(origin,direction);
        } else {
            // perspective projection
            double t = Math.tan(Math.toRadians(camera.getFovY()/2));
            var direction = new Vector3d(
                    normalizedX*t*getAspectRatio(),
                    normalizedY*t,
                    -1);
            var origin = new Point3d();

            return new Ray(origin,direction);
        }
    }

    /**
     * In OpenGL camera space +Y is up and in screen space +Y is down so invert the Y value.
     * @return the cursor position as (-1...1,-1...1,0).
     */
    public Vector3d getCursorAsNormalized() {
        return getCursorAsNormalized(mouseX, mouseY);
    }

    /**
     * In OpenGL camera space +Y is up and in screen space +Y is down so invert the Y value.
     * @return the cursor position as (-1...1,-1...1,0).
     */
    public Vector3d getCursorAsNormalized(double x,double y) {
        return new Vector3d(
                (2.0*x/canvasWidth) - 1.0,
                1.0 - (2.0*y/canvasHeight),
                0);
    }

    public Point2d getCursorPosition() {
        return new Point2d(mouseX, mouseY);
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

    @Override
    public void beforeSceneChange(Node oldScene) {}

    @Override
    public void afterSceneChange(Node newScene) {
        // find the first active camera in the scene.
        Node scene = Registry.getScene();
        var camera = scene.findFirstChild(Camera.class);
        if(camera!=null) {
            setActiveCamera(camera);
        }
    }


    public Camera getActiveCamera() {
        if(Registry.cameras.getList().isEmpty()) throw new RuntimeException("No cameras available.");
        return activeCamera;
    }

    public void setActiveCamera(Camera camera) {
        activeCamera = camera;
        // set camera to registry active camera
        cameraListModel.setSelectedItem(activeCamera);
    }

    public boolean isHardwareAccelerated() {
        return false;
    }

    public void setHardwareAccelerated(boolean selected) {
        // do nothing
    }

    public boolean isVerticalSync() {
        return false;
    }

    public void setVerticalSync(boolean selected) {
        // do nothing
    }

    public int getFsaaSamples() {
        return 0;
    }

    public void setFsaaSamples(Integer value) {
    }

    public void savePrefs() {}

    public boolean isOriginShift() {
        return originShift;
    }

    public void setOriginShift(boolean b) {
        originShift = b;
    }
}