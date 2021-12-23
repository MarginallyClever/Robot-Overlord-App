package com.marginallyclever.robotOverlord.shape.save;

import java.io.OutputStream;

import com.marginallyclever.robotOverlord.shape.Mesh;


public abstract interface MeshSaver {
	public String getEnglishName();
	public String getValidExtensions();

	/**
	 * save data from model to outputStream 
	 * @param outputStream
	 * @param model
	 * @throws Exception
	 */
	public void save(OutputStream outputStream,Mesh model) throws Exception;
}
