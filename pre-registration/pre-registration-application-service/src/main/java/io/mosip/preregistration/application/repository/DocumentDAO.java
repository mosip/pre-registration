package io.mosip.preregistration.application.repository;

import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_ID;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.application.constant.PreRegApplicationConstant.LOGGER_SESSIONID;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.application.code.DocumentStatusMessages;
import io.mosip.preregistration.application.errorcodes.DocumentErrorCodes;
import io.mosip.preregistration.application.errorcodes.DocumentErrorMessages;
import io.mosip.preregistration.application.exception.DocumentNotFoundException;
import io.mosip.preregistration.application.service.util.DocumentServiceUtil;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.common.entity.DocumentEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
public class DocumentDAO {

	/** Autowired reference for {@link #documentRepository}. */
	@Autowired
	@Qualifier("documentRepository")
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentServiceUtil serviceUtil;

	/**
	 * Logger configuration for DocumnetDAO
	 */
	private static Logger log = LoggerConfiguration.logConfig(DocumentDAO.class);

	public List<DocumentEntity> findBypreregId(String preId) {
		List<DocumentEntity> entityList = null;
		try {
			entityList = documentRepository.findByDemographicEntityPreRegistrationId(preId);
			if (serviceUtil.isNull(entityList)) {
				throw new DocumentNotFoundException(DocumentErrorCodes.PRG_PAM_DOC_005.toString(),
						DocumentStatusMessages.DOCUMENT_IS_MISSING.getMessage());
			}
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In findBydocumentId method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
		return entityList;
	}

	public DocumentEntity findBydocumentId(String documentId) {
		DocumentEntity entity = null;
		try {
			entity = documentRepository.findBydocumentId(documentId);
			if (entity == null) {
				throw new DocumentNotFoundException(DocumentErrorCodes.PRG_PAM_DOC_005.toString(),
						DocumentStatusMessages.DOCUMENT_IS_MISSING.getMessage());
			}
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In findBydocumentId method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
		return entity;
	}

	public DocumentEntity findSingleDocument(String preId, String catCode) {
		DocumentEntity entity = null;
		try {
			entity = documentRepository.findSingleDocument(preId, catCode);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In findSingleDocument method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
		return entity;
	}

	public int deleteAllBydocumentId(String documentId) {

		try {
			return documentRepository.deleteAllBydocumentId(documentId);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In deleteAllBydocumentId method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
	}

	public int deleteAllBypreregId(String preregId) {
		try {
			return documentRepository.deleteAllByDemographicEntityPreRegistrationId(preregId);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In deleteAllBypreregId method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
	}

	public boolean existsByPreregId(String preregId) {
		return documentRepository.existsByDemographicEntityPreRegistrationId(preregId);
	}

	public DocumentEntity saveDocument(DocumentEntity entity) {
		try {
			return documentRepository.save(entity);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In saveDocument method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
	}

	public DocumentEntity updateDocument(DocumentEntity entity) {
		try {
			return documentRepository.update(entity);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In updateDocument method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
	}

	public DemographicEntity getDemographicEntityForPrid(String prid) {
		try {
			return documentRepository.getDemographicEntityForPreRegistrationId(prid);
		} catch (DataAccessLayerException ex) {
			log.error(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
					"In getDemographicEntityForPrid method of DocumnetDAO - " + ExceptionUtils.getFullStackTrace(ex));
			throw new TableNotAccessibleException(DocumentErrorCodes.PRG_PAM_DOC_012.toString(),
					DocumentErrorMessages.DOCUMENT_TABLE_NOTACCESSIBLE.getMessage(), ex.getCause());
		}
	}
}