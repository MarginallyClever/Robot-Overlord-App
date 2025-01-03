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

public class PathTracer {
    private final List<RayXY> rays = new ArrayList<>();
    private final RayPickSystem rayPickSystem = new RayPickSystem();
    private RayTracingWorker rayTracingWorker;
    private Camera activeCamera;
    private int canvasWidth = 640;
    private int canvasHeight = 480;
    private BufferedImage image;

    private int samplesPerPixel = 500;
    private int maxDepth = 3;
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
        if(rayTracingWorker!=null) {
            rayTracingWorker.cancel(true);
            rayTracingWorker = null;
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

            rayTracingWorker = new RayTracingWorker(rays, image);
            rayTracingWorker.execute();
        }
    }

    /**
     * <p>Trace the ray and return the color of the pixel at the end of the ray.</p>
     * @param ray the ray to trace
     * @return the color of the pixel at the end of the ray.
     */
    private ColorDouble trace(Ray ray) {
        ColorDouble incomingLight = new ColorDouble(0, 0, 0);
        ColorDouble rayColor = new ColorDouble(1, 1, 1, 1);

        for (int i = 0; i <= maxDepth; ++i) {
            RayHit rayHit = rayPickSystem.getFirstHit(ray);
            if (rayHit == null) {
                var sky = getSkyColor(ray);
                sky.scale(rayColor);
                incomingLight.add(sky);
                break;
            }

            var mat = getMaterial(rayHit);
            var albedo = new ColorDouble(mat.getDiffuseColor());

            var emittedLight = new ColorDouble(mat.getEmissionColor());
            emittedLight.scale(mat.getEmissionStrength());
            emittedLight.scale(rayColor);

            incomingLight.add(emittedLight);

            boolean isSpecularBounce = mat.getSpecularStrength() > random.nextDouble();
            rayColor.scale(isSpecularBounce ? new ColorDouble(mat.getSpecularColor()) : albedo);

            // monte carlo russian roulette
            double average = (rayColor.r + rayColor.g + rayColor.b ) / 3;
            //double average = 0.2126*rayColor.r + 0.7152*rayColor.g + 0.0722*rayColor.b;
            double p = 1.0/Math.max(1e-6,average);
            if( random.nextDouble() > average) {
                incomingLight.scale(average == 0 ? 0 : p);
                break;
            }
            rayColor.scale(p);

            // Calculate Fresnel reflectance
            double cosTheta = rayHit.normal().dot(ray.getDirection());
            boolean entering = cosTheta < 0;
            //double ior1 = entering ? 1.0 : mat.getIOR(); // Air IOR = 1.0
            //double ior2 = entering ? mat.getIOR() : 1.0;
            //double reflectance = computeFresnel(Math.abs(cosTheta), ior1, ior2);

            // Handle refraction or reflection based on Fresnel reflectance
            if(albedo.a<1.0) {
                // this will return either the reflected or refracted ray
                Ray r3 = getRefraction(ray,rayHit.point(),rayHit.normal(),mat);
                ray.setOrigin(rayHit.point());
                ray.setDirection(r3.getDirection());
                continue;
            }

            // opaque
            ray.setOrigin(rayHit.point());
            // get the cosine weighted random direction
            var diffuseDirection = getRandomUnitVector();
            diffuseDirection.add(rayHit.normal());
            diffuseDirection.normalize();
            if (diffuseDirection.lengthSquared() < 1e-6) {
                // edge case where diffuseDirection is zero.
                diffuseDirection.set(rayHit.normal());
            }

            Vector3d specularDirection = reflect(ray.getDirection(),rayHit.normal());
            var dir = MathHelper.interpolate(diffuseDirection, specularDirection, (isSpecularBounce ? mat.getReflectivity() : 0));
            dir.normalize();
            ray.setDirection(dir);
        }

        return incomingLight;
    }

    private double computeFresnel(double cosTheta, double ior1, double ior2) {
        double r0 = Math.pow((ior1 - ior2) / (ior1 + ior2), 2);
        return r0 + (1 - r0) * Math.pow(1 - cosTheta, 5);
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
        var rayDirection = ray.getDirection();
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
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setActiveCamera(Camera camera) {
        this.activeCamera = camera;
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

            System.out.println("samples per pixel: "+samplesPerPixel + ", max depth: "+maxDepth);

            // in parallel, trace each ray and store the result in a buffer
            while(true) {
                if(isCancelled()) return null;

                rays.stream().parallel().forEach(pixel -> {
                    if(isCancelled()) return;

                    // jiggle the ray a little bit to get a better anti-aliasing effect
                    var nx =       (2.0*(pixel.x+random.nextDouble()-0.5)/canvasWidth ) - 1.0;
                    var ny = 1.0 - (2.0*(pixel.y+random.nextDouble()-0.5)/canvasHeight);
                    var ray = getRayThroughPoint(activeCamera,nx,ny);
                    // sum the total color of all samples
                    pixel.sum.add(trace(ray));
                    pixel.samples++;
                    // then get the average of the samples
                    pixel.average.set(pixel.sum);
                    pixel.average.scale(1.0/pixel.samples);
                    toneMap(pixel.average);
                    // store the result in the buffer
                    drawPixel(pixel,pixel.average.getColor());

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

            fireProgressUpdate(latestProgress);
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

    public double getAspectRatio() {
        return (double)canvasWidth/(double)canvasHeight;
    }

    private void fireProgressUpdate(int latestProgress) {
        for( var listener : listeners.getListeners(ProgressListener.class)) {
            listener.onProgressUpdate(latestProgress);
        }
    }

    public void addProgressListener(PathTracerPanel pathTracerPanel) {
        listeners.add(ProgressListener.class,pathTracerPanel);
    }
}
