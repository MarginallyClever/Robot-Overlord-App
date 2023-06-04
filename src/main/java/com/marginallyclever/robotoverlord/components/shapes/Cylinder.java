package com.marginallyclever.robotoverlord.components.shapes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.json.JSONException;
import org.json.JSONObject;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.SerializationContext;
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
        // Top face
        addTopFace(height, radius);

        // Bottom face
        addBottomFace(height, radius);

        // Body
        addTube(height, radius);
    }

    /**
     * @param height distance from the origin
     * @param radius      radius of the face
     */
    private void addTopFace(float height, float radius) {
        for (int i = 0; i < RESOLUTION_CIRCULAR * Math.min(1, height); ++i) {
            myMesh.addVertex(0, 0, height / 2);
            myMesh.addNormal(0, 0, 1);

            addTopCirclePoint(height, radius, i, RESOLUTION_CIRCULAR);
            addTopCirclePoint(height, radius, i + height, RESOLUTION_CIRCULAR);
        }
    }

    private void addBottomFace(float height, float radius) {
        for (int i = 0; i < RESOLUTION_CIRCULAR * Math.min(1, height); ++i) {
            myMesh.addVertex(0, 0, -1 * height / 2);
            myMesh.addNormal(0, 0, -1);

            addBottomCirclePoint(height, radius, i, RESOLUTION_CIRCULAR);
            addBottomCirclePoint(height, radius, i - height, RESOLUTION_CIRCULAR);
        }
    }

    /**
     * points on the end caps
     * @param height
     * @param radius
     * @param i
     * @param resolution
     */
    private void addTopCirclePoint(float height, float r, float i, int resolution) {
        double a = Math.PI * 2.0 * (double) i / (double) (resolution * Math.max(1, height));

        float x = (float) Math.cos(a) * r;
        float y = (float) Math.sin(a) * r;
        float z = height / 2;

        myMesh.addVertex(x, y, z);
        myMesh.addNormal(0, 0, 1);
    }

    private void addBottomCirclePoint(float height, float r, float i, int resolution) {
        double a = Math.PI * 2.0 * (double) i / (double) (resolution * Math.max(1, height));

        float x = (float) Math.cos(a) * r;
        float y = (float) Math.sin(a) * r;
        float z = -height / 2;

        myMesh.addVertex(x, y, z);
        myMesh.addNormal(0, 0, -1);
    }

    /**
     * @param height
     * @param radius
     */
    private void addTube(float height, float radius) {
        for (int i = 0; i < RESOLUTION_TUBULAR * Math.(1, height); ++i) {
            addTubeSegment(height, radius, i, RESOLUTION_TUBULAR);
        }
    }

    // the wall of the cylinder
    private void addTubeSegment(float height, float radius, int step, int resolution) {
        float z0 = (step) / (float) (resolution) - (height / 2);
        float z1 = (step + 1) / (float) (resolution) - (height / 2);

        for (int i = 0; i < RESOLUTION_CIRCULAR * height; ++i) {
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
        double a = Math.PI * 2.0 * (double) i / (double) (resolution * height);
        float x = (float) Math.cos(a);
        float y = (float) Math.sin(a);
        myMesh.addVertex(x * radius, y * radius, z);
        myMesh.addNormal(x, y, 0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateModel();
    }

    @Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		jo.put("height", height.toJSON(context));
		jo.put("radius", radius.toJSON(context));

		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
		super.parseJSON(jo, context);
		height.parseJSON(jo.getJSONObject("height"), context);
		radius.parseJSON(jo.getJSONObject("radius"), context);
	}
}
