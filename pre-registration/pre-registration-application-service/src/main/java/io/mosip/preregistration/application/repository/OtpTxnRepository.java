package io.mosip.preregistration.application.repository;

import io.mosip.preregistration.application.entity.OtpTransaction;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

public interface OtpTxnRepository extends BaseRepository<OtpTransaction, String> {

	Boolean existsByOtpHashAndStatusCode(String otpHash, String statusCode);

	OtpTransaction findTopByOtpHashAndStatusCode(String otpHash, String statusCode);
	
	OtpTransaction findByRefIdAndStatusCode(String refId, String statusCode);

	/**
	 * Obtain the number of count of request_dTtimes for particular userId with
	 * within the otpRequestDTime and oneMinuteBeforeTime.
	 *
	 * @param otpRequestDTime     the otp request D time
	 * @param oneMinuteBeforeTime the one minute before time
	 * @param refId               the ref id
	 * @return the int
	 */
	@Query("Select count(1) from OtpTransaction  where generatedDtimes <= :otpRequestDTime and "
			+ "generatedDtimes >= :oneMinuteBeforeTime and refId=:refId")
	public int countRequestDTime(@Param("otpRequestDTime") LocalDateTime otpRequestDTime,
			@Param("oneMinuteBeforeTime") LocalDateTime oneMinuteBeforeTime, @Param("refId") String refId);

	@Query("Select count(1) from OtpTransaction  where refId = :refId and " + "statusCode = :statusCode and "
			+ "expiryDtimes > :currenttime")
	int checkotpsent(@Param("refId") String userid, @Param("statusCode") String statusCode,
			@Param("currenttime") LocalDateTime currenttime);
}
