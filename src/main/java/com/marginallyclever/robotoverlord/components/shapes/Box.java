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
 * A box with a width, height, and length of 1.  It is centered around the origin.
 * TODO add texture coordinates
 */
public class Box extends ShapeComponent implements PropertyChangeListener {
    public final DoubleParameter width = new DoubleParameter("width",1.0);
    public final DoubleParameter height = new DoubleParameter("height",1.0);
    public final DoubleParameter length = new DoubleParameter("length",1.0);

    public Box() {
        this(1.0,1.0,1.0);
    }

    /**
     * Create a box with the given dimensions.
     * @param width
     * @param length
     * @param height
     */
    public Box(double width,double length,double height) {
        super();

        myMesh = new Mesh();

        this.width.set(width);
        this.length.set(length);
        this.height.set(height);

        this.width.addPropertyChangeListener(this);
        this.length.addPropertyChangeListener(this);
        this.height.addPropertyChangeListener(this);

        updateModel();
    }

    // Procedurally generate a list of triangles that form a box, subdivided by some amount.
    private void updateModel() {
        myMesh.clear();
        myMesh.setRenderStyle(GL3.GL_TRIANGLES);
        //shape.renderStyle=GL3.GL_LINES;  // set to see the wireframe

        float w = width.get().floatValue()*0.5f;
        float d = length.get().floatValue()*0.5f;
        float h = height.get().floatValue()*0.5f;

        int wParts = (int)Math.ceil(w);
        int hParts = (int)Math.ceil(h);
        int dParts = (int)Math.ceil(d);

        Vector3d n=new Vector3d();
        Vector3d p0=new Vector3d();
        Vector3d p1=new Vector3d();
        Vector3d p2=new Vector3d();
        Vector3d p3=new Vector3d();

        // top
        n.set( 0, 0, 1);
        p0.set( w, d,h);
        p1.set(-w, d,h);
        p2.set(-w,-d,h);
        p3.set( w,-d,h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

        // bottom
        n.set( 0, 0,-1);
        p0.set(-w, d,-h);
        p1.set( w, d,-h);
        p2.set( w,-d,-h);
        p3.set(-w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

        // sides
        n.set( 0, 1, 0);
        p0.set(-w, d,h);
        p1.set( w, d,h);
        p2.set( w, d,-h);
        p3.set(-w, d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

        n.set( 0,-1, 0);
        p0.set( w,-d,h);
        p1.set(-w,-d,h);
        p2.set(-w,-d,-h);
        p3.set( w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,(int)(w/10),hParts);

        n.set( 1, 0, 0);
        p0.set( w, d,-h);
        p1.set( w, d,h);
        p2.set( w,-d,h);
        p3.set( w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);

        n.set(-1, 0, 0);
        p0.set(-w,-d,h);
        p1.set(-w, d,h);
        p2.set(-w, d,-h);
        p3.set(-w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);

        setModel(myMesh);
    }

    /**
     * Subdivide a plane into triangles.
     * @param n plane normal
     * @param p0 northwest corner
     * @param p1 northeast corner
     * @param p2 southeast corner
     * @param p3 southwest corner
     * @param xParts east/west divisions
     * @param yParts north/south divisions
     */
    private void addSubdividedPlane(Vector3d n,
                                    Vector3d p0,
                                    Vector3d p1,
                                    Vector3d p2,
                                    Vector3d p3,
                                    int xParts,
                                    int yParts) {
        xParts = Math.max(xParts, 1);
        yParts = Math.max(yParts, 1);

        Vector3d pA=new Vector3d();
        Vector3d pB=new Vector3d();
        Vector3d pC=new Vector3d();
        Vector3d pD=new Vector3d();
        Vector3d pE=new Vector3d();
        Vector3d pF=new Vector3d();
        Vector3d pG=new Vector3d();
        Vector3d pH=new Vector3d();

        for(int x=0;x<xParts;x++) {
            float x0 = (float)x/(float)xParts;
            float x1 = (float)(x+1)/(float)xParts;

            pA.set(MathHelper.interpolate(p0, p1, x0));
            pB.set(MathHelper.interpolate(p0, p1, x1));
            pC.set(MathHelper.interpolate(p3, p2, x0));
            pD.set(MathHelper.interpolate(p3, p2, x1));

            for(int y=0;y<yParts;y++) {
                float y0 = (float)y/(float)yParts;
                float y1 = (float)(y+1)/(float)yParts;

                pE.set(MathHelper.interpolate(pA, pC, y0));
                pF.set(MathHelper.interpolate(pB, pD, y0));
                pG.set(MathHelper.interpolate(pA, pC, y1));
                pH.set(MathHelper.interpolate(pB, pD, y1));

                if(myMesh.getRenderStyle() == GL3.GL_TRIANGLES) {
                    for(int i=0;i<6;++i) {
                        myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
                        myMesh.addColor(1,1,1,1);
                    }
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);

                    myMesh.addTexCoord(0,0);
                    myMesh.addTexCoord(1,0);
                    myMesh.addTexCoord(1,1);

                    myMesh.addTexCoord(0,0);
                    myMesh.addTexCoord(1,1);
                    myMesh.addTexCoord(0,1);
                } else if(myMesh.getRenderStyle() == GL3.GL_LINES) {
                    for(int i=0;i<8;++i) {
                        myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
                        myMesh.addColor(1,1,1,1);
                    }
                    myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);

                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

                    myMesh.addTexCoord(x1,y0);//f
                    myMesh.addTexCoord(x1,y1);//h

                    myMesh.addTexCoord(x1,y1);//h
                    myMesh.addTexCoord(x0,y0);//e

                    myMesh.addTexCoord(x1,y1);//h
                    myMesh.addTexCoord(x1,y1);//g

                    myMesh.addTexCoord(x1,y1);//g
                    myMesh.addTexCoord(x0,y0);//e
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateModel();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("width", width.get());
        json.put("length", length.get());
        json.put("height", height.get());
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("width")) width.set(jo.getDouble("width"));
        if(jo.has("length")) length.set(jo.getDouble("length"));
        if(jo.has("height")) height.set(jo.getDouble("height"));
    }
}
