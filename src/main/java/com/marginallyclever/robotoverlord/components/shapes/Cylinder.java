package com.marginallyclever.robotoverlord.components.shapes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

/**
 * A cylinder with a radius of 0.5 and a height of 1. It is centered at the
 * origin.
 * TODO add texture coordinates
 */
public class Cylinder extends ShapeComponent implements PropertyChangeListener {
    public static final int RESOLUTION_CIRCULAR = 20;
    public static final int RESOLUTION_TUBULAR = 5;

    public final DoubleParameter radius = new DoubleParameter("Radius", 0.5f);
    public final DoubleParameter height = new DoubleParameter("Height", 2);

    public Cylinder() {
        super();

        myMesh = new Mesh();
        updateModel();
        setModel(myMesh);

        radius.addPropertyChangeListener(this);
        height.addPropertyChangeListener(this);
    }

    private void updateModel() {
        myMesh.clear();
        myMesh.setRenderStyle(GL2.GL_TRIANGLES);

        addCylinder(height.get().floatValue(), radius.get().floatValue());
    }

    private void addCylinder(float height, float radius) {
        addFace(height, radius);
        addFace(-height, radius);
        addTube(height, radius);
    }

    /**
     * @param z distance from the origin
     * @param r radius of the face
     */
    private void addFace(float distance, float r) {
        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            myMesh.addVertex(0, 0, distance * 0.5f);
            myMesh.addNormal(0, 0, distance);

            addCirclePoint(r, i, RESOLUTION_CIRCULAR, distance);
            addCirclePoint(r, i + distance, RESOLUTION_CIRCULAR, distance);
        }
    }

    /**
     * points on the end caps
     * @param r
     * @param i
     * @param resolution
     * @param distance
     */
    private void addCirclePoint(float r, float i, int resolution, float distance) {
        double a = Math.PI * 2.0 * (double) i / (double) resolution;

        float x = (float) Math.cos(a) *   0.5f;
        float y = (float) Math.sin(a) *   0.5f;
        float z = distance * 0.5f;

        myMesh.addVertex(x, y, z);
        myMesh.addNormal(0, 0, distance);
    }

    private void addTube(float height, float radius) {
        for (int i = 0; i < RESOLUTION_TUBULAR; ++i) {
            addTubeSegment(height, radius, i, RESOLUTION_TUBULAR);
        }
    }

    // the wall of the cylinder
    private void addTubeSegment(float height, float radius, int step, int resolution) {
        float z0 = (step) / (float) resolution - radius;
        float z1 = (step + 1) / (float) resolution - radius;

        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            addTubePoint(height, radius, i, RESOLUTION_CIRCULAR, z1);
            addTubePoint(height, radius, i, RESOLUTION_CIRCULAR, z0);
            addTubePoint(height, radius, i + 1, RESOLUTION_CIRCULAR, z1);

            addTubePoint(height, radius, i, RESOLUTION_CIRCULAR, z0);
            addTubePoint(height, radius, i + 1, RESOLUTION_CIRCULAR, z0);
            addTubePoint(height, radius, i + 1, RESOLUTION_CIRCULAR, z1);
        }
    }

    // points on the wall
    private void addTubePoint(float height, float radius, int i, int resolution, float z) {
        double a = Math.PI * 2.0 * (double) i / (double) resolution;
        float x = (float) Math.cos(a);
        float y = (float) Math.sin(a);
        myMesh.addVertex(x * radius, y * radius, z);
        myMesh.addNormal(x, y, 0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateModel();
    }
}
