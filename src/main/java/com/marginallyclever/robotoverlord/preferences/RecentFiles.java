package com.marginallyclever.robotoverlord.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Save and load a list of recently opened files to/from Preferences
 *
 * @author Dan Royer
 * @since 2.5
 */
public class RecentFiles {
	private static final Logger logger = LoggerFactory.getLogger(RecentFiles.class);
	public static final int MAX_FILES=10;
	private final List<String> filenames = new ArrayList<>();
	private final Preferences prefs = Preferences.userRoot().node("RobotOverlord").node("Recent files");
	
	public RecentFiles() {
		super();
		// load recent files from prefs
		for(int i=0;i<MAX_FILES;++i) {
			String fn = prefs.get(String.valueOf(i),null);
			if(fn==null) break;
			filenames.add(fn);
		}
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public int size() {
		return filenames.size();
	}

	public void add(String filename) {
		// if it's already in the list, remove it.
		if(filenames.contains(filename)) {
			remove(filename);
		}

		// put it at the top of the list.
		filenames.add(0,filename);

		// trim the list to MAX_FILES
		while(filenames.size()>MAX_FILES) {
			filenames.remove(filenames.size()-1);
		}

		save();
	}

	public void remove(String filename) {
		filenames.remove(filename);
		save();
	}

	/**
	 * Save the recent files list to {@link Preferences}.
	 */
	public void save() {
		try {
			prefs.clear();
			int i = 0;
			for (String fn : filenames) {
				if (fn == null) continue;
				if (fn.trim().isEmpty()) continue;
				prefs.put(String.valueOf(i), fn);
				i++;
			}
			prefs.flush();
		} catch (Exception e) {
			logger.warn("Failed to save.", e);
		}
	}
}
