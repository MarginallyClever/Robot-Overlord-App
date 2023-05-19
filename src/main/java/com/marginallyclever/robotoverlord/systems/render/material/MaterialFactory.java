package com.marginallyclever.robotoverlord.systems.render.material;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * {@link MaterialFactory} loads a Material from a file using one of many {@link MaterialLoader} classes.
 *
 * @author Dan Royer
 */
public class MaterialFactory {
	private static final Logger logger = LoggerFactory.getLogger(MaterialFactory.class);
	private static final MaterialLoader [] loaders = {
			new LoadMTL(),
	};

	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param filename file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return an instance of Material.  It may contain nothing.
	 */
	public static MaterialComponent load(String filename) {
		MaterialComponent material = new MaterialComponent();

		if(filename!=null && !filename.trim().isEmpty()) {
			attemptLoad(filename, material);
		}

		return material;
	}

	private static void attemptLoad(String filename,MaterialComponent material) {
		for( MaterialLoader loader : loaders ) {
			if(isValidExtension(filename,loader)) {
				loadMaterialWithLoader(filename,material,loader);
				return;
			}
		}
	}
	
	private static boolean isValidExtension(String filename, MaterialLoader loader) {
		filename = filename.toLowerCase();
		String [] extensions = loader.getValidExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e)) return true;
		}
		return false;
	}

	private static void loadMaterialWithLoader(String filename, MaterialComponent material, MaterialLoader loader) {
		logger.info("Loading "+filename+" with "+loader.getEnglishName());

		try(BufferedInputStream stream = FileHelper.open(filename)) {
			loader.load(stream,material);
		}
		catch(Exception e) {
			logger.error("Failed to load Material: "+e.getMessage());
		}
	}

	public static ArrayList<FileFilter> getAllExtensions() {
		ArrayList<FileFilter> filters = new ArrayList<>();
		
		for( MaterialLoader loader : loaders ) {
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		}
		return filters;
	}

	public static boolean canLoad(String absolutePath) {
		for( MaterialLoader loader : loaders ) {
			if(Arrays.stream(loader.getValidExtensions()).anyMatch(absolutePath::endsWith)) return true;
		}
		return false;
	}
}
