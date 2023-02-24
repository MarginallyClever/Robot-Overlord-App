package com.marginallyclever.robotoverlord.swinginterface.translator;

import com.marginallyclever.util.PreferencesHelper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

/**
 * MultilingualSupport is the translation engine.  You ask for a string it finds the matching string in the currently selected language.
 *
 * @author dan royer
 * @author Peter Colapietro
 * See <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
 */
public final class Translator {
	private static final Logger logger = LoggerFactory.getLogger(Translator.class);

	/**
	 * Working directory. This represents the directory where the java executable launched the jar from.
	 */
	public static final String LANGUAGES_DIRECTORY = /*File.separator + */"languages"/*+File.separator*/;


	/**
	 * The name of the preferences node containing the user's choice.
	 */
	private static final String LANGUAGE_KEY = "language";

	/**
	 *
	 */
	private static final Preferences languagePreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);


	/**
	 * The default choice when nothing has been selected.
	 */
	private static String defaultLanguage = "English";

	/**
	 * The current choice
	 */
	private static String currentLanguage;

	/**
	 * a list of all languages and their translations strings
	 */
	private static final Map<String, TranslatorLanguage> languages = new HashMap<String, TranslatorLanguage>();

	/**
	 *
	 */
	static public void start() {
		logger.debug("Translator start");
		
		Locale locale = Locale.getDefault();
		defaultLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
		logger.debug("Default language = "+defaultLanguage);

		loadLanguages();
		loadConfig();

		if (isThisTheFirstTimeLoadingLanguageFiles()) {
			chooseLanguage();
		}
	}


	// display a dialog box of available languages and let the user select their preference.
	static public void chooseLanguage() {
		JPanel panel = new JPanel(new BorderLayout());

		final String[] languageList = getLanguageList();
		final JComboBox<String> languageOptions = new JComboBox<String>(languageList);
		int currentIndex = getCurrentLanguageIndex();
		languageOptions.setSelectedIndex(currentIndex);

		panel.add(languageOptions, BorderLayout.CENTER);
		
		int result;
		do {
			result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		saveConfig();
	}


	/**
	 * @return true if this is the first time loading language files (probably on install)
	 */
	static private boolean isThisTheFirstTimeLoadingLanguageFiles() {
		// Did the language file disappear?  Offer the language dialog.
		try {
			if (doesLanguagePreferenceExist()) {
				return false;
			}
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		return true;
	}

	/**
	 * @return true if a preferences node exists
	 * @throws BackingStoreException if the backing store is inaccessible
	 */
	static private boolean doesLanguagePreferenceExist() throws BackingStoreException {
		return Arrays.asList(languagePreferenceNode.keys()).contains(LANGUAGE_KEY);
	}

	/**
	 * save the user's current langauge choice
	 */
	static public void saveConfig() {
		languagePreferenceNode.put(LANGUAGE_KEY, currentLanguage);
	}

	/**
	 * load the user's language choice
	 */
	static public void loadConfig() {
		currentLanguage = languagePreferenceNode.get(LANGUAGE_KEY, defaultLanguage);
	}


	/**
	 * Scan folder for language files.
	 * See <a href="http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file">stackoverflow</a>
	 * @throws IllegalStateException No language files found
	 */
	static public void loadLanguages() {
		try {
			if(loadLanguageInPath(getLanguagesPath())) return;
			if(loadLanguageInPath(getUserDirectory())) return;
		}
		catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		logger.debug("No translations found.  Defaulting to blank language.");
		TranslatorLanguage languageContainer  = new TranslatorLanguage();
		languages.put(languageContainer.getName(), languageContainer);
	}

	private static boolean loadLanguageInPath(Path path) throws IOException {
		logger.debug("Looking for language files in " + path.toString());

		int found = 0;
		Stream<Path> walk = Files.walk(path, 1);	// check inside the JAR file.
		Iterator<Path> it = walk.iterator();
		while( it.hasNext() ) {
			if(loadLanguage(it.next().toString())) found++;
		}
		walk.close();

		return found>0;
	}

	private static boolean loadLanguage(String name) throws FileNotFoundException {
		logger.debug("Looking at " + name);
		// We'll look inside the JAR file first, then look in the working directory. this way
		// new translation files in the working directory will replace the old JAR files.
		//if( f.isDirectory() || f.isHidden() ) continue;
		if (!FilenameUtils.getExtension(name).equalsIgnoreCase("xml")) {
			logger.debug("Skipping, not an XML file.");
			return false;
		}

		// found an XML file in the /languages folder.  Good sign!
		String nameInsideJar = LANGUAGES_DIRECTORY + "/" + FilenameUtils.getName(name);
		InputStream stream = Translator.class.getClassLoader().getResourceAsStream(nameInsideJar);
		String actualFilename = "Jar:" + nameInsideJar;
		File externalFile = new File(name);
		if (externalFile.exists()) {
			stream = new FileInputStream(name);
			actualFilename = name;
		}
		if (stream != null) {
			logger.debug("Found " + actualFilename);
			TranslatorLanguage lang = new TranslatorLanguage();
			try {
				lang.loadFromInputStream(stream);
			} catch (Exception e) {
				logger.error("Failed to load " + actualFilename, e);
				// if the xml file is invalid then an exception can occur.
				// make sure lang is empty in case of a partial-load failure.
				lang = new TranslatorLanguage();
			}

			if (!lang.getName().isEmpty() &&
					!lang.getAuthor().isEmpty()) {
				// we loaded a language file that seems pretty legit.
				languages.put(lang.getName(), lang);
				return true;
			}
		}
		return false;
	}

	private static Path getUserDirectory() {
		logger.debug("Looking for user.dir");
		Path rootPath = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
		logger.debug("user.dir="+rootPath);
		return rootPath;
	}

	/**
	 * @return the path to the working directory
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static Path getLanguagesPath() throws URISyntaxException, IOException {
		logger.debug("Looking for languages path '"+ LANGUAGES_DIRECTORY +"'.");
		URL a = Translator.class.getClassLoader().getResource(LANGUAGES_DIRECTORY);
		assert a != null;
		URI uri = a.toURI();
		logger.debug("found.");

		Path myPath;
		if (uri.getScheme().equals("jar")) {
			FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
			myPath = fileSystem.getPath(LANGUAGES_DIRECTORY);
		} else {
			myPath = Paths.get(uri);
		}
		return myPath;
	}

	/**
	 * @param key they key to translate
	 * @return the translated value for key
	 */
	static public String get(String key) {
		String value = null;
		try {
			value = languages.get(currentLanguage).get(key);
		} catch (Exception e) {
			logger.error("Translated string missing: "+key,e);
			//e.printStackTrace();
		}
		return value;
	}

	/**
	 * @return the array of language names
	 */
	static public String[] getLanguageList() {
		final String[] choices = new String[languages.keySet().size()];
		final Object[] lang_keys = languages.keySet().toArray();

		for (int i = 0; i < lang_keys.length; ++i) {
			choices[i] = (String)lang_keys[i];
		}

		return choices;
	}

	/**
	 * @param currentLanguage the name of the language to make active.
	 */
	static public void setCurrentLanguage(String currentLanguage) {
		Translator.currentLanguage = currentLanguage;
	}

	static public int getCurrentLanguageIndex() {
		String [] set = getLanguageList();
		// find the current language
		for( int i=0;i<set.length; ++i) {
			if( set[i].equals(Translator.currentLanguage)) return i;
		}
		// now try the default
		for( int i=0;i<set.length; ++i) {
			if( set[i].equals(Translator.defaultLanguage)) return i;
		}
		// failed both, return 0 for the first option.
		return 0;
	}
}
