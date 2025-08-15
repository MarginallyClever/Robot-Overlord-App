package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.ColorDouble;
import com.marginallyclever.ro3.apps.pathtracer.PathTracerHelper;
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
        if(rayHit.normal().dot(in)<=0) {
            // back facing, no light
            return new ColorDouble(0,0,0);
        }

        // lambertian diffuse BRDF
        ColorDouble a = new ColorDouble(getDiffuseColor());
        a.scale(1.0/ Math.PI); // diffuse BRDF
        return a;
    }

    public double getProbableDistributionFunction(RayHit rayHit, Vector3d in, Vector3d out) {
        double cosTheta = rayHit.normal().dot(in);
        if( cosTheta <= 0 ) return 0.0;

        return cosTheta / Math.PI;
    }

    public ScatterRecord scatter(Ray ray, RayHit hitRecord, Random random) {
        Vector3d newDirection = PathTracerHelper.getRandomCosineWeightedHemisphere(random, hitRecord.normal());
        double p = 1.0/ 2.0 * Math.PI; // cosine weighted hemisphere
        double cosTheta = hitRecord.normal().dot(newDirection);
        ColorDouble attenuation = BRDF(hitRecord,newDirection,ray.getDirection());
        attenuation.scale(cosTheta);
        return new ScatterRecord(newDirection,p,attenuation);
    }
}
