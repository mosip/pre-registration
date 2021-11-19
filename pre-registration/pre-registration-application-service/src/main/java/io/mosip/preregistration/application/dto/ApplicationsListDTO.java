/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.dto;

import java.io.Serializable;
import java.util.List;

import io.mosip.preregistration.core.common.entity.ApplicationEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * List of applications
 * 
 * @author Mayura D
 * @since 1.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ApplicationsListDTO implements Serializable {

	private static final long serialVersionUID = -1703307212966796829L;

	private List<ApplicationEntity> allApplications;

}
