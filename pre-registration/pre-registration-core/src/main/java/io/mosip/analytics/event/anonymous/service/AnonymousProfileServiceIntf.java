package io.mosip.analytics.event.anonymous.service;

import io.mosip.analytics.event.anonymous.dto.AnonymousProfileRequestDTO;
import io.mosip.analytics.event.anonymous.dto.AnonymousProfileResponseDTO;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

/**
 * Business service for anonymous profile service intf in MOSIP Pre-Registration.
 */
public interface AnonymousProfileServiceIntf {

	AuthUserDetails authUserDetails();

	/*
	 * This method is used to save anonymous profile of the user.
	 * 
	 * 
	 * @param request AnonymousProfileRequestDTO
	 * 
	 * @return AnonymousProfileResponseDTO
	 */
	AnonymousProfileResponseDTO saveAnonymousProfile(AnonymousProfileRequestDTO request);

}
