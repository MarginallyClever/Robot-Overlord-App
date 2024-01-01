package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * A cylinder with a radius of 0.5 and a height of 2. It is centered at the
 * origin.
 */
public class Cylinder extends Mesh {
    public static final int RESOLUTION_CIRCULAR = 32;
    public static final int RESOLUTION_LENGTH = 5;

    public float radius0 = 0.5f;
    public float radius1 = 0.5f;
    public float height = 2;

    public Cylinder() {
        this(2, 0.5f, 0.5f);
    }

    public Cylinder(double height, double radius0, double radius1) {
        super();

        this.radius0 = (float)radius0;
        this.radius1 = (float)radius1;
        this.height = (float)height;
        updateModel();
    }

    private void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLES);
        addCylinder(height, radius0, radius1);
    }

    private void addCylinder(float height, float radius0,float radius1) {
        float halfHeight = height / 2;
        if(radius0>0) addFace(-halfHeight, radius0);
        if(radius1>0) addFace(halfHeight, radius1);
        addTube(-halfHeight, halfHeight, radius0,radius1);
    }

    private void addFace(float z, float r) {
        float sign = z > 0 ? 1 : -1;
        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            this.addVertex(0, 0, z);
            this.addTexCoord(0.5f,0.5f);
            this.addNormal(0, 0, sign);

            addCirclePoint(r, i, RESOLUTION_CIRCULAR, z);
            addCirclePoint(r, i + sign, RESOLUTION_CIRCULAR, z);
        }
    }

    // points on the end caps
    private void addCirclePoint(float r, float i, int resolution, float z) {
        float sign = z > 0 ? 1 : -1;
        double a = MathHelper.interpolate(0,Math.PI*2.0, (double)i/(double)resolution);
        float x = (float)Math.cos(a);
        float y = (float)Math.sin(a);
        this.addVertex(x*r,y*r,z);
        this.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        this.addNormal(0,0,sign);
    }

    private void addTube(float h0, float h1, float r0, float r1) {
        float rStart = r0;
        float hStart = h0;

        float diff = (r0-r1)/(h1-h0);

        for (int i = 0; i < RESOLUTION_LENGTH; ++i) {
            float rEnd = MathHelper.interpolate(r0, r1, (double)(i+1) / (double)RESOLUTION_LENGTH);
            float hEnd = MathHelper.interpolate(h0, h1, (double)(i+1) / (double)RESOLUTION_LENGTH);
            addTubeSegment(hStart,hEnd,rStart,rEnd, diff);
            rStart = rEnd;
            hStart = hEnd;
        }
    }

    // the wall of the cylinder
    private void addTubeSegment(float z0,float z1,float r0, float r1, float diff) {
        for(int i = 0; i< RESOLUTION_CIRCULAR; ++i) {
            addTubePoint(diff,r1, i, z1);
            addTubePoint(diff,r0, i, z0);
            addTubePoint(diff,r1, i+1, z1);

            addTubePoint(diff,r0, i, z0);
            addTubePoint(diff,r0, i+1, z0);
            addTubePoint(diff,r1, i+1, z1);
        }
    }

    // points on the wall
    private void addTubePoint(float diff,float radius, int i,float z) {
        double a = Math.PI*2.0 * (double)i/(double)RESOLUTION_CIRCULAR;
        float x = (float)Math.cos(a);
        float y = (float)Math.sin(a);
        Vector3d n = new Vector3d(x,y,diff);
        n.normalize();
        this.addVertex(x*radius, y*radius, z);
        this.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        this.addNormal((float)n.x, (float)n.y, (float)n.z);
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
