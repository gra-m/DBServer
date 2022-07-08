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
		logProperties.load(new FileInputStream(LOG_PROPS_FILE_NAME));
		boolean hasLogLevel = logProperties.containsKey(LOG_LEVEL);
		if (hasLogLevel) {
			logLevel = (logProperties.getProperty(LOG_LEVEL).trim());
			switch (logLevel.toUpperCase()) {
				case "SEVERE" -> LOGGER.setLevel(Level.SEVERE);
				case "WARNING" -> LOGGER.setLevel(Level.WARNING);
				case "INFO" -> LOGGER.setLevel(Level.INFO);
				case "CONFIG" -> LOGGER.setLevel(Level.CONFIG);
				case "FINE" -> LOGGER.setLevel(Level.FINE);
				case "FINER" -> LOGGER.setLevel(Level.FINER);
				case "FINEST" -> LOGGER.setLevel(Level.FINEST);
				case "OFF" -> LOGGER.setLevel(Level.OFF);
				case "ALL" -> LOGGER.setLevel(Level.ALL);
			}
		} else LOGGER.setLevel(Level.SEVERE);
		LOGGER.info("@setUpLogger(name): Set logger name: " + name + " to level " + logLevel);
		return LOGGER;
	}
}
