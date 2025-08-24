package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
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
    private static final double EPSILON = 1e-6;
    private static final int MIN_BOUNCES = 3; // minimum number of bounces before Russian roulette can kick in
    private static final double LIGHT_SAMPLING_PROBABILITY = 0.25;
    private static final double ENVIRONMENT_LIGHT_PROB = 0.25;
    private static final double MAX_THROUGHPUT = 10.0;
    private static final double MAX_CONTRIBUTION = 20.0;

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
    private static final SplittableRandom random = new SplittableRandom();

    private final EventListenerList listeners = new EventListenerList();

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

        rays.clear();//*
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                rays.add(new RayXY(x,y));
            }
        }//*/
        //rays.add(new RayXY(50,50));
        //rays.add(new RayXY(658-50,50));

        firePending();
        rayPickSystem.reset(true);
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

        for (int depth = 0; depth < maxDepth; ++depth) {
            if(depth >= MIN_BOUNCES && russianRouletteTermination(throughput)) break;

            // get the first hit along the ray
            RayHit rayHit = rayPickSystem.getFirstHit(ray,true);
            if (rayHit == null) {
                // hit nothing
                var sky = new ColorDouble(getSkyColor(ray));
                sky.multiply(throughput);
                radiance.add(sky);
                break;
            }

            // hit something
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
                handleEmissiveHit(ray,rayHit,prevRay,prevHit,throughput,radiance,mat);
                break;
            }

            // probabilistic light sampling
            double p = random.nextDouble();
            if(p < LIGHT_SAMPLING_PROBABILITY) {
                probabilisticLightSampling(p, ray, rayHit, mat, throughput, radiance);
            }

            // pick the next ray direction
            ScatterRecord scatterRecord = mat.scatter(ray, rayHit, random);
            Vector3d wi = scatterRecord.direction;
            Vector3d wo = ray.getWo();
            ColorDouble brdf = scatterRecord.attenuation;
            double pdf = mat.getProbableDistributionFunction(rayHit,wi,wo);
            if(pdf<= EPSILON) break;
            throughput.multiply(brdf);
            throughput.scale(1.0 / pdf); // cosine-weighted diffuse
            throughput.clamp(0,MAX_THROUGHPUT);
            if(throughput.r<EPSILON && throughput.g<EPSILON && throughput.b<EPSILON) {
                // no more light to bounce
                break;
            }

            prevHit = rayHit;
            prevRay = ray;

            ray = new Ray(rayHit.point(), wi);
        }

        pixel.add(radiance);
    }

    private void probabilisticLightSampling(double p, Ray ray, RayHit rayHit, Material mat, ColorDouble throughput, ColorDouble radiance) {
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
        if(lightHit == null) return; // occluded

        // hit a thing
        var target = lightHit.target();
        var mat2 = RayPickSystem.getMaterial(target);
        if(mat2 == null) mat2 = defaultMaterial;
        if(!mat2.isEmissive()) return;

        // BRDF
        Vector3d wo = ray.getWo();
        ColorDouble brdf = mat.BRDF(rayHit,wi,wo);
        double pdfBSDF = mat.getProbableDistributionFunction(rayHit,wi,wo);
        double cosThetaLight = Math.max(0, -lightHit.normal().dot(wi));
        double pdfLight = Math.max(EPSILON,pdfLightSolidAngle(lightHit.triangle().getArea(),distanceSquared,cosThetaLight));
        double wLight = misWeight(pdfLight,pdfBSDF);

        // the light is visible
        ColorDouble lightEmission = mat2.getEmittedLight();

        // the light contributes
        ColorDouble contribution = new ColorDouble(lightEmission);
        contribution.multiply(brdf);
        contribution.scale( 1.0 / (pdfLight*p));
        contribution.multiply(throughput);
        contribution.scale(wLight);
        contribution.clamp(0,MAX_CONTRIBUTION);
        radiance.add(contribution);
    }

    /**
     * @param throughput the current throughput of the ray
     * @return true if the path should terminate
     */
    private boolean russianRouletteTermination(ColorDouble throughput) {
        double maxThroughput = Math.max(throughput.r, Math.max(throughput.g, throughput.b));
        if(maxThroughput == 1.0) return false;

        double q = 1.0 - maxThroughput;
        if (q > 0.95) q = 0.95;  // limit the probability of stopping
        if (random.nextDouble() < q) return true;

        throughput.scale(1.0 / (1.0 - q));  // adjust throughput to account for the probability of stopping
        return false;
    }

    private void handleEmissiveHit(Ray ray, RayHit rayHit, Ray prevRay, RayHit prevHit, ColorDouble throughput, ColorDouble radiance,Material mat) {
        if(prevHit == null) {
            // First bounce: no MIS, just add light directly
            ColorDouble emittedLight = mat.getEmittedLight();
            emittedLight.multiply(throughput);
            radiance.add(emittedLight);
            return;
        }

        var mat2 = RayPickSystem.getMaterial(prevHit.target());
        if(mat2 == null) mat2 = defaultMaterial;

        Vector3d wi = new Vector3d(ray.getDirection());
        Vector3d wo = prevRay.getWo();

        Point3d prevPoint = ray.getOrigin();
        Point3d lightPoint = rayHit.point();

        double distanceSquared = lightPoint.distanceSquared(prevPoint);
        Vector3d lightDir = new Vector3d();
        lightDir.sub(lightPoint, prevPoint);
        lightDir.normalize();
        double cosThetaLight = Math.max(0.0, -rayHit.normal().dot(lightDir));
        double pdfLight = pdfLightSolidAngle(rayHit.triangle().getArea(), distanceSquared, cosThetaLight);
        double pdfBSDF = mat2.getProbableDistributionFunction(prevHit, wi, wo);
        double wBSDF = misWeight(pdfBSDF, pdfLight);

        ColorDouble emittedLight = mat.getEmittedLight();
        emittedLight.multiply(throughput);
        emittedLight.scale(wBSDF);
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
                    // get the jiggled ray
                    var ray = getStratifiedSample(sqrtSPP,random,pixel);
                    // sum the total color of all samples
                    trace(ray,pixel);
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
            pathTracingWorker =null;
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
     * @param sqrtSPP the square of the total samples per pixel.
     * @param random the pretty random number generator
     * @param pixel the {@link RayXY} with the 2d coordinate on the viewport.
     * @return the jiggled
     */
    private Ray getStratifiedSample(int sqrtSPP, SplittableRandom random, RayXY pixel) {
        int s = pixel.getSamples();
        int sx = s % sqrtSPP; // x coordinate in the sqrt grid
        int sy = s / sqrtSPP; // y coordinate in the sqrt grid

        double jx = (sx+random.nextDouble()) / sqrtSPP;
        double jy = (sy+random.nextDouble()) / sqrtSPP;
        // jiggle the ray a little bit to get a better anti-aliasing effect
        var nx =       (2.0*(pixel.x+jx)/canvasWidth ) - 1.0;
        var ny = 1.0 - (2.0*(pixel.y+jy)/canvasHeight);
        return activeCamera.getRayThroughPoint(nx,ny, canvasWidth, canvasHeight);
    }
}
