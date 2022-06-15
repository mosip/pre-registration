package io.mosip.preregistration.datasync.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationsDTO  implements Serializable {
	
	private static final long serialVersionUID = 1898616434106604338L;
	
	@ApiModelProperty(value = "Application Ids", position = 1)
	private List<ApplicationDTO> applications;

}
