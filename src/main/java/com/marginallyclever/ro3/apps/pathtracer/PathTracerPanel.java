package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.apps.viewport.viewporttool.SelectionTool;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.raypicking.RayPickSystem;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * PathTracerPanel performs basic rendering of a scene using path tracing.
 */
public class PathTracerPanel extends JPanel implements SceneChangeListener {
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private Camera activeCamera;
    private int canvasWidth = 640;
    private int canvasHeight = 480;
    private BufferedImage buffer;
    private final List<RayXY> rays = new ArrayList<>();
    private final JLabel centerLabel = new JLabel();

    record RayXY(Ray ray,int x,int y) {}

    public PathTracerPanel() {
        super(new BorderLayout());
        setupToolbar();
        add(toolBar, BorderLayout.NORTH);
        add(centerLabel,BorderLayout.CENTER);

        // resize listener, update the canvas size
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.addSceneChangeListener(this);
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.removeSceneChangeListener(this);
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
    }


    private void addCamera(Object source,Camera camera) {
        if(cameraListModel.getIndexOf(camera) == -1) {
            cameraListModel.addElement(camera);
        }
    }

    private void removeCamera(Object source,Camera camera) {
        cameraListModel.removeElement(camera);
    }

    private void setupToolbar() {
        // add the same camera selection that appears in ViewportPanel

        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addCameraSelector();
        toolBar.add(new AbstractAction() {
            {
                putValue(Action.NAME, "Render");
                putValue(Action.SHORT_DESCRIPTION, "Render the scene using path tracing.");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                render();
            }
        });
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


    public Camera getActiveCamera() {
        if(Registry.cameras.getList().isEmpty()) throw new RuntimeException("No cameras available.");
        return activeCamera;
    }

    public void setActiveCamera(Camera camera) {
        activeCamera = camera;
        // set camera to registry active camera
        cameraListModel.setSelectedItem(activeCamera);
    }

    public void render() {
        canvasWidth = getWidth();
        canvasHeight = getHeight();
        buffer = new BufferedImage(canvasWidth,canvasHeight,BufferedImage.TYPE_INT_RGB);

        // update rays
        rays.clear();
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                var nx = (2.0*x/getWidth()) - 1.0;
                var ny = 1.0 - (2.0*y/getHeight());
                rays.add(new RayXY(getRayThroughPoint(activeCamera,nx,ny),x,y));
            }
        }

        // clear view
        var g = buffer.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,canvasWidth,canvasHeight);

        // in parallel, trace each ray and store the result in a buffer
        rays.stream().parallel().forEach(ray -> {
            // trace the ray
            RayPickSystem rayPickSystem = new RayPickSystem();
            RayHit rayHit = rayPickSystem.getFirstHit(ray.ray);
            // store the result in the buffer
            if(rayHit==null) return;
            //var p = ray.getPoint(rayHit.distance());

            // set color based on distance
            var c = rayHit.distance();
            c = 255 - Math.max(0, Math.min(255, c));
            Color color;

            var t = rayHit.target();
            if(t != null) {
                var mat = t.findFirstSibling(Material.class);
                if(mat!=null) {
                    Color inter = mat.getDiffuseColor();
                    var c2 = c/255.0;
                    color = new Color(
                            (int)(c2 * inter.getRed()),
                            (int)(c2 * inter.getGreen()),
                            (int)(c2 * inter.getBlue()));
                } else {
                    color = new Color((int) c, (int) c, (int) c);
                }
            } else {
                color = new Color((int) c, (int) c, (int) c);
            }
            buffer.setRGB(ray.x, ray.y, color.getRGB());
        });

        // then draw the buffer to the screen
        g.drawImage(buffer,0,0,null);

        centerLabel.setIcon(new ImageIcon(buffer));
        // display buffer in the center of this panel.
        repaint();
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

    public double getAspectRatio() {
        return (double)canvasWidth/(double)canvasHeight;
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
}
