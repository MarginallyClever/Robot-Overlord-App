package com.marginallyclever.robotOverlord.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

/**
 * Smooth STL models and save them back to disk.  Meant for one time processing files.
 * @author dan royer
 */
public class ModelSmoother {/*
	public static void main(String[] argv) throws IllegalArgumentException, IOException {
		float vertexEpsilon = 0.1f;
		float normalEpsilon = 0.25f;
		
		if( argv.length     == 0 ) throw new IllegalArgumentException("not enough parameters");
		if( argv.length % 2 != 0 ) throw new IllegalArgumentException("not enough parameters");
		
		int i;
		for(i=0;i<argv.length;++i) {
			String sourceName = argv[i+0];
			String destName   = argv[i+1];
			saveModelToFile(sourceName,destName,vertexEpsilon,normalEpsilon);
		}
	}*/

	private static void saveModelToFile(String inName,String outName,float vertexEpsilon,float normalEpsilon) throws IOException {
		try {
			Model m = ModelFactory.createModelFromFilename(inName);
			smoothNormals(m,vertexEpsilon,normalEpsilon);
			
			File file = new File(outName);
			FileOutputStream fos = new FileOutputStream(file);
			//m.getModelLoadAndSave().saveToStreamAsBinary(fos);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void smoothAll() throws IOException {
		float vertexEpsilon = 0.1f;
		float normalEpsilon = 0.25f;
		String wd = System.getProperty("user.dir");
		System.out.println("Working directory="+wd);
		
		System.out.println("hand");			saveModelToFile("/AH/WristRot.stl",		wd + "/AH/WristRot-smooth.stl2",	vertexEpsilon,normalEpsilon);
		System.out.println("anchor");		saveModelToFile("/AH/rotBaseCase.stl",	wd + "/AH/rotBaseCase-smooth.stl2",	vertexEpsilon,normalEpsilon);
		System.out.println("shoulder");		saveModelToFile("/AH/Shoulder_r1.stl",	wd + "/AH/Shoulder_r1-smooth.stl2",	vertexEpsilon,normalEpsilon);
		System.out.println("elbow");		saveModelToFile("/AH/Elbow.stl",		wd + "/AH/Elbow-smooth.stl2",		vertexEpsilon,normalEpsilon);
		System.out.println("forearm");		saveModelToFile("/AH/Forearm.stl",		wd + "/AH/Forearm-smooth.stl2",		vertexEpsilon,normalEpsilon);
		System.out.println("wrist");		saveModelToFile("/AH/Wrist_r1.stl",		wd + "/AH/Wrist_r1-smooth.stl2",	vertexEpsilon,normalEpsilon);
	}
	

	/**
	 * Smooth normals.  Find points within vertexEpsilon of each other, sharing normals within normalEpsilon of each other, and then 
	 * @param vertexEpsilon
	 * @param normalEpsilon
	 */
	public static void smoothNormals(Model model,float vertexEpsilon,float normalEpsilon) {
		float vertexEpsilonSquared = vertexEpsilon * vertexEpsilon;
		float normalEpsilonSquared = normalEpsilon * normalEpsilon;

		int numVertexes = model.vertexArray.size();
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		boolean [] skip = new boolean[numVertexes];

		int i,j;
		for(i=0;i<numVertexes;++i) {
			if(skip[i]) continue;

			// find vertices that are in the same position
			float p1x = model.vertices.get(i*3+0);
			float p1y = model.vertices.get(i*3+1);
			float p1z = model.vertices.get(i*3+2);

			float n1x = model.normals.get(i*3+0);
			float n1y = model.normals.get(i*3+1);
			float n1z = model.normals.get(i*3+2);

			indexList.clear();
			indexList.add(i);
			
			for(j=i+1;j<numVertexes;++j) {
				if(skip[j]) continue;

				float p2x = model.vertices.get(j*3+0);
				float p2y = model.vertices.get(j*3+1);
				float p2z = model.vertices.get(j*3+2);
				if( lengthDifferenceSquared(p1x,p1y,p1z,p2x,p2y,p2z) <= vertexEpsilonSquared ) {

					float n2x = model.normals.get(j*3+0);
					float n2y = model.normals.get(j*3+1);
					float n2z = model.normals.get(j*3+2);
					if( lengthDifferenceSquared(n1x,n1y,n1z,n2x,n2y,n2z) <= normalEpsilonSquared ) {
						indexList.add(j);
					}
				}
			}
			
			if(indexList.size()>1) {
				n1x=0;
				n1y=0;
				n1z=0;

				int size = indexList.size();
				int k;
				for(k=0;k<size;++k) {
					j = indexList.get(k)*3;
					n1x += model.normals.get(j+0);
					n1y += model.normals.get(j+1);
					n1z += model.normals.get(j+2);
				}
				float len = length(n1x,n1y,n1z);
				n1x /= len;
				n1y /= len;
				n1z /= len;

				for(k=0;k<size;++k) {
					j = indexList.get(k);
					skip[j]=true;
					j*=3;
					model.normals.put(j+0, n1x);
					model.normals.put(j+1, n1y);
					model.normals.put(j+2, n1z);
				}
			}
		}
	}

	
	private static float lengthDifferenceSquared(float p1x,float p1y,float p1z,float p2x,float p2y,float p2z) {
		float dx = p2x-p1x;
		float dy = p2y-p1y;
		float dz = p2z-p1z;

		return lengthSquared(dx,dy,dz);
	}
	
	
	private static float lengthSquared(float dx,float dy,float dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	private static float length(float dx,float dy,float dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}
}
