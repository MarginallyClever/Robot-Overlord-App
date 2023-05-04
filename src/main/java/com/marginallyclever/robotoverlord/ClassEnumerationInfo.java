package com.marginallyclever.robotoverlord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// stuff for trying to find and load plugins, part of future expansion
public class ClassEnumerationInfo {
	private static final Logger logger = LoggerFactory.getLogger(ClassEnumerationInfo.class);

	@SuppressWarnings("unused")
	private String getPath(Class<?> mysteryClass) {
	    String cn = mysteryClass.getName();
	    //logger.info("cn "+cn);
	    String rn = cn.replace('.', '/') + ".class";
	    //logger.info("rn "+rn);
	    String path = getClass().getClassLoader().getResource(rn).getPath();
	    //logger.info("path "+path);
	    int ix = path.indexOf("!");
	    if(ix >= 0) {
	        path = path.substring(0, ix);
	    }
	    return path;
	}
	
	public void enumerateJarContents(String absPathToJarFile) throws IOException {
	    JarFile jarFile = new JarFile(absPathToJarFile);
	    Enumeration<JarEntry> e = jarFile.entries();
	    while (e.hasMoreElements()) {
			enumerateJarContents2(e.nextElement());
		}
	    jarFile.close();
	}
	
	private static void enumerateJarContents2(Object obj) {
       JarEntry entry = (JarEntry)obj;
       String name = entry.getName();
       long size = entry.getSize();
       long compressedSize = entry.getCompressedSize();
       logger.info(name + "\t" + size + "\t" + compressedSize);
     }
	
	// Load a class from a Jar file.
	// @param absPathToJarFile c:\some\path\myfile.jar
	// @param className like com.mypackage.myclass
	protected void LoadClasses(String absPathToJarFile,String className) throws Exception {
		File file  = new File(absPathToJarFile);
		URL url = file.toURI().toURL();  
		URL[] urls = new URL[]{url};
		URLClassLoader classLoader = new URLClassLoader(urls);
		@SuppressWarnings("unused")
		Class<?> mysteryClass = classLoader.loadClass(className);
		// TODO finish me
		classLoader.close();
	}
}
