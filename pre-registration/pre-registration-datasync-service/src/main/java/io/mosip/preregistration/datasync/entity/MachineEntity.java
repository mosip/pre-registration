package io.mosip.preregistration.datasync.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name= "machine_encryption_key",schema = "prereg")
@Data
public class MachineEntity {
	
	@Column(name="machine_id")
	@Id
	private String machineId;
	
	@Column(name="encrypted_publickey")
	private String encryptedPublicKey;
	

}
