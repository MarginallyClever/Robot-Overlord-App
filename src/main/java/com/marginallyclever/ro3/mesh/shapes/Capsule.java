package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link Capsule} is a {@link Mesh}.  It is a cylinder with rounded ends.</p>
 */
public class Capsule extends ProceduralMesh {
    public static final int RESOLUTION_CIRCULAR = 16;
    public static final int RESOLUTION_LENGTH = 4;
    public static final int RESOLUTION_DOME = 8;

    public float radius = 0.5f;
    public float height = 1;

    public Capsule() {
        this(2, 0.5f);
    }

    public Capsule(double height, double radius) {
        super();
        if(radius<=0) throw new IllegalArgumentException("radius must be greater than zero.");
        if(height<=0) throw new IllegalArgumentException("height must be greater than zero.");
        if(height<radius*2) throw new IllegalArgumentException("height must be greater than radius*2.");
        this.radius = (float)radius;
        this.height = (float)height;
        updateModel();
    }

    @Override
    public String getEnglishName() {
        return "Capsule";
    }

    @Override
    public void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLES);

        float h = height - radius*2;
        float h2=h/2;

        List<Vector3d> points = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();
        double pi2 = Math.PI/2.0;
        double doneAngleStep = pi2/RESOLUTION_DOME;

        // consider a line that runs from the North Pole, around 1/4 of the circle,
        // down the cylinder, and around the second 1/4 circle to the South Pole.
        for(int i=0;i<=RESOLUTION_DOME;++i) {
            double angle = i * doneAngleStep;
            double z = Math.cos(angle);
            double x = Math.sin(angle);
            points.add(new Vector3d(x*radius,0,z*radius+h2));
            normals.add(new Vector3d(x,0,z));
        }

        for(int i=0;i<=RESOLUTION_LENGTH;++i) {
            points.add(new Vector3d(radius,0,h2 - i*h/RESOLUTION_LENGTH));
            normals.add(new Vector3d(1,0,0));
        }

        for(int i=0;i<=RESOLUTION_DOME;++i) {
            double angle = i * doneAngleStep + pi2;
            double z = Math.cos(angle);
            double x = Math.sin(angle);
            points.add(new Vector3d(x*radius,0,z*radius-h2));
            normals.add(new Vector3d(x,0,z));
        }

        // North Pole
        addVertex(0,0,radius+h2);
        addNormal(0,0,1);
        // South Pole
        addVertex(0,0,-radius-h2);
        addNormal(0,0,-1);

        double circleAngleStep = Math.PI*2/RESOLUTION_CIRCULAR;
        // now that we have one line, rotate it around the Z axis to make the rest of the cylinder.
        for(int i=0;i<RESOLUTION_CIRCULAR;++i) {
            double angle = i * circleAngleStep;
            double c = Math.cos(angle);
            double s = Math.sin(angle);
            for(int j=0;j<points.size();++j) {
                Vector3d p = points.get(j);
                Vector3d n = normals.get(j);
                addVertex((float)(p.x*c - p.y*s), (float)(p.x*s + p.y*c), (float)p.z);
                addNormal((float)(n.x*c - n.y*s), (float)(n.x*s + n.y*c), (float)n.z);
            }
        }

        // generate indexes for triangles.
        int totalVerticesPerLayer = points.size(); // north dome, cylinder body, south dome

        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            // poles + domes + cylinder
            int off0 = 2 + i * totalVerticesPerLayer;
            // one "row" over.
            int off1 = 2 + ((i + 1) % RESOLUTION_CIRCULAR) * totalVerticesPerLayer;
            for (int j = 0; j < totalVerticesPerLayer - 1; ++j) {
                int current = off0 + j;
                int next = current + 1;
                int above = off1 + j;
                int aboveNext = above + 1;

                if(j==0) {
                    addIndex(0);
                    addIndex(current);
                    addIndex(above);
                }
                // Create two triangles for each quadrilateral
                addIndex(current);
                addIndex(next);
                addIndex(above);

                addIndex(next);
                addIndex(aboveNext);
                addIndex(above);

                if(j==totalVerticesPerLayer-2) {
                    addIndex(current);
                    addIndex(next);
                    addIndex(1);
                }
            }
        }

        fireMeshChanged();
    }
/*
    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("radius0", radius0);
        json.put("radius1", radius1);
        json.put("height", height);
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("radius")) {
            radius0 = (jo.getDouble("radius"));
            radius1 = (jo.getDouble("radius"));
        } else {
            if(jo.has("radius0")) radius0 = (jo.getDouble("radius0"));
            if(jo.has("radius1")) radius1 = (jo.getDouble("radius1"));
        }
        if(jo.has("height")) height = (jo.getDouble("height"));
    }
*/
}
