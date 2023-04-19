package com.marginallyclever.robotoverlord.components.path;

import java.io.BufferedInputStream;

/**
 * {@link TurtlePathLoader} interface for all classes that load a TurtlePath.  Call upon by {@link TurtlePathFactory}
 * @author Dan Royer
 *
 */
public interface TurtlePathLoader {
	String getEnglishName();
	String[] getValidExtensions();

	/**
	 * Load data from stream
	 * @param inputStream source of data
	 * @param model mesh into which data will be loaded
	 * @throws Exception if something goes wrong
	 */
	void load(BufferedInputStream inputStream, GCodePath model) throws Exception;
}
