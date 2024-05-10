package io.mosip.preregistration.application.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.util.ProxyMasterdataServiceUtil;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
public class ProxyMasterDataService {

	@Autowired
	private ProxyMasterdataServiceUtil util;

	private Logger log = LoggerConfiguration.logConfig(ProxyMasterDataService.class);

	public Object getMasterDataResponse(String body, HttpServletRequest request) {
		log.info("In getMasterDataResponse of proxymasterdataservice");

		return util.masterDataRestCall(util.getUrl(request), body, util.getHttpMethodType(request));

	}

}
