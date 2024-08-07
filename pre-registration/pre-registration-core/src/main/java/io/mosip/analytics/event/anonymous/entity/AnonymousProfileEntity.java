package io.mosip.analytics.event.anonymous.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "anonymous_profile", schema = "prereg")
public class AnonymousProfileEntity implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7119177316682307235L;

	/** id of the anonymous profile **/
	@Id
	@Column(name = "id")
	private String id;

	/** Anonymous Profile JSON **/
	@Column(name = "profile", nullable = false)
	private String profile;

	/** The created by. */
	@Column(name = "cr_by")
	private String createdBy;

	/** The create date time. */
	@Column(name = "cr_dtimes")
	private LocalDateTime createDateTime;

	/** The updated by. */
	@Column(name = "upd_by")
	private String updatedBy;

	/** The update date time. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updateDateTime;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

	@Column(name = "del_dtimes")
	private LocalDateTime delDtimes;

}
