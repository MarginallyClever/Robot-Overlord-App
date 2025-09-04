package com.marginallyclever.convenience.helpers;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Methods to make loading files from disk or jar item easier.
 */
public class FileHelper {
	/**
	 * Open a file.  open() looks in three places:<br>
	 *  - The file may be contained inside a zip, as indicated by the filename "zipname:filename".<br>
	 *  - The file may be a item inside a jar file.
	 *  - The file may be on disk.
	 *     
	 * @param filename The file to open.  May be "filename.ext" or "zipfile.zip:filename.ext"
	 * @return BufferedInputStream to the file contents
	 * @throws IOException file open failure
	 */
	public static BufferedInputStream open(String filename) throws IOException {
		int index = filename.lastIndexOf(":");
		int index2 = filename.lastIndexOf(":\\");  // hack for windows file system
		if(index!=-1 && index!=index2) {
			return loadFromZip(filename.substring(0, index), filename.substring(index+1));
		} else {
			return new BufferedInputStream(getInputStream(filename));
		}
	}

	private static InputStream getInputStream(String fname) throws IOException {
		InputStream s = FileHelper.class.getResourceAsStream(fname);
		if( s==null ) {
			s = new FileInputStream(fname);
		}
		return s;
	}

	private static BufferedInputStream loadFromZip(String zipFilePath,String fileToExtract) throws IOException {
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(fileToExtract);
			if(entry==null) {
				throw new IOException("file not found in zip");
			}

			// read buffered stream into temp file.
			String fnameSuffix = fileToExtract.substring(fileToExtract.lastIndexOf(".")+1);
			String fnameNoSuffix = fileToExtract.substring(0,fileToExtract.length()-(fnameSuffix.length()+1));
			File f = File.createTempFile(fnameNoSuffix, fnameSuffix);
			f.setReadable(true);
			f.setWritable(true);
			f.deleteOnExit();

			InputStream inputStream = zipFile.getInputStream(entry);
			FileOutputStream outputStream = new FileOutputStream(f);

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
            outputStream.close();
		    // return temp file as input stream
            return new BufferedInputStream(new FileInputStream(f));
	    }
	}

	public static String getUserHome() {
		return System.getProperty("user.home");
	}

	public static String getTempDirectory() { 
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * Get the absolute path to a file.  If the file is not found, return the filename.
	 * The file may be relative to the current working directory (user.dir), absolute, or a item.
	 * @param filename the file to find
	 * @return the absolute path to the file, or the original filename if the file is not found.
	 */
	public static String getAbsolutePathOrFilename(String filename) {
		String absolutePath;
		// look for the file relative to the current working directory.
		File pathCheck = new File(PathHelper.getCurrentWorkingDirectory(),filename);
		if(pathCheck.exists()) absolutePath = pathCheck.getAbsolutePath();
		else {
			// if we don't find the file, maybe it's absolute.
			pathCheck = new File(filename);
			if(pathCheck.exists()) absolutePath = pathCheck.getAbsolutePath();
			else {
				// maybe it's a item file?
				absolutePath = filename;
			}
		}
		return absolutePath;
	}
}
