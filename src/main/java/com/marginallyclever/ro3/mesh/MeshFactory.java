package com.marginallyclever.ro3.mesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.factories.Factory;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.factories.Resource;
import com.marginallyclever.ro3.mesh.load.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.*;

/**
 * {@link MeshFactory} loads a mesh from a file using one of many {@link MeshLoader} classes.  It also keeps a pool of
 * all mesh loaded so that only one instance of each shape is loaded.
 *
 */
public class MeshFactory extends Factory {
	private static final Logger logger = LoggerFactory.getLogger(MeshFactory.class);
	private final MeshLoader [] loaders = {
			new Load3MF(),
			new LoadAMF(),
			new LoadOBJ(),
			new LoadPLY(),
			new LoadSTL(),
	};

	// the pool of all mesh loaded
	private final Map<String, Resource<Mesh>> cache = new HashMap<>();
	
	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param filename file from which to load.  May be "filename.ext" or "zipfile.zip:filename.ext"
	 * @return a non-null instance of Mesh.  It may contain nothing.
	 */
	public Mesh get(Lifetime lifetime,String filename) {
		if(filename == null || filename.trim().isEmpty()) return null;

        String absolutePath = FileHelper.getAbsolutePathOrFilename(filename);
        return cache.computeIfAbsent(absolutePath, _-> {
                var mesh = new Mesh();
                attemptLoad(absolutePath,mesh);
                return new Resource<>(mesh, lifetime);
        }).item();
	}

	/**
	 * Try to load a mesh from a file using one of the available loaders.
	 * @param filename The file to open.  May be "filename.ext" or "zipfile.zip:filename.ext"
	 * @param mesh the mesh to load into
	 */
	private void attemptLoad(String filename, Mesh mesh) {
		for( MeshLoader loader : loaders ) {
			if(isValidExtension(filename,loader)) {
				loadMeshWithLoader(filename,mesh,loader);
				return;
			}
		}
	}
	
	private boolean isValidExtension(String filename, MeshLoader loader) {
		filename = filename.toLowerCase();
		String [] extensions = loader.getValidExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e)) return true;
		}
		return false;
	}

	/**
	 * Load a mesh from a file using a specific loader.
	 * @param filename The file to open.  May be "filename.ext" or "zipfile.zip:filename.ext"
	 * @param mesh the mesh to load into
	 * @param loader the loader to use
	 */
	private void loadMeshWithLoader(String filename, Mesh mesh, MeshLoader loader) {
		//logger.info("Loading "+filename+" with "+loader.getEnglishName());

		mesh.setSourceName(filename);
		mesh.setDirty(true);

		try(BufferedInputStream stream = FileHelper.open(filename)) {
			loader.load(stream,mesh);
		}
		catch(Exception e) {
			logger.error("Failed to load mesh: "+e.getMessage());
		}

		mesh.updateBoundingBox();
	}

	public void reload(Mesh myMesh) {
		myMesh.clear();
		attemptLoad(myMesh.getSourceName(),myMesh);
	}

	public List<FileFilter> getAllExtensions() {
		ArrayList<FileFilter> filters = new ArrayList<>();
		
		for( MeshLoader loader : loaders ) {
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		}
		return filters;
	}

	public boolean canLoad(String absolutePath) {
		String lowerCasePath = absolutePath.toLowerCase();
		for( MeshLoader loader : loaders ) {
			if(Arrays.stream(loader.getValidExtensions()).anyMatch(ext -> lowerCasePath.endsWith(ext.toLowerCase()))) return true;
		}
		return false;
	}

    public boolean hasMaterial(String absolutePath) {
		for( MeshLoader loader : loaders ) {
			if(loader.hasMaterial(absolutePath)) return true;
		}
		return false;
    }

	public String getMaterialPath(String absolutePath) {
		for( MeshLoader loader : loaders ) {
			if(loader.hasMaterial(absolutePath)) return loader.getMaterialPath(absolutePath);
		}
		return null;
	}

    public List<String> getAllSourcesForExport() {
        return getResources(Lifetime.SCENE).stream().map(Mesh::getSourceName).toList();
    }

    /**
     * Get all meshes in the pool with a specific lifetime.
     * @param lifetime the lifetime to filter by
     * @return a collection of meshes with the specified lifetime
     */
    public List<Mesh> getResources(Lifetime lifetime) {
        return cache.values().stream()
                .filter(e->e.lifetime()== lifetime)
                .map(Resource::item)
                .toList();
    }

    public List<Mesh> getAllResources() {
        return cache.values().stream()
                .map(Resource::item)
                .toList();
    }

    /**
     * Clear all meshes with lifetime SCENE from the pool.
     */
    @Override
    public void reset() {
		cache.values().removeIf(e->e.lifetime() == Lifetime.SCENE);
    }

    /**
     * Add a mesh to the pool if it is not already present.
     * @param lifetime the lifetime of the mesh
     * @param key the key to use for the mesh
     * @param mesh the mesh to add
     */
    public void addToPool(Lifetime lifetime,String key,Mesh mesh) {
        cache.putIfAbsent(key, new Resource<>(mesh,lifetime) );
    }

    /**
     * Remove a mesh from the pool.
     * @param mesh the mesh to remove
     */
    public void removeFromPool(Mesh mesh) {
        cache.remove(mesh);
    }

    /**
     * Unload all meshes in the pool from OpenGL memory.  This must be called from the OpenGL thread.
     * @param gl the OpenGL context
     */
    public void unloadAll(GL3 gl) {
        cache.values().forEach(e -> e.item().unload(gl) );
    }
}
