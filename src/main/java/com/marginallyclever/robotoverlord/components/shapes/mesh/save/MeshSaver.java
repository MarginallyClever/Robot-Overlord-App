package com.marginallyclever.robotoverlord.components.shapes.mesh.save;

import com.marginallyclever.robotoverlord.components.shapes.mesh.Mesh;

import java.io.OutputStream;


public abstract interface MeshSaver {
	public String getEnglishName();
	public String getValidExtensions();

	/**
	 * save data from model to outputStream 
	 * @param outputStream
	 * @param model
	 * @throws Exception
	 */
	public void save(OutputStream outputStream, Mesh model) throws Exception;
}
