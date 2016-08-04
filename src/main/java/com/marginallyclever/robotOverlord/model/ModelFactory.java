package com.marginallyclever.robotOverlord.model;

import java.util.Iterator;
import java.util.LinkedList;

public class ModelFactory {
	static LinkedList<Model> modelPool = new LinkedList<Model>();

	/**
	 * Model factory makes sure to only load one instance of each source file.  The instance returned might not yet be loaded.
	 * @param sourceName
	 * @param loadScale
	 * @return the Model instance.
	 */
	public static Model createModelFromFilename(String sourceName) {
		if(sourceName == null || sourceName.trim().length()==0) return null;
		
		// find the existing model in the pool
		Iterator<Model> iter = ModelFactory.modelPool.iterator();
		while(iter.hasNext()) {
			Model m = iter.next();
			if(m.getSourceName().equals(sourceName)) {
				return m;
			}
		}
		
		Model m = new Model();
		m.setSourceName(sourceName);
		modelPool.add(m);
		return m;
	}

	/**
	 * Same as createModelFromFilename, and scales the model on load.
	 * @param sourceName
	 * @param loadScale
	 * @return the Model instance.
	 */
	public static Model createModelFromFilename(String sourceName,float loadScale) {
		Model m = createModelFromFilename(sourceName);
		m.loadScale = loadScale;
		return m;
	}
}
