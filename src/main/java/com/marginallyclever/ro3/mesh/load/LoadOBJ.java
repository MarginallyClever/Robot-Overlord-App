package com.marginallyclever.ro3.mesh.load;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@link LoadOBJ} is a {@link MeshLoader} that loads a
 * <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ files</a> into a {@link Mesh}.</p>
 */
public class LoadOBJ implements MeshLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadOBJ.class);

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
		Map<String,OBJMaterial> materials = new HashMap<>();
		OBJMaterial currentMaterial = null;

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String line;
		while( ( line = br.readLine() ) != null ) {
			line = line.trim();
			if(line.startsWith("mtllib ")) {
				try {
					// material library

					// get the path from model.getSourceName() aka remove the filename at the end.
					String path = model.getSourceName();
					path = path.substring(0,path.lastIndexOf(File.separator)+1) + line.substring(7);
					materials.putAll(loadMaterialLibrary(path));
				} catch(Exception e) {
					logger.warn("Error loading material: {}",e.getMessage());
				}
			}
			if(line.startsWith("usemtl ")) {
				// change material choice
				String name = line.substring(7);
				currentMaterial = materials.get(name);
			}
			if(line.startsWith("g ")) {
				// new body
			}
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
								vertexArray.get(index*3  ),
								vertexArray.get(index*3+1),
								vertexArray.get(index*3+2));
						if(currentMaterial!=null) {
							model.addColor(
									currentMaterial.diffuse[0],
									currentMaterial.diffuse[1],
									currentMaterial.diffuse[2],
									1);
						}
					} catch(Exception e) {
                        logger.error("Error parsing vertex data: {}", e.getMessage());
					}
					// texture data (if any)
					if(subTokens.length>1 && !subTokens[1].isEmpty()) {
						int indexT = Integer.parseInt(subTokens[1])-1;
						try {
							model.addTexCoord(
									texCoordArray.get(indexT*2  ),
									texCoordArray.get(indexT*2+1));
						} catch(Exception e) {
                            logger.error("Error texture data: {}", e.getMessage());
						}
					}
					// normal data (if any)
					if(subTokens.length>2 && !subTokens[2].isEmpty()) {
						int indexN = Integer.parseInt(subTokens[2])-1;
						try {
							model.addNormal(
									normalArray.get(indexN*3  ),
									normalArray.get(indexN*3+1),
									normalArray.get(indexN*3+2));
						} catch(Exception e) {
							logger.error("Error normal data: {}",e.getMessage());
						}
					}
				}
			}
		}
	}

	Map<String,OBJMaterial> loadMaterialLibrary(String filename) throws IOException {
		InputStream inputStream = FileHelper.open(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		OBJMaterial mat = null;
		Map<String,OBJMaterial> map = new HashMap<>();

		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("newmtl ")) {
				// name
				mat = new OBJMaterial();
				mat.name = line.substring(7);
				map.put(mat.name,mat);
			}

			if(mat==null) continue;

			if (line.startsWith("map_Kd ")) {
				// texture
				mat.texture = line.substring(7);
			} else if (line.startsWith("Ka ")) {
				// ambient
				String[] tokens = line.split("\\s+");
				mat.ambient[0] = Float.parseFloat(tokens[1]);
				mat.ambient[1] = Float.parseFloat(tokens[2]);
				mat.ambient[2] = Float.parseFloat(tokens[3]);
			} else if (line.startsWith("Kd ")) {
				// diffuse
				String[] tokens = line.split("\\s+");
				mat.diffuse[0] = Float.parseFloat(tokens[1]);
				mat.diffuse[1] = Float.parseFloat(tokens[2]);
				mat.diffuse[2] = Float.parseFloat(tokens[3]);
			} else if (line.startsWith("Ks ")) {
				// specular
				String[] tokens = line.split("\\s+");
				mat.specular[0] = Float.parseFloat(tokens[1]);
				mat.specular[1] = Float.parseFloat(tokens[2]);
				mat.specular[2] = Float.parseFloat(tokens[3]);
			} else if (line.startsWith("Ke ")) {
				// emissive
				String[] tokens = line.split("\\s+");
				mat.emissive[0] = Float.parseFloat(tokens[1]);
				mat.emissive[1] = Float.parseFloat(tokens[2]);
				mat.emissive[2] = Float.parseFloat(tokens[3]);
			} else if (line.startsWith("Ns ")) {
				// shininess
				mat.shininess = Float.parseFloat(line.substring(3));
			} else if (line.startsWith("d ")) {
				// transparency
				mat.transparency = Float.parseFloat(line.substring(2));
			}/*
			else if (line.startsWith("Ni ")) {}  // optical density
			else if (line.startsWith("illum ")) {}  // illumination model
			*/
		}
		return map;
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
