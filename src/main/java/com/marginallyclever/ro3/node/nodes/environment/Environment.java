package com.marginallyclever.ro3.node.nodes.environment;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Environment controls external factors like the sun, the skybox, etc.
 */
public class Environment extends Node {
    public final Vector3d sunlightSource = new Vector3d(50,150,750);  // vector
    public static final double SUN_DISTANCE = 200;
    public Color sunlightColor = new Color(0xfd,0xfb,0xd3,255);
    public double sunlightStrength = 1;
    public Color ambientColor = new Color(0x20,0x20,0x20,255);
    private double declination = 0;  // degrees, +/-90
    private double timeOfDay = 12;  // 0-24

    public Environment() {
    super("Environment");
  }

    public Environment(String name) {
        super(name);
        sunlightSource.set(calculateSunPosition());
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("declination", declination);
        json.put("timeOfDay", timeOfDay);
        json.put("sunlightColor", sunlightColor.getRGB());
        json.put("sunlightStrength", sunlightStrength);
        json.put("ambientColor", ambientColor.getRGB());
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
        sunlightSource.set(calculateSunPosition());
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
        sunlightSource.set(calculateSunPosition());
    }

    /**
     * @return 0..24 (hours)
     */
    public double getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(double timeOfDay) {
        this.timeOfDay = timeOfDay;
        sunlightSource.set(calculateSunPosition());
    }

    private Vector3d calculateSunPosition() {
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
}
