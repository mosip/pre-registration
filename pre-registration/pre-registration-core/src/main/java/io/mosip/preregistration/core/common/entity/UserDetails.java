package io.mosip.preregistration.core.common.entity;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity to store canonical user identifiers to avoid storing PII plaintext across tables.
 */
@Entity
@Table(name = "user_details", schema = "prereg")
@Getter
@Setter
@NoArgsConstructor
public class UserDetails {

    @Id
    @Column(name = "user_id")
    /**
     * user id used by this Pre-Registration type.
     */
    private UUID userId;

    @Column(name = "identifier_hash", nullable = false)
    /**
     * identifier hash used by this Pre-Registration type.
     */
    private String identifierHash; // SHA-256 over normalized identifier

    @Column(name = "identifier_encrypted", nullable = false)
    /**
     * Performs identifier for MOSIP Pre-Registration.
     *
     * @param recovery recovery used by identifier
     * @return identifier result
     */
    private String identifierEncrypted; // Encrypted identifier (mandatory, used for audit/notification recovery)

    @Column(name = "cr_dtimes")
    /**
     * cr dtimes used by this Pre-Registration type.
     */
    private LocalDateTime crDtimes;

    @Column(name = "encrypted_dtimes")
    /**
     * encrypted dtimes used by this Pre-Registration type.
     */
    private LocalDateTime encryptedDtimes;

}
