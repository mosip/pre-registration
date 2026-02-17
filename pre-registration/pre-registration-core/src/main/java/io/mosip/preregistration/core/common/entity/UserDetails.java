package io.mosip.preregistration.core.common.entity;

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
    private String userId; // UUID or business id

    @Column(name = "identifier_hash")
    private String identifierHash; // SHA-256 over normalized identifier

    @Column(name = "identifier_encrypted")
    private String identifierEncrypted; // Encrypted value (nullable)
} 
