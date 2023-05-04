package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.io.BufferedInputStream;

/**
 * {@link MeshLoader} interface for all classes that load a {@link Mesh}.  Call upon by {@link MeshFactory}
 * @author Dan Royer
 *
 */
public interface MeshLoader {
	String getEnglishName();
	String[] getValidExtensions();

	/**
	 * Load data from stream
	 * @param inputStream source of data
	 * @param model mesh into which data will be loaded
	 * @throws Exception if something goes wrong
	 */
	void load(BufferedInputStream inputStream, Mesh model) throws Exception;

	/**
	 * Does this loader find a material file near the mesh file?
	 * @param absolutePath path to mesh file
	 * @return true if a material file is found
	 */
    default boolean hasMaterial(String absolutePath) {
		return false;
	}

	/**
	 * Get the path to the material file
	 * @param absolutePath path to mesh file
	 * @return path to material file or null.
	 */
	default String getMaterialPath(String absolutePath) {
		return null;
	}
}
