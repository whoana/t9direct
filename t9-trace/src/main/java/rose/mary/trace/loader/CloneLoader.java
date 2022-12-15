/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.loader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pep.per.mint.common.util.Util;
import rose.mary.trace.core.cache.CacheProxy; 
import rose.mary.trace.core.data.common.Bot;
import rose.mary.trace.core.data.common.State; 
import rose.mary.trace.core.database.FromDatabase;  
import rose.mary.trace.core.exception.ExceptionHandler;
import rose.mary.trace.core.monitor.ThroughputMonitor;
import rose.mary.trace.database.service.BotService;

/**
 * <pre>
 * The BotLoader has a role to loading a {@link Bot Bot} message into the table TOP0503
 * </pre>
 *
 * @author whoana
 * @since Sep 19, 2019
 */
public class CloneLoader implements Runnable {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final int DEFAULT_COMMIT_COUNT = 1000;

	private boolean isShutdown = true;

	private BotService botService;

	private Thread thread = null;

	private ExceptionHandler exceptionHandler;

	private CacheProxy<String, State> cloneCache;

	private CacheProxy<String, State> finCache;

	private CacheProxy<String, State> errorCache;

	private ThroughputMonitor tpm;

	private int commitCount = DEFAULT_COMMIT_COUNT;

	private long delayForNoMessage = 10;

	private long exceptionDelay = 1;

	private Map<String, State> loadBots = new LinkedHashMap<String, State>();

	// private Map<String, String> dbLoadStates = new LinkedHashMap<String,
	// String>();

	private long commitLapse = System.currentTimeMillis();

	private long maxCommitWait = 1000;

	String name;

	FromDatabase fromDatabase;

	/**
	 *
	 * @param commitCount
	 * @param delayForNoMessage
	 * @param cloneCache
	 * @param errorCache
	 * @param botService
	 * @param tpm
	 * @param exceptionHandler
	 */
	public CloneLoader(
			String name,
			int commitCount,
			long delayForNoMessage,
			CacheProxy<String, State> cloneCache,
			CacheProxy<String, State> finCache,
			CacheProxy<String, State> errorCache,
			BotService botService,
			ThroughputMonitor tpm,
			ExceptionHandler exceptionHandler) {
		this.name = name;
		this.commitCount = commitCount;
		this.cloneCache = cloneCache;
		this.finCache = finCache;
		this.errorCache = errorCache;
		this.botService = botService;
		this.tpm = tpm;
		this.exceptionHandler = exceptionHandler;
		this.delayForNoMessage = delayForNoMessage;
	}

	// finCache 삭제 옵션 , 외부 설정으로 등록되지 않은 단계
	// 필요해지면 놀출 20220825
	boolean deleteFinishedBotOpt = false;

	/**
	 *
	 * @throws Exception
	 */
	public void commit() throws Exception {
		try {
			Collection<State> bots = loadBots.values();
 

			int count = loadBots.size();

			if (tpm != null)
				tpm.count(count);
 
			botService.mergeBots(bots, finCache); // SIGKILL LOG : SKL-BL001
  

			cloneCache.removeAll(loadBots.keySet()); // SIGKILL LOG : SKL-BL003
 
		} catch (Exception e) {
			// ----------------------------------------------------
			// 20220905
			// 예외 발생시 에러캐시로 옮기고 B.C데이터 삭제하는 부분에 대해서는
			// 수정이 필요한지 고민해볼 부분이 있따.
			// 에러큐로 빼지 않고 그대로 놔두고 시스템 종료, 문제해결, 재기동 후
			// 에러큐에 넣지 안아도 B.C 에 있는 것은 재처리 되므로....
			// ----------------------------------------------------
			if (errorCache != null) {
				errorCache.put(loadBots);
			}
			cloneCache.removeAll(loadBots.keySet());

			// 20221111
			// errorCache를 이용하는 것으로 일단 수정하자.
			// logger.error("BotLoader commit Exception", e);
			// throw e;
		} finally {
			// dbLoadStates.clear();
			loadBots.clear();
 

			commitLapse = System.currentTimeMillis();

		}
	}

	/**
	 *
	 */
	public void rollback() {
	}

	/**
	 *
	 * @param tpm
	 */
	public void setThroughputMonitor(ThroughputMonitor tpm) {
		this.tpm = tpm;
	}

	/**
	 *
	 * @return
	 */
	public ThroughputMonitor getThroughputMonitor() {
		return tpm;
	}

	/**
	 *
	 * @throws Exception
	 */
	public void start() throws Exception {
		if (thread != null)
			stop();
		thread = new Thread(this, name);
		isShutdown = false;
		thread.start();
	}

	public void stop() {
	 

		isShutdown = true;
		if (thread != null)
			thread.interrupt();
	}

	public void run() {
		 
		runGracefully();

	}
 

	public void stopGracefully() {
		isShutdown = true;
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
 
	/**
	 *
	 */
	public void runGracefully() {
		logger.info(Util.join("start botLoader:[" + name + "]"));
		while (Thread.currentThread() == thread && !isShutdown) {
			try {
				if (loadBots.size() > 0 && (System.currentTimeMillis() - commitLapse >= maxCommitWait)) {
 
					commit(); 
				}

				Set<String> keys = null;
				if (cloneCache.isAccessable()) {
					keys = cloneCache.keys();
				}
				if (keys == null || keys.size() == 0) {
					try {
						Thread.sleep(delayForNoMessage);
						continue;
					} catch (java.lang.InterruptedException ie) {
						isShutdown = true;
						break;
					}
				}

				for (String key : keys) { 
					State state = cloneCache.get(key);
					  
					addBatch(key, state);
					if (loadBots.size() > 0 && (loadBots.size() % commitCount == 0)) {
						try { 
							commit(); 
							break;
						} catch (Exception e) {
							if (exceptionHandler != null) {
								exceptionHandler.handle("", e);
							} else {
								logger.error("", e);
							}
							break;
						}
					}
				}
			} catch (Exception e) {
				if (exceptionHandler != null) {
					exceptionHandler.handle("", e);
				} else {
					logger.error("", e);
				}

				try {
					Thread.sleep(exceptionDelay);
				} catch (InterruptedException e1) {
					isShutdown = true;
					break;
				}
			}
		}

		try {
			commit();
		} catch (Exception e) {
			if (exceptionHandler != null) {
				exceptionHandler.handle("", e);
			} else {
				logger.error("", e);
			}
		}

		isShutdown = true;
		logger.info(Util.join("stop botLoader:[" + name + "]"));
	}

	/**
	 * @param trace
	 */
	private void addBatch(String key, State state) {
		loadBots.put(key, state);
		// dbLoadStates.put(key, state.getBotId());
	}

	/**
	 *
	 * @return
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 *
	 * @param loaderExceptionHandler
	 */
	public void setExceptionHandler(ExceptionHandler loaderExceptionHandler) {
		this.exceptionHandler = loaderExceptionHandler;
	}

	/**
	 * @return the commitCount
	 */
	public int getCommitCount() {
		return commitCount;
	}

	/**
	 * @param commitCount the commitCount to set
	 */
	public void setCommitCount(int commitCount) {
		this.commitCount = commitCount;
	}

	public FromDatabase getFromDatabase() {
		return fromDatabase;
	}

	public void setFromDatabase(FromDatabase fromDatabase) {
		this.fromDatabase = fromDatabase;
	}
}
