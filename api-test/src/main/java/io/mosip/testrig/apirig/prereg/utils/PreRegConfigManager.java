package io.mosip.testrig.apirig.prereg.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.prereg.testrunner.MosipTestRunner;
import io.mosip.testrig.apirig.utils.ConfigManager;

/**
 * Pre-Registration flavour of the apitest-commons {@link ConfigManager}.
 * <p>
 * Adds the module-specific properties file to the common configuration. All getters (DB URLs,
 * credentials, {@code IsDebugEnabled()}, {@code getproperty(..)}) are inherited from
 * {@link ConfigManager}.
 */
public class PreRegConfigManager extends ConfigManager {
	private static final Logger LOGGER = Logger.getLogger(PreRegConfigManager.class);

	/**
	 * Loads the prereg properties file and passes its entries to {@link ConfigManager#init(Map)}.
	 * <p>
	 * The file is read from {@code <globalResourcePath>/config/}. Its name comes from the system
	 * property {@code prereg.propertiesFile} (default {@code prereg.properties}). A load failure is
	 * logged and the common configuration is still initialised, without module overrides.
	 * <p>
	 * Call after {@code AdminTestUtil.init()} and the resource extraction step, so the config
	 * folder exists.
	 */
	public static void init() {
		Logger configManagerLogger = Logger.getLogger(ConfigManager.class);
		configManagerLogger.setLevel(Level.WARN);

		Map<String, Object> moduleSpecificPropertiesMap = new HashMap<>();
		try {
			String requested = System.getProperty("prereg.propertiesFile", "prereg.properties");
			if (requested == null || requested.isBlank()) {
				requested = "prereg.properties";
			}
			String resourceRoot = MosipTestRunner.getGlobalResourcePath() + "/config/";
			File propsFile = new File(resourceRoot + requested);
			LOGGER.info("Loading prereg properties from: " + propsFile.getAbsolutePath());
			Properties props = getproperties(propsFile.getAbsolutePath());
			for (String key : props.stringPropertyNames()) {
				moduleSpecificPropertiesMap.put(key, props.getProperty(key));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		init(moduleSpecificPropertiesMap);
		// Commons init() parses this into the component map but does not keep the raw
		// string. Reload from the file we just read so booking= (and others) are present.
		Object componentUrls = moduleSpecificPropertiesMap.get("mosip_components_base_urls");
		if (componentUrls instanceof String urls && !urls.isBlank()) {
			loadComponentBaseURLs(urls);
		}
	}

}
