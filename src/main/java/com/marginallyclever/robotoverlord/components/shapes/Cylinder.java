package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

/**
 * A cylinder with a radius of 0.5 and a height of 1.  It is centered at the origin.
 * TODO add texture coordinates
 */
public class Cylinder extends ShapeComponent {
    public static final int RESOLUTION_CIRCULAR = 20;
    public static final int RESOLUTION_TUBULAR = 5;

    public Cylinder() {
        super();

        myMesh = new Mesh();
        updateModel();
        setModel(myMesh);
    }

    private void updateModel() {
        myMesh.clear();
        myMesh.setRenderStyle(GL2.GL_TRIANGLES);

        addFace(1);
        addFace(-1);
        addTube();
    }

    private void addFace(int z) {
        for (int i = 0; i < RESOLUTION_CIRCULAR; ++i) {
            myMesh.addVertex(0, 0, (float)z*0.5f);
            myMesh.addNormal(0, 0, z);

            addCirclePoint(i, RESOLUTION_CIRCULAR, z);
            addCirclePoint(i + z, RESOLUTION_CIRCULAR, z);
        }
    }

    // points on the end caps
    private void addCirclePoint(int i,int resolution,float z) {
        double a = Math.PI*2.0 * (double)i/(double)resolution;
        myMesh.addVertex((float)Math.cos(a)*0.5f,(float)Math.sin(a)*0.5f,z*0.5f);
        myMesh.addNormal(0,0,z);
    }

    private void addTube() {
        for (int i = 0; i < RESOLUTION_TUBULAR; ++i) {
            addTubeSegment(i, RESOLUTION_TUBULAR);
        }
    }

    // the wall of the cylinder
    private void addTubeSegment(int step, int resolution) {
        float z0 = (step  )/(float)resolution - 0.5f;
        float z1 = (step+1)/(float)resolution - 0.5f;

        for(int i = 0; i< RESOLUTION_CIRCULAR; ++i) {
            addTubePoint(i, RESOLUTION_CIRCULAR,z1);
            addTubePoint(i, RESOLUTION_CIRCULAR,z0);
            addTubePoint(i+1, RESOLUTION_CIRCULAR,z1);

            addTubePoint(i, RESOLUTION_CIRCULAR,z0);
            addTubePoint(i+1, RESOLUTION_CIRCULAR,z0);
            addTubePoint(i+1, RESOLUTION_CIRCULAR,z1);
        }
    }

    // points on the wall
    private void addTubePoint(int i,int resolution,float z) {
        double a = Math.PI*2.0 * (double)i/(double)resolution;
        float x = (float)Math.cos(a);
        float y = (float)Math.sin(a);
        myMesh.addVertex(x*0.5f,y*0.5f,z);
        myMesh.addNormal(x,y,0);
    }
}
