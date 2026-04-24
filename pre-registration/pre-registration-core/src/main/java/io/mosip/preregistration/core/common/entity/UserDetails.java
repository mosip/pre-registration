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
    private UUID userId;

    @Column(name = "identifier_hash", nullable = false)
    private String identifierHash; // SHA-256 over normalized identifier

    @Column(name = "identifier_encrypted", nullable = false)
    private String identifierEncrypted; // Encrypted identifier (mandatory, used for audit/notification recovery)

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "encrypted_dtimes")
    private LocalDateTime encryptedDtimes;

}
