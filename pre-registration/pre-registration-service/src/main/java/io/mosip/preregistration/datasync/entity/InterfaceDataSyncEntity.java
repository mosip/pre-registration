package io.mosip.preregistration.datasync.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author M1046129
 *
 */
@Component
@Entity
@Table(name = "intf_processed_prereg_list", schema = "prereg")
@Getter
@Setter
@NoArgsConstructor
public class InterfaceDataSyncEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2624621243252003059L;

	@Id
	@EmbeddedId
	/**
	 * ipprlst pk used by this Pre-Registration type.
	 */
	private InterfaceDataSyncTablePK ipprlst_PK;

	@Column(name = "lang_code")
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;

	@Column(name = "cr_by")
	/**
	 * created by used by this Pre-Registration type.
	 */
	private String createdBy;

	@Column(name = "cr_dtimes")
	/**
	 * created date used by this Pre-Registration type.
	 */
	private LocalDateTime createdDate;

	@Column(name = "upd_by")
	/**
	 * updated by used by this Pre-Registration type.
	 */
	private String updatedBy;

	@Column(name = "upd_dtimes")
	/**
	 * updated date used by this Pre-Registration type.
	 */
	private LocalDateTime updatedDate;

	/**
	 * Returns effective created by.
	 *
	 * @return get effective created by result
	 */
	public String getEffectiveCreatedBy() {
		return this.createdBy;
	}

	/**
	 * Returns effective updated by.
	 *
	 * @return get effective updated by result
	 */
	public String getEffectiveUpdatedBy() {
		return this.updatedBy;
	} 

	@Column(name = "is_deleted")
	/**
	 * is deleted used by this Pre-Registration type.
	 */
	private boolean isDeleted;

	@Column(name = "del_dtimes")
	/**
	 * del time used by this Pre-Registration type.
	 */
	private LocalDateTime delTime;

}
