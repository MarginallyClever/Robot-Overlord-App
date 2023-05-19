package com.marginallyclever.convenience.log;

import com.marginallyclever.convenience.helpers.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/**
 * static log methods available everywhere
 * @author Dan Royer
 * See org.slf4j.Logger
 */
public class Log {
	private static final Logger logger = LoggerFactory.getLogger(Log.class);
	public static String LOG_FILE_PATH = FileHelper.getUserDirectory();
	public static String LOG_FILE_NAME_TXT = "log.txt";
	public static final String PROGRAM_START_STRING = "PROGRAM START";
	public static final String PROGRAM_END_STRING = "PROGRAM END";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final ArrayList<LogListener> listeners = new ArrayList<>();

	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}

	public static String getLogLocation() {
		return LOG_FILE_PATH+ File.separator + LOG_FILE_NAME_TXT;
	}

	public static void start() {
		System.out.println("log path="+LOG_FILE_PATH);

		boolean hadCrashed = crashReportCheck();

		logger.info(PROGRAM_START_STRING);
		logger.info("------------------------------------------------");
		Properties p = System.getProperties();
		Set<String> names = p.stringPropertyNames();
		for(String n : names) {
			logger.info(n+" = "+p.get(n));
		}
		logger.info("------------------------------------------------");
		if(hadCrashed) {
			logger.info("Crash detected on previous run");
		}
	}

	public static void end() {
		logger.info(PROGRAM_END_STRING);
	}

	private static boolean crashReportCheck() {
		File oldLogFile = new File(LOG_FILE_PATH+LOG_FILE_NAME_TXT);
		if( oldLogFile.exists() ) {
			// read last line of file
			String ending = readLastNonEmptyLine(oldLogFile);
			return !ending.contains(PROGRAM_END_STRING);
		}
		return false;
	}

	/**
	 * wipe the log file
	 */
	public static void deleteOldLog() {
		Path p = Paths.get(getLogLocation());
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * read the last non-empty line of a file
	 * @param file the file to read
	 * @return the last line in the file
	 */
	private static String readLastNonEmptyLine(File file) {
		String lastLine = null;
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			long pointer = raf.length() - 1;
			StringBuilder sb = new StringBuilder();
			while (pointer >= 0) {
				raf.seek(pointer);
				char c = (char) raf.read();
				if (c == '\n') {
					if (sb.length() > 0) {
						lastLine = sb.reverse().toString();
						if (!lastLine.trim().isEmpty()) {
							break;
						}
						sb.setLength(0);
					}
				} else {
					sb.append(c);
				}
				pointer--;
			}
			if (lastLine == null && sb.length() > 0) {
				lastLine = sb.reverse().toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lastLine;
	}

	@Deprecated
	public static String secondsToHumanReadable(double totalTime) {
		return millisecondsToHumanReadable((long)(totalTime*1000));
	}

	/**
	 * Turns milliseconds into h:m:s
	 * @param millis milliseconds
	 * @return human-readable string
	 */
	public static String millisecondsToHumanReadable(long millis) {
		long s = millis / 1000;
		long m = s / 60;
		long h = m / 60;
		m %= 60;
		s %= 60;

		String elapsed = "";
		if (h > 0) elapsed += h + "h";
		if (h > 0 || m > 0) elapsed += m + "m";
		elapsed += s + "s ";

		return elapsed;
	}
}
