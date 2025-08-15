package com.marginallyclever.ro3.node.nodes.pose.poses.space;

import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.ProceduralMeshFactory;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * Represents a celestial body - star, planet, moon, etc.  Celestial bodies are assumed to be so large that the
 * player can't alter their course.  As such their movement should be deterministic and predictable no matter the
 * time step.
 */
public class CelestialBody extends Pose {
    public double mass;  // 10^24 kg
    public double radius;  // km
    public double rotationalPeriod;  // hours
    public double perihelion;  // 10^6 km
    public double aphelion;  // 10^6 km
    public double orbitalPeriod;  // days
    public double orbitalVelocity;  // km/s
    public double orbitalInclination;  // degrees
    public double orbitalEccentricity;  // 0=circle, 1=parabola
    public double obliquityToOrbit; // degrees

    public CelestialBody() {
        this("CelestialBody");

        var mi = new MeshInstance();
        this.addChild(mi);
        Mesh m = ProceduralMeshFactory.createMesh("Sphere");
        mi.setMesh(m);
        var mat = new Material();
        this.addChild(mat);
    }

    public CelestialBody(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new CelestialBodyPanel(this));
        super.getComponents(list);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-planet-16.png")));
    }

    public void updateSize() {
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();

        json.put("mass",mass);
        json.put("radius",radius);
        json.put("rotationalPeriod",rotationalPeriod);
        json.put("perihelion",perihelion);
        json.put("aphelion",aphelion);
        json.put("orbitalPeriod",orbitalPeriod);
        json.put("orbitalVelocity",orbitalVelocity);
        json.put("orbitalInclination",orbitalInclination);
        json.put("orbitalEccentricity",orbitalEccentricity);
        json.put("obliquityToOrbit",obliquityToOrbit);

        return json;    
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("mass")) mass = toJSON().getDouble("mass");
        if(json.has("radius")) radius = toJSON().getDouble("radius");
        if(json.has("rotationalPeriod")) rotationalPeriod = toJSON().getDouble("rotationalPeriod");
        if(json.has("perihelion")) perihelion = toJSON().getDouble("perihelion");
        if(json.has("aphelion")) aphelion = toJSON().getDouble("aphelion");
        if(json.has("orbitalPeriod")) orbitalPeriod = toJSON().getDouble("orbitalPeriod");
        if(json.has("orbitalVelocity")) orbitalVelocity = toJSON().getDouble("orbitalVelocity");
        if(json.has("orbitalInclination")) orbitalInclination = toJSON().getDouble("orbitalInclination");
        if(json.has("orbitalEccentricity")) orbitalEccentricity = toJSON().getDouble("orbitalEccentricity");
        if(json.has("obliquityToOrbit")) obliquityToOrbit = toJSON().getDouble("obliquityToOrbit");
    }
}
