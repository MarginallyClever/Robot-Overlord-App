package com.marginallyclever.util;

import org.json.JSONObject;
import org.json.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static com.marginallyclever.util.PreferencesHelper.MakelangeloPreferenceKey.*;

/**
 * Helper class to be used when accessing preferences.
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class PreferencesHelper implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(PreferencesHelper.class);

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Internal mapping of all Makelangelo preference nodes.
	 */
	private static final Map<MakelangeloPreferenceKey, ? extends Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

	/**
	 * Future Makelagelo root preference node path name.
	 */
	@SuppressWarnings("unused")
	private static final String MAKELANGELO_ROOT_PATH_NAME = PreferencesHelper.class.getPackage().getName();

	/**
	 * Legacy preference node path.
	 */
	@Deprecated
	private static final String LEGACY_MAKELANGELO_ROOT_PATH_NAME = "DrawBot";

	/**
	 * Graphics preference node path, used to store things such as {@code "Draw all while running"} used when OpenGL
	 * renders the application.
	 */
	private static final String GRAPHICS_PATH_NAME = "Graphics";

	/**
	 * Machine preference node path, used to store things such as paper height and width, invert left and right motors,
	 * etc.
	 * See com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings
	 */
	private static final String MACHINES_PATH_NAME = "Machines";

	/**
	 *
	 */
	private static final String LANGUAGE_PATH_NAME = "Language";

	/**
	 * Initializes {@link CLASS_TO_PREFERENCE_NODE_MAP}.
	 *
	 * See <a href="http://stackoverflow.com/a/507658">How can I Initialize a static Map?</a>
	 */
	static {
		final Map<MakelangeloPreferenceKey, ? super Preferences> initialMap = new HashMap<MakelangeloPreferenceKey, Preferences>();
		final Preferences userRootPreferencesNode = MarginallyCleverPreferences.userRoot();
		// TODO write unit test/tool to import/export machine configurations.
		//final Preferences makelangeloPreferenceNode = userRootPreferencesNode.node(MAKELANGELO_ROOT_PATH_NAME);
		//new MarginallyCleverPreferences((AbstractPreferences) userRootPreferencesNode.node(thisPackageName), userRootPreferencesNode.node(thisPackageName).name()); 
		final Preferences legacyMakelangeloPreferenceNode = userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME);//new MarginallyCleverPreferences((AbstractPreferences) userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME), userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME).name());
		try {
			legacyMakelangeloPreferenceNode.sync();
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		//initialMap.put(MAKELANGELO_ROOT, makelangeloPreferenceNode);
		initialMap.put(LEGACY_MAKELANGELO_ROOT, legacyMakelangeloPreferenceNode);
		initialMap.put(GRAPHICS, legacyMakelangeloPreferenceNode.node(GRAPHICS_PATH_NAME));
		initialMap.put(MACHINES, legacyMakelangeloPreferenceNode.node(MACHINES_PATH_NAME));
		initialMap.put(LANGUAGE, legacyMakelangeloPreferenceNode.node(LANGUAGE_PATH_NAME));
		@SuppressWarnings("unchecked")
		final Map<? extends MakelangeloPreferenceKey, ? extends Preferences> castedMap = (Map<? extends MakelangeloPreferenceKey, ? extends Preferences>) initialMap;
		CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap(castedMap);
	}

	/**
	 * NOOP Constructor.
	 *
	 * @throws IllegalStateException
	 */
	private PreferencesHelper() throws IllegalStateException {
		throw new IllegalStateException();
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

	/**
	 * Enumeration used when getting a specific preference node.
	 * See #getPreferenceNode(MakelangeloPreferenceKey)
	 */
	public enum MakelangeloPreferenceKey {
		GRAPHICS,
		MACHINES,
		LANGUAGE,
		@Deprecated
		LEGACY_MAKELANGELO_ROOT,
		//MAKELANGELO_ROOT
	}

	/**
	 * @param <P> an extension of Preferences
	 * @param preferenceNode Preference node whose name, and key values,
	 *                       as well as those of its children's are to be logged.
	 */
	public static <P extends Preferences> void logPreferenceNode(P preferenceNode) {
		try {
			logger.info("node name:"+preferenceNode);
			logKeyValuesForPreferenceNode(preferenceNode);
			final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
			for (String childNodeName : childrenPreferenceNodeNames) {
				final Preferences childNode = preferenceNode.node(childNodeName);
				logPreferenceNode(childNode);
			}
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
	}


	/**
	 * @param <P> an extension of Preferences
	 * @param preferenceNode Preference node to log key value pairs for.
	 * @throws BackingStoreException on failure to store
	 */
	public static <P extends Preferences> void logKeyValuesForPreferenceNode(P preferenceNode) throws BackingStoreException {
		final String[] keys = preferenceNode.keys();
		for (String key : keys) {
			logger.info("key:"+key+" value:"+ preferenceNode.get(key, null));
		}
	}


	/**
	 * @param <P> an extension of Preferences
	 * @param sourcePreferenceNode      Preference node to be copied from.
	 * @param destinationPreferenceNode Preference node to be copied to.
	 */
	public static <P extends Preferences> void copyPreferenceNode(P sourcePreferenceNode, P destinationPreferenceNode) {
		try {
			final String[] keys = sourcePreferenceNode.keys();
			for (String key : keys) {
				final String value = sourcePreferenceNode.get(key, null);
				destinationPreferenceNode.put(key, value);
			}
			final String[] childNames = sourcePreferenceNode.childrenNames();
			for (String childName : childNames) {
				final Preferences destinationChildNode = destinationPreferenceNode.node(childName);
				copyPreferenceNode(sourcePreferenceNode.node(childName), destinationChildNode);
			}
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
	}


	/**
	 * Recursively clears all the preferences (key-value associations) for a given node and its children.
	 *
	 * @param <P> an extension of Preferences
	 * @param preferenceNode Preference node that you want recursively cleared of all key value pairs.
	 * @throws BackingStoreException on failure to store
	 * See <a href="http://stackoverflow.com/a/6411855"></a>
	 */
	private static <P extends Preferences> void clearAll(P preferenceNode) throws BackingStoreException {
		final String[] childrenNames = preferenceNode.childrenNames();
		for (String childNodeName : childrenNames) {
			final Preferences childNode = preferenceNode.node(childNodeName);
			final String[] childNodesChildren = childNode.childrenNames();
			if (childNodesChildren != null) {
				final boolean hasChildren = childNodesChildren.length != 0;
				if (hasChildren) {
					clearAll(childNode);
				}
				childNode.clear();
			}
		}
		preferenceNode.clear();
	}

	/**
	 * Removes all of the preferences (key-value associations) in this
	 * preference node with no effect on any descendants
	 * of this node.
	 */
	@SuppressWarnings("unused")
	private static <P extends Preferences> void shallowClearPreferences(P preferenceNode) {
		try {
			preferenceNode.clear();
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Removes all of the preferences (key-value associations) in this
	 * preference node and any descendants of this node.
	 */
	@SuppressWarnings("unused")
	private static <P extends Preferences> void deepClearPreferences(P preferenceNode) {
		try {
			preferenceNode.clear();
			final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
			for (String childNodeName : childrenPreferenceNodeNames) {
				final Preferences childNode = preferenceNode.node(childNodeName);
				childNode.clear();
			}
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * @param sourcePreferenceNode      Preference node to be copied from.
	 * @param destinationPreferenceNode Preference node to be copied to and logged.
	 * <br>
	 *   {@link #copyPreferenceNode(Preferences, Preferences)}
	 * <br>
	 *   {@link #logPropertiesNode(Properties)}
	 * <br>
	 *   {@link #logAncestryable(Ancestryable)}
	 */
	@SuppressWarnings("unused")
	private void copyAndLogPreferenceNode(AbstractPreferences sourcePreferenceNode, MarginallyCleverPreferences destinationPreferenceNode) {
		try {
			PreferencesHelper.clearAll(destinationPreferenceNode);
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		PreferencesHelper.copyPreferenceNode(sourcePreferenceNode, destinationPreferenceNode);
		final File preferencesFile = MarginallyCleverPreferencesFileFactory.getPropertiesPreferencesFile();
		final Properties p = new Properties();
		try (final FileInputStream inStream = new FileInputStream(preferencesFile)) {
			p.load(inStream);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		logPropertiesNode(p);
		logAncestryable(destinationPreferenceNode);
	}

	/**
	 * @param preferenceNode preference node to be logged.
	 */
	public static void logAncestryable(Ancestryable preferenceNode) {
		final JSONObject object = new JSONObject(preferenceNode.getChildren());
		logger.info( object.toString());
	}

	/**
	 * @param properties Properties to be logged.
	 */
	public static <P extends Properties> void logPropertiesNode(P properties) {
		
		final JSONObject jsonObject = Property.toJSONObject(properties);
		logger.info( jsonObject.toString());
	}
}
