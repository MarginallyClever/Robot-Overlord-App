package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.PanelHelper;
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
import java.util.Random;
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
    private final ColorDouble skyColor = new ColorDouble(0.5,0.7,1.0);
    private ColorDouble ambientColor = new ColorDouble(new Color(64,64,64));
    private ColorDouble sunlightColor = new ColorDouble(1,1,1,25.0/255.0);
    private final Vector3d sunlightSource = new Vector3d(150,150,150);
    private final JProgressBar progressBar = new JProgressBar();
    private RayTracingWorker rayTracingWorker;
    private final Material defaultMaterial = new Material();
    private long startTime;
    private final JLabel runTime = new JLabel();
    private static final Random random = new Random();

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

        var spp = PanelHelper.addNumberFieldInt("Samples per pixel",samplesPerPixel);
        spp.addPropertyChangeListener("value",e->setSamplesPerPixel(((Number)e.getNewValue()).intValue()));
        toolBar.add(spp);

        var md = PanelHelper.addNumberFieldInt("Max Depth",maxDepth);
        md.addPropertyChangeListener("value",e->setMaxDepth(((Number)e.getNewValue()).intValue()));
        toolBar.add(md);

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
        toolBar.add(runTime);
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
        if(rayTracingWorker!=null) {
            rayTracingWorker.cancel(true);
            rayTracingWorker = null;
        } else {
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

            rayTracingWorker = new RayTracingWorker(rays, buffer);
            rayTracingWorker.execute();
        }
    }

    /**
     * <p>Trace the ray and return the color of the pixel at the end of the ray.</p>
     * @param ray the ray to trace
     * @param depth the maximum number of bounces to trace
     * @return the color of the pixel at the end of the ray.
     */
    private ColorDouble rayColor(Ray ray,int depth) {
        if(depth<=0) return new ColorDouble(0,0,0);

        // trace the ray
        RayHit rayHit = rayPickSystem.getFirstHit(ray);
        if(rayHit==null) return getSkyColor(ray);
        return getIntersectionColor(ray,rayHit,depth);
    }

    // sky color
    // TODO use ViewportSettings to get the angle and color of the sun.
    private ColorDouble getSkyColor(Ray ray) {
        Vector3d d = ray.getDirection();
        d.normalize();
        Vector3d sun = new Vector3d(sunlightSource);
        sun.normalize();
        var dot = Math.max(0,sun.dot(d));

        var sd = Math.pow(dot,3);
        //var a = 0.5 * (-d.z + 1.0);
        var a = 1.0-sd;
        return new ColorDouble(
                /* a * skyColor.r + */ a * ambientColor.r + sd * sunlightColor.r * sunlightColor.a,
                /* a * skyColor.g + */ a * ambientColor.g + sd * sunlightColor.g * sunlightColor.a,
                /* a * skyColor.b + */ a * ambientColor.b + sd * sunlightColor.b * sunlightColor.a);
    }

    private ColorDouble getIntersectionColor(Ray ray, RayHit rayHit, int depth) {
        // if material, set color based on material.  else... white.
        var target = rayHit.target();
        assert(target!=null);

        var mat = target.findFirstSibling(Material.class);
        if(mat==null) mat = defaultMaterial;

        ColorDouble albedo = new ColorDouble(mat.getDiffuseColor());
        ColorDouble sum = new ColorDouble(albedo);

        // get the point where the ray hit
        var hitPoint = ray.getPoint(rayHit.distance());

        // get face normal
        var normal = rayHit.normal();
        double reflectivity = mat.getReflectivity();
        if(reflectivity<1.0) {
            // lambertian reflection - bounce and collect light from other surfaces
            var direction = new Vector3d(normal);
            direction.add(PathTracerPanel.getRandomUnitVector());
            if (direction.lengthSquared() < 1e-6) {
                direction.set(normal);
            }
            var newRay = new Ray(hitPoint, direction);
            var newColor = rayColor(newRay, depth - 1);

            // check in shadow
            //Vector3d lightDirection = new Vector3d(sunlightSource);
            //lightDirection.normalize();
            //boolean inShadow = isInShadow(from, lightDirection,rayHit);
            //var incident = Math.max(0,lightDirection.dot(normal));
            //double s = incident * (inShadow ? 0.5 : 1.0) / 255.0;

            // result *= ambient + (diffuseLight * specularLight) * (1.0-shadow)
            // result += emissionLight
            sum.scale(newColor);
        }
        if(reflectivity>0) {
            // reflection
            var reflected = reflect(ray.getDirection(),normal);
            var reflectedRay = new Ray(hitPoint,reflected);
            var reflectedColor = rayColor(reflectedRay,depth-1);
            sum = blend(reflectedColor,sum,reflectivity);
        }

        var alpha = albedo.a;
        if(alpha<1.0) {
            // at least semi-transparent.  use index of refraction to calculate refraction.
            var rayDirection = ray.getDirection();
            rayDirection.normalize();

            var ior = mat.getIOR();
            var cosTheta = Math.min(-normal.dot(rayDirection),1.0);
            // since normals face outside an object, if cosI is positive the ray is exiting the object.
            boolean backFace = cosTheta > 0;
            if(backFace) normal.negate();

            var ri = backFace ? 1.0/ior : ior;
            var sinTheta = Math.sqrt(1.0-cosTheta*cosTheta);
            var cannotRefract = ri * sinTheta > 1.0;
            var nextDir = cannotRefract || reflectance(cosTheta,ri) > Math.random()
                    ? reflect(rayDirection,normal) // total internal reflection
                    : refract(rayDirection,normal,ri); // refraction

            // Recursively compute the color along the next ray
            Ray nextRay = new Ray(hitPoint, nextDir);
            ColorDouble throughColor = rayColor(nextRay, depth-1);

            sum = blend(sum,throughColor,alpha);
        }

        ColorDouble emissionColor = new ColorDouble(mat.getEmissionColor());
        emissionColor.scale(emissionColor.a*255);
        sum.add(emissionColor);
        return sum;
    }

    private void toneMap(ColorDouble d) {
        acesApprox(d);
    }

    private void acesApprox(ColorDouble v) {
        v.scale(0.6);
        double a = 2.51;
        double b = 0.03;
        double c = 2.43;
        double d = 0.59;
        double e = 0.14;
        v.r = Math.max(0,Math.min(1, (v.r * (a*v.r+b)) / (v.r * (c*v.r+d)+e) ));
        v.g = Math.max(0,Math.min(1, (v.g * (a*v.g+b)) / (v.g * (c*v.g+d)+e) ));
        v.b = Math.max(0,Math.min(1, (v.b * (a*v.b+b)) / (v.b * (c*v.b+d)+e) ));
    }

    /**
     * @param a the first color
     * @param b the second color
     * @param alpha the blend factor (0...1)
     * @return a * alpha + b * (1-alpha)
     */
    private ColorDouble blend(ColorDouble a,ColorDouble b,double alpha) {
        double oneMinusAlpha = 1.0-alpha;
        return new ColorDouble(
            a.r * alpha + b.r * oneMinusAlpha,
            a.g * alpha + b.g * oneMinusAlpha,
            a.b * alpha + b.b * oneMinusAlpha
        );
    }

    /**
     * Use Shlick's approximation for reflectance
     * @param cosI the cosine of the angle of incidence
     * @param eta the ratio of the refractive indices of the two materials
     * @return the reflectance
     */
    private double reflectance(double cosI, double eta) {
        var r0 = (1.0-eta) / (1.0+eta);
        r0 *= r0;
        return r0 + (1.0-r0) * Math.pow(1.0-cosI,5);
    }

    /**
     * Reflect the vector v off the normal n
     * @param v the vector to reflect
     * @param n the normal
     * @return the reflected vector
     */
    private Vector3d reflect(Vector3d v, Vector3d n) {
        var dot = 2.0 * v.dot(n);
        return new Vector3d(
            v.x - n.x * dot,
            v.y - n.y * dot,
            v.z - n.z * dot);
    }

    /**
     * Refract the vector uv through the normal n
     * @param uv the vector to refract
     * @param n the normal
     * @param eta the ratio of the refractive indices of the two materials
     * @return the refracted vector
     */
    private Vector3d refract(Vector3d uv, Vector3d n, double eta) {
        var cosTheta = Math.min(-uv.dot(n), 1.0);
        // vec3 r_out_perp =  etai_over_etat * (uv + cos_theta*n);
        Vector3d outPerpendicular = new Vector3d(n);
        n.scaleAdd(cosTheta,uv);
        n.scale(eta);
        // vec3 r_out_parallel = -sqrt(abs(1.0 - r_out_perp.length_squared())) * n;
        Vector3d outParallel = new Vector3d(n);
        outParallel.scale(-Math.sqrt(Math.abs(1.0 - outPerpendicular.lengthSquared())));
        outPerpendicular.add(outParallel);
        return outPerpendicular;
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
    public static Vector3d getRandomUnitVectorOnHemisphere(Vector3d n) {
        var v = getRandomUnitVector();
        if(v.dot(n) < 0) {
            v.negate();
        }
        return v;
    }

    /**
     * @return a random vector on the unit sphere
     */
    private static Vector3d getRandomUnitVector() {
        double t1 = random.nextDouble() * 2.0 * Math.PI;
        var y = (random.nextDouble() - 0.5) * 2.0;
        double t2 = Math.sqrt(1.0 - y*y);
        var x = t2 * Math.cos(t1);
        var z = t2 * Math.sin(t1);
        return new Vector3d(x,y,z);
    }

    /**
     * I'm told this isn't evenly distributed.
     * @return a random vector on the unit sphere
     */
    public static Vector3d getRandomUnitVectorOld2() {
        double u, v, s;
        do {
            u = 2.0 * random.nextDouble() - 1.0;
            v = 2.0 * random.nextDouble() - 1.0;
            s = u * u + v * v;
        } while (s >= 1 || s == 0);

        double scale = Math.sqrt(1 - s);
        double x = 2.0 * u * scale;
        double y = 2.0 * v * scale;
        double z = 1.0 - 2.0 * s;

        return new Vector3d(x, y, z);
    }

    /**
     * I'm told this isn't evenly distributed.
     * @return a random vector on the unit sphere
     */
    public static Vector3d getRandomUnitVectorOld() {
        var p = new Vector3d();
        double len;
        while(true) {
            p.set(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
            len = p.lengthSquared();
            if (1e-16 < len && len <= 1) {
                p.scale(1.0 / Math.sqrt(len));
                return p;
            }
        }
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
            direction.normalize();
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
            int total = pixels.size() * samplesPerPixel;
            AtomicInteger completed = new AtomicInteger(0);

            startTime = System.currentTimeMillis();

            System.out.println("samples per pixel: "+samplesPerPixel);
            System.out.println("max depth: "+maxDepth);

            // clear view
            var graphics = buffer.getGraphics();
            graphics.setColor(centerLabel.getBackground());
            graphics.fillRect(0,0,canvasWidth,canvasHeight);

            // in parallel, trace each ray and store the result in a buffer
            while(true) {
                if(isCancelled()) return null;

                rays.stream().parallel().forEach(pixel -> {
                    if(isCancelled()) return;
                    // jiggle the ray a little bit to get a better anti-aliasing effect
                    var nx =       (2.0*(pixel.x+Math.random()-0.5)/canvasWidth ) - 1.0;
                    var ny = 1.0 - (2.0*(pixel.y+Math.random()-0.5)/canvasHeight);
                    var ray = getRayThroughPoint(activeCamera,nx,ny);
                    // sum the total color of all samples
                    pixel.sum.add(rayColor(ray,maxDepth));
                    // and count
                    pixel.samples++;
                    // then get the average of the samples
                    pixel.average.set(pixel.sum);
                    pixel.average.scale(1.0/pixel.samples);
                    toneMap(pixel.average);
                    var result = pixel.average.getColor();
                    // store the result in the buffer
                    drawPixel(pixel,result);

                    // Update progress
                    int done = completed.incrementAndGet();
                    // Optionally only publish occasionally to avoid flooding the EDT
                    if (done % 100 == 0 || done == total) {
                        publish((int) ((done * 100.0f) / total));

                        if(done == total) {
                            cancel(true);
                        }
                    }
                });
            }
        }

        @Override
        protected void process(List<Integer> chunks) {
            // The last value in chunks is the most recent progress value
            int latestProgress = chunks.get(chunks.size() - 1);
            // Update progress bar here
            progressBar.setValue(latestProgress);

            var elapsed = System.currentTimeMillis() - startTime;
            // display in hh:mm:ss:ms
            runTime.setText(String.format("%02d:%02d:%02d:%03d",
                    elapsed / 3600000,
                    (elapsed % 3600000) / 60000,
                    (elapsed % 60000) / 1000,
                    elapsed % 1000));
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
        sunlightColor = new ColorDouble(env.getSunlightColor());
        ambientColor = new ColorDouble(env.getAmbientColor());
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public void setSamplesPerPixel(int samplesPerPixel) {
        this.samplesPerPixel = samplesPerPixel;
    }
}
