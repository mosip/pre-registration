package io.mosip.preregistration.datasync.repository;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.datasync.entity.MachineEntity;

@Repository
@Transactional
public interface MachineRepository extends BaseRepository<MachineEntity, String> {

}
