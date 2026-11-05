package io.mosip.preregistration.core.common.dto;

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
	private int pageNo;

	/**
	 * page size used by this Pre-Registration type.
	 */
	private int pageSize;

	/**
	 * sort used by this Pre-Registration type.
	 */
	private Sort sort;

	/**
	 * total items used by this Pre-Registration type.
	 */
	private long totalItems;

	/**
	 * total pages used by this Pre-Registration type.
	 */
	private int totalPages;

	/**
	 * data used by this Pre-Registration type.
	 */
	private List<T> data;
}
