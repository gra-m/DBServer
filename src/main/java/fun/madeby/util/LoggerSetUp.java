package fun.madeby.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Gra_m on 2022 07 08
 */

public final class LoggerSetUp {
	private static final String LOG_PROPS_FILE_NAME = "config.properties";
	private static final String LOG_LEVEL = "LOG_LEVEL";

	public static Logger setUpLogger(String name) throws IOException {
		Logger LOGGER = Logger.getLogger(name);
		Properties logProperties = new Properties();
		String logLevel = "";

		try (FileInputStream fis = new FileInputStream(LOG_PROPS_FILE_NAME)) {
		logProperties.load(fis);
		}

		boolean hasLogLevel = logProperties.containsKey(LOG_LEVEL);
		if (hasLogLevel) {
			logLevel = (logProperties.getProperty(LOG_LEVEL).trim());
			switch (logLevel.toUpperCase()) {
				case "SEVERE" -> LOGGER.setLevel(Level.SEVERE); // shows when all
				case "WARNING" -> LOGGER.setLevel(Level.WARNING); // shows when all
				case "INFO" -> LOGGER.setLevel(Level.INFO); // shows when all
				case "CONFIG" -> LOGGER.setLevel(Level.CONFIG); // xx
				case "FINE" -> LOGGER.setLevel(Level.FINE); // xx
				case "FINER" -> LOGGER.setLevel(Level.FINER); // xx
				case "FINEST" -> LOGGER.setLevel(Level.FINEST); // xx
				case "OFF" -> LOGGER.setLevel(Level.OFF);
				case "ALL" -> LOGGER.setLevel(Level.ALL);
			}
		} else LOGGER.setLevel(Level.SEVERE);
		LOGGER.finest("@setUpLogger(name): Set logger name: " + name + " to level " + logLevel);
		return LOGGER;
	}
}
