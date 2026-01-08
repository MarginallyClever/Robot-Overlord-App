package com.marginallyclever.ro3.node.nodes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.*;
import com.marginallyclever.ro3.apps.viewport.TextureLayerIndex;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.raypicking.Hit;
import com.marginallyclever.ro3.shader.ShaderProgram;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    private static final Logger logger = LoggerFactory.getLogger(Material.class);
    private static final String RESOURCE_PATH = "/com/marginallyclever/ro3/node/nodes/material/";

    private final List<TextureWithMetadata> textures = new ArrayList<>();
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

        for (TextureLayerIndex ti : TextureLayerIndex.values()) {
            String filename = RESOURCE_PATH + ti.getName() + ".jpg";
            textures.add(Registry.textureFactory.get(Lifetime.APPLICATION, filename));
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MaterialPanel(this));
        super.getComponents(list);
    }

    public void setDiffuseTexture(TextureWithMetadata texture) {
        textures.set(TextureLayerIndex.ALBEDO.getIndex(),texture);
    }

    public TextureWithMetadata getDiffuseTexture() {
        return getTexture(TextureLayerIndex.ALBEDO.getIndex());
    }

    public void setNormalTexture(TextureWithMetadata texture) {
        textures.set(TextureLayerIndex.NORMAL.getIndex(), texture);
    }

    public TextureWithMetadata getNormalTexture() {
        return getTexture(TextureLayerIndex.NORMAL.getIndex());
    }

    public void setSpecularTexture(TextureWithMetadata texture) {
        textures.set(TextureLayerIndex.METALLIC.getIndex(), texture);
    }

    public TextureWithMetadata getSpecularTexture() {
        return getTexture(TextureLayerIndex.METALLIC.getIndex());
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("diffuseColor", diffuseColor.getRGB());
        json.put("specularColor", specularColor.getRGB());
        json.put("emissionColor", emissionColor.getRGB());
        json.put("emissionStrength", emissionStrength);
        json.put("shininess", shininess);
        json.put("specularStrength", specularStrength);
        json.put("isLit", isLit);
        json.put("ior", ior);
        json.put("reflectivity", reflectivity);

        json.put("version",1);
        JSONObject texturesJson = new JSONObject();
        for(var ti : TextureLayerIndex.values()) {
            var tex = getTexture(ti.getIndex());
            if(tex!=null) {
                texturesJson.put(ti.getName(), tex.getSource());
            }
        }
        json.put("textures", texturesJson);

        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("diffuseColor")) diffuseColor = new Color(from.getInt("diffuseColor"),true);
        if(from.has("specularColor")) specularColor = new Color(from.getInt("specularColor"),true);
        if(from.has("emissionColor")) emissionColor = new Color(from.getInt("emissionColor"),true);
        if(from.has("emissionStrength")) emissionStrength = from.getDouble("emissionStrength");
        if(from.has("shininess")) shininess = from.getInt("shininess");
        if(from.has("specularStrength")) specularStrength = from.getDouble("specularStrength");
        if(from.has("isLit")) isLit = from.getBoolean("isLit");
        if(from.has("ior")) ior = from.getDouble("ior");
        if(from.has("reflectivity")) reflectivity = from.getDouble("reflectivity");

        int version = from.optInt("version",0);  // future use
        if(version==0) {
            // old format: single texture
            if(from.has("texture")) {
                setDiffuseTexture(Registry.textureFactory.get(Lifetime.SCENE,from.getString("texture")));
            }
            if(from.has("specularTexture")) setSpecularTexture(Registry.textureFactory.get(Lifetime.SCENE,from.getString("specularTexture")));
            if(from.has("normalTexture")) setNormalTexture(Registry.textureFactory.get(Lifetime.SCENE,from.getString("normalTexture")));
        }
        if(version==1) {
            var texturesFrom = from.optJSONObject("textures");
            if(texturesFrom!=null) {
                for(var ti : TextureLayerIndex.values()) {
                    var source = texturesFrom.optString(ti.getName());
                    if(source!=null && !source.isEmpty()) {
                        var tex = Registry.textureFactory.get(Lifetime.SCENE,source);
                        setTexture(ti.getIndex(),tex);
                    }
                }
            }
        }
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
     * @param arg0 0...1.0
     */
    public void setReflectivity(double arg0) {
        this.reflectivity = arg0;
    }

    /**
     * @return the reflectivity of the material.
     */
    public double getReflectivity() {
        return reflectivity;
    }

    /**
     * Calculates the combination of the overall diffuse color, the diffuse texture, and the per-vertex color at the hit
     * point.
     * @param hit the ray hit record containing the triangle and point of intersection.
     * @return the color from the diffuse components at the UV coordinates of the ray hit, or white if no texture is set.
     */
    private Color getDiffuseColorAt(Hit hit) {
        if(hit.triangle()==null) {
            return Color.WHITE;
        }
        ColorDouble diffuseColor = new ColorDouble(getDiffuseColor());
        diffuseColor.multiply(new ColorDouble(hit.triangle().getColorAt(hit.point())));

        var diffuseTexture = getDiffuseTexture();
        if (diffuseTexture != null) {
            var uv = hit.triangle().getUVAt(hit.point());
            var textureColor = diffuseTexture.getColorAt(uv.x, uv.y);
            diffuseColor.multiply(new ColorDouble(textureColor));
        }

        Color finalColor = new Color(
                (int)(diffuseColor.r * 255),
                (int)(diffuseColor.g * 255),
                (int)(diffuseColor.b * 255),
                (int)(diffuseColor.a * 255)
        );
        return finalColor;
    }

    /**
     * Cosine-weighted hemisphere PDF for diffuse reflection.
     * @param hit the hit record
     * @param in incoming direction (unit, towards surface)
     * @param out outgoing direction (unit, towards viewer)
     * @return the PDF value
     */
    public double getPDF(Hit hit, Vector3d in, Vector3d out) {
        double cosTheta = hit.normal().dot(in);
        if( cosTheta <= 0 ) {
            return 0.0;
        }

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

    public ScatterRecord scatter(Ray ray, Hit hit, RayXY pixel) {
        Vector3d n = hit.normal();
        Vector3d wo = ray.getWo();
        var p = hit.point();

        var albedoMap = textures.get(TextureLayerIndex.ALBEDO.getIndex());
        var metalMap = textures.get(TextureLayerIndex.METALLIC.getIndex());
        var uv = hit.triangle().getUVAt(hit.point());
/*
        if(this.reflectivity>0) {
            double r = pixel.halton.nextDouble(PathTracer.CHANNEL_BSDF_SAMPLING);
            if(r <= this.reflectivity) {
                // imperfect reflection
                Vector3d reflectDir = reflect(ray.getDirection(), n);
                var F0 = new ColorDouble(1,1,1,1);
                var record = new ScatterRecord(new Ray(p, reflectDir), F0, 1.0, true);
                record.type = ScatterRecord.ScatterType.SPECULAR;
                return record;
            }
        }*/

        ColorDouble diffuse = new ColorDouble(getDiffuseColorAt(hit));

        // Fresnel reflectance using Schlick
        ColorDouble spec = new ColorDouble(getSpecularColor());
        spec.scale(getSpecularStrength());

        // Mix dielectric F0 (spec) with albedo for metallic behavior

        Color metalColor = (metalMap != null) ? metalMap.getColorAt(uv.x, uv.y) : new Color(0,0,0,0);
        double mR = Math.clamp(metalColor.getRed()   / 255.0, 0.0, 1.0 );
        double mG = Math.clamp(metalColor.getGreen() / 255.0, 0.0, 1.0 );
        double mB = Math.clamp(metalColor.getBlue()  / 255.0, 0.0, 1.0 );

        // where specular/metallic map is black, reflect nothing (use diffuse color)
        ColorDouble F0 = new ColorDouble(
            spec.r * mR + diffuse.r * (1.0 - mR),
            spec.g * mG + diffuse.g * (1.0 - mG),
            spec.b * mB + diffuse.b * (1.0 - mB),
            1.0);

        // cosθ for Fresnel and diffuse
        double cosTheta = Math.max(0, ray.getDirection().dot(n));
        double R = schlickFresnel(cosTheta, spec);

        // Lobe weights

        double wt = (1.0-diffuse.a);  // transmission weight
        double wd = (diffuse.r+diffuse.g+diffuse.b)/3.0 * (1.0 - wt);  // diffuse weight
        double ws = R * (1 - wt);  // specular reflection weight

        double sum = wd + ws + wt;
        wd /= sum;
        ws /= sum;

        // Random choice
        double r = pixel.halton.nextDouble(PathTracer.CHANNEL_BSDF_SAMPLING);
        if(r > wd + ws) {
            // --- Transmission lobe ---
            double etai = 1.0;
            double etat = this.ior;

            Vector3d nn = new Vector3d(n);
            double cosi = -ray.getDirection().dot(n);
            if (cosi < 0) {
                // inside object → flip normal, swap indices
                cosi = -cosi;
                nn.negate();
                double tmp = etai;
                etai = etat;
                etat = tmp;
            }

            double eta = etai / etat;
            double sin2t = eta * eta * (1.0 - cosi * cosi);

            ScatterRecord record;
            if (sin2t > 1.0) {
                // Total internal reflection → fall back to mirror
                Vector3d reflectDir = reflect(ray.getDirection(), n);
                record = new ScatterRecord(new Ray(p, reflectDir), F0, 1.0, true);
                record.type = ScatterRecord.ScatterType.SPECULAR;
                return record;
            }

            // Refract
            double cost = Math.sqrt(1.0 - sin2t);
            Vector3d refractDir = new Vector3d(ray.getDirection());
            refractDir.scale(eta);

            Vector3d ns = new Vector3d(nn);
            ns.scale(eta * cosi - cost);
            refractDir.add(ns);
            refractDir.normalize();

            var attenuation = new ColorDouble(1,1,1,1);
            record = new ScatterRecord(new Ray(p, refractDir), attenuation, 1.0, true);
            record.type = ScatterRecord.ScatterType.REFRACTIVE;
            return record;
        } else if (r > wd) {
            // --- Specular lobe ---
            Vector3d reflectDir = reflect(ray.getDirection(), n);

            if (this.reflectivity == 1.0) {
                // perfect mirror
                var record = new ScatterRecord(new Ray(p, reflectDir), F0, 1.0, true);
                record.type = ScatterRecord.ScatterType.SPECULAR;
                return record;
            } else {
                // glossy reflection: use metal-tinted F0 in the Phong BRDF
                Vector3d wi = samplePhongLobe(reflectDir, shininess, pixel);
                double pdf = phongPdf(wi, reflectDir, shininess);
                double cosi = Math.max(0, wi.dot(n));
                ColorDouble brdf = phongBrdf(wi, wo, reflectDir, F0, shininess);
                var attenuation = new ColorDouble(brdf);
                attenuation.scale(cosi / pdf);

                var record = new ScatterRecord(new Ray(p, wi), attenuation, pdf, false);
                record.type = ScatterRecord.ScatterType.SPECULAR;
                return record;
            }
        } else {
            // --- Diffuse lobe ---
            Vector3d wi = PathTracerHelper.getRandomCosineWeightedHemisphere(pixel.halton,n);
            double pdf = getPDF(hit,wi,wo);

            //var brdf = new ColorDouble(diffuse);
            //brdf.scale(1.0/Math.PI);
            //brdf.scale(cosTheta/pdf);
            //var record = new ScatterRecord(new Ray(p, wi), brdf, pdf, false);

            var attenuation = (pdf>0) ? new ColorDouble(diffuse) : new ColorDouble(0,0,0);

            var record = new ScatterRecord(new Ray(p, wi), attenuation, pdf, false);
            record.type = ScatterRecord.ScatterType.DIFFUSE;
            return record;
        }
    }

    /**
     * Sample a direction around the reflection axis using a Phong (cos^n) lobe.
     * @param axis unit reflection direction
     * @param shininess Phong exponent n (>=0)
     * @param pixel pixel for random number generation
     * @return sampled direction (unit)
     */
    private Vector3d samplePhongLobe(Vector3d axis, int shininess, RayXY pixel) {
        double n = Math.max(0, shininess);
        double u1 = pixel.halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_U);
        double u2 = pixel.halton.nextDouble(PathTracer.CHANNEL_HEMISPHERE_V);

        // Invert CDF for cos^n theta
        double cosTheta = Math.pow(u1, 1.0 / (n + 1.0));
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        double phi = 2.0 * Math.PI * u2;

        double x = sinTheta * Math.cos(phi);
        double y = sinTheta * Math.sin(phi);
        double z = cosTheta;

        // Build orthonormal basis around axis
        Vector3d w = new Vector3d(axis);
        w.normalize();
        Vector3d up = Math.abs(w.z) < 0.999 ? new Vector3d(0, 0, 1) : new Vector3d(0, 1, 0);
        Vector3d u = new Vector3d();
        u.cross(up, w);
        u.normalize();
        Vector3d v = new Vector3d();
        v.cross(w, u);

        Vector3d dir = new Vector3d();
        // local (x,y,z) -> world
        dir.scaleAdd(x, u, dir);
        dir.scaleAdd(y, v, dir);
        dir.scaleAdd(z, w, dir);
        dir.normalize();
        return dir;
    }

    /**
     * PDF of the Phong lobe used in samplePhongLobe.
     * @param wi sampled direction (unit)
     * @param axis reflection axis (unit)
     * @param shininess Phong exponent
     * @return pdf value
     */
    private double phongPdf(Vector3d wi, Vector3d axis, int shininess) {
        double n = Math.max(0, shininess);
        double cosAlpha = axis.dot(wi);
        if (cosAlpha <= 0.0) return 0.0;
        return (n + 1.0) * 0.5 / Math.PI * Math.pow(cosAlpha, n);
    }

    /**
     * Normalized Phong BRDF (specular term only).
     * f_r = Ks * (n+2)/(2π) * (r·wi)^n
     * @param wi incoming (next) direction
     * @param wo outgoing (view) direction (unused here but kept for interface completeness)
     * @param axis perfect reflection direction
     * @param specularColor specular tint
     * @param shininess Phong exponent
     * @return specular BRDF value
     */
    private ColorDouble phongBrdf(Vector3d wi, Vector3d wo, Vector3d axis, ColorDouble specularColor, int shininess) {
        double n = Math.max(0, shininess);
        double cosAlpha = axis.dot(wi);
        if (cosAlpha <= 0.0) return new ColorDouble(0,0,0);
        double factor = (n + 2.0) * 0.5 / Math.PI * Math.pow(cosAlpha, n);
        ColorDouble ks = new ColorDouble(specularColor);
        ks.scale(factor);
        return ks;
    }

    private double schlickFresnel(double cosTheta, ColorDouble specularColor) {
        // get max component of specular color
        double F0 = Math.max(specularColor.r, Math.max(specularColor.g, specularColor.b));
        return F0 + (1 - F0) * Math.pow((1 - cosTheta), 5);

        // Use Schlick's approximation for reflectance
        //double r0 = (1 - ref_idx) / (1 + ref_idx);
        //r0 = r0 * r0;
        //return r0 + (1 - r0) * Math.pow((1 - cosine), 5);
    }

    /**
     * Reflect a vector off a surface normal.
     *
     * @param v the incoming vector
     * @param n the surface normal
     * @return the reflected vector
     */
    private Vector3d reflect(Vector3d v, Vector3d n) {
        // r = v - 2(v·n)n
        Vector3d result = new Vector3d(v);
        Vector3d temp = new Vector3d(n);
        temp.scale(2.0 * v.dot(n));
        result.sub(temp);
        result.normalize();
        return result;
    }

    /**
     * Diffuse-only BRDF (f) for next-event (light) sampling.
     * Excludes specular (never light sampled) and transmission (delta).
     * Does NOT include the cosine term or any pdf factors.
     */
    public ColorDouble lightSamplingBRDF(Hit hit, Vector3d wi, Vector3d wo) {
        Vector3d n = hit.normal();
        double cosI = n.dot(wi);
        if (cosI <= 0.0) return new ColorDouble(0,0,0);

        // Base diffuse reflectance (optionally could sample texture)
        ColorDouble kd = new ColorDouble(getDiffuseColor());

        // Transmission weight encoded in alpha (as in scatter()).
        double wt = (1.0-kd.a);
        if (wt >= 0.999) return new ColorDouble(0,0,0); // effectively transparent: no surface shading

        // Pure Lambert BRDF: Kd / π (no cos term here)
        kd.scale(1.0 / Math.PI);

        return kd;
    }

    public TextureWithMetadata getTexture(int index) {
        if(index<0) throw new IllegalArgumentException("Invalid texture index: "+index);
        if(index>= TextureLayerIndex.values().length) throw new IllegalArgumentException("Invalid texture index: "+index);

        return textures.get(index);
    }

    public void setTexture(int index, TextureWithMetadata e) {
        textures.set(index, e);
    }

    public void use(GL3 gl3, ShaderProgram shaderProgram) {
        shaderProgram.setColor(gl3,"diffuseColor",this.getDiffuseColor());
        shaderProgram.setColor(gl3,"specularColor",this.getSpecularColor());
        shaderProgram.setColor(gl3,"emissionColor",this.getEmissionColor());
        shaderProgram.set1i(gl3,"useLighting",this.isLit() ? 1 : 0);
        shaderProgram.set1i(gl3,"shininess",this.getShininess());
        shaderProgram.set1f(gl3, "specularStrength", (float)this.getSpecularStrength()*10);
        // TODO add this settings for texture filters and apply them here.
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MIN_FILTER,GL3.GL_LINEAR);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MAG_FILTER,GL3.GL_LINEAR);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_S,GL3.GL_CLAMP_TO_BORDER);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_T,GL3.GL_CLAMP_TO_BORDER);
        gl3.glEnable(GL3.GL_TEXTURE_2D);

        // Iterates texture layers; binds textures to shader indices
        for(TextureLayerIndex tli : TextureLayerIndex.values()) {
            int i = tli.getIndex();
            // nvidia drivers may optimize a texture unit away, which causes this to fail.
            // To mitigate, we catch exceptions and only log the first occurrence per material-layer combination.
            // better would be to pre-check active texture units, but this is simpler for now.
            shaderProgram.set1i(gl3, tli.getName(), i);

            gl3.glActiveTexture(GL3.GL_TEXTURE0 + i);

            TextureWithMetadata tex = this.getTexture(i);
            if (tex != null) {
                tex.use(gl3, shaderProgram);
                gl3.glBindTexture(GL3.GL_TEXTURE_2D, tex.getTexture().getTextureObject());
            } else {
                gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
            }
        }
    }
}
