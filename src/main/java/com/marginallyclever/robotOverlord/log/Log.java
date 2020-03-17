package com.marginallyclever.robotOverlord.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * static log methods available everywhere
 * @author Dan Royer
 * @since Makelangelo 7.3.0
 * See org.slf4j.Logger
 */
public class Log {
	private static final Logger logger = LoggerFactory.getLogger(RobotOverlord.class);
	private static ArrayList<LogListener> listeners = new ArrayList<LogListener>();

	
	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}
	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * wipe the log file
	 * @author dan royer
	 */
	public static void clear() {
		Path p = FileSystems.getDefault().getPath("log.html");
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// print starting time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		write("<h3>" + sdf.format(cal.getTime()) + "</h3>\n");
	}


	/**
	 * Appends a message to the log file
	 * @param msg HTML to put in the log file
	 */
	public static void write(String msg) {
		try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream("log.html", true), StandardCharsets.UTF_8)) {
			PrintWriter logToFile = new PrintWriter(fileWriter);
			logToFile.write(msg);
			logToFile.flush();
		} catch (IOException e) {
			logger.error("{}", e);
		}
		
		for( LogListener listener : listeners ) {
			listener.logEvent(msg);
		}
	}


	/**
	 * Turns milliseconds into h:m:s
	 * @param millis milliseconds
	 * @return human-redable string
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
	

	/**
	 * Appends a message to the log file
	 * @param color the hex code or HTML name of the color for this message
	 * @param msg the text
	 */
	public static void write(String color, String msg) {
		write("<font color='"+color+"'>"+msg+"</font>\n");
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message append text as red HTML
	 */
	public static void error(String message) {
		write("red",message);
	}

	/**
	 * Appends a message to the log file.  Color will be green.
	 * @param message append text as green HTML
	 */
	public static void message(String message) {
		write("green",message);		
	}
}
