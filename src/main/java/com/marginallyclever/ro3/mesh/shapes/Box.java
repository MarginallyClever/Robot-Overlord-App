package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * A box with a width, height, and length of 1.  It is centered around the origin.
 */
public class Box extends Mesh {
    public float width = 1.0f;
    public float height = 1.0f;
    public float length = 1.0f;

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

        this.width = (float)width;
        this.length = (float)length;
        this.height = (float)height;

        updateModel();
    }

    // Procedurally generate a list of triangles that form a box, subdivided by some amount.
    private void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLES);
        //shape.renderStyle=GL3.GL_LINES;  // set to see the wireframe

        float w = width*0.5f;
        float d = length*0.5f;
        float h = height*0.5f;

        int wParts = (int)Math.ceil(w);
        int hParts = (int)Math.ceil(h);
        int dParts = (int)Math.ceil(d);

        Vector3d n=new Vector3d();
        Vector3d p0=new Vector3d();
        Vector3d p1=new Vector3d();
        Vector3d p2=new Vector3d();
        Vector3d p3=new Vector3d();

        // top
        n.set(0, 0, 1);
        p0.set( w, d,h);
        p1.set(-w, d,h);
        p2.set(-w,-d,h);
        p3.set( w,-d,h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

        // bottom
        n.set( 0, 0,-1);
        p0.set( -w, d,-h);
        p1.set(  w, d,-h);
        p2.set(  w,-d,-h);
        p3.set( -w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

        // sides
        n.set(0, 1, 0);
        p0.set(-w, d,h);
        p1.set( w, d,h);
        p2.set( w, d,-h);
        p3.set(-w, d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

        n.set(0,-1, 0);
        p0.set( w,-d,h);
        p1.set(-w,-d,h);
        p2.set(-w,-d,-h);
        p3.set( w,-d,-h);
        addSubdividedPlane(n,p0,p1,p2,p3,(int)(w/10),hParts);

        n.set(1, 0, 0);
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

            pA = MathHelper.interpolate(p0, p1, x0);
            pB = MathHelper.interpolate(p0, p1, x1);
            pC = MathHelper.interpolate(p3, p2, x0);
            pD = MathHelper.interpolate(p3, p2, x1);

            for(int y=0;y<yParts;y++) {
                float y0 = (float)y/(float)yParts;
                float y1 = (float)(y+1)/(float)yParts;

                pE = MathHelper.interpolate(pA, pC, y0);
                pF = MathHelper.interpolate(pB, pD, y0);
                pG = MathHelper.interpolate(pA, pC, y1);
                pH = MathHelper.interpolate(pB, pD, y1);

                if(this.getRenderStyle() == GL3.GL_TRIANGLES) {
                    for(int i=0;i<6;++i) {
                        this.addNormal((float)n.x, (float)n.y, (float)n.z);
                        this.addColor(1,1,1,1);
                    }
                    this.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    this.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    this.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    this.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    this.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    this.addVertex((float)pG.x, (float)pG.y, (float)pG.z);

                    this.addTexCoord(0,0);
                    this.addTexCoord(1,0);
                    this.addTexCoord(1,1);

                    this.addTexCoord(0,0);
                    this.addTexCoord(1,1);
                    this.addTexCoord(0,1);
                } else if(this.getRenderStyle() == GL3.GL_LINES) {
                    for(int i=0;i<8;++i) {
                        this.addNormal((float)n.x, (float)n.y, (float)n.z);
                        this.addColor(1,1,1,1);
                    }
                    this.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    this.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    this.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    this.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

                    this.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    this.addVertex((float)pG.x, (float)pG.y, (float)pG.z);

                    this.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
                    this.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

                    this.addTexCoord(x1,y0);//f
                    this.addTexCoord(x1,y1);//h

                    this.addTexCoord(x1,y1);//h
                    this.addTexCoord(x0,y0);//e

                    this.addTexCoord(x1,y1);//h
                    this.addTexCoord(x1,y1);//g

                    this.addTexCoord(x1,y1);//g
                    this.addTexCoord(x0,y0);//e
                }
            }
        }
    }
/*
    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("width", width);
        json.put("length", length);
        json.put("height", height);
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("width")) width = jo.getDouble("width"));
        if(jo.has("length")) length = jo.getDouble("length"));
        if(jo.has("height")) height = jo.getDouble("height"));
    }
*/
}
