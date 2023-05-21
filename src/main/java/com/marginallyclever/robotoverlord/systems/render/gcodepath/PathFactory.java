package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import com.marginallyclever.convenience.helpers.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * {@link PathFactory} loads a {@link GCodePath} from a file using one of many {@link PathLoader} classes.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class PathFactory {
    private static final Logger logger = LoggerFactory.getLogger(PathFactory.class);
    private static final PathLoader[] loaders = {
            new Slic3rGCodePathLoader(),
    };

    // the pool of all mesh loaded
    private static final LinkedList<GCodePath> pathPool = new LinkedList<>();

    /**
     * Makes sure to only load one instance of each source file.  Loads all the data immediately.
     * @param filename file from which to load.  may be filename.ext or zipfile.zip:filename.ext
     * @return an instance of TurtlePath.  It may contain nothing.
     */
    public static GCodePath load(String filename) {
        if(filename == null || filename.trim().length()==0) return null;

        GCodePath mesh = getTurtlePathFromPool(filename);
        if(mesh!=null) return mesh;

        mesh = new GCodePath();
        attemptLoad(filename,mesh);

        pathPool.add(mesh);
        return mesh;
    }

    private static GCodePath getTurtlePathFromPool(String filename) {
        // find the existing shape in the pool
        for( GCodePath m : pathPool) {
            String sourceName = m.getSourceName();
            if(sourceName==null) continue;
            if(filename.equals(sourceName)) {
                return m;
            }
        }

        return null;
    }

    private static void attemptLoad(String filename, GCodePath mesh) {
        for( PathLoader loader : loaders ) {
            if(isValidExtension(filename,loader)) {
                loadTurtlePathWithLoader(filename,mesh,loader);
                return;
            }
        }
    }

    private static boolean isValidExtension(String filename, PathLoader loader) {
        filename = filename.toLowerCase();
        String [] extensions = loader.getValidExtensions();
        for( String e : extensions ) {
            if(filename.endsWith(e)) return true;
        }
        return false;
    }

    private static void loadTurtlePathWithLoader(String filename, GCodePath path, PathLoader loader) {
        logger.info("Loading "+filename+" with "+loader.getEnglishName());

        path.setSourceName(filename);
        path.setDirty(true);

        try(BufferedInputStream stream = FileHelper.open(filename)) {
            loader.load(stream,path);
        }
        catch(Exception e) {
            logger.error("Failed to load gcodepath: "+e.getMessage());
        }
    }

    public static void reload(GCodePath myGCodePath) {
        myGCodePath.clear();
        attemptLoad(myGCodePath.getSourceName(), myGCodePath);
    }

    public static ArrayList<FileFilter> getAllExtensions() {
        ArrayList<FileFilter> filters = new ArrayList<>();

        for( PathLoader loader : loaders ) {
            filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
        }
        return filters;
    }

    public static boolean canLoad(String absolutePath) {
        for( PathLoader loader : loaders ) {
            if(Arrays.stream(loader.getValidExtensions()).anyMatch(absolutePath::endsWith)) return true;
        }
        return false;
    }
}
