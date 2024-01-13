package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * store command line options for use in the app
 */
public class CommandLineOptions {
	private static final Logger logger = LoggerFactory.getLogger(CommandLineOptions.class);

	protected String [] argv;

	public void set(String [] argv) {
		this.argv = argv;
		logger.info("Command line options: {}", Arrays.toString(argv));
	}

	public boolean hasOption(String option) {
        for (String s : argv) {
            if (s.equals(option)) {
                return true;
            }
        }
		return false;
	}
}
