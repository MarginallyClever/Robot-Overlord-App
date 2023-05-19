package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link MeshFactory} loads a mesh from a file using one of many {@link MeshLoader} classes.  It also keeps a pool of
 * all mesh loaded so that only one instance of each shape is loaded.
 *
 * @author Dan Royer
 */
public class MeshFactory {
	private static final Logger logger = LoggerFactory.getLogger(MeshFactory.class);
	private static final MeshLoader [] loaders = {
			new Load3MF(),
			new LoadAMF(),
			new LoadOBJ(),
			new LoadPLY(),
			new LoadSTL(),
	};
	
	// the pool of all mesh loaded
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
			String sourceName = m.getSourceName();
			if(sourceName==null) continue;
			if(filename.equals(sourceName)) {
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
		logger.info("Loading "+filename+" with "+loader.getEnglishName());

		mesh.setSourceName(filename);
		mesh.setDirty(true);

		try(BufferedInputStream stream = FileHelper.open(filename)) {
			loader.load(stream,mesh);
		}
		catch(Exception e) {
			logger.error("Failed to load mesh: "+e.getMessage());
		}

		mesh.updateCuboid();
	}

	public static void reload(Mesh myMesh) {
		myMesh.clear();
		attemptLoad(myMesh.getSourceName(),myMesh);
	}

	public static List<FileFilter> getAllExtensions() {
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

    public static boolean hasMaterial(String absolutePath) {
		for( MeshLoader loader : loaders ) {
			if(loader.hasMaterial(absolutePath)) return true;
		}
		return false;
    }

	public static String getMaterialPath(String absolutePath) {
		for( MeshLoader loader : loaders ) {
			if(loader.hasMaterial(absolutePath)) return loader.getMaterialPath(absolutePath);
		}
		return null;
	}
}
