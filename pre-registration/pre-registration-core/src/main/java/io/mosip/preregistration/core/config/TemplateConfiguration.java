package io.mosip.preregistration.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;

/**
 * 
 * Configuration class for setting up the TemplateManager bean.
 *
 * @author Sanober Noor
 * @since 1.0.0
 */
@Configuration
public class TemplateConfiguration {
	/**
	 * Creates a TemplateManager bean using the provided TemplateManagerBuilder.
	 *
	 * @param templateManagerBuilder the builder used to create the TemplateManager
	 * @return the configured TemplateManager instance
	 */
	@Bean
	public TemplateManager templateManager(TemplateManagerBuilder templateManagerBuilder) {
		return templateManagerBuilder.build();
	}
}