/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.DocumentResponseDTO;
import io.mosip.preregistration.application.service.DocumentServiceIntf;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 * This class provides different API's to perform operations on Document upload.
 * 
 * @author Kishan Rathore
 * @author Rajath KR
 * @since 1.0.0
 */
@RestController
@RequestMapping("/")
@Tag(name = "document-controller", description = "Document Controller")
public class DocumentController {

	/**
	 * Autowired reference for {@link #DocumentUploadService}
	 */
	@Autowired
	private DocumentServiceIntf documentUploadService;

	/**
	 * Logger configuration for DocumentController
	 */
	private static Logger log = LoggerConfiguration.logConfig(DocumentController.class);

	/**
	 * Post API to upload the document.
	 * 
	 * @param preRegistrationId preRegistrationId
	 * @param reqDto            pass documentString
	 * @param file              pass files
	 * @return response in a format specified in API document
	 * 
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdocumentspreregistrationid())")
	@PostMapping(path = "/documents/{preRegistrationId}", consumes = { "multipart/form-data" })
	@Operation(summary = "fileUpload", description = "Document Upload", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DocumentResponseDTO>> fileUpload(
			@PathVariable(value = "preRegistrationId") String preRegistrationId,
			@RequestPart(value = "Document request", required = true) String reqDto,
			@RequestPart(value = "file", required = true) MultipartFile file) {
		log.debug("sessionId", "idType", "id", "In doc controller ");
		log.debug("sessionId", "idType", "id", "Pre-id " + preRegistrationId);
		log.info("sessionId", "idType", "id",
				"In fileUpload method of document controller to upload the document for request " + reqDto.toString());
		log.info("sessionId", "idType", "id", "iN Controller v2");

		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.uploadDocument(file, reqDto, preRegistrationId));
	}

	/**
	 * 
	 * Post API to copy the document from source to destination by Preregistration
	 * Id
	 * 
	 * @param preRegistrationId pass destination_preId
	 * @param catCode           pass cat_type
	 * @param sourcePrId        pass source_prId
	 * 
	 * @return response in a format specified in API document
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutdocumentspreregistrationid())")
	@PutMapping(path = "/documents/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "copyDocument", description = "Copy uploaded document", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document successfully copied"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DocumentResponseDTO>> copyDocument(
			@Valid @PathVariable(required = true, value = "preRegistrationId") String preRegistrationId,
			@Valid @RequestParam(required = true) String catCode,
			@Valid @RequestParam(required = true) String sourcePreId) {

		log.info("sessionId", "idType", "id",
				"In copyDocument method of document controller to copy the document for request " + catCode + ","
						+ sourcePreId + "," + preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.copyDocument(catCode, sourcePreId, preRegistrationId));
	}

	/**
	 * Get API to fetch all the documents for a Preregistration Id
	 * 
	 * @param pre_registration_id pass preRegistrationId
	 * @return response in a format specified in API document
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetdocumentspreregistrationid())")
	@GetMapping(path = "/documents/preregistration/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getDocumentforDocId", description = "Get All Document for Pre-Registration Id", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Documents reterived successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DocumentsMetaData>> getAllDocumentforPreid(
			@Valid @PathVariable(required = true) String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In getAllDocumentforPreid method of document controller to get all the document for pre_registration_id "
						+ preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.getAllDocumentForPreId(preRegistrationId));
	}

	/**
	 * Get API to fetch document for a document Id
	 * 
	 * @param documentId        pass documentId as path variable
	 * @param preRegistrationId pass preRegistrationId as request param
	 * @return response in a format specified in API document
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetdocumentsdocumentid())")
	@GetMapping(path = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "getDocumentforDocId", description = "Get All Document for Document Id", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Documents reterived successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<DocumentDTO>> getDocumentforDocId(
			@Valid @PathVariable(required = true) String documentId,
			@Valid @RequestParam(required = true, value = "preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In getAllDocumentforDocId method of document controller to get all the document for documentId "
						+ documentId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.getDocumentForDocId(documentId, preRegistrationId));
	}

	/**
	 * Delete API to delete the document for a Document Id
	 * 
	 * 
	 * @param documentId        pass documentId
	 * @param preRegistrationId pass the preRegistratoinId
	 * @return response in a format specified in API document
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeletedocumentsdocumentid())")
	@DeleteMapping(path = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "deleteDocument", description = "Delete document by document Id", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document successfully deleted"),
			@ApiResponse(responseCode = "204", description = "No Content" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			})
	public ResponseEntity<MainResponseDTO<DocumentDeleteResponseDTO>> deleteDocument(
			@Valid @PathVariable(required = true) String documentId,
			@Valid @RequestParam(required = true, value = "preRegistrationId") String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In deleteDocument method of document controller to delete the document for documentId " + documentId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.deleteDocument(documentId, preRegistrationId));

	}

	/**
	 * Delete API to delete all the documents for a preregistrationId
	 * 
	 * @param preRegistrationId pass preregistrationId
	 * @return response in a format specified in API document
	 */

	//@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeletedocumentspreregistrationid())")
	@DeleteMapping(path = "/documents/preregistration/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "deleteAllByPreId", description = "Delete all documents by pre-registration Id", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Documents successfully deleted"),
			@ApiResponse(responseCode = "204", description = "No Content" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
	})
	public ResponseEntity<MainResponseDTO<DocumentDeleteResponseDTO>> deleteAllByPreId(
			@Valid @PathVariable(required = true) String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In deleteDocument method of document controller to delete all the document for preId "
						+ preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(documentUploadService.deleteAllByPreId(preRegistrationId));
	}

	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutdocumentsdocumentid())")
	@PutMapping(path = "/documents/document/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "updateDocRefId", description = "update document reference Id", tags = "document-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document Reference Id successfully updated"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<String>> updateDocRefId(
			@Valid @PathVariable(required = true) String documentId,
			@Valid @RequestParam(required = true, value = "preRegistrationId") String preRegistrationId,
			@Valid @RequestParam(required = true, value = "refNumber") String refNumebr) {
		log.info("sessionId", "idType", "id",
				"In updateDocRefId method of document controller to update the docRefId for documentId " + documentId
						+ "preregistrationId " + preRegistrationId + "DocRefId " + refNumebr);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.updateDocRefId(documentId, preRegistrationId, refNumebr));

	}
}
