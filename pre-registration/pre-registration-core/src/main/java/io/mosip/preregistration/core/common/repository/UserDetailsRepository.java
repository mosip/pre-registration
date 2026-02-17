package io.mosip.preregistration.core.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.preregistration.core.common.entity.UserDetails;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, String> {
    Optional<UserDetails> findByIdentifierHash(String identifierHash);
}
