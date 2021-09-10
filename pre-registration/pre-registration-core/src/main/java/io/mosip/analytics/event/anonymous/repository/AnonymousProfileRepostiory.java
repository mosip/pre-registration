package io.mosip.analytics.event.anonymous.repository;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import io.mosip.analytics.event.anonymous.entity.AnonymousProfileEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository
@Transactional
public interface AnonymousProfileRepostiory extends BaseRepository<AnonymousProfileEntity, String> {
	
	
}
