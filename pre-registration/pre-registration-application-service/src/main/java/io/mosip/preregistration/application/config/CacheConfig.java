package io.mosip.preregistration.application.config;

import org.springframework.beans.factory.annotation.Value;
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

	@Value("${preregistration.cache.memoryEvictionPolicy}")
	private String memoryEvictionPolicy;

	@Value("${preregistration.cache.maxEntriesLocalHeap}")
	private long maxEntriesLocalHeap;

	@Value("${preregistration.cache.setMaxBytesLocalDisk}")
	private String setMaxBytesLocalDisk;

	@Value("${preregistration.cache.TimeToLiveSeconds}")
	private long TimeToLiveSeconds;

	@Value("${preregistration.cache.setlogging}")
	private boolean setLogging;

	@Value("${preregistration.cache.isEternal}")
	private boolean isEternal;

	@Bean
	public net.sf.ehcache.CacheManager ehCacheManager() {
		CacheConfiguration masterDataCache = new CacheConfiguration();
		masterDataCache.setName("masterdata-cache");
		masterDataCache.setMemoryStoreEvictionPolicy(memoryEvictionPolicy);
		masterDataCache.setMaxEntriesLocalHeap(maxEntriesLocalHeap);
		masterDataCache.setTimeToLiveSeconds(TimeToLiveSeconds);
		masterDataCache.setLogging(setLogging);
		masterDataCache.setEternal(isEternal);
		masterDataCache.setMaxBytesLocalDisk(setMaxBytesLocalDisk);
		
		
		CacheConfiguration loginCache = new CacheConfiguration();
		loginCache.setName("login-cache");
		loginCache.setMemoryStoreEvictionPolicy(memoryEvictionPolicy);
		loginCache.setMaxEntriesLocalHeap(maxEntriesLocalHeap);
		loginCache.setTimeToLiveSeconds(TimeToLiveSeconds);
		loginCache.setLogging(setLogging);
		loginCache.setEternal(isEternal);
		loginCache.setMaxBytesLocalDisk(setMaxBytesLocalDisk);

		net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
		config.addCache(masterDataCache);
		config.addCache(loginCache);
		return net.sf.ehcache.CacheManager.newInstance(config);
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager());
	}

}
