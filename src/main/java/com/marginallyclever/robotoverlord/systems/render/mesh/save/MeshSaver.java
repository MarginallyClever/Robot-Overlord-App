package com.marginallyclever.robotoverlord.systems.render.mesh.save;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.io.OutputStream;

/**
 * A MeshSaver is a script that saves a mesh to a file.
 *
 * @author Dan Royer
 * @since 2.5.0
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
