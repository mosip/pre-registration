package io.mosip.preregistration.application.dto;

import java.util.List;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Megha Tanga
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO<T> {

	/**
	 * page no used by this Pre-Registration type.
	 */
	private Integer pageNo;
	/**
	 * page size used by this Pre-Registration type.
	 */
	private Integer pageSize;
	/**
	 * sort used by this Pre-Registration type.
	 */
	private Sort sort;
	/**
	 * total items used by this Pre-Registration type.
	 */
	private Long totalItems;
	/**
	 * total pages used by this Pre-Registration type.
	 */
	private Integer totalPages;
	/**
	 * data used by this Pre-Registration type.
	 */
	private List<T> data;
}
