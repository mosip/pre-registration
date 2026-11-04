package io.mosip.preregistration.core.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * Spring configuration for logger configuration in Pre-Registration.
 */
public final class LoggerConfiguration {
	/**
	 * Instantiates a new pre-reg logger.
	 */
	private LoggerConfiguration() {
	}

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @param clazz clazz used by log config
	 * @return log config result
	 */
	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}
