package com.marginallyclever.robotOverlord.shape;

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
	 * Load data from stream
	 * @param inputStream source of data
	 * @returns Mesh containing all parsed data
	 * @throws Exception
	 */
	public Mesh load(BufferedInputStream inputStream) throws Exception;

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
