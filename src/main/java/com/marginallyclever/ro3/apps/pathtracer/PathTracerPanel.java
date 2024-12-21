package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.raypicking.RayPickSystem;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>PathTracerPanel performs basic rendering of a scene using path tracing.</p>
 * <p>Special thanks to <a href='https://raytracing.github.io/books/RayTracingInOneWeekend.html'>Ray Tracing in One Weekend</a></p>
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
    private final RayPickSystem rayPickSystem = new RayPickSystem();
    private int samplesPerPixel = 20;
    private int maxDepth = 4;
    private final Color skyColor = new Color(
            (int)(255.0 * 0.5),
            (int)(255.0 * 0.7),
            (int)(255.0 * 1.0));
    private Color ambientColor = new Color(64,64,64);
    private Color sunlightColor = new Color(255,255,255);
    private final Vector3d sunlightSource = new Vector3d(150,150,150);
    private final JProgressBar progressBar = new JProgressBar();
    private RayTracingWorker rayTracingWorker;

    record RayXY(int x,int y) {}

    public PathTracerPanel() {
        super(new BorderLayout());
        setupToolbar();
        add(toolBar, BorderLayout.NORTH);
        add(centerLabel,BorderLayout.CENTER);
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
        toolBar.add(progressBar);
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

    private int clampColor(double c) {
        return (int)(Math.max(0,Math.min(255,c)));
    }

    public void render() {
        getSunlight();
        canvasWidth = getWidth();
        canvasHeight = getHeight();
        buffer = new BufferedImage(canvasWidth,canvasHeight,BufferedImage.TYPE_INT_RGB);
        centerLabel.setIcon(new ImageIcon(buffer));
        progressBar.setValue(0);

        // update rays
        rays.clear();
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                rays.add(new RayXY(x,y));
            }
        }

        if(rayTracingWorker!=null) {
            rayTracingWorker.cancel(true);
        }
        rayTracingWorker = new RayTracingWorker(rays,buffer);
        rayTracingWorker.execute();
    }

    /**
     * <p>Trace the ray and return the color of the pixel at the end of the ray.</p>
     * @param ray the ray to trace
     * @param depth the maximum number of bounces to trace
     * @return the color of the pixel at the end of the ray.
     */
    private Color rayColor(Ray ray,int depth) {
        if(depth<=0) return Color.BLACK;

        // trace the ray
        RayHit rayHit = rayPickSystem.getFirstHit(ray);
        if(rayHit==null) return getSkyColor(ray);
        return getIntersectionColor(ray,rayHit,depth);
    }

    // sky color
    // TODO use ViewportSettings to get the angle and color of the sun.
    private Color getSkyColor(Ray ray) {
        Vector3d d = ray.getDirection();
        d.normalize();
        var a = 0.5 * (-d.z + 1.0);
        return new Color(
                clampColor(a * skyColor.getRed()   + (1.0-a)*255.0),
                clampColor(a * skyColor.getGreen() + (1.0-a)*255.0),
                clampColor(a * skyColor.getBlue()  + (1.0-a)*255.0));
    }

    private Color getIntersectionColor(Ray ray, RayHit rayHit, int depth) {
        // if material, set color based on material.  else... white.
        var target = rayHit.target();
        assert(target!=null);

        var mat = target.findFirstSibling(Material.class);
        Color diffuseColor = (mat==null)? Color.WHITE : mat.getDiffuseColor();
        Color emissionColor = (mat==null)? Color.BLACK : mat.getEmissionColor();

        // get the point where the ray hit
        // get face normal
        var normal = rayHit.normal();
        // lambertian reflection
        var direction = new Vector3d(normal);
        direction.add(getRandomUnitVector());
        if(direction.lengthSquared() < 1e-6) {
            direction.set(normal);
        }

        // bounce and collect light from other surfaces
        var from = ray.getPoint(rayHit.distance());
        var newRay = new Ray(from,direction);
        var newColor = rayColor(newRay,depth-1);

        var diffuseR = diffuseColor.getRed()/255.0;
        var diffuseG = diffuseColor.getGreen()/255.0;
        var diffuseB = diffuseColor.getBlue()/255.0;

        // check in shadow
        Vector3d lightDirection = new Vector3d(sunlightSource);
        lightDirection.normalize();
        boolean inShadow = isInShadow(from, lightDirection,rayHit);
        var incident = Math.max(0,lightDirection.dot(normal));
        double s = incident * (inShadow ? 0.5 : 1.0) / 255.0;

        // result *= ambient + (diffuseLight * specularLight) * (1.0-shadow)
        // result += emissionLight
        diffuseR *= ambientColor.getRed()   + s * sunlightColor.getRed()   * newColor.getRed();
        diffuseG *= ambientColor.getGreen() + s * sunlightColor.getGreen() * newColor.getGreen();
        diffuseB *= ambientColor.getBlue()  + s * sunlightColor.getBlue()  * newColor.getBlue();

        return new Color(
                clampColor(emissionColor.getRed()   + diffuseR ),
                clampColor(emissionColor.getGreen() + diffuseG ),
                clampColor(emissionColor.getBlue()  + diffuseB )
        );
    }

    private boolean isInShadow(Vector3d point, Vector3d lightDirection, RayHit rayHit) {
        Vector3d p2 = new Vector3d(rayHit.normal());
        p2.scaleAdd(1e-10,point);
        Ray shadowRay = new Ray(p2, lightDirection);
        RayHit shadowHit = rayPickSystem.getFirstHit(shadowRay);
        return shadowHit != null;
    }

    /**
     * @param n the normal
     * @return a random unit vector on the hemisphere defined by the normal.
     */
    private Vector3d getRandomUnitVectorOnHemisphere(Vector3d n) {
        var v = getRandomUnitVector();
        if(v.dot(n) < 0) {
            v.negate();
        }
        return v;
    }

    private Vector3d getRandomUnitVector() {
        var p = new Vector3d();
        do {
            p.set(Math.random()*2-1,Math.random()*2-1,Math.random()*2-1);
        } while(p.lengthSquared() <=1e-6);
        p.normalize();
        return p;
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

    private class RayTracingWorker extends SwingWorker<Void,Integer> {
        private final List<RayXY> pixels;
        private final BufferedImage image;

        public RayTracingWorker(List<RayXY> pixels,BufferedImage image) {
            this.pixels = pixels;
            this.image = image;
        }

        @Override
        protected Void doInBackground() throws Exception {
            int total = pixels.size();
            AtomicInteger completed = new AtomicInteger(0);

            // clear view
            var graphics = buffer.getGraphics();
            graphics.setColor(centerLabel.getBackground());
            graphics.fillRect(0,0,canvasWidth,canvasHeight);

            // in parallel, trace each ray and store the result in a buffer
            rays.stream().parallel().forEach(pixel -> {
                double r=0, g=0, b=0;
                for(int i=0;i<samplesPerPixel;++i) {
                    // jiggle the ray a little bit to get a better anti-aliasing effect
                    var nx =       (2.0*(pixel.x+Math.random()-0.5)/canvasWidth ) - 1.0;
                    var ny = 1.0 - (2.0*(pixel.y+Math.random()-0.5)/canvasHeight);
                    var ray = getRayThroughPoint(activeCamera,nx,ny);
                    Color result = rayColor(ray,maxDepth);
                    r += result.getRed();
                    g += result.getGreen();
                    b += result.getBlue();
                }
                var result = new Color(
                        clampColor(r/samplesPerPixel),
                        clampColor(g/samplesPerPixel),
                        clampColor(b/samplesPerPixel));
                // store the result in the buffer
                drawPixel(pixel,result);

                // Update progress
                int done = completed.incrementAndGet();
                // Optionally only publish occasionally to avoid flooding the EDT
                if (done % 100 == 0 || done == total) {
                    publish((int) ((done * 100.0f) / total));
                }
            });

            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            // The last value in chunks is the most recent progress value
            int latestProgress = chunks.get(chunks.size() - 1);
            // Update progress bar here
            progressBar.setValue(latestProgress);

            // display buffer in the center of this panel.
            repaint();
        }

        @Override
        protected void done() {
            // Rendering finished
            rayTracingWorker=null;
        }

        private void drawPixel(RayXY pixel, Color c) {
            // Convert Ray’s coordinate to pixel indices and set the pixel’s color
            image.setRGB(pixel.x, pixel.y, c.getRGB());
        }
    }

    private void getSunlight() {
        Environment env = Registry.getScene().findFirstChild(Environment.class);
        if(null==env) {
            env = new Environment();
            Registry.getScene().addChild(env);
        }

        sunlightSource.set(env.getSunlightSource());
        sunlightColor = env.getSunlightColor();
        ambientColor = env.getAmbientColor();
    }
}
