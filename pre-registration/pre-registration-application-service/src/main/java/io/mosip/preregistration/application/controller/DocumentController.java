/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.dto.DocumentResponseDTO;
import io.mosip.preregistration.application.service.DocumentServiceIntf;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides different API's to perform operations on Document upload.
 * 
 * @author Kishan Rathore
 * @author Rajath KR
 * @since 1.0.0
 */
@RestController
@RequestMapping("/")
@Tag(name = "Document Handler")
@CrossOrigin("*")
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
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PostMapping(path = "/documents/{preRegistrationId}", consumes = { "multipart/form-data" })
	@Operation(summary  = "Document Upload")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Document uploaded successfully") })
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
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PutMapping(path = "/documents/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Copy uploaded document")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Document successfully copied") })
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
	@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@GetMapping(path = "/documents/preregistration/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Get All Document for Pre-Registration Id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Documents reterived successfully") })
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
	@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@GetMapping(path = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Get All Document for Document Id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Documents reterived successfully") })
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

	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@DeleteMapping(path = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Delete document by document Id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Document successfully deleted") })
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

	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@DeleteMapping(path = "/documents/preregistration/{preRegistrationId}", produces = MediaType.APPLICATION_JSON_VALUE)

	@Operation(summary  = "Delete all documents by pre-registration Id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Documents successfully deleted") })
	public ResponseEntity<MainResponseDTO<DocumentDeleteResponseDTO>> deleteAllByPreId(
			@Valid @PathVariable(required = true) String preRegistrationId) {
		log.info("sessionId", "idType", "id",
				"In deleteDocument method of document controller to delete all the document for preId "
						+ preRegistrationId);
		return ResponseEntity.status(HttpStatus.OK).body(documentUploadService.deleteAllByPreId(preRegistrationId));
	}

	@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_OFFICER','REGISTRATION_SUPERVISOR','REGISTRATION_ ADMIN')")
	@PutMapping(path = "/documents/document/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "update document reference Id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Document Reference Id successfully updated ") })
	public ResponseEntity<MainResponseDTO<String>> updateDocRefId(
			@Valid @PathVariable(required = true) String documentId,
			@Valid @RequestParam(required = true, value = "preRegistrationId") String preRegistrationId,
			@Valid @RequestParam(required = true, value = "docRefId") String docRefId) {
		log.info("sessionId", "idType", "id",
				"In updateDocRefId method of document controller to update the docRefId for documentId " + documentId
						+ "preregistrationId " + preRegistrationId + "DocRefId " + docRefId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(documentUploadService.updateDocRefId(documentId, preRegistrationId, docRefId));

	}
}
