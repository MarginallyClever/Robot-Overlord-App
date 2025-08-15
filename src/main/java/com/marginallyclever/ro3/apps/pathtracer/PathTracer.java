package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.material.Material;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link PathTracer} performs multithreaded path tracing (aka ray tracing with bounces)
 */
public class PathTracer {
    private static final double EPSILON = 1e-6;
    private static final int MIN_BOUNCES = 3; // minimum number of bounces before Russian roulette can kick in
    private static final double LIGHT_SAMPLING_PROBABILITY = 0.25;
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
    private static final Random random = new Random();

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

        getSunlight();
        if(activeCamera==null) throw new RuntimeException("No active camera!");
        if(canvasHeight==0 || canvasWidth==0) throw new RuntimeException("Canvas size is zero!");

        rays.clear();
        for(int y=0;y<canvasHeight;y++) {
            for(int x=0;x<canvasWidth;x++) {
                rays.add(new RayXY(x,y));
            }
        }
        //rays.add(new RayXY(429,473));

        rayPickSystem.reset(true);
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

        for (int i = 0; i < maxDepth; ++i) {
            if(i >= MIN_BOUNCES && russianRouletteTermination(throughput)) break;

            // get the first hit along the ray
            RayHit rayHit = rayPickSystem.getFirstHit(ray,true);
            if (rayHit == null) {
                // hit nothing
                var sky = getSkyColor(ray);
                sky.multiply(throughput);
                radiance.add(sky);
                break;
            }

            // hit something
            if(i==0) {
                // if this is the first hit, record the depth
                pixel.depth = rayHit.distance();
                if(deepestHit < rayHit.distance()) {
                    deepestHit = rayHit.distance(); // update the deepest hit
                }
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
            Vector3d wi = PathTracerHelper.getRandomCosineWeightedHemisphere(random,rayHit.normal());
            Vector3d wo = new Vector3d(ray.getDirection());
            wo.negate();

            ColorDouble brdf = mat.BRDF(rayHit,wi,wo);
            double pdf = mat.getProbableDistributionFunction(rayHit,wi,wo);
            double cosTheta = Math.max(0,rayHit.normal().dot(wi));
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

            ScatterRecord scatterRecord = mat.scatter(ray, rayHit, random);
            ray = new Ray(rayHit.point(),scatterRecord.direction);
        }

        pixel.add(radiance);
    }

    private void probabilisticLightSampling(double p, Ray ray, RayHit rayHit, Material mat, ColorDouble throughput, ColorDouble radiance) {
        // get an emissive surface
        RayHit emissiveHit = rayPickSystem.getRandomEmissiveSurface();
        if(emissiveHit == null) return;

        Vector3d toLight = new Vector3d();
        toLight.sub(emissiveHit.point(), rayHit.point());
        double distanceSquared = toLight.lengthSquared();
        toLight.normalize();

        // shoot a shadow ray to see if the light is visible
        Point3d from = new Point3d();
        from.scaleAdd(EPSILON,rayHit.normal(),rayHit.point());
        Ray lightRay = new Ray(from, toLight);
        RayHit lightHit = rayPickSystem.getFirstHit(lightRay,true);
        if(lightHit == null) return;

        // hit a thing
        var target = lightHit.target();
        var mat2 = target.findFirstSibling(Material.class);
        if(mat2 == null || !mat2.isEmissive()) return;

        // the light is visible
        ColorDouble lightEmission = getEmittedLight(mat2);
        double cosTheta = Math.max(0, rayHit.normal().dot(toLight));
        double cosThetaLight = Math.max(0, -lightHit.normal().dot(toLight));

        Vector3d wi = new Vector3d(toLight);
        Vector3d wo = new Vector3d(ray.getDirection());
        wo.negate();

        ColorDouble brdf = mat.BRDF(rayHit,wi,wo);
        double pdfBSDF = mat.getProbableDistributionFunction(rayHit,wi,wo);
        double pdfLight = Math.max(EPSILON,pdfLightSolidAngle(lightHit.triangle().getArea(),distanceSquared,cosThetaLight));
        double wLight = misWeight(pdfLight,pdfBSDF);
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
        if(prevHit != null) {
            Vector3d wi = new Vector3d(ray.getDirection());
            Vector3d wo = new Vector3d(prevRay.getDirection());
            wo.negate();

            Point3d prevPoint = ray.getOrigin();
            Point3d lightPoint = rayHit.point();
            Vector3d nLight = rayHit.normal();

            double distanceSquared = lightPoint.distanceSquared(prevPoint);
            Vector3d lightDir = new Vector3d();
            lightDir.sub(lightPoint, prevPoint);
            lightDir.normalize();
            double cosThetaLight = Math.max(0.0, -nLight.dot(lightDir));

            var mat2 = prevHit.target().findFirstSibling(Material.class);
            if(mat2!=null) {
                double pdfLight = pdfLightSolidAngle(rayHit.triangle().getArea(), distanceSquared, cosThetaLight);
                double pdfBSDF = mat2.getProbableDistributionFunction(prevHit, wi, wo);
                double wBSDF = misWeight(pdfBSDF, pdfLight);

                ColorDouble emittedLight = getEmittedLight(mat);
                emittedLight.multiply(throughput);
                emittedLight.scale(wBSDF);
                radiance.add(emittedLight);
            } else throw new RuntimeException("no material?");
        } else {
            // First bounce: no MIS, just add light directly
            ColorDouble emittedLight = getEmittedLight(mat);
            emittedLight.multiply(throughput);
            radiance.add(emittedLight);
        }
    }

    private double pdfLightSolidAngle(double lightArea, double distanceSquared,double cosThetaLight) {
        if (cosThetaLight <= 0.0) {
            return 0.0; // back-facing, zero probability
        }
        double pA = 1.0 / lightArea; // uniform on area
        return (pA * distanceSquared) / cosThetaLight;
    }

    private static double misWeight(double pdfA, double pdfB) {
        double a2 = pdfA * pdfA;
        double b2 = pdfB * pdfB;
        return a2 / (a2 + b2);
    }

    public ColorDouble getEmittedLight(Material mat) {
        var emittedLight = new ColorDouble(mat.getEmissionColor());
        emittedLight.scale(mat.getEmissionStrength());
        return emittedLight;
    }

    /**
     * sky or sun color, depending on angle of incidence
     * @param ray the ray to check
     * @return the color of the sky
     */
    private ColorDouble getSkyColor(Ray ray) {
        Vector3d d = ray.getDirection();
        d.normalize();
        Vector3d sun = new Vector3d(sunlightSource);
        sun.normalize();
        var dot = Math.max(0,sun.dot(d));

        var sd = Math.pow(dot,5);
        //var a = 0.5 * (-d.z + 1.0);
        var a = 1.0-sd;
        return new ColorDouble(
                a * ambientColor.r + sd * sunlightColor.r * sunlightStrength,
                a * ambientColor.g + sd * sunlightColor.g * sunlightStrength,
                a * ambientColor.b + sd * sunlightColor.b * sunlightStrength);
    }

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
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getDepthMap() {
        return depthMap;
    }

    public void setActiveCamera(Camera camera) {
        this.activeCamera = camera;
    }

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

            //System.out.println("samples per pixel: "+samplesPerPixel + ", max depth: "+maxDepth);

            // in parallel, trace each ray and store the result in a buffer
            while(!isCancelled()) {
                rays.stream().parallel().forEach(pixel -> {
                    if(isCancelled()) return;

                    var ray = getStratifiedSample(sqrtSPP,random,activeCamera,pixel);

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
        }

        private void drawPixel(RayXY pixel, Color c) {
            // Convert Ray’s coordinate to pixel indices and set the pixel’s color
            image.setRGB(pixel.x, pixel.y, c.getRGB());
            // convert pixel.depth to a rainbow heatmap color
            if(pixel.depth == Double.POSITIVE_INFINITY) {
                // no hit, set to black
                depthMap.setRGB(pixel.x, pixel.y, Color.BLACK.getRGB());
            } else {
                double depthValue = Math.max(0,Math.min(1,pixel.depth / deepestHit));
                // Convert depth value to a color (e.g., blue for near, red for far)
                depthMap.setRGB(pixel.x, pixel.y, unitToRainbow(depthValue).getRGB());
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

    public void addProgressListener(PathTracerPanel pathTracerPanel) {
        listeners.add(ProgressListener.class,pathTracerPanel);
    }

    public Ray getStratifiedSample(int sqrtSPP, Random random, Camera camera,RayXY pixel) {
        int s = pixel.getSamples();
        int sx = s % sqrtSPP; // x coordinate in the sqrt grid
        int sy = s / sqrtSPP; // y coordinate in the sqrt grid

        double jx = (sx+random.nextDouble()) / sqrtSPP;
        double jy = (sy+random.nextDouble()) / sqrtSPP;
        // jiggle the ray a little bit to get a better anti-aliasing effect
        var nx =       (2.0*(pixel.x+jx)/canvasWidth ) - 1.0;
        var ny = 1.0 - (2.0*(pixel.y+jy)/canvasHeight);
        return getRayThroughPoint(camera,nx,ny);
    }

    /**
     * <p>Return the ray, in world space, that starts at the camera and passes through this viewport at (x,y) in the
     * current projection.  x,y should be normalized screen coordinates adjusted for the vertical flip.</p>
     * <p>Remember that in OpenGL the camera -Z=forward, +X=right, +Y=up</p>
     * @param normalizedX the cursor position in screen coordinates [-1,1]
     * @param normalizedY the cursor position in screen coordinates [-1,1]
     * @return the ray coming through the viewport in the current projection.
     */
    public Ray getRayThroughPoint(Camera camera, double normalizedX, double normalizedY) {
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
    public Ray getRayThroughPointUntransformed(Camera camera, double normalizedX, double normalizedY) {
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
}
