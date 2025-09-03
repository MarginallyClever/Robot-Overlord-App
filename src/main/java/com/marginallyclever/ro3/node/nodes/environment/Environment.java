package com.marginallyclever.ro3.node.nodes.environment;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.ColorDouble;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.mesh.proceduralmesh.ProceduralMeshFactory;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * <p>Environment controls external factors like the sun, the skybox, etc.</p>
 * <p>TODO <a href="https://antongerdelan.net/opengl/cubemaps.html">use the OpenGL cube map texture</a>?</p>
 */
public class Environment extends Node {
    public final Vector3d sunlightSource = new Vector3d(50,150,750);  // vector
    public static final double SUN_DISTANCE = 200;
    public Color sunlightColor = new Color(0xfd,0xfb,0xd3,255);
    public double sunlightStrength = 1;
    public Color ambientColor = new Color(0x20,0x20,0x20,255);
    private double declination = 0;  // degrees, +/-90
    private double timeOfDay = 12;  // 0-24
    private TextureWithMetadata skyTexture;
    private boolean skyShapeIsSphere = false;

    // sky mesh - either a box or a sphere
    private Mesh mesh;

    public Environment() {
        super("Environment");
        //skyTexture = Registry.textureFactory.load("/com/marginallyclever/ro3/node/nodes/environment/skybox.png");
        //buildBox();

        skyTexture = Registry.textureFactory.load("/com/marginallyclever/ro3/node/nodes/pose/poses/space/milkyway_2020_4k_print.jpg");
        skyTexture.setDoNotExport(true);
        buildSphere();
    }

    /**
     * Build a box with the given dimensions.
     * <p>Textures are mapped to the box as follows:</p>
     * <pre>
     * +---+---+---+---+
     * |   | Z |   |   |
     * +---+---+---+---+
     * | X-| Y | X | Y-|
     * +---+---+---+---+
     * |   | Z-|   |   |
     * +---+---+---+---+</pre>
     */
    private void buildBox() {
        mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_QUADS);

        float adj = 1f/256f;
        float a=0.00f+adj;
        float b=0.25f;
        float c=0.50f;
        float d=0.75f;
        float e=1.00f-adj;

        float f=1f/3f+adj*3;
        float g=2f/3f-adj*3;
        int v = 100;
        // build the top face (z+)
        mesh.addTexCoord(b,g);  mesh.addVertex(-v, v, v);
        mesh.addTexCoord(c,g);  mesh.addVertex( v, v, v);
        mesh.addTexCoord(c,e);  mesh.addVertex( v,-v, v);
        mesh.addTexCoord(b,e);  mesh.addVertex(-v,-v, v);
        // build the bottom face (z-)
        mesh.addTexCoord(b,a);  mesh.addVertex(-v, v, -v);
        mesh.addTexCoord(c,a);  mesh.addVertex( v, v, -v);
        mesh.addTexCoord(c,f);  mesh.addVertex( v,-v, -v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v,-v, -v);
        // build north face (y+)
        mesh.addTexCoord(b,g);  mesh.addVertex(-v, v,  v);
        mesh.addTexCoord(c,g);  mesh.addVertex( v, v,  v);
        mesh.addTexCoord(c,f);  mesh.addVertex( v, v, -v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v, v, -v);
        // build south face (y-)
        mesh.addTexCoord(e,g);  mesh.addVertex(-v, -v,  v);
        mesh.addTexCoord(d,g);  mesh.addVertex( v, -v,  v);
        mesh.addTexCoord(d,f);  mesh.addVertex( v, -v, -v);
        mesh.addTexCoord(e,f);  mesh.addVertex(-v, -v, -v);
        // build east face (x+)
        mesh.addTexCoord(d,g);  mesh.addVertex(v, -v,  v);
        mesh.addTexCoord(c,g);  mesh.addVertex(v,  v,  v);
        mesh.addTexCoord(c,f);  mesh.addVertex(v,  v, -v);
        mesh.addTexCoord(d,f);  mesh.addVertex(v, -v, -v);
        // build west face (x-)
        mesh.addTexCoord(a,g);  mesh.addVertex(-v, -v,  v);
        mesh.addTexCoord(b,g);  mesh.addVertex(-v,  v,  v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v,  v, -v);
        mesh.addTexCoord(a,f);  mesh.addVertex(-v, -v, -v);
    }

    private void buildSphere() {
        mesh = new Sphere(100);
        ((Sphere)mesh).updateModel();
    }

    // TODO don't mention opengl directly outside of the rendering code
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        mesh.unload(gl3);
        skyTexture.unload();
    }

    public Environment(String name) {
    super(name);
  }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("declination", declination);
        json.put("timeOfDay", timeOfDay);
        json.put("sunlightColor", sunlightColor.getRGB());
        json.put("sunlightStrength", sunlightStrength);
        json.put("ambientColor", ambientColor.getRGB());
        if(skyTexture!=null) json.put("skyTexture", skyTexture.getSource());
        json.put("skyShapeIsSphere", skyShapeIsSphere);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        declination = from.optDouble("declination", declination);
        timeOfDay = from.optDouble("timeOfDay", timeOfDay);
        sunlightColor = new Color(from.optInt("sunlightColor", sunlightColor.getRGB()));
        sunlightStrength = from.optDouble("sunlightStrength", sunlightStrength);
        ambientColor = new Color(from.optInt("ambientColor", ambientColor.getRGB()));
        sunlightSource.set(getSunPosition());
        if(from.has("skyTexture")) {
            skyTexture = Registry.textureFactory.load(from.optString("skyTexture"));
        }
        skyShapeIsSphere = from.optBoolean("skyShapeIsSphere", skyShapeIsSphere);
    }

    public Color getSunlightColor() {
        return sunlightColor;
    }

    public void setSunlightColor(Color color) {
        sunlightColor = color;
    }

    public double getSunlightStrength() {
        return sunlightStrength;
    }

    public void setSunlightStrength(double strength) {
        sunlightStrength = strength;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Color color) {
        ambientColor = color;
    }

    public Vector3d getSunlightSource() {
        return new Vector3d(sunlightSource);
    }

    public double getDeclination() {
        return declination;
    }

    public void setDeclination(double declination) {
        this.declination = declination;
        sunlightSource.set(getSunPosition());
    }

    /**
     * @return 0..360 (scale by 24/360 to get hours)
     */
    public double getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(double timeOfDay) {
        this.timeOfDay = timeOfDay;
        sunlightSource.set(getSunPosition());
    }

    private Vector3d getSunPosition() {
        Matrix4d m = new Matrix4d();
        m.rotX(Math.toRadians(180-declination));
        Vector3d vx = MatrixHelper.getXAxis(m);
        Vector3d vy = MatrixHelper.getZAxis(m);

        double hourAngle = Math.toRadians(timeOfDay); // Convert hours to degrees
        //System.out.println("hourAngle="+(timeOfDay%24)+" declination="+declination);

        var result = new Vector3d();
        vy.scale(Math.cos(hourAngle));
        vx.scale(Math.sin(hourAngle));
        result.add(vx, vy);

        result.scale(SUN_DISTANCE);

        return result;
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new EnvironmentPanel(this));
        super.getComponents(list);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/environment/icons8-environment-16.png")));
    }

    public TextureWithMetadata getSkyTexture() {
        return skyTexture;
    }

    public void setSkyTexture(TextureWithMetadata skyTextureSource) {
        this.skyTexture = skyTextureSource;
        // TODO find the DrawBackground render pass and update the texture
    }

    public boolean isSkyShapeIsSphere() {
        return skyShapeIsSphere;
    }

    public void setSkyShapeIsSphere(boolean skyShapeIsSphere) {
        this.skyShapeIsSphere = skyShapeIsSphere;
        // TODO unload the old mesh from opengl
        if(isSkyShapeIsSphere()) {
            buildSphere();
        } else {
            buildBox();
        }
    }

    public Mesh getSkyMesh() {
        return mesh;
    }

    /**
     * sky or sun color, depending on angle of incidence
     * @param ray the ray to check
     * @return the color of the sky
     */
    private ColorDouble getSkyColor(Ray ray) {
        sunlightSource.set(this.getSunlightSource());
        sunlightSource.normalize();
        var sunlightColorD = new ColorDouble(this.getSunlightColor());
        var ambientColorD = new ColorDouble(this.getAmbientColor());
        sunlightStrength = this.getSunlightStrength();

        Vector3d d = ray.getDirection();
        d.normalize();
        var dot = Math.clamp(sunlightSource.dot(d),0,1);
        var sd = Math.pow(dot,16);
        var a = 1.0-sd;
        return new ColorDouble(
                a * ambientColorD.r + sd * sunlightColorD.r * sunlightStrength,
                a * ambientColorD.g + sd * sunlightColorD.g * sunlightStrength,
                a * ambientColorD.b + sd * sunlightColorD.b * sunlightStrength);
    }

    /**
     * Locate the Environment in the scene.  If there is a texture assigned, look up the UV color.
     * If there is no texture, return the sky/sun color.
     * @param ray
     * @return
     */
    public ColorDouble getEnivornmentColor(Ray ray) {
        if(getSkyTexture()==null) { // no texture, return sky/sun effect
            return new ColorDouble(getSkyColor(ray));
        }
        // Is the sky a cube or a sphere?
        if(isSkyShapeIsSphere()) {
            // get the texture coordinate on the sphere
        } else {
            // get texture coordinate on cube
        }

        return getSkyColor(ray);  // placeholder
    }
}
