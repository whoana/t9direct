/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pep.per.mint.common.util.Util;
import rose.mary.trace.RunMode;
import rose.mary.trace.T9;
import rose.mary.trace.core.cache.CacheProxy;
import rose.mary.trace.core.data.common.Trace;
import rose.mary.trace.core.envs.Variables;
import rose.mary.trace.core.exception.ExceptionHandler;
import rose.mary.trace.core.monitor.ThroughputMonitor;
import rose.mary.trace.database.service.TraceService;

/**
 * <pre>
 * rose.mary.trace.database
 * Loader.java
 * </pre>
 * 
 * @author whoana
 * @date Aug 26, 2019
 */
public class TraceLoader implements Runnable {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final static int DEFAULT_COMMIT_COUNT = 1000;

	private boolean isShutdown = true;

	private TraceService traceLoadService;

	private Thread thread = null;

	private ExceptionHandler exceptionHandler;

	private CacheProxy<String, Trace> distributeCache;

	private CacheProxy<String, Trace> mergeCache;

	private CacheProxy<String, Trace> errorCache;

	private ThroughputMonitor tpm;

	private int commitCount = DEFAULT_COMMIT_COUNT;

	private long delayForNoMessage = 1000;

	private long exceptionDelay = 5000;

	private boolean loadError = true;

	private boolean loadContents = true;

	private Map<String, Trace> loadItems = new LinkedHashMap<String, Trace>();

	private long commitLapse = System.currentTimeMillis();

	private long maxCommitWait = 1000;

	private List<String> dups = new ArrayList<String>();

	private String oldKey = null;

	String name;

	/**
	 * 
	 * @param name
	 * @param commitCount
	 * @param delayForNoMessage
	 * @param loadError
	 * @param loadContents
	 * @param distributeCache
	 * @param mergeCache
	 * @param errorCache
	 * @param traceLoadService
	 * @param tpm
	 * @param exceptionHandler
	 */
	public TraceLoader(String name, int commitCount, long delayForNoMessage, boolean loadError, boolean loadContents,
			CacheProxy<String, Trace> distributeCache, CacheProxy<String, Trace> mergeCache,
			CacheProxy<String, Trace> errorCache, TraceService traceLoadService, ThroughputMonitor tpm,
			ExceptionHandler exceptionHandler) {
		this.name = name;
		this.commitCount = commitCount;
		this.distributeCache = distributeCache;
		this.mergeCache = mergeCache;
		this.traceLoadService = traceLoadService;
		this.tpm = tpm;
		this.exceptionHandler = exceptionHandler;
		this.delayForNoMessage = delayForNoMessage;
		this.loadError = loadError;
		this.loadContents = loadContents;
		this.errorCache = errorCache;
	}

	/**
	 * <pre>
	 * 	Database ?????? ?????? ????????? ???????????? ?????? ????????? ?????? ?????? ????????? ????????? ??????
	 * </pre>
	 * 
	 * @throws Exception
	 */
	public void commit() throws Exception {
		try {
			Collection<Trace> collection = loadItems.values();
			traceLoadService.load(collection, loadError, loadContents);
 
			if (tpm != null)
				tpm.count(loadItems.size());

			if (T9.runMode == RunMode.Server) {
				mergeCache.put(loadItems);
			} else if (T9.runMode == RunMode.Distributor) {
				distribute(loadItems);
			}
 
			distributeCache.removeAll(loadItems.keySet());
 
		} catch (Exception e) {
			// ----------------------------------------------------
			// 20220905
			// ?????? ????????? ??????????????? ????????? D.C????????? ???????????? ????????? ????????????
			// ????????? ???????????? ???????????? ????????? ??????.
			// ???????????? ?????? ?????? ????????? ????????? ????????? ??????, ????????????, ????????? ???
			// ???????????? ?????? ????????? D.C ??? ?????? ?????? ????????? ?????????....
			//
			// 20221110
			// ????????????????????? ????????? ?????? ?????? ????????????, ???????????? ????????? ?????? ?????????
			// ???????????? ????????? ??? ?????? ????????????
			// ?????? ?????? ??? ????????? ????????? ??????????????? ?????? ???????????? ?????? ??????????????? ????????? ??????
			// ----------------------------------------------------
			if (errorCache != null) {
				errorCache.put(loadItems);
			}
			distributeCache.removeAll(loadItems.keySet());
			// ----------------------------------------------------

			// 20221111
			// errorCache??? ???????????? ????????? ?????? ????????????.
			// logger.error("Loader commit Exception", e);
			// throw e;
		} finally {

			loadItems.clear();

			if (Variables.debugLineByLine)
				logger.debug(name + "-TLLBLD0106");

			commitLapse = System.currentTimeMillis();
		}
	}
	 
	Distributor distributor;

	public void setDistributor(Distributor distributor) {
		this.distributor = distributor;
	}

	private void distribute(Map<String, Trace> traces) throws Exception {
		distributor.distribute(traces);
	}

	public void rollback() {

	}

	public void setThroughputMonitor(ThroughputMonitor tpm) {
		this.tpm = tpm;
	}

	public ThroughputMonitor getThroughputMonitor() {
		return tpm;
	}

	public void start() throws Exception {
		if (thread != null)
			stop();
		thread = new Thread(this, name);
		isShutdown = false;
		thread.start();
	}

	public void stop() {
		stopGracefully();
	}

	public void run() {
 
		runGracefully();
		 
	}

	public void stopGracefully() {
		isShutdown = true;
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
 

	public void runGracefully() {

		logger.info(Util.join("start loader:[" + name + "]"));

		while (Thread.currentThread() == thread && !isShutdown) {

			try { 

				Collection<Trace> values = distributeCache.values();
				if (values == null || values.size() == 0) {
					try {
						Thread.sleep(delayForNoMessage);
						continue;
					} catch (java.lang.InterruptedException ie) {
						isShutdown = true;
						break;
					}
				}

				String regDate = Util.getFormatedDate("yyyyMMddHHmmssSSS");
				for (Trace trace : values) {
					String key = trace.getId();
					trace.setRegDate(regDate);
					if (mergeCache.containsKey(key)) {
						distributeCache.remove(key);
					}
					loadItems.put(key, trace);
 
					if (loadItems.size() >= commitCount) {
						break;
					}
					 
				}
				commit();

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
		logger.info(Util.join("stop loader:[" + name + "]"));
	}

	/* src block backup 20221221 */
	/* 
	public void runGracefully() {

		logger.info(Util.join("start loader:[" + name + "]"));

		while (Thread.currentThread() == thread && !isShutdown) {

			try {
				if (loadItems.size() > 0 && (System.currentTimeMillis() - commitLapse >= maxCommitWait)) {

					commit();
				}

				Collection<Trace> values = distributeCache.values();
				if (values == null || values.size() == 0) {
					try {
						Thread.sleep(delayForNoMessage);
						continue;
					} catch (java.lang.InterruptedException ie) {
						isShutdown = true;
						break;
					}
				}

				String regDate = Util.getFormatedDate("yyyyMMddHHmmssSSS");
				for (Trace trace : values) {
					String key = trace.getId();
					trace.setRegDate(regDate);

					if (mergeCache.containsKey(key)) {
						// delete the trace loaded already.
						distributeCache.remove(key);
					}

					loadItems.put(key, trace);

					// exepct to delete comming soon(current date : 202209)
					// if (oldKey != null && oldKey.equals(key)) dups.add(key);

					if (loadItems.size() > 0 && (loadItems.size() % commitCount == 0)) {

						try {
							commit();
							break;
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
								return;
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
		logger.info(Util.join("stop loader:[" + name + "]"));
	}
	*/


	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

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

}
