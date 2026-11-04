package io.mosip.preregistration.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Configuration class for Pre-registration
 * 
 * @author Tapaswini Behera
 *
 * @since 1.0.0
 */
@Configuration
public class SSLConfig {

    //Commenting it as its used from Auth Adapter
	// @Bean
	// public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
	// 	return new SimpleClientHttpRequestFactory();
	// }

}
