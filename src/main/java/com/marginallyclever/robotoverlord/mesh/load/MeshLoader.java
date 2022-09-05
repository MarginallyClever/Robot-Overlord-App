package com.marginallyclever.robotoverlord.mesh.load;

import java.io.BufferedInputStream;

import com.marginallyclever.robotoverlord.mesh.Mesh;

/**
 * {@link MeshLoader} interface for all classes that load a mesh.  Call upon by {@link MeshFactory}
 * @author Dan Royer
 *
 */
public abstract interface MeshLoader {
	public String getEnglishName();
	public String[] getValidExtensions();

	/**
	 * Load data from stream
	 * @param inputStream source of data
	 * @returns Mesh containing all parsed data
	 * @throws Exception
	 */
	public Mesh load(BufferedInputStream inputStream) throws Exception;
}
