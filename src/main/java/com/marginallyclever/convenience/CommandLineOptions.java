package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * store command line options for use in the app
 * @author Admin
 *
 */
public class CommandLineOptions {
	private static final Logger logger = LoggerFactory.getLogger(CommandLineOptions.class);

	protected static String [] argv;
	
	static void setFromMain(String [] argv) {
		CommandLineOptions.argv = argv;

		for(int i=0;i<argv.length;++i) {
			String msg = "START OPTION "+argv[i];
			logger.info(msg);
		}
	}
	
	static public boolean hasOption(String option) {
		for(int i=0;i<argv.length;++i) {
			if(argv[i].equals(option)) {
				return true;
			}
		}
		return false;
	}
}
