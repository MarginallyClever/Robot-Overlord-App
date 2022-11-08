package com.marginallyclever.robotoverlord.mesh.load;

import com.marginallyclever.robotoverlord.mesh.Mesh;

import java.io.BufferedInputStream;

/**
 * {@link MeshLoader} interface for all classes that load a mesh.  Call upon by {@link MeshFactory}
 * @author Dan Royer
 *
 */
public interface MeshLoader {
	String getEnglishName();
	String[] getValidExtensions();

	/**
	 * Load data from stream
	 * @param inputStream source of data
	 * @returns Mesh containing all parsed data
	 * @throws Exception
	 */
	Mesh load(BufferedInputStream inputStream) throws Exception;
}
