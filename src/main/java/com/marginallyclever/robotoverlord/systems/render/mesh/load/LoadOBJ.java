package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Vector3d;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
	public void load(BufferedInputStream inputStream, Mesh model) throws Exception {
		ArrayList<Float> vertexArray = new ArrayList<>();
		ArrayList<Float> normalArray = new ArrayList<>();
		ArrayList<Float> texCoordArray = new ArrayList<>();

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
				//logger.info("face len="+tokens.length);
				int index;
				for(int i=1;i<tokens.length;++i) {
					String [] subTokens = tokens[i].split("/");
					// vertex data
					index = Integer.parseInt(subTokens[0])-1;
					
					try {
						model.addVertex(
								vertexArray.get(index * 3),
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
									texCoordArray.get(indexT * 2),
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
									normalArray.get(indexN * 3),
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

	/**
	 * Does this loader find a material file near the mesh file?
	 * @param absolutePath path to mesh file
	 * @return true if a material file is found
	 */
	@Override
	public boolean hasMaterial(String absolutePath) {
		// replace extension of absolutePath with .mtl
		absolutePath = getMaterialPath(absolutePath);
		// check if file exists
		File test = new File(absolutePath);
		return test.exists();
	}

	/**
	 * Get the path to the material file
	 * @param absolutePath path to mesh file
	 * @return path to material file or null.
	 */
	public String getMaterialPath(String absolutePath) {
		if(absolutePath==null || absolutePath.trim().isEmpty()) return null;
		return absolutePath.substring(0,absolutePath.lastIndexOf('.'))+".mtl";
	}
}
