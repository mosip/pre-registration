package io.mosip.preregistration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data transfer object carrying mosip user across Pre-Registration APIs.
 */
public class MosipUserDTO {

	/**
	 * user id used by this Pre-Registration type.
	 */
	private String userId;
	/**
	 * mobile used by this Pre-Registration type.
	 */
	private String mobile;
	/**
	 * mail used by this Pre-Registration type.
	 */
	private String mail;
	/**
	 * lang code used by this Pre-Registration type.
	 */
	private String langCode;
	/**
	 * user password used by this Pre-Registration type.
	 */
	private String userPassword;
	/**
	 * name used by this Pre-Registration type.
	 */
	private String name;
	/**
	 * role used by this Pre-Registration type.
	 */
	private String role;
	/**
	 * r id used by this Pre-Registration type.
	 */
	private String rId;
}
