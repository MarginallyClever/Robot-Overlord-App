package com.marginallyclever.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static com.marginallyclever.util.PreferencesHelper.MakelangeloPreferenceKey.*;

/**
 * Helper class to be used when accessing preferences.
 */
@Deprecated public final class PreferencesHelper implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(PreferencesHelper.class);

	/**
	 * Enumeration used when getting a specific preference node.
	 * See {@link #getPreferenceNode(MakelangeloPreferenceKey)}
	 */
	public enum MakelangeloPreferenceKey {
		GRAPHICS,
		MACHINES,
		LANGUAGE,
		LEGACY_MAKELANGELO_ROOT,
	}

	/**
	 * Internal mapping of all Makelangelo preference nodes.
	 */
	private static final Map<MakelangeloPreferenceKey, ? extends Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

	/**
	 * Legacy preference node path.
	 */
	private static final String LEGACY_MAKELANGELO_ROOT_PATH_NAME = "DrawBot";
	private static final String GRAPHICS_PATH_NAME = "Graphics";
	private static final String MACHINES_PATH_NAME = "Machines";
	private static final String LANGUAGE_PATH_NAME = "Language";

    // Initializes {@link CLASS_TO_PREFERENCE_NODE_MAP}.
	// See <a href="http://stackoverflow.com/a/507658">How can I Initialize a static Map?</a>
	static {
		var initialMap = new HashMap<MakelangeloPreferenceKey, Preferences>();
		Preferences userRootPreferencesNode = MarginallyCleverPreferences.userRoot();
		Preferences legacyMakelangeloPreferenceNode = userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME);
		try {
			legacyMakelangeloPreferenceNode.sync();
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		initialMap.put(LEGACY_MAKELANGELO_ROOT, legacyMakelangeloPreferenceNode);
		initialMap.put(GRAPHICS, legacyMakelangeloPreferenceNode.node(GRAPHICS_PATH_NAME));
		initialMap.put(MACHINES, legacyMakelangeloPreferenceNode.node(MACHINES_PATH_NAME));
		initialMap.put(LANGUAGE, legacyMakelangeloPreferenceNode.node(LANGUAGE_PATH_NAME));
		CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap(initialMap);
	}

	/**
	 * @param <P> an extension of Preferences
	 * @param key enumeration key used to look up a Makelangelo preference value.
	 * @return preference node associated with the given key.
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Preferences> P getPreferenceNode(MakelangeloPreferenceKey key) {
		return (P) CLASS_TO_PREFERENCE_NODE_MAP.get(key);
	}
}
