package com.marginallyclever.ro3.apps.pathtracer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.GenerativeMesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.ro3.raypicking.Hit;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

/**
 * {@link PathTracer} performs multithreaded path tracing (aka ray tracing with bounces)
 */
public class PathTracer {
    public static final int CHANNEL_VIEWPORT_U = 0;
    public static final int CHANNEL_VIEWPORT_V = 0;
    public static final int CHANNEL_HEMISPHERE_U = 0;
    public static final int CHANNEL_HEMISPHERE_V = 0;
    public static final int CHANNEL_RUSSIAN_ROULETTE = 4;
    public static final int CHANNEL_LIGHT_SAMPLING = 5;
    public static final int CHANNEL_BSDF_SAMPLING = 6;

    private static final double EPSILON = 1e-6;

    private int samplesPerPixel = 70;  // number of times to retest each pixel.  the results are then averaged.
    private int maxDepth = 15;  // number of bounces per ray
    private int minBounces = 5;  // minimum number of bounces before Russian roulette can kick in
    private double lightSamplingProbability = 0.0;  // 0...1
    private double maxContribution = 20.0;  // clamp the maximum contribution of a single bounce to avoid fireflies
    private double exposure = 1.0;  // exposure multiplier
    private boolean activateToneMap=true;  // apply simple tone mapping to the final color

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


    private Environment environment;
    private final Material defaultMaterial = new Material();
    private long startTime;

    private final EventListenerList listeners = new EventListenerList();

    private Pose displayContainer;
    private Mesh displayMesh;

    public PathTracer() {
        super();
        loadPreferences();
    }

    public void stop() {
        if(pathTracingWorker == null) return;
        pathTracingWorker.cancel(true);
        pathTracingWorker = null;
    }

    public void start() {
        if(pathTracingWorker != null) return;

        System.out.println("Starting PathTracer "+canvasHeight+"x"+canvasWidth+" @ "+samplesPerPixel + "spp, max depth "+maxDepth);

        environment = Registry.getScene().findFirstChild(Environment.class);
        if(activeCamera==null) throw new RuntimeException("No active camera!");
        if(canvasHeight==0 || canvasWidth==0) throw new RuntimeException("Canvas size is zero!");

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

    private void allocateRays() {
        rays.clear();
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                rays.add(new RayXY(x,y));
            }
        }
    }

    /**
     * March the ray through the scene.  This is the core of the path tracer.
     * @param ray the ray to trace
     * @param pixel the pixel in which to store the result.
     */
    private void trace(Ray ray,RayXY pixel) {
        ColorDouble radiance = new ColorDouble(0, 0, 0);
        ColorDouble throughput = new ColorDouble(1, 1, 1, 1);
        Hit prevHit=null;
        Ray prevRay=null;
        ScatterRecord prevScatter=null;

        int depth;
        for (depth = 0; depth < maxDepth; ++depth) {
            if(depth >= minBounces && russianRouletteTermination(throughput,pixel)) break;

            // get the first hit along the ray
            Hit hit = rayPickSystem.getFirstHit(ray);
            pixel.addRayHistory(new Ray(ray), hit);
            if (hit == null && environment != null ) {
                // hit nothing
                ColorDouble sky = environment.getEnivornmentColor(ray);
                sky.multiply(throughput);
                radiance.add(sky);
                break;
            }

            // first hit on something.  record the depth and normal.
            if(pixel.samples==0 && depth==0) {
                // if this is the first hit, record the depth and the deepest hit.
                pixel.depth = hit.distance();
                deepestHit = Math.max(deepestHit, pixel.depth);
                pixel.normal = hit.normal();
            }

            // does this material emit light?
            Material mat = RayPickSystem.getMaterial(hit.target());
            if(mat == null) mat = defaultMaterial;
            if(mat.isEmissive()) {
                handleEmissiveHit(ray, hit,prevRay,prevHit,throughput,radiance,mat,prevScatter);
                break;
            }

            ScatterRecord scatterRecord = mat.scatter(ray, hit,pixel);
            if(!scatterRecord.isSpecular) {
                // probabilistic light sampling AKA Next Event Estimation (NEE)
                double p = pixel.halton.nextDouble(CHANNEL_LIGHT_SAMPLING);
                if (p < lightSamplingProbability) {
                    probabilisticLightSampling(p, ray, hit, mat, throughput, radiance,pixel);
                }
            }

            prevHit = hit;
            prevRay = ray;
            prevScatter = scatterRecord;

            // pick the next ray direction
            ray = new Ray(hit.point(), scatterRecord.ray.getDirection());
            throughput.multiply(scatterRecord.attenuation);
            if(throughput.r<EPSILON && throughput.g<EPSILON && throughput.b<EPSILON) {
                // no more light to bounce
                break;
            }
        }

        pixel.add(radiance,exposure,activateToneMap);
    }

    /**
     * Probabilistic light sampling with Multiple Importance Sampling (MIS).
     * @param p the probability of selecting this strategy
     * @param ray the current ray
     * @param hit the current ray hit
     * @param mat the material at the ray hit
     * @param throughput the current throughput of the ray
     * @param radiance the accumulated radiance
     */
    private void probabilisticLightSampling(double p, Ray ray, Hit hit, Material mat, ColorDouble throughput, ColorDouble radiance, RayXY pixel) {
        // get an emissive surface
        Hit emissiveHit = rayPickSystem.getRandomEmissiveSurface();
        if(emissiveHit == null) return;

        Vector3d wi = new Vector3d();
        wi.sub(emissiveHit.point(), hit.point());
        double distanceSquared = wi.lengthSquared();
        wi.normalize();

        // shoot a shadow ray to see if the light is visible
        Point3d origin = new Point3d();
        origin.scaleAdd(EPSILON, hit.normal(), hit.point());
        Ray lightRay = new Ray(origin, wi);
        Hit lightHit = rayPickSystem.getFirstHit(lightRay);
        pixel.addRayHistory(new Ray(lightRay),lightHit);
        if(lightHit == null) return; // occluded

        // hit a thing
        var mat2 = RayPickSystem.getMaterial(lightHit.target());
        if(mat2 == null) mat2 = defaultMaterial;
        if(!mat2.isEmissive()) return;

        Vector3d wo = ray.getWo();
        ColorDouble brdf = mat.lightSamplingBRDF(hit,wi,wo);

        double area;
        if(lightHit.target().getMesh() instanceof Sphere sphere) {
            area = 4.0 * Math.PI * Math.pow(sphere.radius,2);
        } else {
            area = lightHit.triangle().getArea();
        }
        double cosThetaLight = Math.max(0, -lightHit.normal().dot(wi));
        double pdfLight = Math.max(EPSILON, pdfLightSolidAngle(area,distanceSquared,cosThetaLight));
        double pdfBSDF = mat.getPDF(hit,wi,wo);

        // Include strategy selection probabilities in MIS
        double pl = pdfLight * p;
        double pb = pdfBSDF * (1.0 - p);
        double wBSDF = misWeight(pb,pl);

        // contribution
        ColorDouble contribution = new ColorDouble(mat2.getEmittedLight());
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
     * @param hit the current ray hit
     * @param prevRay the previous ray, or null if this is the first ray
     * @param prevHit the previous ray hit, or null if this is the first hit
     * @param throughput the current throughput of the ray
     * @param radiance the accumulated radiance
     * @param mat the emissive material
     * @param prevScatter the scatter record of the previous hit, or null if this is the first hit
     */
    private void handleEmissiveHit(Ray ray, Hit hit, Ray prevRay, Hit prevHit, ColorDouble throughput, ColorDouble radiance, Material mat, ScatterRecord prevScatter) {
        if(prevHit == null) {
            // First bounce: no MIS, just add light directly
            ColorDouble emittedLight = mat.getEmittedLight();
            emittedLight.multiply(throughput);
            emittedLight.clamp(0, maxContribution);
            radiance.add(emittedLight);
            return;
        }

        var prevMat = RayPickSystem.getMaterial(prevHit.target());
        if(prevMat == null) prevMat = defaultMaterial;

        Vector3d wi = new Vector3d(ray.getDirection());
        Vector3d wo = prevRay.getWo();

        Point3d prevPoint = ray.getOrigin();
        Point3d lightPoint = hit.point();

        double distanceSquared = lightPoint.distanceSquared(prevPoint);
        Vector3d lightDir = new Vector3d();
        lightDir.sub(lightPoint, prevPoint);
        lightDir.normalize();
        double cosThetaLight = Math.max(0.0, -hit.normal().dot(lightDir));

        ColorDouble emittedLight = mat.getEmittedLight();

        // if the previous bounce was specular we can only have come from the BSDF sampling
        // so we don't need to do MIS.
        if(prevScatter.type != ScatterRecord.ScatterType.SPECULAR) {
            double area;
            if(hit.target().getMesh() instanceof Sphere sphere) {
                area = 4.0 * Math.PI * Math.pow(sphere.radius,2);
            } else {
                area = hit.triangle().getArea();
            }

            double pdfBSDF = prevMat.getPDF(prevHit, wi, wo);
            double pdfLight = Math.max(EPSILON,
                    pdfLightSolidAngle(area, distanceSquared, cosThetaLight));
            double wBSDF = misWeight(pdfLight,pdfBSDF);
            emittedLight.scale(wBSDF);
        }

        emittedLight.multiply(throughput);
        emittedLight.clamp(0, maxContribution);
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
        allocateRays();
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

    /**
     * Fire one ray through the pixel at (x,y) and display the path on screen.
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     */
    public void fireAndDisplayOneRay(int x, int y,int traceDepth) {
        if(!isRunning()) return;
        var list = new ArrayList<RayXY>();
        list.add(fireOneRayWithTracingOn(x,y,traceDepth));
        showRays(list,true);
    }

    /**
     * Fire one ray through the pixel at (x,y)
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     */
    private RayXY fireOneRayWithTracingOn(int x, int y,int traceDepth) {
        var ray = getRayForXY(x,y);

        RayXY pixel = new RayXY(x,y,traceDepth);
        long seed = getSeed(pixel);
        pixel.halton.resetMemory(seed);

        trace(ray,pixel);

        return pixel;
    }

    private Ray getRayForXY(int x,int y) {
        return activeCamera.getRayThroughPoint(
                (2.0 * x / canvasWidth) - 1.0,
                1.0 - (2.0 * y / canvasHeight),
                canvasWidth, canvasHeight);
    }

    private void showRays(List<RayXY> list,boolean showWholeRay) {
        if(displayContainer==null) {
            displayContainer = new Pose();
            displayContainer.setName("PathTracer Container");
            Registry.getScene().addChild(displayContainer);

            MeshInstance displayPath = new MeshInstance();
            displayPath.setName("PathTracer Path");
            displayContainer.addChild(displayPath);

            var mat = new Material();
            mat.setLit(false);
            displayContainer.addChild(mat);
            //mat.setDiffuseColor(new Color(1,1,1,0.1f));

            displayMesh = new GenerativeMesh();
            displayPath.setMesh(displayMesh);
        }

        displayMesh.setRenderStyle(GL3.GL_LINES);
        displayMesh.clear();
        Point3d p1 = new Point3d();

        for( var pixel : list) {
            for (var rayHit : pixel.rayHistory.entrySet()) {
                Ray ray = rayHit.getKey();
                Hit hit = rayHit.getValue();
                Point3d p0 = ray.getOrigin();
                Vector3d d = ray.getDirection();
                d.normalize();


                if (hit == null) {
                    p1.scaleAdd(1.0, d, p0);
                    displayLine(p0,p1,Color.RED,Color.ORANGE);
                    continue;
                }
                /*
                p1 = h.point();
                Material mat = RayPickSystem.getMaterial(h.target());
                displayLine(p0,p1,mat.isEmissive() ? Color.GREEN : Color.YELLOW);

                Point3d p2 = new Point3d(h.normal());
                p2.add(h.point());
                displayLine(p1,p2,Color.BLUE);
                */
                if(showWholeRay)
                {
                    displayLine(hit.point(), p0, new Color(0.2f, 0.2f, 0, 0.1f),new Color(0.2f, 0.2f, 0, 0.1f));
                }

                p1 = new Point3d();
                p1.scaleAdd(-1.0,d,hit.point());
                displayLine(hit.point(),p1,Color.GREEN,Color.CYAN);
/*
                p1.scaleAdd(0.5,hit.normal(),hit.point());
                displayLine(hit.point(),p1,Color.YELLOW);
//*/
                var emissiveHitPoint = hitARandomEmissiveSurface(hit,pixel);
                if(emissiveHitPoint!=null) {
                    var v1 = new Vector3d();
                    v1.sub(emissiveHitPoint.point(), hit.point());
                    v1.normalize();
                    p1.scaleAdd(1, v1, hit.point());
                    displayLine(hit.point(), p1, Color.MAGENTA, Color.BLUE);
                }//*/
            }
        }
    }

    /**
     * pick a random emissive surface and pick a ray towards it.
     * @param hit the current hit from which to shoot the ray
     * @param pixel the pixel to store the ray history in
     * @return the hit on the emissive surface, or null if none was hit
     */
    private Hit hitARandomEmissiveSurface(Hit hit, RayXY pixel) {
        // pick a random emissive surface
        Hit randomEmissiveSurface = rayPickSystem.getRandomEmissiveSurface();
        if(randomEmissiveSurface == null) return null;  // no emissive surfaces

        // get the direction to the emissive surface
        Vector3d towardsEmissiveSurface = new Vector3d();
        towardsEmissiveSurface.sub(randomEmissiveSurface.point(), hit.point());
        towardsEmissiveSurface.normalize();

        // offset the origin a little to avoid self intersection
        Point3d origin = new Point3d();
        origin.scaleAdd(EPSILON, hit.normal(), hit.point());

        // shoot a shadow ray to see if the light is visible
        Ray lightRay = new Ray(origin, towardsEmissiveSurface);

        Hit lightHit = rayPickSystem.getFirstHit(lightRay);
        if(lightHit!=null && lightHit.target() != randomEmissiveSurface.target()) return null; // hit itself, ignore
        return lightHit;
    }

    private void displayLine(Point3d p0, Point3d p1, Color c0,Color c1) {
        displayMesh.addVertex(p0);
        displayMesh.addVertex(p1);
        displayMesh.addColor(c0.getRed(), c0.getGreen(), c0.getBlue(), c0.getAlpha());
        displayMesh.addColor(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());
    }

    public void visualize() {
        if(!isRunning()) return;
        System.out.println("Visualizing all rays...");
        var list = new ArrayList<RayXY>();

        int stepSize=20;

        for(int x=0;x<canvasWidth;x+=stepSize) {
            for(int y=0;y<canvasHeight;y+=stepSize) {
                list.add(fireOneRayWithTracingOn(x,y,1));
            }
        }
        showRays(list,false);
    }

    /**
     * {@link PathTracingWorker} is a SwingWorker that performs the path tracing in the background.  <a
     * href="https://docs.oracle.com/javase/tutorial/uiswing/concurrency/worker.html">SwingWorkers cannot be reused</a>
     * so it must be destroyed and recreated each time the path tracer is started.
     */
    private class PathTracingWorker extends SwingWorker<Void,Integer> {
        private final List<RayXY> pixels;
        private final BufferedImage image;

        public PathTracingWorker(List<RayXY> pixels, BufferedImage image) {
            this.pixels = pixels;
            this.image = image;
        }

        @Override
        protected Void doInBackground() {
            AtomicInteger completed = new AtomicInteger(0);

            startTime = System.currentTimeMillis();

            while(!isCancelled()) {
                pixels.stream().parallel().forEach(pixel -> {
                    if (isCancelled()) return;

                    pixel.halton.resetMemory(getSeed(pixel));

                    // get the jiggled ray
                    var ray = getStratifiedSample(pixel);
                    // sum the total color of all samples
                    trace(ray, pixel);
                    // store the result in the buffer
                    drawPixel(pixel,pixel.radianceAverage.getColor());
                });
                int done = completed.incrementAndGet();
                publish(done);
                if (done >= samplesPerPixel) {
                    cancel(true);
                }
            }

            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            // The last value in chunks is the most recent progress value
            fireProgressUpdate(chunks.getLast());
        }

        @Override
        protected void done() {
            // Rendering finished
            pathTracingWorker = null;
            fireFinished();
        }

        private void drawPixel(RayXY pixel, Color c) {
            // Convert Ray’s coordinate to pixel indices and set the pixel’s color
            image.setRGB(pixel.x, pixel.y, c.getRGB());

            if(pixel.samples==1) {
                // Update depth map.  Convert pixel.depth to a rainbow heatmap color
                if (pixel.depth == Double.POSITIVE_INFINITY) {
                    // no hit, set to black
                    depthMap.setRGB(pixel.x, pixel.y, Color.BLACK.getRGB());
                } else {
                    double depthValue = Math.max(0, Math.min(1, pixel.depth / deepestHit));
                    // Convert depth value to a color (e.g., blue for near, red for far)
                    depthMap.setRGB(pixel.x, pixel.y, unitToRainbow(depthValue).getRGB());
                }
            }
            if(pixel.samples==0) {
                // update normal map
                if (pixel.normal != null) {
                    Color n = new Color((float) (0.5 + 0.5 * pixel.normal.x), (float) (0.5 + 0.5 * pixel.normal.y), (float) (0.5 + 0.5 * pixel.normal.z));
                    normalMap.setRGB(pixel.x, pixel.y, n.getRGB());
                }
            }
        }
    }

    private long getSeed(RayXY pixel) {
        long pixelIndex = pixel.x + (long) pixel.y * canvasWidth;
        long sampleIndex = pixel.samples;
        long combined = (pixelIndex << 32) | (sampleIndex & 0xffffffffL);
        return PathTracer.mix64(combined);
    }

    // Strong 64-bit mix (MurmurHash3 finalizer style)
    public static long mix64(long z) {
        z ^= (z >>> 33);
        z *= 0xff51afd7ed558ccdL;
        z ^= (z >>> 33);
        z *= 0xc4ceb9fe1a85ec53L;
        z ^= (z >>> 33);
        return z;
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

    private void fireStateChange(SwingWorker.StateValue newState) {
        PropertyChangeEvent pce = null;
        for( var listener : listeners.getListeners(PropertyChangeListener.class)) {
            // lazy allocation
            if(pce==null) pce = new PropertyChangeEvent(this,"state", null,newState);
            // notify the listener that the path tracing is finished
            listener.propertyChange(pce);
        }
    }

    private void firePending() {
        fireStateChange(SwingWorker.StateValue.PENDING);
    }

    private void fireStarted() {
        fireStateChange(SwingWorker.StateValue.STARTED);
    }

    private void fireFinished() {
        fireStateChange(SwingWorker.StateValue.DONE);
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

    public void setExposure(double arg0) {
        this.exposure = arg0;
    }

    public double getExposure() {
        return exposure;
    }

    public void setActivateToneMap(boolean activate) {
        this.activateToneMap = activate;
    }

    public boolean isActivateToneMap() {
        return activateToneMap;
    }

    // Save settings
    public void savePreferences() {
        Preferences preferences = Preferences.userNodeForPackage(PathTracer.class);

        preferences.putInt("samplesPerPixel", samplesPerPixel);
        preferences.putInt("maxDepth", maxDepth);
        preferences.putInt("minBounces", minBounces);
        preferences.putDouble("lightSamplingProbability", lightSamplingProbability);
        preferences.putDouble("maxContribution", maxContribution);
        preferences.putDouble("exposure", exposure);
        preferences.putBoolean("activateToneMap", activateToneMap);
    }

    // Load settings
    public void loadPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(PathTracer.class);

        samplesPerPixel = preferences.getInt("samplesPerPixel", samplesPerPixel);
        maxDepth = preferences.getInt("maxDepth", maxDepth);
        minBounces = preferences.getInt("minBounces", minBounces);
        lightSamplingProbability = preferences.getDouble("lightSamplingProbability", lightSamplingProbability);
        maxContribution = preferences.getDouble("maxContribution", maxContribution);
        exposure = preferences.getDouble("exposure", exposure);
        activateToneMap = preferences.getBoolean("activateToneMap", activateToneMap);
    }
}
