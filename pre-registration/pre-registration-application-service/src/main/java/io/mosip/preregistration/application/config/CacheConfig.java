package io.mosip.preregistration.application.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.ehcache.config.CacheConfiguration;

@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

	@Bean
	public net.sf.ehcache.CacheManager ehCacheManager() {
		CacheConfiguration masterDataCache = new CacheConfiguration();
		masterDataCache.setName("masterdata-cache");
		masterDataCache.setMemoryStoreEvictionPolicy("LRU");
		masterDataCache.setMaxEntriesLocalHeap(2000);

		net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
		config.addCache(masterDataCache);
		return net.sf.ehcache.CacheManager.newInstance(config);
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager());
	}
}
