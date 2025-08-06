package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MathHelper;
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
    private static final double LIGHT_SAMPLING_PROBABILITY = 1.0;
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

    public void render() {
        if(pathTracingWorker !=null) {
            pathTracingWorker.cancel(true);
            pathTracingWorker = null;
        } else {
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

            rayPickSystem.reset();
            pathTracingWorker = new PathTracingWorker(rays, image);
            pathTracingWorker.execute();
        }
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
            Material mat = getMaterial(rayHit);
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
            Vector3d wo = new Vector3d(ray.direction());
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

            // Handle refraction or reflection based on Fresnel reflectance
            ColorDouble albedo = new ColorDouble(mat.getDiffuseColor());
            if(albedo.a<1.0) {
                // this will return either the reflected or refracted ray
                ray = getRefraction(ray,rayHit.point(),rayHit.normal(),mat);
            } else {
                // opaque
                boolean isSpecularBounce = mat.getSpecularStrength() > random.nextDouble();
                //throughput.scale(isSpecularBounce ? new ColorDouble(mat.getSpecularColor()) : albedo);
                Vector3d specularDirection = reflect(ray.direction(), rayHit.normal());
                var dir = MathHelper.interpolate(wi, specularDirection, (isSpecularBounce ? mat.getReflectivity() : 0));
                dir.normalize();
                ray = new Ray(rayHit.point(), dir);
            }
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
        from.scaleAdd(-EPSILON,rayHit.normal(),rayHit.point());
        Ray lightRay = new Ray(from, toLight);
        RayHit lightHit = rayPickSystem.getFirstHit(lightRay,true);
        if (lightHit == null) return;

        // hit a thing
        var target = lightHit.target();
        var mat2 = target.findFirstSibling(Material.class);
        if (mat2 == null || !mat2.isEmissive()) return;

        // the light is visible
        ColorDouble lightEmission = getEmittedLight(mat2);
        double cosTheta = Math.max(0, rayHit.normal().dot(toLight));
        double cosThetaLight = Math.max(0, -lightHit.normal().dot(toLight));

        Vector3d wi = new Vector3d(toLight);
        Vector3d wo = new Vector3d(ray.direction());
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
     * @param throughput
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
            Vector3d wi = new Vector3d(ray.direction());
            Vector3d wo = new Vector3d(prevRay.direction());
            wo.negate();

            Point3d prevPoint = ray.origin();
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

    private ColorDouble getBRDF(RayHit rayHit, Vector3d in, Vector3d out, Material mat) {
        // get the diffuse color
        ColorDouble albedo = new ColorDouble(mat.getDiffuseColor());
        albedo.scale(1.0 / Math.PI); // cosine-weighted diffuse
        return albedo;/*
        if(albedo.a<1.0) {
            // this is a semi-transparent material.  use the diffuse color.
        } else {
            // opaque material.  use the diffuse color and specular color.
            albedo.scale(1.0 - mat.getReflectivity());
        }

        // calculate the BRDF
        double cosTheta = Math.max(0, diffuseDirection.dot(rayHit.normal()));
        return new ColorDouble(
                albedo.r * cosTheta,
                albedo.g * cosTheta,
                albedo.b * cosTheta);*/
    }

    public ColorDouble getEmittedLight(Material mat) {
        var emittedLight = new ColorDouble(mat.getEmissionColor());
        emittedLight.scale(mat.getEmissionStrength());
        return emittedLight;
    }

    /**
     * <p>Refract the ray and return the refracted vector.</p>
     * @param incident  the incident vector
     * @param normal   the normal at the hit point
     * @param iorRatio the ratio of the refractive indices of the two materials
     * @return the refracted vector
     */
    private Vector3d refract2(Vector3d incident, Vector3d normal, double iorRatio) {
        double cosI = -normal.dot(incident);
        double sin2T = iorRatio * iorRatio * (1 - cosI * cosI);

        // Total internal reflection
        if (sin2T > 1.0) {
            return null;
        }

        double cosT = Math.sqrt(1.0 - sin2T);
        Vector3d refracted = new Vector3d();
        refracted.scale(iorRatio, incident);
        refracted.scaleAdd(iorRatio * cosI - cosT, normal, refracted);
        return refracted;
    }

    private Material getMaterial(RayHit rayHit) {
        var meshInstance = rayHit.target();
        if(meshInstance==null) return defaultMaterial;
        var mat = meshInstance.findFirstSibling(Material.class);
        return mat == null ? defaultMaterial : mat;
    }

    /**
     * sky or sun color, depending on angle of incidence
     * @param ray the ray to check
     * @return the color of the sky
     */
    private ColorDouble getSkyColor(Ray ray) {
        Vector3d d = ray.direction();
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

    /**
     * Get the refracted ray
     * @param ray the ray to refract
     * @param hitPoint the point where the ray hit
     * @param normal the normal at the hit point
     * @param mat the material at the hit point
     * @return the next ray to trace
     */
    private Ray getRefraction(Ray ray, Point3d hitPoint, Vector3d normal, Material mat) {
        // at least semi-transparent.  use index of refraction.
        var rayDirection = ray.direction();
        rayDirection.normalize();

        var ior = mat.getIOR();
        var cosTheta = Math.min(-normal.dot(rayDirection),1.0);
        // since normals face outside an object, if cosTheta is positive the ray is exiting the object.
        boolean backFace = cosTheta > 0;
        if(backFace) normal.negate();

        var ri = backFace ? 1.0/ior : ior;
        var sinTheta = Math.sqrt(1.0-cosTheta*cosTheta);
        var cannotRefract = ri * sinTheta > 1.0;
        var nextDir = cannotRefract || reflectance(cosTheta,ri) > random.nextDouble()
                ? reflect(rayDirection,normal) // total internal reflection
                : refract(rayDirection,normal,ri); // refraction

        // Recursively compute the color along the next ray
        return new Ray(hitPoint, nextDir);
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
