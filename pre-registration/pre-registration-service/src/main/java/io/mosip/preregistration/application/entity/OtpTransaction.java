package io.mosip.preregistration.application.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manoj SP
 *
 */
@NoArgsConstructor
@Data
@Table(name = "otp_transaction", schema = "prereg")
@Entity
public class OtpTransaction {

	@Id
	/**
	 * id used by this Pre-Registration type.
	 */
	private String id;

	@Column(name = "ref_id")
	/**
	 * ref id used by this Pre-Registration type.
	 */
	private String refId;

	@Column(name = "otp_hash")
	/**
	 * otp hash used by this Pre-Registration type.
	 */
	private String otpHash;

	@Column(name = "generated_dtimes")
	/**
	 * generated dtimes used by this Pre-Registration type.
	 */
	private LocalDateTime generatedDtimes;

	@Column(name = "expiry_dtimes")
	/**
	 * expiry dtimes used by this Pre-Registration type.
	 */
	private LocalDateTime expiryDtimes;

	@Column(name = "validation_retry_count")
	/**
	 * validation retry count used by this Pre-Registration type.
	 */
	private Integer validationRetryCount;

	@Column(name = "status_code")
	/**
	 * status code used by this Pre-Registration type.
	 */
	private String statusCode;

	@Column(name = "lang_code")
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	@Column(name = "cr_by")
	/**
	 * cr by used by this Pre-Registration type.
	 */
	private String crBy;

	@Column(name = "cr_dtimes")
	/**
	 * cr dtimes used by this Pre-Registration type.
	 */
	private LocalDateTime crDtimes;

	@Column(name = "upd_by")
	/**
	 * upd by used by this Pre-Registration type.
	 */
	private String updBy;

	@Column(name = "upd_dtimes")
	/**
	 * upd d times used by this Pre-Registration type.
	 */
	private LocalDateTime updDTimes;

	@Column(name = "is_deleted")
	/**
	 * is deleted used by this Pre-Registration type.
	 */
	private Boolean isDeleted;

	@Column(name = "del_dtimes")
	/**
	 * del dtimes used by this Pre-Registration type.
	 */
	private LocalDateTime delDtimes;
}
