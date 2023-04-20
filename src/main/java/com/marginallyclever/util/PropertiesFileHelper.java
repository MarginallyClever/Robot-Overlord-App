package com.marginallyclever.util;

import com.marginallyclever.robotoverlord.RobotOverlord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Peter Colapietro
 * @since 2015-10-05
 */
public final class PropertiesFileHelper {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileHelper.class);
    private static final String APP_PROPERTIES_FILENAME = "robotoverlord.properties";

    /**
     * @return version number in the form of vX.Y.Z where X is MAJOR, Y is MINOR version, and Z is PATCH
     * See <a href="http://semver.org/">Semantic Versioning 2.0.0</a>
     * @throws IllegalStateException ??
     */
    public static String getVersionPropertyValue() throws IllegalStateException, IOException {
        String versionPropertyValue = "";
        try (final InputStream input = RobotOverlord.class.getClassLoader().getResourceAsStream(APP_PROPERTIES_FILENAME)) {
            if (input == null) {
                throw new IllegalStateException("Sorry, unable to find " + APP_PROPERTIES_FILENAME);
            }
            Properties prop = new Properties();
            //load a properties file from class path, inside static method
            prop.load(input);

            //get the property value and print it out
            versionPropertyValue = prop.getProperty("robotoverlord.version");
            logger.info("robotoverlord.version=" + versionPropertyValue);

        } catch (IllegalStateException | IOException ex) {
            logger.error(ex.getMessage());
            throw ex;
        }
        return versionPropertyValue;
    }
}
