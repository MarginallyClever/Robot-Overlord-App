package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * A box with a width, height, and length of 1.  It is centered around the origin.
 * TODO add texture coordinates
 */
public class Box extends ShapeComponent {
    public Box() {
        super();

        myMesh = new Mesh();
        updateModel();
        setModel(myMesh);
    }

    // Procedurally generate a list of triangles that form a box, subdivided by some amount.
    private void updateModel() {
        myMesh.clear();
        myMesh.renderStyle= GL2.GL_TRIANGLES;
        //shape.renderStyle=GL2.GL_LINES;  // set to see the wireframe

        float w = 0.5f;
        float d = 0.5f;
        float h = 0.5f;

        int wParts = 1;
        int hParts = 1;
        int dParts = 1;

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
            pA.set(MathHelper.interpolate(p0, p1, (double)(x  )/(double)xParts));
            pB.set(MathHelper.interpolate(p0, p1, (double)(x+1)/(double)xParts));
            pC.set(MathHelper.interpolate(p3, p2, (double)(x  )/(double)xParts));
            pD.set(MathHelper.interpolate(p3, p2, (double)(x+1)/(double)xParts));

            for(int y=0;y<yParts;y++) {
                pE.set(MathHelper.interpolate(pA, pC, (double)(y  )/(double)yParts));
                pF.set(MathHelper.interpolate(pB, pD, (double)(y  )/(double)yParts));
                pG.set(MathHelper.interpolate(pA, pC, (double)(y+1)/(double)yParts));
                pH.set(MathHelper.interpolate(pB, pD, (double)(y+1)/(double)yParts));

                if(myMesh.renderStyle == GL2.GL_TRIANGLES) {
                    for(int i=0;i<6;++i) {
                        myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
                    }
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
                } else if(myMesh.renderStyle == GL2.GL_LINES) {
                    myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

                    myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);

                    myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
                    myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
                }
            }
        }
    }
}
