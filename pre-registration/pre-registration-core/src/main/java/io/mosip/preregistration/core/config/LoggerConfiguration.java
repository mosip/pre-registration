package io.mosip.preregistration.core.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

public final class LoggerConfiguration {
	/**
	 * Instantiates a new pre-reg logger.
	 */
	private LoggerConfiguration() {
	}

	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}