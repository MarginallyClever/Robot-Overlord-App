package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.ColorDouble;
import com.marginallyclever.ro3.apps.pathtracer.PathTracerHelper;
import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;
import com.marginallyclever.ro3.apps.pathtracer.ScatterRecord;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * <p>{@link Material} contains properties for rendering a surface.  The first use case is to apply a texture to a
 * {@link com.marginallyclever.ro3.mesh.Mesh}.</p>
 *
 * <p>This class provides several functionalities:</p>
 * <ul>
 * <li>It can set and get the texture.</li>
 * <li>It can set and get the diffuse color.</li>
 * <li>It can set and get the specular color.</li>
 * <li>It can set and get the emission color.</li>
 * <li>It can set and get the shininess.</li>
 * <li>It can set and get the lit status.</li>
 * <li>It can serialize and deserialize itself to and from JSON format.</li>
 * </ul>
 */
public class Material extends Node {
    private TextureWithMetadata diffuseTexture;
    private TextureWithMetadata normalTexture;
    private TextureWithMetadata specularTexture;
    private Color diffuseColor = new Color(255,255,255);
    private Color specularColor = new Color(255,255,255);
    private Color emissionColor = new Color(0,0,0);
    private double emissionStrength = 0.0;
    private int shininess = 10;
    private boolean isLit = true;
    private double specularStrength = 0.5;
    private double ior = 1.0;  // index of refraction
    private double reflectivity = 0.0;  // 0...1

    public Material() {
        this("Material");
    }

    public Material(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MaterialPanel(this));
        super.getComponents(list);
    }

    public void setDiffuseTexture(TextureWithMetadata texture) {
        diffuseTexture = texture;
    }

    public TextureWithMetadata getDiffuseTexture() {
        return diffuseTexture;
    }

    public void setNormalTexture(TextureWithMetadata texture) {
        normalTexture = texture;
    }

    public TextureWithMetadata getNormalTexture() {
        return normalTexture;
    }

    public void setSpecularTexture(TextureWithMetadata texture) {
        specularTexture = texture;
    }

    public TextureWithMetadata getSpecularTexture() {
        return specularTexture;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        if(diffuseTexture !=null) json.put("texture", diffuseTexture.getSource());
        if(specularTexture !=null) json.put("specularTexture", specularTexture.getSource());
        if(normalTexture !=null) json.put("normalTexture", normalTexture.getSource());
        json.put("diffuseColor", diffuseColor.getRGB());
        json.put("specularColor", specularColor.getRGB());
        json.put("emissionColor", emissionColor.getRGB());
        json.put("emissionStrength", emissionStrength);
        json.put("shininess", shininess);
        json.put("specularStrength", specularStrength);
        json.put("isLit", isLit);
        json.put("ior", ior);
        json.put("reflectivity", reflectivity);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("texture")) diffuseTexture = Registry.textureFactory.load(from.getString("texture"));
        if(from.has("specularTexture")) specularTexture = Registry.textureFactory.load(from.getString("specularTexture"));
        if(from.has("normalTexture")) normalTexture = Registry.textureFactory.load(from.getString("normalTexture"));
        if(from.has("diffuseColor")) diffuseColor = new Color(from.getInt("diffuseColor"),true);
        if(from.has("specularColor")) specularColor = new Color(from.getInt("specularColor"),true);
        if(from.has("emissionColor")) emissionColor = new Color(from.getInt("emissionColor"),true);
        if(from.has("emissionStrength")) emissionStrength = from.getDouble("emissionStrength");
        if(from.has("shininess")) shininess = from.getInt("shininess");
        if(from.has("specularStrength")) specularStrength = from.getDouble("specularStrength");
        if(from.has("isLit")) isLit = from.getBoolean("isLit");
        if(from.has("ior")) ior = from.getDouble("ior");
        if(from.has("reflectivity")) reflectivity = from.getDouble("reflectivity");
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Color color) {
        diffuseColor = color;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Color color) {
        specularColor = color;
    }

    public Color getEmissionColor() {
        return emissionColor;
    }

    public void setEmissionColor(Color color) {
        emissionColor = color;
    }

    /**
     * @return the emission strength of the material.  &gt;=0
     */
    public double getEmissionStrength() {
        return emissionStrength;
    }

    /**
     * @param emissionStrength the emission strength of the material.  &gt;=0
     */
    public void setEmissionStrength(double emissionStrength) {
        this.emissionStrength = emissionStrength;
    }

    public boolean isEmissive() {
        return emissionStrength>0 && (emissionColor.getRed()>0 || emissionColor.getGreen()>0 || emissionColor.getBlue()>0);
    }

    public void setShininess(int arg0) {
        shininess = Math.max(arg0, 0);
    }

    /**
     * @return the shininess of the material 0...128
     */
    public int getShininess() {
        return shininess;
    }

    public boolean isLit() {
        return isLit;
    }

    public void setLit(boolean isLit) {
        this.isLit = isLit;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-texture-16.png")));
    }

    public void setSpecularStrength(double specularStrength) {
        this.specularStrength = specularStrength;
    }

    public double getSpecularStrength() {
        return specularStrength;
    }

    /**
     * Set the index of refraction.
     * @param ior the index of refraction
     */
    public void setIOR(double ior) {
        this.ior = ior;
    }

    /**
     * @return the index of refraction.
     */
    public double getIOR() {
        return ior;
    }

    /**
     * Set the reflectivity of the material.
     * @param reflectivity 0...1.0
     */
    public void setReflectivity(double reflectivity) {
        this.reflectivity = reflectivity;
    }

    /**
     * @return the reflectivity of the material.
     */
    public double getReflectivity() {
        return reflectivity;
    }

    /**
     * Calculate the Bidirectional Reflectance Distribution Function (BRDF) for this material.
     * @param rayHit the ray hit record containing the normal and other information about the surface.
     * @param in the incoming direction of the ray.
     * @param out the outgoing direction of the ray.
     * @return the BRDF value as a {@link ColorDouble}.
     */
    public ColorDouble BRDF(RayHit rayHit,Vector3d in, Vector3d out) {
        var n = rayHit.normal();
        if(n.dot(in)<=0 || n.dot(out) <= 0.0) {
            // back facing, no light
            return new ColorDouble(0,0,0);
        }

        // Split energy between diffuse and specular
        double ks = getSpecularStrength();   // [0..1]
        double kd = 1.0 - ks;

        // diffuse (lambertian)
        ColorDouble diffuse = new ColorDouble(getDiffuseTextureAt(rayHit));
        diffuse.multiply(new ColorDouble(getDiffuseColor()));
        //ColorDouble diffuse = new ColorDouble(getDiffuseColor());
        diffuse.scale(kd / Math.PI); // diffuse BRDF

        if (ks > 0.0 && getShininess() > 0) {
            // specular (blinn-phong)
            Vector3d h = new Vector3d(in);
            h.add(out);
            if (h.lengthSquared() > 0) {
                h.normalize();
                double specDot = Math.max(0, n.dot(h));
                double norm = (getShininess() + 2.0) / (2.0 * Math.PI);
                double specular = norm * Math.pow(specDot, getShininess());

                ColorDouble spec = new ColorDouble(getSpecularColor());
                spec.scale(specular * ks);
                diffuse.add(spec);
            }
        }

        return diffuse;
    }

    /**
     * @param rayHit the ray hit record containing the triangle and point of intersection.
     * @return the color from the diffuse texture at the UV coordinates of the ray hit, or white if no texture is set.
     */
    private Color getDiffuseTextureAt(RayHit rayHit) {
        if (diffuseTexture == null) return Color.WHITE;
        var uv = rayHit.triangle().getUVAt(rayHit.point());
        return diffuseTexture.getColorAt(uv.x, uv.y);
    }

    public double getProbableDistributionFunction(RayHit rayHit, Vector3d in, Vector3d out) {
        double cosTheta = rayHit.normal().dot(in);
        if( cosTheta <= 0 ) return 0.0;

        return cosTheta / Math.PI;
    }

    /**
     * Calculate the emitted light from a material based on its emission color and strength.
     *
     * @return the emitted light as a ColorDouble
     */
    public ColorDouble getEmittedLight() {
        var emittedLight = new ColorDouble(getEmissionColor());
        emittedLight.scale(getEmissionStrength());
        return emittedLight;
    }

    public ScatterRecord scatter(Ray ray, RayHit rayHit, SplittableRandom random) {
        var in = PathTracerHelper.getRandomCosineWeightedHemisphere(random, rayHit.normal());
        // if the material is reflective, we need to consider that.
        double opacity = (diffuseColor.getAlpha()/255.0);
        if(opacity>0) {
            // partially opaque or fully opaque material
            double reflectivity = getReflectivity();
            if (reflectivity > 0 && random.nextDouble() < reflectivity) {
                // Reflective case
                Vector3d reflected = reflect(ray.getDirection(), rayHit.normal());
                return new ScatterRecord(ScatterRecord.ScatterType.EXPLICIT, reflected, 1.0, new ColorDouble(1, 1, 1, 1));
            }

            double p = random.nextDouble();
            if(p<opacity) {
                var out = ray.getWo();
                var attenuation = BRDF(rayHit,in,out);
                attenuation.scale(1.0/p);
                return new ScatterRecord(ScatterRecord.ScatterType.RANDOM,in,1.0,attenuation);
            }
        }

        // this is a partially transparent material, treat it as a dielectric.
        double cosTheta = rayHit.normal().dot(ray.getDirection());
        boolean entering = cosTheta < 0;
        Vector3d outwardNormal = rayHit.normal();
        if(entering) outwardNormal.negate();
        double etai = entering ? 1.0 : ior;
        double etat = entering ? ior : 1.0;
        double eta = etai / etat;

        double sinTheta2 = eta * eta * (1.0 - cosTheta * cosTheta);

        double absCosTheta = Math.abs(cosTheta);
        if (sinTheta2 > 1.0 || reflectance(absCosTheta, eta) > random.nextDouble()) {
            // Total internal reflection or Fresnel reflection
            Vector3d reflected = reflect(ray.getDirection(), outwardNormal);
            return new ScatterRecord(ScatterRecord.ScatterType.EXPLICIT, reflected, 1.0, new ColorDouble(1,1,1,1));
        }

        // refractive
        var out = getRefractedRay(random, rayHit.normal(), ray.getDirection(), ior);
        return new ScatterRecord(ScatterRecord.ScatterType.EXPLICIT,out,1.0,new ColorDouble(1,1,1,1));
    }

    private double reflectance(double cosine, double ref_idx) {
        // Use Schlick's approximation for reflectance
        double r0 = (1 - ref_idx) / (1 + ref_idx);
        r0 = r0 * r0;
        return r0 + (1 - r0) * Math.pow((1 - cosine), 5);
    }

    /**
     * Reflect a vector off a surface normal.
     *
     * @param v the incoming vector
     * @param n the surface normal
     * @return the reflected vector
     */
    private Vector3d reflect(Vector3d v, Vector3d n) {
        Vector3d result = new Vector3d(v);
        Vector3d temp = new Vector3d(n);
        temp.scale(2.0 * v.dot(n));
        result.sub(temp);
        return result;
    }

    private Vector3d getRefractedRay(SplittableRandom random, Vector3d normal, Vector3d in, double ior) {
        double cosTheta = -normal.dot(in);
        double etai = 1.0, etat = ior;
        Vector3d n = new Vector3d(normal);

        if (cosTheta < 0) {
            // Ray is entering the medium
            cosTheta = -cosTheta;
            double temp = etai;
            etai = etat;
            etat = temp;
            n.negate();
        }

        double eta = etai / etat;
        double sinThetaT2 = eta * eta * (1.0 - cosTheta * cosTheta);

        if (sinThetaT2 > 1.0) {
            // Total internal reflection
            return in;
        }

        double cosThetaT = Math.sqrt(1.0 - sinThetaT2);
        Vector3d out = new Vector3d(in);
        out.scale(eta);
        n.scale(eta * cosTheta - cosThetaT);
        out.add(n);
        out.normalize();

        return out;
    }
}
