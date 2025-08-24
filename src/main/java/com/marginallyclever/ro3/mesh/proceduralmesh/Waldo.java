package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;
import org.json.JSONObject;

import javax.vecmath.Point3d;

/**
 * <p>{@link Waldo} is a {@link Mesh} displays arrows starting at the origin and extending along each axis.
 * The red arrow indicates the +X axis.
 * The green arrow indicates the +Y axis.
 * The blue arrow indicates the +Z axis.</p>
 */
public class Waldo extends ProceduralMesh {
    public float getRadius() {
        return radius;
    }

    /**
     * Set the length of each axis arrow.
     * @param radius the length of each axis arrow.
     */
    public void setRadius(float radius) throws IllegalArgumentException {
        if(radius<=0) throw new IllegalArgumentException("Radius must be positive");
        this.radius = radius;
    }

    public float radius = 1;

    public Waldo() {
        super();
        this.setRenderStyle(GL3.GL_LINES);
        updateModel();
    }

    @Override
    public String getEnglishName() {
        return "Waldo";
    }

    /**
     * Draw a grid of lines in the current color
     */
    @Override
    public void updateModel() {
        this.clear();
        // x
        this.addColor(1,0,0,1);  this.addVertex(0,0,0);
        this.addColor(1,0,0,1);  this.addVertex(radius,0,0);
        // y
        this.addColor(0,1,0,1);  this.addVertex(0,0,0);
        this.addColor(0,1,0,1);  this.addVertex(0,radius,0);
        // z
        this.addColor(0,0,1,1);  this.addVertex(0,0,0);
        this.addColor(0,0,1,1);  this.addVertex(0,0,radius);

        boundingBox.setBounds(new Point3d(1,1,1),new Point3d(0,0,0));
        fireMeshChanged();
    }


    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        updateModel();
    }
}
