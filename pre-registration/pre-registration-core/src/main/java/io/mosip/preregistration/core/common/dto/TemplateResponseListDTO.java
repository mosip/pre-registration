package io.mosip.preregistration.core.common.dto;

import java.util.List;

import lombok.Data;


@Data
/**
 * Data transfer object carrying template response list across Pre-Registration APIs.
 */
public class TemplateResponseListDTO {
	List<TemplateResponseDTO> templates;
}
