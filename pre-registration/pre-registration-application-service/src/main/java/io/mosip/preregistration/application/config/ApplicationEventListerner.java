package io.mosip.preregistration.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.DocumentService;
import io.mosip.preregistration.application.service.LoginService;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Configuration
public class ApplicationEventListerner {

	@Autowired
	private LoginService loginService;

	@Autowired
	private DemographicService demographicService;

	@Autowired
	private DocumentService documentService;

	private Logger log = LoggerConfiguration.logConfig(ApplicationEventListerner.class);

	@EventListener(ApplicationStartedEvent.class)
	public void applicationStartedEvent() {
		log.info("sessionId", "idType", "id", "In applicationStartedEvent method of ApplicationEventListerner");
		log.info("sessionId", "idType", "id",
				"In applicationStartedEvent method of ApplicationEventListerner login service setupLoginService method called");
		demographicService.setup();
		log.info("sessionId", "idType", "id",
				"In applicationStartedEvent method of ApplicationEventListerner demographic Service setup() method is called");
		documentService.setup();
		log.info("sessionId", "idType", "id",
				"In applicationStartedEvent method of ApplicationEventListerner documentService setup() method is called");
	}

}
