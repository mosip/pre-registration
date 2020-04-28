package io.mosip.preregistration.document.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.EventId;
import io.mosip.preregistration.core.code.EventName;
import io.mosip.preregistration.core.code.EventType;
import io.mosip.preregistration.core.code.RequestCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.InvalidRequestException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.AuthTokenUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.document.code.DocumentStatusMessages;
import io.mosip.preregistration.document.dto.DocumentRequestDTO;
import io.mosip.preregistration.document.dto.DocumentResponseDTO;
import io.mosip.preregistration.document.errorcodes.ErrorCodes;
import io.mosip.preregistration.document.errorcodes.ErrorMessages;
import io.mosip.preregistration.document.exception.DocumentFailedToCopyException;
import io.mosip.preregistration.document.exception.DocumentFailedToUploadException;
import io.mosip.preregistration.document.exception.DocumentNotFoundException;
import io.mosip.preregistration.document.exception.FSServerException;
import io.mosip.preregistration.document.exception.InvalidDocumentIdExcepion;
import io.mosip.preregistration.document.exception.util.DocumentExceptionCatcher;
import io.mosip.preregistration.document.repository.util.DocumentDAO;
import io.mosip.preregistration.document.service.util.DocumentServiceUtil;

/**
 * This class provides the service implementation for Document
 * 
 * @author Kishan Rathore
 * @author Rajath KR
 * @author Tapaswini Behera
 * @author Jagadishwari S
 * @since 1.0.0
 */
@Component
public class DocumentService implements DocumentServiceIntf {

	/**
	 * Autowired reference for {@link #DocumnetDAO}
	 */
	@Autowired
	private DocumentDAO documnetDAO;

	/**
	 * Reference for ${mosip.preregistration.document.upload.id} from property file
	 */
	@Value("${mosip.preregistration.document.upload.id}")
	private String uploadId;

	/**
	 * Reference for ${mosip.preregistration.document.scan} from property file
	 */
	@Value("${mosip.preregistration.document.scan}")
	private Boolean scanDocument;

	/**
	 * Reference for ${mosip.preregistration.document.copy.id} from property file
	 */
	@Value("${mosip.preregistration.document.copy.id}")
	private String copyId;

	/**
	 * Reference for ${mosip.preregistration.document.fetch.metadata.id} from
	 * property file
	 */
	@Value("${mosip.preregistration.document.fetch.metadata.id}")
	private String fetchMetaDataId;

	/**
	 * Reference for ${mosip.preregistration.document.fetch.content.id} from
	 * property file
	 */
	@Value("${mosip.preregistration.document.fetch.content.id}")
	private String fetchContentId;

	/**
	 * Reference for ${mosip.preregistration.document.delete.id} from property file
	 */
	@Value("${mosip.preregistration.document.delete.id}")
	private String deleteId;

	/**
	 * Reference for ${mosip.preregistration.document.delete.specific.id} from
	 * property file
	 */
	@Value("${mosip.preregistration.document.delete.specific.id}")
	private String deleteSpecificId;
	/**
	 * Reference for ${version} from property file
	 */
	@Value("${version}")
	private String ver;

	/**
	 * primaryLang
	 */
	@Value("${mosip.primary-language}")
	private String primaryLang;

	/**
	 * Autowired reference for {@link #FileSystemAdapter}
	 */
	@Autowired
	private FileSystemAdapter fs;

	/**
	 * Autowired reference for {@link #DocumentServiceUtil}
	 */
	@Autowired
	private DocumentServiceUtil serviceUtil;

	/**
	 * Request map to store the id and version and this is to be passed to request
	 * validator method.
	 */
	Map<String, String> requiredRequestMap = new HashMap<>();

	/**
	 * Autowired reference for {@link #AuditLogUtil}
	 */
	@Autowired
	private AuditLogUtil auditLogUtil;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	ValidationUtil validationUtil;

	@Autowired
	private AuthTokenUtil tokenUtil;

	/**
	 * Logger configuration for document service
	 */
	private static Logger log = LoggerConfiguration.logConfig(DocumentService.class);

	/**
	 * This method acts as a post constructor to initialize the required request
	 * parameters.
	 */
	@PostConstruct
	public void setup() {
		HttpHeaders headers = tokenUtil.getTokenHeader();
		requiredRequestMap.put("version", ver);
		validationUtil.getAllDocCategoriesAndTypes(primaryLang, headers);
	}

	public AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * This method is used to upload the document by accepting the JsonString and
	 * MultipartFile
	 * 
	 * @param file
	 *            pass the file
	 * @param documentJsonString
	 *            pass document json
	 * @return ResponseDTO
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public MainResponseDTO<DocumentResponseDTO> uploadDocument(MultipartFile file, String documentJsonString,
			String preRegistrationId) {
		log.info("sessionId", "idType", "id", "In service");
		log.info("sessionId", "idType", "id",
				"In uploadDocument method of document service with document string " + documentJsonString);
		MainResponseDTO<DocumentResponseDTO> responseDto = new MainResponseDTO<>();
		MainRequestDTO<DocumentRequestDTO> docReqDto = null;
		boolean isUploadSuccess = false;
		responseDto.setId(uploadId);
		responseDto.setVersion(ver);
		try {
			log.info("sessionId", "idType", "id",
					"calling serviceUtil.createUploadDto  preRegistrationId" + preRegistrationId);
			docReqDto = serviceUtil.createUploadDto(documentJsonString, preRegistrationId);
			responseDto.setId(docReqDto.getId());
			responseDto.setVersion(docReqDto.getVersion());
			requiredRequestMap.put("id", uploadId);
			if (validationUtil.requestValidator(prepareRequestParamMap(docReqDto), requiredRequestMap)) {
				if (scanDocument) {
					serviceUtil.isVirusScanSuccess(file);
				}
				if (serviceUtil.fileSizeCheck(file.getSize()) && serviceUtil.fileExtensionCheck(file)) {
					serviceUtil.isValidRequest(docReqDto.getRequest(), preRegistrationId);
					validationUtil.langvalidation(docReqDto.getRequest().getLangCode());
					log.info("sessionId", "idType", "id",
							"calling validationUtil.validateDocuments preRegistrationId" + preRegistrationId);
					validationUtil.validateDocuments(docReqDto.getRequest().getLangCode(),
							docReqDto.getRequest().getDocCatCode(), docReqDto.getRequest().getDocTypCode(),
							preRegistrationId);
					DocumentResponseDTO docResponseDtos = createDoc(docReqDto.getRequest(), file, preRegistrationId);
					responseDto.setResponse(docResponseDtos);
				}
			}
			isUploadSuccess = true;
			responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In uploadDoucment method of document service - " + ex.getMessage());
			new DocumentExceptionCatcher().handle(ex, responseDto);
		} finally {

			if (isUploadSuccess) {
				setAuditValues(EventId.PRE_404.toString(), EventName.UPLOAD.toString(), EventType.BUSINESS.toString(),
						"Document uploaded & the respective Pre-Registration data is saved in the document table",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Document upload failed & the respective Pre-Registration data save unsuccessfull ",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			}
		}

		return responseDto;
	}

	/**
	 * This method is used to store the uploaded document into table
	 * 
	 * @param document
	 *            pass the document
	 * @param file
	 *            pass file
	 * @return ResponseDTO
	 * @throws IOException
	 *             on input errors
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public DocumentResponseDTO createDoc(DocumentRequestDTO document, MultipartFile file, String preRegistrationId)
			throws IOException, EncryptionFailedException {
		log.info("sessionId", "idType", "id", "In createDoc method of document service");
		DocumentResponseDTO docResponseDto = new DocumentResponseDTO();
		if (serviceUtil.getPreRegInfoRestService(preRegistrationId)) {
			DocumentEntity getentity = documnetDAO.findSingleDocument(preRegistrationId, document.getDocCatCode());
			DocumentEntity documentEntity = serviceUtil.dtoToEntity(file, document, authUserDetails().getUserId(),
					preRegistrationId, getentity);
			if (getentity != null) {
				documentEntity.setDocumentId(String.valueOf(getentity.getDocumentId()));
			}
			documentEntity.setDocName(file.getOriginalFilename());
			LocalDateTime encryptedTimestamp = DateUtils.getUTCCurrentDateTime();
			documentEntity.setEncryptedDateTime(encryptedTimestamp);
			byte[] encryptedDocument = cryptoUtil.encrypt(file.getBytes(), encryptedTimestamp);
			documentEntity.setDocHash(HashUtill.hashUtill(encryptedDocument));
			documentEntity = documnetDAO.saveDocument(documentEntity);
			String key = documentEntity.getDocCatCode() + "_" + documentEntity.getDocumentId();

			boolean isStoreSuccess = fs.storeFile(documentEntity.getDemographicEntity().getPreRegistrationId(), key,
					new ByteArrayInputStream(encryptedDocument));

			if (!isStoreSuccess) {
				throw new FSServerException(ErrorCodes.PRG_PAM_DOC_009.toString(),
						ErrorMessages.DOCUMENT_FAILED_TO_UPLOAD.getMessage());
			}
			docResponseDto.setPreRegistrationId(documentEntity.getDemographicEntity().getPreRegistrationId());
			docResponseDto.setDocId(String.valueOf(documentEntity.getDocumentId()));
			docResponseDto.setDocName(documentEntity.getDocName());
			docResponseDto.setDocCatCode(documentEntity.getDocCatCode());
			docResponseDto.setDocTypCode(documentEntity.getDocTypeCode());
			docResponseDto.setDocFileFormat(FilenameUtils.getExtension(documentEntity.getDocName()));

		}
		return docResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.preregistration.document.service.DocumentServiceIntf#copyDocument(
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MainResponseDTO<DocumentResponseDTO> copyDocument(String catCode, String sourcePreId,
			String destinationPreId) {
		log.info("sessionId", "idType", "id", "In copyDocument method of document service");
		String sourceBucketName;
		String sourceKey;
		MainResponseDTO<DocumentResponseDTO> responseDto = new MainResponseDTO<>();
		responseDto.setId(copyId);
		responseDto.setVersion(ver);
		boolean isCopySuccess = false;
		try {
			if (sourcePreId == null || sourcePreId.isEmpty() || destinationPreId == null
					|| destinationPreId.isEmpty()) {
				throw new InvalidRequestException(
						io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_001.toString(),
						io.mosip.preregistration.core.errorcodes.ErrorMessages.MISSING_REQUEST_PARAMETER.getMessage(),
						null);
			} else if (serviceUtil.isValidCatCode(catCode)) {
				boolean sourceStatus = serviceUtil.getPreRegInfoRestService(sourcePreId);
				boolean destinationStatus = serviceUtil.getPreRegInfoRestService(destinationPreId);

				DocumentEntity documentEntity = documnetDAO.findSingleDocument(sourcePreId, catCode);
				DocumentEntity destEntity = documnetDAO.findSingleDocument(destinationPreId, catCode);
				if (documentEntity != null && sourceStatus && destinationStatus) {
					DocumentEntity copyDocumentEntity = documnetDAO.saveDocument(
							serviceUtil.documentEntitySetter(destinationPreId, documentEntity, destEntity));
					sourceKey = documentEntity.getDocCatCode() + "_" + documentEntity.getDocumentId();
					sourceBucketName = documentEntity.getDemographicEntity().getPreRegistrationId();
					copyFile(copyDocumentEntity, sourceBucketName, sourceKey);
					DocumentResponseDTO documentResponseDTO = new DocumentResponseDTO();
					documentResponseDTO.setPreRegistrationId(destinationPreId);
					documentResponseDTO.setDocId(copyDocumentEntity.getDocumentId());
					documentResponseDTO.setDocName(copyDocumentEntity.getDocName());
					documentResponseDTO.setDocCatCode(copyDocumentEntity.getDocCatCode());
					documentResponseDTO.setDocTypCode(copyDocumentEntity.getDocTypeCode());
					documentResponseDTO.setDocFileFormat(copyDocumentEntity.getDocFileFormat());
					responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
					responseDto.setResponse(documentResponseDTO);
				} else {
					throw new DocumentNotFoundException(ErrorCodes.PRG_PAM_DOC_005.toString(),
							DocumentStatusMessages.DOCUMENT_IS_MISSING.getMessage());
				}
			}
			isCopySuccess = true;

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In copyDoucment method of document service - " + ex.getMessage());
			new DocumentExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isCopySuccess) {
				setAuditValues(EventId.PRE_409.toString(), EventName.COPY.toString(), EventType.BUSINESS.toString(),
						"Document copied from source PreId to destination PreId is successfully saved in the document table",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(), EventType.SYSTEM.toString(),
						"Document failed to copy from source PreId to destination PreId ",
						AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			}
		}
		return responseDto;

	}

	/**
	 * This method will copy the file from sourceFile to destinationFile
	 * 
	 * @param copyDocumentEntity
	 * @param sourceBucketName
	 * @param sourceKey
	 */
	public void copyFile(DocumentEntity copyDocumentEntity, String sourceBucketName, String sourceKey) {
		String destinationBucketName;
		String destinationKey;
		if (copyDocumentEntity != null) {
			destinationBucketName = copyDocumentEntity.getDemographicEntity().getPreRegistrationId();
			destinationKey = copyDocumentEntity.getDocCatCode() + "_" + copyDocumentEntity.getDocumentId();
			boolean isStoreSuccess = fs.copyFile(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
			if (!isStoreSuccess) {
				throw new FSServerException(ErrorCodes.PRG_PAM_DOC_009.toString(),
						ErrorMessages.DOCUMENT_FAILED_TO_UPLOAD.getMessage());
			}

		} else {
			throw new DocumentFailedToCopyException(ErrorCodes.PRG_PAM_DOC_011.toString(),
					ErrorMessages.DOCUMENT_FAILED_TO_COPY.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.document.service.DocumentServiceIntf#
	 * getAllDocumentForPreId(java.lang.String)
	 */
	@Override
	public MainResponseDTO<DocumentsMetaData> getAllDocumentForPreId(String preId) {
		log.info("sessionId", "idType", "id", "In getAllDocumentForPreId method of document service");
		MainResponseDTO<DocumentsMetaData> responseDto = new MainResponseDTO<>();
		responseDto.setId(fetchMetaDataId);
		responseDto.setVersion(ver);
		boolean isRetrieveSuccess = false;
		boolean isDocNotFound = false;
		Map<String, String> requestParamMap = new HashMap<>();
		try {
			requestParamMap.put(RequestCodes.PRE_REGISTRATION_ID, preId);
			if (validationUtil.requstParamValidator(requestParamMap) && serviceUtil.getPreRegInfoRestService(preId)) {
				List<DocumentEntity> documentEntities = documnetDAO.findBypreregId(preId);
				responseDto.setResponse(createDocumentResponse(documentEntities));
				responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
			}
			isRetrieveSuccess = true;

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getAllDocumentForPreId method of document service - " + ex.getMessage());
			if (ex instanceof DocumentNotFoundException)
				isDocNotFound = true;
			new DocumentExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_401.toString(), EventName.RETRIEVE.toString(), EventType.BUSINESS.toString(),
						"Retrieval of document is successfull", AuditLogVariables.MULTIPLE_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			} else {
				if (isDocNotFound) {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "No documents found for the application",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				} else {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "Retrieval of document is failed",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				}
			}
		}
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.document.service.DocumentServiceIntf#
	 * getDocumentForDocId(java.lang.String, java.lang.String)
	 */
	@Override
	public MainResponseDTO<DocumentDTO> getDocumentForDocId(String docId, String preId) {
		log.info("sessionId", "idType", "id", "In getAllDocumentForDocId method of document service");
		MainResponseDTO<DocumentDTO> responseDto = new MainResponseDTO<>();
		DocumentDTO docDto = new DocumentDTO();
		responseDto.setId(fetchContentId);
		responseDto.setVersion(ver);
		boolean isRetrieveSuccess = false;
		boolean isDocNotFound = false;
		Map<String, String> requestParamMap = new HashMap<>();
		try {
			requestParamMap.put(RequestCodes.PRE_REGISTRATION_ID, preId);
			if (validationUtil.requstParamValidator(requestParamMap) && serviceUtil.getPreRegInfoRestService(preId)) {
				DocumentEntity documentEntity = documnetDAO.findBydocumentId(docId);
				if (!documentEntity.getDemographicEntity().getPreRegistrationId().equals(preId)) {
					throw new InvalidDocumentIdExcepion(ErrorCodes.PRG_PAM_DOC_022.name(),
							ErrorMessages.INVALID_DOCUMENT_ID.getMessage());
				}
				String key = documentEntity.getDocCatCode() + "_" + documentEntity.getDocumentId();
				InputStream sourcefile = fs.getFile(documentEntity.getDemographicEntity().getPreRegistrationId(), key);
				if (sourcefile == null) {
					throw new FSServerException(ErrorCodes.PRG_PAM_DOC_005.toString(),
							ErrorMessages.DOCUMENT_FAILED_TO_FETCH.getMessage());
				}
				byte[] cephBytes = IOUtils.toByteArray(sourcefile);
				if (documentEntity.getDocHash().equals(HashUtill.hashUtill(cephBytes))) {
					docDto.setDocument(cryptoUtil.decrypt(cephBytes, documentEntity.getEncryptedDateTime()));
					responseDto.setResponse(docDto);
				} else {
					log.error("sessionId", "idType", "id", "In dtoSetter method of document service - "
							+ io.mosip.preregistration.core.errorcodes.ErrorMessages.HASHING_FAILED.name());
					throw new HashingException(
							io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_010.name(),
							io.mosip.preregistration.core.errorcodes.ErrorMessages.HASHING_FAILED.name());
				}
				responseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
			}
			isRetrieveSuccess = true;

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In getAllDocumentForPreId method of document service - " + ex.getMessage());
			if (ex instanceof DocumentNotFoundException)
				isDocNotFound = true;
			new DocumentExceptionCatcher().handle(ex, responseDto);
		} finally {
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_401.toString(), EventName.RETRIEVE.toString(), EventType.BUSINESS.toString(),
						"Retrieval of document is successfull", AuditLogVariables.MULTIPLE_ID.toString(),
						authUserDetails().getUserId(), authUserDetails().getUsername());
			} else {
				if (isDocNotFound) {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "No documents found for the application",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				} else {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "Retrieval of document is failed",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				}
			}
		}
		return responseDto;
	}

	/**
	 * This method will set the document Dto from document entity
	 * 
	 * @param entityList
	 * 
	 * @return List<DocumentMultipartResponseDTO>
	 * @throws IOException
	 */
	public DocumentsMetaData createDocumentResponse(List<DocumentEntity> entityList) throws IOException {
		List<DocumentMultipartResponseDTO> allDocRes = new ArrayList<>();
		DocumentsMetaData documentsMetaData = new DocumentsMetaData();
		for (DocumentEntity doc : entityList) {
			System.out.println("Demographic preid: " + doc.getDemographicEntity().getStatusCode());
			DocumentMultipartResponseDTO allDocDto = new DocumentMultipartResponseDTO();
			allDocDto.setDocCatCode(doc.getDocCatCode());
			allDocDto.setDocName(doc.getDocName());
			allDocDto.setDocumentId(doc.getDocumentId());
			allDocDto.setDocTypCode(doc.getDocTypeCode());
			allDocDto.setLangCode(doc.getLangCode());
			allDocRes.add(allDocDto);
		}
		documentsMetaData.setDocumentsMetaData(allDocRes);
		return documentsMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.preregistration.document.service.DocumentServiceIntf#deleteDocument(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MainResponseDTO<DocumentDeleteResponseDTO> deleteDocument(String documentId, String preRegistrationId) {
		log.info("sessionId", "idType", "id", "In deleteDocument method of document service");
		MainResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainResponseDTO<>();
		delResponseDto.setId(deleteSpecificId);
		delResponseDto.setVersion(ver);
		boolean isRetrieveSuccess = false;
		boolean isDocNotFound = false;
		Map<String, String> requestParamMap = new HashMap<>();
		try {
			requestParamMap.put(RequestCodes.PRE_REGISTRATION_ID, preRegistrationId);
			if (validationUtil.requstParamValidator(requestParamMap)
					&& serviceUtil.getPreRegInfoRestService(preRegistrationId)) {
				DocumentEntity documentEntity = documnetDAO.findBydocumentId(documentId);
				if (!documentEntity.getDemographicEntity().getPreRegistrationId().equals(preRegistrationId)) {
					throw new InvalidDocumentIdExcepion(ErrorCodes.PRG_PAM_DOC_022.name(),
							ErrorMessages.INVALID_DOCUMENT_ID.getMessage());
				}
				if (documnetDAO.deleteAllBydocumentId(documentId) > 0) {
					String key = documentEntity.getDocCatCode() + "_" + documentEntity.getDocumentId();
					boolean isDeleted = fs.deleteFile(documentEntity.getDemographicEntity().getPreRegistrationId(),
							key);
					if (!isDeleted) {
						throw new FSServerException(ErrorCodes.PRG_PAM_DOC_006.toString(),
								ErrorMessages.DOCUMENT_FAILED_TO_DELETE.getMessage());
					}
					DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
					deleteDTO.setMessage(DocumentStatusMessages.DOCUMENT_DELETE_SUCCESSFUL.getMessage());
					delResponseDto.setResponse(deleteDTO);
				}
			}
			delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());
			isRetrieveSuccess = true;

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In deleteDocument method of document service - " + ex.getMessage());
			if (ex instanceof DocumentNotFoundException)
				isDocNotFound = true;
			new DocumentExceptionCatcher().handle(ex, delResponseDto);
		} finally {
			if (isRetrieveSuccess) {
				setAuditValues(EventId.PRE_403.toString(), EventName.DELETE.toString(), EventType.BUSINESS.toString(),
						"Document successfully deleted from the document table",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				if (isDocNotFound) {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "No documents found for the application",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				} else {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "Document deletion failed", AuditLogVariables.NO_ID.toString(),
							authUserDetails().getUserId(), authUserDetails().getUsername());
				}
			}
		}
		return delResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.preregistration.document.service.DocumentServiceIntf#
	 * deleteAllByPreId(java.lang.String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MainResponseDTO<DocumentDeleteResponseDTO> deleteAllByPreId(String preregId) {
		log.info("sessionId", "idType", "id", "In deleteAllByPreId method of document service");
		boolean isDeleteSuccess = false;
		boolean isDocNotFound = false;
		MainResponseDTO<DocumentDeleteResponseDTO> deleteRes = new MainResponseDTO<>();
		deleteRes.setId(deleteId);
		deleteRes.setVersion(ver);
		Map<String, String> requestParamMap = new HashMap<>();
		try {
			requestParamMap.put(RequestCodes.PRE_REGISTRATION_ID, preregId);
			if (validationUtil.requstParamValidator(requestParamMap)
					&& serviceUtil.getPreRegInfoRestService(preregId)) {
				List<DocumentEntity> documentEntityList = documnetDAO.findBypreregId(preregId);
				DocumentDeleteResponseDTO deleteDTO = deleteFile(documentEntityList, preregId);
				deleteRes.setResponse(deleteDTO);
				deleteRes.setResponsetime(serviceUtil.getCurrentResponseTime());
			}

			isDeleteSuccess = true;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In deleteAllByPreId method of document service - " + ex.getMessage());
			if (ex instanceof DocumentNotFoundException)
				isDocNotFound = true;
			new DocumentExceptionCatcher().handle(ex, deleteRes);
		} finally {
			if (isDeleteSuccess) {
				setAuditValues(EventId.PRE_403.toString(), EventName.DELETE.toString(), EventType.BUSINESS.toString(),
						"Document successfully deleted from the document table",
						AuditLogVariables.MULTIPLE_ID.toString(), authUserDetails().getUserId(),
						authUserDetails().getUsername());
			} else {
				if (isDocNotFound) {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "No documents found for the application",
							AuditLogVariables.NO_ID.toString(), authUserDetails().getUserId(),
							authUserDetails().getUsername());
				} else {
					setAuditValues(EventId.PRE_405.toString(), EventName.EXCEPTION.toString(),
							EventType.SYSTEM.toString(), "Document deletion failed", AuditLogVariables.NO_ID.toString(),
							authUserDetails().getUserId(), authUserDetails().getUsername());
				}
			}
		}
		return deleteRes;
	}

	public DocumentDeleteResponseDTO deleteFile(List<DocumentEntity> documentEntityList, String preregId) {
		log.info("sessionId", "idType", "id", "In pre-registration service inside delete File method " + preregId);
		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		if (documnetDAO.deleteAllBypreregId(preregId) >= 0) {
			for (DocumentEntity documentEntity : documentEntityList) {
				String key = documentEntity.getDocCatCode() + "_" + documentEntity.getDocumentId();
				fs.deleteFile(documentEntity.getDemographicEntity().getPreRegistrationId(), key);
			}
			deleteDTO.setMessage(DocumentStatusMessages.ALL_DOCUMENT_DELETE_SUCCESSFUL.getMessage());
		}

		return deleteDTO;
	}

	/**
	 * This method is used to audit all the document events
	 * 
	 * @param eventId
	 * @param eventName
	 * @param eventType
	 * @param description
	 * @param idType
	 */
	public void setAuditValues(String eventId, String eventName, String eventType, String description, String idType,
			String userId, String userName) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setEventId(eventId);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setDescription(description);
		auditRequestDto.setSessionUserId(userId);
		auditRequestDto.setSessionUserName(userName);
		auditRequestDto.setId(idType);
		auditRequestDto.setModuleId(AuditLogVariables.DOC.toString());
		auditRequestDto.setModuleName(AuditLogVariables.DOCUMENT_SERVICE.toString());
		auditLogUtil.saveAuditDetails(auditRequestDto);
	}

	/**
	 * This method is used to add the initial request values into a map for input
	 * validations.
	 * 
	 * @param MainRequestDTO
	 *            pass requestDTO
	 * @return a map for request input validation
	 */
	public Map<String, String> prepareRequestParamMap(MainRequestDTO<DocumentRequestDTO> requestDTO) {
		Map<String, String> inputValidation = new HashMap<>();
		inputValidation.put(RequestCodes.ID, requestDTO.getId());
		inputValidation.put(RequestCodes.VER, requestDTO.getVersion());
		if (!(requestDTO.getRequesttime() == null || requestDTO.getRequesttime().toString().isEmpty())) {
			LocalDate date = requestDTO.getRequesttime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
			inputValidation.put(RequestCodes.REQ_TIME, date.toString());
		} else {
			inputValidation.put(RequestCodes.REQ_TIME, null);
		}
		inputValidation.put(RequestCodes.REQUEST, requestDTO.getRequest().toString());
		return inputValidation;
	}
}
