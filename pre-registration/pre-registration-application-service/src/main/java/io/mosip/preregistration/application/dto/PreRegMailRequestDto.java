package io.mosip.preregistration.application.dto;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;


/**
 * General-purpose of {@code MailRequestDto} class used to store Mail request
 * Info's
 * 
 */
@Data
public class PreRegMailRequestDto {

	/**
	 * Variable to hold mail to
	 */
	String[] mailTo;
	/**
	 * Variable to hold mail cc
	 */
	String[] mailCc;
	/**
	 * Variable to hold mail subject
	 */
	String mailSubject;
	/**
	 * Variable to hold mail content
	 */
	String mailContent;
	/**
	 * Variable to hold attachments
	 */
	MultipartFile[] attachments;

}
