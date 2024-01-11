package com.marginallyclever.ro3.mesh.save;

import com.marginallyclever.ro3.mesh.Mesh;

import java.io.OutputStream;

/**
 * {@link MeshSaver} saves a {@link Mesh} to a file.
 *
 */
public interface MeshSaver {
	String getEnglishName();
	String getValidExtensions();

	/**
	 * save data from model to outputStream 
	 * @param outputStream
	 * @param model
	 * @throws Exception
	 */
	void save(OutputStream outputStream, Mesh model) throws Exception;
}
