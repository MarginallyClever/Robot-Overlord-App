package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import org.json.JSONObject;

import javax.vecmath.Vector3d;

/**
 * {@link Box} is a {@link Mesh} with a width, height, and length of 1.  It is centered around the origin.
 */
public class Box extends ProceduralMesh {
    public double width;
    public double height;
    public double length;

    public Box() {
        this(1.0,1.0,1.0);
    }

    /**
     * Create a box with the given dimensions.
     * @param width width of the box (x)
     * @param length length of the box (y)
     * @param height height of the box (z)
     */
    public Box(double width,double length,double height) {
        super();

        this.setRenderStyle(GL3.GL_TRIANGLES);
        this.width = width;
        this.length = length;
        this.height = height;

        updateModel();
    }

    @Override
    public String getEnglishName() {
        return "Box";
    }

    @Override
    public void updateModel() {
        this.clear();

        double w = width*0.5f;
        double d = length*0.5f;
        double h = height*0.5f;

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
        addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

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

        fireMeshChanged();
    }

    /**
     * Subdivide a plane into triangles.
     * <pre>
     *     x0    x1
     * p0--A-----B--p1
     * |   |     |   |
     * |   E-----F   | y0
     * |   |     |   |
     * |   G-----H   | y1
     * |   |     |   |
     * p2--C-----D--p3
     * </pre>
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

        Vector3d pA, pB, pC, pD, pE, pF, pG, pH;

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

                    this.addTexCoord(x0,y0);
                    this.addTexCoord(x1,y0);
                    this.addTexCoord(x1,y1);

                    this.addTexCoord(x0,y0);
                    this.addTexCoord(x1,y1);
                    this.addTexCoord(x0,y1);
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

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("width", width);
        json.put("length", length);
        json.put("height", height);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("width")) width = from.getDouble("width");
        if(from.has("length")) length = from.getDouble("length");
        if(from.has("height")) height = from.getDouble("height");
        updateModel();
    }
}
