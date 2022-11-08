package com.marginallyclever.robotoverlord.mesh.load;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.mesh.Mesh;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class MeshFactory {
	private static MeshLoader [] loaders = { new LoadSTL(), new LoadOBJ(), new Load3MF(), new LoadAMF(), new LoadPLY() };
	
	// the pool of all shapes loaded
	private static LinkedList<Mesh> meshPool = new LinkedList<>();
	
	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param filename file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return an instance of Mesh.  It may contain nothing.
	 */
	public static Mesh load(String filename) {
		if(filename == null || filename.trim().length()==0) return null;
		
		Mesh m = getMeshFromPool(filename);
		if(m!=null) return m;

		try {
			m = attemptLoad(filename);
		}
		catch(Exception e) {
			Log.error("Failed to load mesh: "+e.getLocalizedMessage());
		}
		if(m!=null) {
			meshPool.add(m);
			return m;
		}
		// failed to load, return empty mesh
		return new Mesh();
	}

	private static Mesh getMeshFromPool(String filename) {
		// find the existing shape in the pool
		for( Mesh m : meshPool ) {
			if(m.getSourceName().equals(filename)) {
				return m;
			}
		}

		return null;
	}

	private static Mesh attemptLoad(String filename) throws Exception {
		for( MeshLoader loader : loaders ) {
			if(isValidExtension(filename,loader)) {			
				return loadMeshWithLoader(filename,loader);
			}
		}
		return null;
	}
	
	private static boolean isValidExtension(String filename, MeshLoader loader) {
		filename = filename.toLowerCase();
		String [] extensions = loader.getValidExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e)) return true;
		}
		return false;
	}

	private static Mesh loadMeshWithLoader(String filename, MeshLoader loader) throws Exception {
		Mesh m=null;

		try(BufferedInputStream stream = FileAccess.open(filename)) {
			m = loader.load(stream);
		}

		if(m!=null) {
			m.setSourceName(filename);
			m.updateCuboid();
		}

		return m;
	}

	public static void reload(Mesh myMesh) throws Exception {
		throw new java.lang.UnsupportedOperationException("MeshFactory.reload() not implemented.");
	}

	public static ArrayList<FileFilter> getAllExtensions() {
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		
		for( MeshLoader loader : loaders ) {
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		
		}
		return filters;
	}
}
