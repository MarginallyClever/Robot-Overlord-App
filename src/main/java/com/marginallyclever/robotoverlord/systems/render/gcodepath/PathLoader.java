package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import java.io.BufferedInputStream;

/**
 * {@link PathLoader} interface for all classes that load a {@link GCodePath}.  Call upon by {@link PathFactory}
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface PathLoader {
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
