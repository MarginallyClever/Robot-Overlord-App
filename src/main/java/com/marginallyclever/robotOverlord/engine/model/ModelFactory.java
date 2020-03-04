package com.marginallyclever.robotOverlord.engine.model;

import java.io.BufferedInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ServiceLoader;

import com.marginallyclever.convenience.FileAccess;


public class ModelFactory {
	static LinkedList<Model> modelPool = new LinkedList<Model>();

	/**
	 * Model factory makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param sourceName file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return the Model instance.
	 * @throws Exception if file cannot be read successfully
	 */
	public static Model createModelFromFilename(String sourceName) throws Exception {
		if(sourceName == null || sourceName.trim().length()==0) return null;
		
		// find the existing model in the pool
		Iterator<Model> iter = ModelFactory.modelPool.iterator();
		while(iter.hasNext()) {
			Model m = iter.next();
			if(m.getSourceName().equals(sourceName)) {
				return m;
			}
		}
		
		Model m=null;
		
		// not in pool.  Find a serviceLoader that can load this file type.
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ModelLoadAndSave loader = i.next();
			if(loader.canLoad() && loader.canLoad(sourceName)) {
				BufferedInputStream stream = FileAccess.open(sourceName);
				m = loader.load(stream);
				m.setSourceName(sourceName);
				// Maybe add a m.setSaveAndLoader(loader); ?
				modelPool.add(m);
				break;
			}
		}

		if(m==null) {
			throw new Exception("No loader found for "+sourceName);
		}
		
		return m;
	}

	/**
	 * Model factory makes sure to only load one instance of each source file.  Loads all the data immediately.  Also scales the data.
	 * @param sourceName file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @param loadScale scale the model file by this value (1 is no scale) on load.
	 * @return the Model instance.
	 * @throws Exception if file cannot be read successfully
	 */
	public static Model createModelFromFilename(String sourceName,float loadScale) throws Exception {
		Model m = createModelFromFilename(sourceName);
		if(m!=null) m.scale = loadScale;
		return m;
	}
}
