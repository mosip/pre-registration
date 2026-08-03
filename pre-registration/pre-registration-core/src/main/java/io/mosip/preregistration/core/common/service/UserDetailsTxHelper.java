package io.mosip.preregistration.core.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.preregistration.core.common.entity.UserDetails;
import io.mosip.preregistration.core.common.repository.UserDetailsRepository;

/**
 * Transaction boundary for inserting a canonical user row.
 *
 * <p>This exists as a separate bean on purpose. {@link UserDetailsService} previously annotated its
 * own {@code createInternalUser} with {@code @Transactional} and called it from within the same
 * class, so the Spring proxy was bypassed and the annotation had no effect at all.
 *
 * <p>The propagation matters as much as the proxy. Losing the race on
 * {@code identifier_hash}'s unique constraint raises a {@code DataIntegrityViolationException}, and
 * on PostgreSQL that also aborts the transaction the statement ran in — any follow-up query in that
 * same transaction fails with {@code 25P02 current transaction is aborted}. Running the insert in
 * its own {@code REQUIRES_NEW} transaction confines the abort to that inner transaction, leaving the
 * caller's transaction healthy so it can catch the violation and re-read the winner's row.
 *
 * <p>The catch therefore belongs in the caller, outside this boundary — not inside this method.
 */
@Service
public class UserDetailsTxHelper {

	private final UserDetailsRepository userDetailsRepository;

	public UserDetailsTxHelper(UserDetailsRepository userDetailsRepository) {
		this.userDetailsRepository = userDetailsRepository;
	}

	/**
	 * Inserts the row in a transaction of its own.
	 *
	 * @param userDetails fully populated row to insert
	 * @return the persisted row
	 * @throws org.springframework.dao.DataIntegrityViolationException if another thread inserted the
	 *         same {@code identifier_hash} first; this inner transaction is rolled back and the
	 *         caller's transaction remains usable
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UserDetails saveInNewTransaction(UserDetails userDetails) {
		return userDetailsRepository.save(userDetails);
	}
}
