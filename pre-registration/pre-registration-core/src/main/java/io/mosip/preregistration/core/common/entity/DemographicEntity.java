/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.entity;

import java.io.Serializable;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NamedQuery;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This entity class defines the database table details for PreRegistration.
 * 
 * @author Rajath KR
 * @author Sanober Noor
 * @author Tapaswini Behera
 * @author Jagadishwari S
 * @author Ravi C Balaji
 * @since 1.0.0
 *
 */
/**
 * @author M1046129
 *
 */
@Component
@Data
@Entity
@Table(name = "applicant_demographic", schema = "prereg")
@NoArgsConstructor
@NamedQuery(name = "DemographicEntity.findByCreatedByOrderByCreateDateTime", query = "SELECT e FROM DemographicEntity e  WHERE e.createdBy=:userId and e.statusCode <>:statusCode order by e.createDateTime desc")
@NamedQuery(name = "DemographicEntity.findByCreatedBy", query = "SELECT e FROM DemographicEntity e  WHERE e.createdBy=:userId and e.statusCode <>:statusCode order by e.createDateTime desc")
@NamedQuery(name = "DemographicEntity.findBypreRegistrationId", query = "SELECT r FROM DemographicEntity r  WHERE r.preRegistrationId=:preRegId")
public class DemographicEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "demographicEntity")
	private List<DocumentEntity> documentEntity;

	/** The pre registration id. */
	@Id
	@Column(name = "prereg_id")
	private String preRegistrationId;

	/** The JSON */
	@JdbcTypeCode(java.sql.Types.BINARY)
	@Column(name = "demog_detail", columnDefinition = "bytea")
	private byte[] applicantDetailJson;

	/** The status_code */
	@Column(name = "status_code", nullable = false)
	private String statusCode;

	/** The lang_code */
	@Column(name = "lang_code", nullable = false)
	private String langCode;

	/** The created by. */
	@Column(name = "cr_by")
	private String createdBy;

	/** The created appuser by. */
	@Column(name = "cr_appuser_id")
	private String crAppuserId;

	/** The create date time. */
	@Column(name = "cr_dtimes")
	private LocalDateTime createDateTime;

	/** The updated by. */
	@Column(name = "upd_by")
	private String updatedBy;

	/** The update date time. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updateDateTime;

	/**
	 * Encrypted Date Time
	 */
	@Column(name = "encrypted_dtimes")
	private LocalDateTime encryptedDateTime;

	@Column(name = "demog_detail_hash")
	private String demogDetailHash;
}