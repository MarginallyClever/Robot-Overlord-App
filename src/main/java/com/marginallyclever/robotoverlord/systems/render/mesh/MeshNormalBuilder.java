package com.marginallyclever.robotoverlord.systems.render.mesh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;

/**
 * A tool to build normals for a mesh.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
public class MeshNormalBuilder {
	private static final Logger logger = LoggerFactory.getLogger(MeshNormalBuilder.class);

	public static void buildNormals(Mesh mesh) {
    	//logger.info("Building normals...");
		mesh.normalArray.clear();
		
		if(mesh.getHasIndexes()) buildSmoothIndexedNormals(mesh);
		else buildDirectNormals(mesh);
		
		mesh.setDirty(true);
    	//logger.info("Normals generated.");
	}
	
	// vertexes are not shared.  one normal per vertex, identical for all three points of a triangle.
	// assumes we are rendering GL_TRIANGLES only.
	private static void buildDirectNormals(Mesh mesh) {
    	for(int i=0;i<mesh.getNumVertices();i+=3) {
    		Vector3d n = buildNormalFromThreePoints(mesh,i,i+1,i+2);
    		
    		mesh.addNormal((float)n.x, (float)n.y, (float)n.z);
    		mesh.addNormal((float)n.x, (float)n.y, (float)n.z);
    		mesh.addNormal((float)n.x, (float)n.y, (float)n.z);
    	}
	}

	// vertexes and normals are shared.  a vertex may be part of many triangles.
	// the normal should therefore be a blend between each of these triangle faces.
	private static void buildSmoothIndexedNormals(Mesh mesh) {
		// make normal array same size as vertex array.
		int size = mesh.getNumVertices();
		
		Vector3d [] myNormals = new Vector3d[size];
		for(int i=0;i<size;++i) myNormals[i] = new Vector3d();
		
		// build all normals for all faces.
		logger.info("Find normals for every face...");
		logger.info("Averaging normals for all vertices...");
		for(int i=0;i<mesh.indexArray.size();i+=3) {
			int a = mesh.indexArray.get(i  );
			int b = mesh.indexArray.get(i+1);
			int c = mesh.indexArray.get(i+2);
    		Vector3d n = buildNormalFromThreePoints(mesh,a,b,c);
    		myNormals[a].add(n);
    		myNormals[b].add(n);
    		myNormals[c].add(n);
		}

		for( Vector3d n : myNormals ) {
			// this normal may have had several normals added to it.
			n.normalize();
			mesh.addNormal((float)n.x, (float)n.y, (float)n.z);
		}
	}

	private static Vector3d buildNormalFromThreePoints(Mesh m,int a,int b,int c) {
		Vector3d vA = m.getVertex(a);
		Vector3d vB = m.getVertex(b);
		Vector3d vC = m.getVertex(c);
        Vector3d nCA = new Vector3d();
        Vector3d nBA = new Vector3d();
        Vector3d n = new Vector3d();
        
		nCA.sub(vC,vA);
		nBA.sub(vB,vA);
		nCA.normalize();
		nBA.normalize();
		n.cross(nBA,nCA);
		n.normalize();

		return n;
	}
}
