package io.mosip.preregistration.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import net.sf.ehcache.config.CacheConfiguration;

@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

	@Autowired
	private Environment env;

	@Bean
	public net.sf.ehcache.CacheManager ehCacheManager() {
		CacheConfiguration masterDataCache = new CacheConfiguration();
		masterDataCache.setName("masterdata-cache");
		masterDataCache.setMemoryStoreEvictionPolicy(
				env.getProperty("preregistration.masterdata.cache.memoryEvictionPolicy", "LRU"));
		masterDataCache.setMaxEntriesLocalHeap(
				Integer.parseInt(env.getProperty("preregistration.masterdata.cache.maxEntriesLocalHeap", "10000")));
		masterDataCache.setTimeToLiveSeconds(
				Integer.parseInt(env.getProperty("preregistration.masterdata.cache.TimeToLiveSeconds", "3600")));
		masterDataCache
				.setLogging(Boolean.parseBoolean(env.getProperty("preregistration.masterdata.cache.setLogging")));
		masterDataCache
				.setEternal(Boolean.parseBoolean(env.getProperty("preregistration.masterdata.cache.setEternal")));
		masterDataCache.setMaxBytesLocalDisk(env.getProperty("preregistration.masterdata.cache.setMaxBytesLocalDisk"));

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
