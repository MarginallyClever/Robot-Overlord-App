package com.marginallyclever.robotoverlord.mesh.load;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.mesh.Mesh;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class MeshFactory {
	private static final MeshLoader [] loaders = { new LoadSTL(), new LoadOBJ(), new Load3MF(), new LoadAMF(), new LoadPLY() };
	
	// the pool of all shapes loaded
	private static final LinkedList<Mesh> meshPool = new LinkedList<>();
	
	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param filename file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return an instance of Mesh.  It may contain nothing.
	 */
	public static Mesh load(String filename) {
		if(filename == null || filename.trim().length()==0) return null;
		
		Mesh mesh = getMeshFromPool(filename);
		if(mesh!=null) return mesh;

		mesh = new Mesh();
		attemptLoad(filename,mesh);
		meshPool.add(mesh);
		return mesh;
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

	private static void attemptLoad(String filename,Mesh mesh) {
		for( MeshLoader loader : loaders ) {
			if(isValidExtension(filename,loader)) {
				loadMeshWithLoader(filename,mesh,loader);
				return;
			}
		}
	}
	
	private static boolean isValidExtension(String filename, MeshLoader loader) {
		filename = filename.toLowerCase();
		String [] extensions = loader.getValidExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e)) return true;
		}
		return false;
	}

	private static void loadMeshWithLoader(String filename, Mesh mesh, MeshLoader loader) {
		Log.message("Loading "+filename+" with "+loader.getEnglishName());

		mesh.setSourceName(filename);
		mesh.setDirty(true);

		try(BufferedInputStream stream = FileAccess.open(filename)) {
			loader.load(stream,mesh);
		}
		catch(Exception e) {
			Log.error("Failed to load mesh: "+e.getLocalizedMessage());
		}

		mesh.updateCuboid();
		mesh.setDirty(true);
	}

	public static void reload(Mesh myMesh) {
		myMesh.clear();
		attemptLoad(myMesh.getSourceName(),myMesh);
	}

	public static ArrayList<FileFilter> getAllExtensions() {
		ArrayList<FileFilter> filters = new ArrayList<>();
		
		for( MeshLoader loader : loaders ) {
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		
		}
		return filters;
	}

	public static boolean canLoad(String absolutePath) {
		for( MeshLoader loader : loaders ) {
			if(Arrays.stream(loader.getValidExtensions()).anyMatch(absolutePath::endsWith)) return true;
		}
		return false;
	}
}
