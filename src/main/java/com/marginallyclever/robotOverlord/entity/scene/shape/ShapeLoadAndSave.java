package com.marginallyclever.robotOverlord.entity.scene.shape;

import java.io.BufferedInputStream;
import java.io.OutputStream;

public interface ShapeLoadAndSave {
	public String getEnglishName();
	public String getValidExtensions();

	/**
	 * Can you load at all?
	 * @return true for yes
	 */
	public boolean canLoad();

	/**
	 * Can you load this file?
	 * @return true for yes
	 */
	public boolean canLoad(String filename);
	
	/**
	 * Load data from stream into model 
	 * @param inputStream
	 * @param model
	 * @returns true if model loaded OK.
	 * @throws Exception
	 */
	public boolean load(BufferedInputStream inputStream,Mesh model) throws Exception;

	/**
	 * Can you save at all?
	 * @return true for yes
	 */
	public boolean canSave();
	
	/**
	 * Can you save this file?
	 * @return true for yes
	 */
	public boolean canSave(String filename);
	
	/**
	 * save data from model to outputStream 
	 * @param outputStream
	 * @param model
	 * @throws Exception
	 */
	public void save(OutputStream outputStream,Mesh model) throws Exception;
}
