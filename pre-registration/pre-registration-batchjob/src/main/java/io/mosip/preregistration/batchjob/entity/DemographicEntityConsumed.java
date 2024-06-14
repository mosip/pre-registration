/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This entity class defines the database table details for PreRegistration.
 * 
 * @author Aiham Hasan
 * @since 1.2.0
 *
 */
@Component
@Entity
@Table(name = "applicant_demographic_consumed", schema = "prereg")
@Setter
@NoArgsConstructor
public class DemographicEntityConsumed implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

//	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "demographicEntity")
//	private List<DocumentEntity> documentEntity;

//	@OneToOne(fetch = FetchType.EAGER,cascade=CascadeType.ALL, mappedBy = "applicationId")
//	private ApplicationEntity applicationEntity;

//	@OneToOne(fetch = FetchType.EAGER,cascade=CascadeType.ALL, mappedBy = "demographicEntity")
//	private RegistrationBookingEntity registrationBookingEntity;

	/** The pre registration id. */
	@Column(name = "prereg_id", nullable = false)
	@Id
	private String preRegistrationId;

	/** The JSON */
	@Column(name = "demog_detail")
	private byte[] applicantDetailJson;

	// Setter methods for requesttime are overridden manually
	public void setApplicantDetailJson(byte[] applicantDetailJson) {
		this.applicantDetailJson = applicantDetailJson != null ? applicantDetailJson.clone() : null;
	}

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
