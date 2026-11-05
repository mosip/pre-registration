package io.mosip.preregistration.application.service;

import jakarta.servlet.http.HttpServletRequest;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.util.ProxyMasterdataServiceUtil;
import io.mosip.preregistration.core.config.LoggerConfiguration;

@Service
/**
 * Business service for proxy master data service in MOSIP Pre-Registration.
 */
public class ProxyMasterDataService {
	@Autowired
	/**
	 * util used by this Pre-Registration type.
	 */
	private ProxyMasterdataServiceUtil util;

	/**
	 * Performs log config for MOSIP Pre-Registration.
	 *
	 * @return log config result
	 */
	private Logger log = LoggerConfiguration.logConfig(ProxyMasterDataService.class);

	/**
	 * Returns master data response.
	 *
	 * @param body body used by get master data response
	 * @param request request used by get master data response
	 * @return get master data response result
	 */
	public Object getMasterDataResponse(String body, HttpServletRequest request) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getMasterDataResponse of proxymasterdataservice");

		return util.masterDataRestCall(util.getUrl(request), body, util.getHttpMethodType(request));
	}
}
