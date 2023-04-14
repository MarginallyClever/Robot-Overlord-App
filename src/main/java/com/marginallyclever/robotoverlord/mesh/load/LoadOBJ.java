package com.marginallyclever.robotoverlord.mesh.load;

import com.marginallyclever.robotoverlord.mesh.Mesh;

import javax.vecmath.Vector3d;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Loads <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ files</a> into a Mesh.
 * @author Dan Royer
 * @since 1.6.0
 */
public class LoadOBJ implements MeshLoader {
	@Override
	public String getEnglishName() {
		return "Wavefront Object File (OBJ)";
	}
	
	@Override
	public String[] getValidExtensions() {
		return new String[]{"obj"};
	}
	
	@Override
	public void load(BufferedInputStream inputStream,Mesh model) throws Exception {
		ArrayList<Float> vertexArray = new ArrayList<Float>();
		ArrayList<Float> normalArray = new ArrayList<Float>();
		ArrayList<Float> texCoordArray = new ArrayList<Float>();

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		String line;
		while( ( line = br.readLine() ) != null ) {
			line = line.trim();
			if(line.startsWith("v ")) {
				// vertex
				String[] tokens = line.split("\\s+");
				vertexArray.add(Float.parseFloat(tokens[1]));
				vertexArray.add(Float.parseFloat(tokens[2]));
				vertexArray.add(Float.parseFloat(tokens[3]));
			} else if(line.startsWith("vn ")) {
				// normal - might not be unit length
				String[] tokens = line.split("\\s+");

				float x=Float.parseFloat(tokens[1]);
				float y=Float.parseFloat(tokens[2]);
				float z=Float.parseFloat(tokens[3]);
				Vector3d v = new Vector3d(x,y,z);
				float len = (float)v.length();
				if(len>0) {
					x/=len;
					y/=len;
					z/=len;
				}				
				normalArray.add(x);
				normalArray.add(y);
				normalArray.add(z);
			} else if(line.startsWith("vt ")) {
				// texture coordinate
				String[] tokens = line.split("\\s+");
				texCoordArray.add(Float.parseFloat(tokens[1]));
				texCoordArray.add(Float.parseFloat(tokens[2]));
			} else if(line.startsWith("f ")) {
				// face
				String[] tokens = line.split("\\s+");
				//Log.message("face len="+tokens.length);
				int index;
				for(int i=1;i<tokens.length;++i) {
					String [] subTokens = tokens[i].split("/");
					// vertex data
					index = Integer.parseInt(subTokens[0])-1;
					
					try {
						model.addVertex(
								vertexArray.get(index*3+0),
								vertexArray.get(index*3+1),
								vertexArray.get(index*3+2));
					} catch(Exception e) {
						e.printStackTrace();
					}
					// texture data (if any)
					if(subTokens.length>1 && subTokens[1].length()>0) {
						int indexT = Integer.parseInt(subTokens[1])-1;
						try {
							model.addTexCoord(
									texCoordArray.get(indexT*2+0),
									texCoordArray.get(indexT*2+1));
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					// normal data (if any)
					if(subTokens.length>2 && subTokens[2].length()>0) {
						int indexN = Integer.parseInt(subTokens[2])-1;
						try {
							model.addNormal(
									normalArray.get(indexN*3+0),
									normalArray.get(indexN*3+1),
									normalArray.get(indexN*3+2));
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
