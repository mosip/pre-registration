/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.controller;

import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.util.DataValidationUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This class provides different API's to perform operations on
 * Transliteration Application
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping("/transliteration")
@Tag(name = "Transliteration Controller", description = "Transliteration Controller")
public class TransliterationController {

	/** Autowired reference for {@link #transliterationService}. */
	@Autowired
	private TransliterationService transliterationService;
	
	@Autowired
	private RequestValidator requestValidator;
	
	/** The Constant for GET UPDATED DATE TIME application. */
	private static final String TRANS = "pre-registration.transliteration.transliterate";
	
	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	/**
	 * Post API to transliterate from transliteration application.
	 * 
	 * @param requestDTO
	 * @return responseDto with transliterated toFieldValue. 
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPosttransliterationtransliterate())")
	@PostMapping(path = "/transliterate", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Get Pre-Registartion-Translitration data", description = "Get Pre-Registartion-Translitration data", tags = "Transliteration Controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Given key is translitrated successfully"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<TransliterationResponseDTO>> translitrator(
			@Validated @RequestBody(required = true) MainRequestDTO<TransliterationRequestDTO> requestDTO, @ApiIgnore Errors errors) {
		requestValidator.validateId(TRANS, requestDTO.getId(), errors);
		DataValidationUtil.validate(errors,TRANS);
		return ResponseEntity.status(HttpStatus.OK).body(transliterationService.translitratorService(requestDTO));
	}
}
