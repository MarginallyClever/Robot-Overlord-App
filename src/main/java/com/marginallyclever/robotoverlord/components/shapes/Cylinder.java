package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A cylinder with a radius of 0.5 and a height of 2. It is centered at the
 * origin.
 * TODO add texture coordinates
 */
public class Cylinder extends ShapeComponent implements PropertyChangeListener {
    public static final int RESOLUTION_CIRCULAR = 32;
    public static final int RESOLUTION_LENGTH = 5;

    public final DoubleParameter radius0 = new DoubleParameter("R0", 0.5f);
    public final DoubleParameter radius1 = new DoubleParameter("R1", 0.5f);
    public final DoubleParameter height = new DoubleParameter("Height", 2);

    public Cylinder() {
        this(2, 0.5f, 0.5f);
    }

    public Cylinder(double height, double radius0, double radius1) {
        super();

        myMesh = new Mesh();

        this.radius0.set(radius0);
        this.radius1.set(radius1);
        this.height.set(height);

        this.radius0.addPropertyChangeListener(this);
        this.radius1.addPropertyChangeListener(this);
        this.height.addPropertyChangeListener(this);

        updateModel();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(radius0.get()<0) radius0.set(0.0);
        if(radius1.get()<0) radius1.set(0.0);
        if(height.get()<0) height.set(0.0);
        updateModel();
    }

    private void updateModel() {
        myMesh.clear();
        myMesh.setRenderStyle(GL3.GL_TRIANGLES);

        addCylinder(height.get().floatValue(), radius0.get().floatValue(), radius1.get().floatValue());
        setModel(myMesh);
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
            myMesh.addVertex(0, 0, z);
            myMesh.addTexCoord(0.5f,0.5f);
            myMesh.addNormal(0, 0, sign);

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
        myMesh.addVertex(x*r,y*r,z);
        myMesh.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        myMesh.addNormal(0,0,sign);
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
        myMesh.addVertex(x*radius, y*radius, z);
        myMesh.addTexCoord(0.5f+x*0.5f,0.5f+y*0.5f);
        myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("radius0", radius0.get());
        json.put("radius1", radius1.get());
        json.put("height", height.get());
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("radius")) {
            radius0.set(jo.getDouble("radius"));
            radius1.set(jo.getDouble("radius"));
        } else {
            if(jo.has("radius0")) radius0.set(jo.getDouble("radius0"));
            if(jo.has("radius1")) radius1.set(jo.getDouble("radius1"));
        }
        if(jo.has("height")) height.set(jo.getDouble("height"));
    }
}
