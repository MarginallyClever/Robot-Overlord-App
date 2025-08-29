package com.marginallyclever.ro3.apps.pathtracer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.raypicking.RayPickSystem;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link PathTracer} performs multithreaded path tracing (aka ray tracing with bounces)
 */
public class PathTracer {
    public static final int CHANNEL_VIEWPORT_U = 0;
    public static final int CHANNEL_VIEWPORT_V = 1;
    public static final int CHANNEL_HEMISPHERE_U = 2;
    public static final int CHANNEL_HEMISPHERE_V = 3;
    public static final int CHANNEL_RUSSIAN_ROULETTE = 4;
    public static final int CHANNEL_LIGHT_SAMPLING = 5;
    public static final int CHANNEL_BSDF_SAMPLING = 6;

    private static final double EPSILON = 1e-6;
    private int minBounces = 5; // minimum number of bounces before Russian roulette can kick in
    private double lightSamplingProbability = 1.0;
    private double maxContribution = 100000.0;

    private final List<RayXY> rays = new ArrayList<>();
    private final RayPickSystem rayPickSystem = new RayPickSystem();
    private PathTracingWorker pathTracingWorker;
    private Camera activeCamera;
    private int canvasWidth = 640;
    private int canvasHeight = 480;
    // stores the final color image of the scene
    private BufferedImage image;
    // depth map
    private BufferedImage depthMap;
    private double deepestHit = 0.0; // the deepest hit in the scene, used for depth map scaling
    // normal map
    private BufferedImage normalMap;

    // number of times to retest each pixel.  the results are then averaged.
    private int samplesPerPixel = 70;
    // number of bounces per ray
    private int maxDepth = 15;

    private ColorDouble ambientColor = new ColorDouble(0.25,0.25,0.25,1);
    private ColorDouble sunlightColor = new ColorDouble(1,1,1,1);
    private double sunlightStrength = 1.0;
    private final Vector3d sunlightSource = new Vector3d(150,150,150);
    private final Material defaultMaterial = new Material();
    private long startTime;

    private final EventListenerList listeners = new EventListenerList();

    private Pose displayContainer;
    private MeshInstance displayPath;
    private Mesh displayMesh;

    public PathTracer() {
        super();
    }

    public void stop() {
        if(pathTracingWorker == null) return;
        pathTracingWorker.cancel(true);
        pathTracingWorker = null;
    }

    public void start() {
        if(pathTracingWorker !=null) return;

        System.out.println("Starting PathTracer "+canvasHeight+"x"+canvasWidth+" @ "+samplesPerPixel + "spp, max depth "+maxDepth);

        getSunlight();
        if(activeCamera==null) throw new RuntimeException("No active camera!");
        if(canvasHeight==0 || canvasWidth==0) throw new RuntimeException("Canvas size is zero!");

        rays.clear();
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                rays.add(new RayXY(x,y));
            }
        }

        firePending();
        if(displayContainer!=null) {
            if(Registry.getScene().getChildren().contains(displayContainer)) {
                Registry.getScene().removeChild(displayContainer);
            }
            displayContainer = null;
        }
        rayPickSystem.reset(true);
        deepestHit = 0;
        fireStarted();
        pathTracingWorker = new PathTracingWorker(rays, image);
        pathTracingWorker.execute();
    }

    /**
     * <p>Trace the ray and return the color of the pixel at the end of the ray.</p>
     * @param ray the ray to trace
     * @param pixel the pixel to store the result in
     */
    private void trace(Ray ray,RayXY pixel) {
        ColorDouble radiance = new ColorDouble(0, 0, 0);
        ColorDouble throughput = new ColorDouble(1, 1, 1, 1);
        RayHit prevHit=null;
        Ray prevRay=null;
        ScatterRecord prevScatter=null;

        for (int depth = 0; depth < maxDepth; ++depth) {
            if(depth >= minBounces && russianRouletteTermination(throughput,pixel)) break;

            // get the first hit along the ray
            RayHit rayHit = rayPickSystem.getFirstHit(ray,true);
            pixel.addRayRecord(new Ray(ray),rayHit);
            if (rayHit == null) {
                // hit nothing
                var sky = new ColorDouble(getSkyColor(ray));
                sky.multiply(throughput);
                radiance.add(sky);
                break;
            }

            // first ray hit something.  record the depth and normal.
            if(depth==0) {
                // if this is the first hit, record the depth and the deepest hit.
                pixel.depth = rayHit.distance();
                deepestHit = Math.max(deepestHit, pixel.depth);
                pixel.normal = rayHit.normal();
            }

            // does this material emit light?
            Material mat = RayPickSystem.getMaterial(rayHit.target());
            if(mat == null) mat = defaultMaterial;
            if(mat.isEmissive()) {
                handleEmissiveHit(ray,rayHit,prevRay,prevHit,throughput,radiance,mat,prevScatter);
                break;
            }

            ScatterRecord scatterRecord = mat.scatter(ray, rayHit,pixel);
            if(!scatterRecord.isSpecular) {
                // probabilistic light sampling AKA Next Event Estimation (NEE)
                double p = pixel.halton.nextDouble(CHANNEL_LIGHT_SAMPLING);
                if (p < lightSamplingProbability) {
                    probabilisticLightSampling(p, ray, rayHit, mat, throughput, radiance,pixel);
                }
            }

            prevHit = rayHit;
            prevRay = ray;
            prevScatter = scatterRecord;

            // pick the next ray direction
            ray = new Ray(rayHit.point(), scatterRecord.ray.getDirection());
            var oldThroughput = new ColorDouble(throughput);
            throughput.multiply(scatterRecord.attenuation);
            //throughput.clamp(0,MAX_THROUGHPUT);
            if(throughput.r<EPSILON && throughput.g<EPSILON && throughput.b<EPSILON) {
                // no more light to bounce
                break;
            }
        }

        pixel.add(radiance);
    }

    /**
     * Probabilistic light sampling with Multiple Importance Sampling (MIS).
     * @param p the probability of selecting this strategy
     * @param ray the current ray
     * @param rayHit the current ray hit
     * @param mat the material at the ray hit
     * @param throughput the current throughput of the ray
     * @param radiance the accumulated radiance
     */
    private void probabilisticLightSampling(double p, Ray ray, RayHit rayHit, Material mat, ColorDouble throughput, ColorDouble radiance, RayXY pixel) {
        // get an emissive surface
        RayHit emissiveHit = rayPickSystem.getRandomEmissiveSurface();
        if(emissiveHit == null) return;

        Vector3d wi = new Vector3d();
        wi.sub(emissiveHit.point(), rayHit.point());
        double distanceSquared = wi.lengthSquared();
        wi.normalize();

        // shoot a shadow ray to see if the light is visible
        Point3d origin = new Point3d();
        origin.scaleAdd(EPSILON,rayHit.normal(),rayHit.point());
        Ray lightRay = new Ray(origin, wi);
        RayHit lightHit = rayPickSystem.getFirstHit(lightRay,true);
        pixel.addRayRecord(new Ray(lightRay),lightHit);
        if(lightHit == null) return; // occluded

        // hit a thing
        var mat2 = RayPickSystem.getMaterial(lightHit.target());
        if(mat2 == null) mat2 = defaultMaterial;
        if(!mat2.isEmissive()) return;

        Vector3d wo = ray.getWo();
        ColorDouble brdf = mat.lightSamplingBRDF(rayHit,wi,wo);

        double area;
        if(lightHit.target().getMesh() instanceof Sphere sphere) {
            area = 4.0 * Math.PI * Math.pow(sphere.radius,2);
        } else {
            area = lightHit.triangle().getArea();
        }
        double cosThetaLight = Math.max(0, -lightHit.normal().dot(wi));
        double pdfLight = Math.max(EPSILON,
                pdfLightSolidAngle(area,distanceSquared,cosThetaLight));
        double pdfBSDF = mat.getProbableDistributionFunction(rayHit,wi,wo);

        // Include strategy selection probabilities in MIS
        double pl = pdfLight * p;
        double pb = pdfBSDF * (1.0 - p);
        double wBSDF = misWeight(pl, pb);

        // the light is visible
        ColorDouble lightEmission = mat2.getEmittedLight();

        // the light contributes
        ColorDouble contribution = new ColorDouble(lightEmission);
        contribution.multiply(brdf);
        contribution.scale( 1.0 / (pdfLight*p));
        contribution.multiply(throughput);
        contribution.scale(wBSDF);
        contribution.clamp(0, maxContribution);
        radiance.add(contribution);
    }

    /**
     * @param throughput the current throughput of the ray
     * @return true if the path should terminate
     */
    private boolean russianRouletteTermination(ColorDouble throughput,RayXY pixel) {
        double maxThroughput = Math.max(throughput.r, Math.max(throughput.g, throughput.b));
        if(maxThroughput == 1.0) return false;

        double p = Math.max(0.05,maxThroughput);  // limit the probability of stopping
        if (pixel.halton.nextDouble(CHANNEL_RUSSIAN_ROULETTE) > p) return true;

        throughput.scale(1.0 / p);  // adjust throughput to account for the probability of stopping
        return false;
    }

    /**
     * Handle direct hits to emissive surfaces.
     * @param ray the current ray
     * @param rayHit the current ray hit
     * @param prevRay the previous ray, or null if this is the first ray
     * @param prevHit the previous ray hit, or null if this is the first hit
     * @param throughput the current throughput of the ray
     * @param radiance the accumulated radiance
     * @param mat the emissive material
     * @param prevScatter the scatter record of the previous hit, or null if this is the first hit
     */
    private void handleEmissiveHit(Ray ray, RayHit rayHit, Ray prevRay, RayHit prevHit, ColorDouble throughput, ColorDouble radiance,Material mat,ScatterRecord prevScatter) {
        if(prevHit == null) {
            // First bounce: no MIS, just add light directly
            ColorDouble emittedLight = mat.getEmittedLight();
            emittedLight.multiply(throughput);
            radiance.add(emittedLight);
            return;
        }

        var prevMat = RayPickSystem.getMaterial(prevHit.target());
        if(prevMat == null) prevMat = defaultMaterial;

        Vector3d wi = new Vector3d(ray.getDirection());
        Vector3d wo = prevRay.getWo();

        Point3d prevPoint = ray.getOrigin();
        Point3d lightPoint = rayHit.point();

        double distanceSquared = lightPoint.distanceSquared(prevPoint);
        Vector3d lightDir = new Vector3d();
        lightDir.sub(lightPoint, prevPoint);
        lightDir.normalize();
        double cosThetaLight = Math.max(0.0, -rayHit.normal().dot(lightDir));

        ColorDouble emittedLight = mat.getEmittedLight();

        if(prevScatter.type == ScatterRecord.ScatterType.SPECULAR) {
            // if the previous bounce was specular, we can only have come from the BSDF sampling
            // so we don't need to do MIS.
        } else {
            double area;
            if(rayHit.target().getMesh() instanceof Sphere sphere) {
                area = 0.00001;//4.0 * Math.PI * Math.pow(sphere.radius,2);
            } else {
                area = rayHit.triangle().getArea();
            }

            double pdfBSDF = prevMat.getProbableDistributionFunction(prevHit, wi, wo);
            double pdfLight = Math.max(EPSILON,
                    pdfLightSolidAngle(area, distanceSquared, cosThetaLight));
            double wBSDF = misWeight(pdfBSDF, pdfLight);
            emittedLight.scale(wBSDF);
        }

        emittedLight.multiply(throughput);
        radiance.add(emittedLight);
    }

    private double pdfLightSolidAngle(double lightArea, double distanceSquared,double cosThetaLight) {
        if (cosThetaLight <= 0.0) {
            return 0.0; // back-facing, zero probability
        }
        double pA = 1.0 / lightArea; // uniform on area
        return (pA * distanceSquared) / cosThetaLight;
    }

    /**
     * Calculate the Multiple Importance Sampling (MIS) weight using the power heuristic.
     *
     * @param pdfA probability density function value for strategy A
     * @param pdfB probability density function value for strategy B
     * @return the MIS weight for balancing between the two sampling strategies
     */
    private static double misWeight(double pdfA, double pdfB) {
        double a2 = pdfA * pdfA;
        double b2 = pdfB * pdfB;
        return a2 / (a2 + b2);
    }

    /**
     * sky or sun color, depending on angle of incidence
     * @param ray the ray to check
     * @return the color of the sky
     */
    private ColorDouble getSkyColor(Ray ray) {
        Vector3d d = ray.getDirection();
        d.normalize();
        var dot = Math.max(0,sunlightSource.dot(d));
        var sd = Math.pow(dot,5);
        var a = 1.0-sd;
        return new ColorDouble(
                a * ambientColor.r + sd * sunlightColor.r * sunlightStrength,
                a * ambientColor.g + sd * sunlightColor.g * sunlightStrength,
                a * ambientColor.b + sd * sunlightColor.b * sunlightStrength);
    }

    /**
     * Get the timestamp when the path tracing started.
     *
     * @return the start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Set the size of the canvas.  Destroys the previous image and creates a new one.
     * @param width the width
     * @param height the height
     */
    public void setSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        image = new BufferedImage(canvasWidth,canvasHeight,BufferedImage.TYPE_INT_RGB);
        depthMap = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        normalMap = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Get the rendered image buffer containing the path traced scene.
     *
     * @return the rendered image as a BufferedImage
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the depth map buffer containing the distance information for each pixel.
     *
     * @return the depth map as a BufferedImage
     */
    public BufferedImage getDepthMap() {
        return depthMap;
    }

    public BufferedImage getNormalMap() {
        return normalMap;
    }

    /**
     * Set the camera to be used for rendering the scene.
     *
     * @param camera the camera to use for path tracing
     */
    public void setActiveCamera(Camera camera) {
        this.activeCamera = camera;
    }

    /**
     * Check if the path tracer is currently processing.
     *
     * @return true if the worker is active and not done or cancelled, false otherwise
     */
    public boolean isRunning() {
        return pathTracingWorker != null && !pathTracingWorker.isDone() && !pathTracingWorker.isCancelled();
    }

    public void fireOneRay(int x, int y) {
        if(!isRunning()) return;

        Ray ray = activeCamera.getRayThroughPoint(
                (2.0 * x / canvasWidth) - 1.0,
                1.0 - (2.0 * y / canvasHeight),
                canvasWidth, canvasHeight);
        RayXY pixel = new RayXY(x,y);
        pixel.debug = true;
        long seed = pixel.samples + pixel.x * 73856093L + pixel.y * 19349663L;
        pixel.halton.resetMemory(seed);
        trace(ray,pixel);

        var list = new ArrayList<RayXY>();
        list.add(pixel);
        showRays(list);
    }

    private void showRays(List<RayXY> list) {
        if(displayContainer==null) {
            displayContainer = new Pose();
            displayContainer.setName("PathTracer Container");
            Registry.getScene().addChild(displayContainer);

            displayPath = new MeshInstance();
            displayPath.setName("PathTracer Path");
            displayContainer.addChild(displayPath);

            var mat = new Material();
            mat.setLit(false);
            displayContainer.addChild(mat);
            //mat.setDiffuseColor(new Color(1,1,1,0.1f));

            displayMesh = new Mesh();
            displayPath.setMesh(displayMesh);
        }

        displayMesh.setRenderStyle(GL3.GL_LINES);
        displayMesh.clear();
        for( var pixel : list) {
            for (var e : pixel.rays.entrySet()) {
                Ray r = e.getKey();
                RayHit h = e.getValue();
                Point3d p0 = r.getOrigin();
                Vector3d d = r.getDirection();
                d.normalize();
                Point3d p1;
                Color c;
                if (h == null) {
                    // no hit, draw a short ray
                    p1 = new Point3d();
                    p1.scaleAdd(1.0, d, p0);
                    c = Color.RED;
                    displayLine(p0,p1,c);
                } else {
                    p1 = h.point();
                    Material mat = RayPickSystem.getMaterial(h.target());
                    c = mat.isEmissive() ? Color.GREEN : Color.YELLOW;
                    displayLine(p0,p1,c);

                    c = Color.BLUE;
                    Point3d p2 = new Point3d(h.normal());
                    p2.add(h.point());
                    displayLine(p1,p2,c);
                }
            }
        }
    }

    private void displayLine(Point3d p0, Point3d p1, Color c) {
        displayMesh.addVertex((float) p0.x, (float) p0.y, (float) p0.z);
        displayMesh.addVertex((float) p1.x, (float) p1.y, (float) p1.z);
        displayMesh.addColor(c.getRed(), c.getGreen(), c.getBlue(), 1.0f);
        displayMesh.addColor(c.getRed(), c.getGreen(), c.getBlue(), 1.0f);
    }

    private class PathTracingWorker extends SwingWorker<Void,Integer> {
        private final List<RayXY> pixels;
        private final BufferedImage image;

        public PathTracingWorker(List<RayXY> pixels, BufferedImage image) {
            this.pixels = pixels;
            this.image = image;
        }

        @Override
        protected Void doInBackground() {
            int total = pixels.size() * samplesPerPixel;
            int sqrtSPP = (int)Math.sqrt(samplesPerPixel);
            AtomicInteger completed = new AtomicInteger(0);

            startTime = System.currentTimeMillis();

            // in parallel, trace each ray and store the result in a buffer
            while(!isCancelled()) {
                rays.stream().parallel().forEach(pixel -> {
                    if(isCancelled()) return;

                    long seed = pixel.samples + pixel.x * 73856093L + pixel.y * 19349663L;
                    pixel.halton.resetMemory(seed);

                    // get the jiggled ray
                    var ray = getStratifiedSample(pixel);
                    // sum the total color of all samples
                    trace(ray, pixel);
                    // store the result in the buffer
                    drawPixel(pixel,pixel.colorAverage.getColor());
                    // Update progress
                    int done = completed.incrementAndGet();
                    // Only publish occasionally to avoid flooding the EDT
                    if (done % 100 == 0 || done == total) {
                        publish((int) ((done * 100.0f) / total));
                        if(done == total) {
                            cancel(true);
                        }
                    }
                });
            }
            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            // The last value in chunks is the most recent progress value
            int latestProgress = chunks.getLast();

            fireProgressUpdate(latestProgress);
        }

        @Override
        protected void done() {
            // Rendering finished
            //showRays(rays);
            pathTracingWorker = null;
            fireFinished();
        }

        private void drawPixel(RayXY pixel, Color c) {
            // Convert Ray’s coordinate to pixel indices and set the pixel’s color
            image.setRGB(pixel.x, pixel.y, c.getRGB());

            if(pixel.samples>2) return;

            // Update depth map.  Convert pixel.depth to a rainbow heatmap color
            if(pixel.depth == Double.POSITIVE_INFINITY) {
                // no hit, set to black
                depthMap.setRGB(pixel.x, pixel.y, Color.BLACK.getRGB());
            } else {
                double depthValue = Math.max(0,Math.min(1,pixel.depth / deepestHit));
                // Convert depth value to a color (e.g., blue for near, red for far)
                depthMap.setRGB(pixel.x, pixel.y, unitToRainbow(depthValue).getRGB());
            }

            if(pixel.samples>1) return;

            // update normal map
            if(pixel.normal != null) {
                Color n = new Color((float) (0.5 + 0.5 * pixel.normal.x), (float) (0.5 + 0.5 * pixel.normal.y), (float) (0.5 + 0.5 * pixel.normal.z));
                normalMap.setRGB(pixel.x, pixel.y, n.getRGB());
            }
        }
    }

    private Color unitToRainbow(double value) {
        // Clamp value to [0,1]
        value = Math.max(0.0, Math.min(1.0, value));
        // Map value to hue (0 = red, 1 = violet)
        float hue = (float)(value); // 0.7~0.0 covers red->violet
        float saturation = 1.0f;
        float brightness = 1.0f;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    private void getSunlight() {
        Environment env = Registry.getScene().findFirstChild(Environment.class);
        if(null==env) {
            env = new Environment();
            Registry.getScene().addChild(env);
        }

        sunlightSource.set(env.getSunlightSource());
        sunlightSource.normalize();
        sunlightColor = new ColorDouble(env.getSunlightColor());
        ambientColor = new ColorDouble(env.getAmbientColor());
        sunlightStrength = env.getSunlightStrength();
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

    private void fireProgressUpdate(int latestProgress) {
        for( var listener : listeners.getListeners(ProgressListener.class)) {
            listener.onProgressUpdate(latestProgress);
        }
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(ProgressListener.class,listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(PropertyChangeListener.class,listener);
    }

    private void fireStateChange(Object oldState, Object newState) {
        PropertyChangeEvent pce = null;
        for( var listener : listeners.getListeners(PropertyChangeListener.class)) {
            // lazy allocation
            if(pce==null) pce = new PropertyChangeEvent(this,"state",oldState,newState);
            // notify the listener that the path tracing is finished
            listener.propertyChange(pce);
        }
    }

    private void firePending() {
        fireStateChange(null, SwingWorker.StateValue.PENDING);
    }

    private void fireStarted() {
        fireStateChange(null, SwingWorker.StateValue.STARTED);
    }

    private void fireFinished() {
        fireStateChange(null,SwingWorker.StateValue.DONE);
    }

    /**
     * <p>Get the ray leaving the camera at a given pixel, stratified.  Instead of jiggling the ray
     * across the entire pixel, the pixel area is divided into an n*n grid, where n=sqrt(total samples).
     * The ray is then jiggled inside the area of each grid cell.</p>
     *
     * @param pixel the {@link RayXY} with the 2d coordinate on the viewport.
     * @return the jiggled
     */
    private Ray getStratifiedSample(RayXY pixel) {
        int s = pixel.getSamples();
        // FIX: wrap both indices within the grid

        double jx = pixel.halton.nextDouble(PathTracer.CHANNEL_VIEWPORT_U);
        double jy = pixel.halton.nextDouble(PathTracer.CHANNEL_VIEWPORT_V);
        // jiggle the ray a little bit to get a better anti-aliasing effect
        var nx =       (2.0*(pixel.x+jx)/canvasWidth ) - 1.0;
        var ny = 1.0 - (2.0*(pixel.y+jy)/canvasHeight);
        return activeCamera.getRayThroughPoint(nx,ny, canvasWidth, canvasHeight);
    }


    public double getMaxContribution() {
        return maxContribution;
    }

    public void setMaxContribution(double limit) {
        this.maxContribution = Math.max(0,limit);
    }

    public double getLightSamplingProbability() {
        return lightSamplingProbability;
    }

    public void setLightSamplingProbability(double probability) {
        this.lightSamplingProbability = Math.clamp(probability,0,1);
    }

    public int getMinBounces() {
        return minBounces;
    }

    public void setMinBounces(int count) {
        this.minBounces = Math.max(1,count);
    }
}
