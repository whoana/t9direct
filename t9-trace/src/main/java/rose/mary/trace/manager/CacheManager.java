/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.manager;

import java.util.Collection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rose.mary.trace.core.cache.CacheManagerProxy;
import rose.mary.trace.core.cache.CacheProxy;
import rose.mary.trace.core.cache.chronicle.ChronicleCacheManagerProxy;
import rose.mary.trace.core.cache.ehcache.EhcacheManagerProxy;
import rose.mary.trace.core.cache.infinispan.InfinispanCacheManagerProxy;
import rose.mary.trace.core.cache.java.JavaCacheManagerProxy;
import rose.mary.trace.core.config.CacheManagerConfig;
import rose.mary.trace.core.data.common.InterfaceInfo;
import rose.mary.trace.core.data.common.State;
import rose.mary.trace.core.data.common.StateEvent;
import rose.mary.trace.core.data.common.Trace;
import rose.mary.trace.core.data.common.Unmatch;
import rose.mary.trace.core.exception.NotYetImplementedException;
import rose.mary.trace.core.exception.SystemError;

/**
 * <pre>
 * rose.mary.trace.manager
 * CacheManager.java
 * </pre>
 * 
 * @author whoana
 * @date Aug 5, 2019
 */
public class CacheManager {

	Logger logger = LoggerFactory.getLogger(CacheManager.class);
	// Logger logger = LoggerFactory.getLogger("rose.mary.trace.SystemLogger");

	CacheManagerConfig cacheManagerConfig = null;

	CacheManagerProxy cmp = null;

	List<CacheProxy<String, Trace>> distributeCaches = null;
	CacheProxy<String, Trace> mergeCache = null;
	CacheProxy<String, Trace> backupCache = null;

	CacheProxy<String, Integer> routingCache = null;

	List<CacheProxy<String, StateEvent>> botCaches = null;
	List<CacheProxy<String, State>> cloneCaches = null;

	CacheProxy<String, State> finCache = null;

	CacheProxy<String, Trace> errorCache01 = null;
	CacheProxy<String, State> errorCache02 = null;

	CacheProxy<String, Trace> testCache = null;

	CacheProxy<String, InterfaceInfo> interfaceCache = null;

	CacheProxy<String, Unmatch> unmatchCache = null;

	CacheProxy<String, SystemError> systemErrorCache = null;

	CacheProxy<String, String> dbCache = null;

	Map<String, CacheProxy> cacheMap = new LinkedHashMap<String, CacheProxy>();

	public CacheManager(CacheManagerConfig cacheManagerConfig) {
		this.cacheManagerConfig = cacheManagerConfig;
	}

	public void prepare() throws Exception {

		if (CacheManagerConfig.VENDOR_EHCACHE.equalsIgnoreCase(cacheManagerConfig.getVendor())) {
			cmp = new EhcacheManagerProxy(cacheManagerConfig);
		} else if (CacheManagerConfig.VENDOR_INFINISPAN.equalsIgnoreCase(cacheManagerConfig.getVendor())) {
			cmp = new InfinispanCacheManagerProxy(cacheManagerConfig);
		} else if (CacheManagerConfig.VENDOR_CHRONICLE.equalsIgnoreCase(cacheManagerConfig.getVendor())) {
			cmp = new ChronicleCacheManagerProxy(cacheManagerConfig);
		} else if (CacheManagerConfig.VENDOR_NATIVE.equalsIgnoreCase(cacheManagerConfig.getVendor())) {
			cmp = new JavaCacheManagerProxy(cacheManagerConfig);
		} else {
			throw new NotYetImplementedException();
		}
		cmp.initialize();
		distributeCaches = cmp.getDistributeCaches();
		mergeCache = cmp.getMergeCache();
		backupCache = cmp.getBackupCache();

		botCaches = cmp.getBotCaches();
		cloneCaches = cmp.getCloneCaches();
		finCache = cmp.getFinCache();

		routingCache = cmp.getRoutingCache();
		errorCache01 = cmp.getErrorCache01();
		errorCache02 = cmp.getErrorCache02();
		interfaceCache = cmp.getInterfaceCache();
		unmatchCache = cmp.getUnmatchCache();
		systemErrorCache = cmp.getSystemErrorCache();

		for (CacheProxy distributeCache : distributeCaches) {
			cacheMap.put(distributeCache.getName(), distributeCache);
		}

		cacheMap.put(mergeCache.getName(), mergeCache);
		cacheMap.put(backupCache.getName(), backupCache);

		for (CacheProxy botCache : botCaches) {
			cacheMap.put(botCache.getName(), botCache);
		}

		for (CacheProxy cloneCache : cloneCaches) {
			cacheMap.put(cloneCache.getName(), cloneCache);
		}

		cacheMap.put(finCache.getName(), finCache);
		cacheMap.put(routingCache.getName(), routingCache);
		cacheMap.put(errorCache01.getName(), errorCache01);
		cacheMap.put(errorCache02.getName(), errorCache02);
		cacheMap.put(interfaceCache.getName(), interfaceCache);
		cacheMap.put(unmatchCache.getName(), unmatchCache);
		cacheMap.put(systemErrorCache.getName(), systemErrorCache);

		// logger.info("All Caches are prepared.");
		// Collection<CacheProxy> caches = cacheMap.values();
		// for (CacheProxy cache : caches) {
		// logger.info("The cache "+ cache.getName() + " is prepared.");
		// }

		startDistributeCacheSizeChecker();
	}

	private void checkDistributeCacheSize() {
		for (CacheProxy cache : distributeCaches) {
			int size = cache.size();
			cache.setCheckedSize(size);
			if (size > 900000)
				logger.info("The cache " + cache.getName() + " 's checked size too much:" + cache.getCheckedSize());
		}
	}

	Thread distributeCacheSizeChecker = null;
	Runnable distributeCacheSizeCheckerRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				if (distributeCacheSizeChecker.isInterrupted())
					break;
				checkDistributeCacheSize();
				try {

					Thread.sleep(cacheManagerConfig.getCheckSizeDelay());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}

	};

	public void startDistributeCacheSizeChecker() {
		if (distributeCacheSizeChecker != null)
			stopDistributeCacheSizeChecker();
		distributeCacheSizeChecker = new Thread(distributeCacheSizeCheckerRunnable);
		distributeCacheSizeChecker.start();
	}

	public void stopDistributeCacheSizeChecker() {
		distributeCacheSizeChecker.interrupt();
		distributeCacheSizeChecker = null;
	}

	/**
	 * @return the testCache
	 */
	public CacheProxy<String, Trace> getTestCache() {
		return testCache;
	}

	public List<CacheProxy<String, Trace>> getDistributeCaches() {
		return distributeCaches;
	}

	/**
	 * @return
	 */
	public CacheProxy<String, Trace> getMergeCache() {
		return mergeCache;
	}

	/**
	 * @return the errorCache01
	 */
	public CacheProxy<String, Trace> getErrorCache01() {
		return errorCache01;
	}

	/**
	 * @return the errorCache02
	 */
	public CacheProxy<String, State> getErrorCache02() {
		return errorCache02;
	}

	/**
	 * @return the interfaceCache
	 */
	public CacheProxy<String, InterfaceInfo> getInterfaceCache() {
		return interfaceCache;
	}

	/**
	 * 
	 */
	public void closeCache() {
		try {
			stopDistributeCacheSizeChecker();
			cmp.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public CacheProxy<String, Trace> getBackupCache() {
		return backupCache;
	}

	/**
	 * @param cacheName
	 * @return
	 */
	public CacheProxy<String, ?> getCache(String cacheName) {
		return cacheMap.get(cacheName);
	}

	public CacheProxy<String, Integer> getRoutingCache() {
		return routingCache;
	}

	public Collection<CacheProxy> caches() {
		return cacheMap.values();
	}

	public List<CacheProxy<String, StateEvent>> getBotCaches() {
		return botCaches;
	}

	public List<CacheProxy<String, State>> getCloneCaches() {
		return cloneCaches;
	}

	public CacheProxy<String, State> getFinCache() {
		return finCache;
	}

	public CacheProxy<String, Unmatch> getUnmatchCache() {
		return unmatchCache;
	}

	public CacheProxy<String, SystemError> getSystemErrorCache() {
		return systemErrorCache;
	}

}
