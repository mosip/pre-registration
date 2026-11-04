package io.mosip.preregistration.booking.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MOSIP booking request-id map ({@code mosip.preregistration.booking.id.*}).
 * OpenAPI for {@code /appointment/**} lives in {@code PreregOpenApiConfig}.
 */
@Configuration
@ConfigurationProperties("mosip.preregistration.booking")
public class BookingConfig {

	/** Request ids keyed by operation name. */
	private Map<String, String> id;

	/**
	 * @param id request-id map from {@code mosip.preregistration.booking.id}
	 */
	public void setId(Map<String, String> id) {
		this.id = id;
	}

	/**
	 * Immutable id map used by {@code BookingExceptionHandler}. Named {@code bookingId}
	 * so it does not collide with application {@code Config#ic()} ({@code @Primary}).
	 *
	 * @return unmodifiable id map
	 */
	@Bean(name = "bookingId")
	public Map<String, String> bookingId() {
		return Collections.unmodifiableMap(id);
	}

}
