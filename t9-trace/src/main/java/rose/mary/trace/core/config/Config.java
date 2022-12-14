/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.core.config;

/**
 * <pre>
 * rose.mary.trace.apps.manager.config
 * Config.java
 * </pre>
 * 
 * @author whoana
 * @date Nov 25, 2019
 */
public class Config {

	String type = "B";
 

	
	// add at 20221213 
	// option for load not finied states to finCache
	boolean loadNotFinishedState = false;

	// add at 20221213 
	// time for load not finied states to finCache
	int loadNotFinishedStateDurationMin = 60;

	ChannelManagerConfig channelManagerConfig;

	CacheManagerConfig cacheManagerConfig;

	ServerManagerConfig serverManagerConfig;

	LoaderManagerConfig loaderManagerConfig;

	BoterManagerConfig boterManagerConfig;

	BotLoaderManagerConfig botLoaderManagerConfig;

	FinisherManagerConfig finisherManagerConfig;

	InterfaceCacheManagerConfig interfaceCacheManagerConfig;

	TraceErrorHandlerManagerConfig traceErrorHandlerManagerConfig;

	UnmatchHandlerManagerConfig unmatchHandlerManagerConfig;

	PolicyConfig policyConfig;

	BotErrorHandlerManagerConfig botErrorHandlerManagerConfig;

	TesterManagerConfig testerManagerConfig;

	SystemErrorTestManagerConfig systemErrorTestManagerConfig;

	/**
	 * @return the channelManagerConfig
	 */
	public ChannelManagerConfig getChannelManagerConfig() {
		return channelManagerConfig;
	}

	/**
	 * @param channelManagerConfig the channelManagerConfig to set
	 */
	public void setChannelManagerConfig(ChannelManagerConfig channelManagerConfig) {
		this.channelManagerConfig = channelManagerConfig;
	}

	/**
	 * @return the cacheManagerConfig
	 */
	public CacheManagerConfig getCacheManagerConfig() {
		return cacheManagerConfig;
	}

	/**
	 * @param cacheManagerConfig the cacheManagerConfig to set
	 */
	public void setCacheManagerConfig(CacheManagerConfig cacheManagerConfig) {
		this.cacheManagerConfig = cacheManagerConfig;
	}

	/**
	 * @return the serverManagerConfig
	 */
	public ServerManagerConfig getServerManagerConfig() {
		return serverManagerConfig;
	}

	/**
	 * @param serverManagerConfig the serverManagerConfig to set
	 */
	public void setServerManagerConfig(ServerManagerConfig serverManagerConfig) {
		this.serverManagerConfig = serverManagerConfig;
	}

	/**
	 * @return the loaderManagerConfig
	 */
	public LoaderManagerConfig getLoaderManagerConfig() {
		return loaderManagerConfig;
	}

	/**
	 * @param loaderManagerConfig the loaderManagerConfig to set
	 */
	public void setLoaderManagerConfig(LoaderManagerConfig loaderManagerConfig) {
		this.loaderManagerConfig = loaderManagerConfig;
	}

	/**
	 * @return the boterManagerConfig
	 */
	public BoterManagerConfig getBoterManagerConfig() {
		return boterManagerConfig;
	}

	/**
	 * @param boterManagerConfig the boterManagerConfig to set
	 */
	public void setBoterManagerConfig(BoterManagerConfig boterManagerConfig) {
		this.boterManagerConfig = boterManagerConfig;
	}

	/**
	 * @return the botLoaderManagerConfig
	 */
	public BotLoaderManagerConfig getBotLoaderManagerConfig() {
		return botLoaderManagerConfig;
	}

	/**
	 * @param botLoaderManagerConfig the botLoaderManagerConfig to set
	 */
	public void setBotLoaderManagerConfig(BotLoaderManagerConfig botLoaderManagerConfig) {
		this.botLoaderManagerConfig = botLoaderManagerConfig;
	}

	/**
	 * @return the finisherManagerConfig
	 */
	public FinisherManagerConfig getFinisherManagerConfig() {
		return finisherManagerConfig;
	}

	/**
	 * @param finisherManagerConfig the finisherManagerConfig to set
	 */
	public void setFinisherManagerConfig(FinisherManagerConfig finisherManagerConfig) {
		this.finisherManagerConfig = finisherManagerConfig;
	}

	/**
	 * @return the interfaceCacheManagerConfig
	 */
	public InterfaceCacheManagerConfig getInterfaceCacheManagerConfig() {
		return interfaceCacheManagerConfig;
	}

	/**
	 * @param interfaceCacheManagerConfig the interfaceCacheManagerConfig to set
	 */
	public void setInterfaceCacheManagerConfig(InterfaceCacheManagerConfig interfaceCacheManagerConfig) {
		this.interfaceCacheManagerConfig = interfaceCacheManagerConfig;
	}

	/**
	 * @return the traceErrorHandlerManagerConfig
	 */
	public TraceErrorHandlerManagerConfig getTraceErrorHandlerManagerConfig() {
		return traceErrorHandlerManagerConfig;
	}

	/**
	 * @param traceErrorHandlerManagerConfig the traceErrorHandlerManagerConfig to
	 *                                       set
	 */
	public void setTraceErrorHandlerManagerConfig(TraceErrorHandlerManagerConfig traceErrorHandlerManagerConfig) {
		this.traceErrorHandlerManagerConfig = traceErrorHandlerManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public UnmatchHandlerManagerConfig getUnmatchHandlerManagerConfig() {
		return unmatchHandlerManagerConfig;
	}

	/**
	 * 
	 * @param unmatchHandlerManagerConfig
	 */
	public void setUnmatchHandlerManagerConfig(UnmatchHandlerManagerConfig unmatchHandlerManagerConfig) {
		this.unmatchHandlerManagerConfig = unmatchHandlerManagerConfig;
	}

	/**
	 * 
	 * @return
	 */
	public PolicyConfig getPolicyConfig() {
		return policyConfig;
	}

	/**
	 * 
	 * @param policyConfig
	 */
	public void setPolicyConfig(PolicyConfig policyConfig) {
		this.policyConfig = policyConfig;
	}

	/**
	 * 
	 * @return
	 */
	public BotErrorHandlerManagerConfig getBotErrorHandlerManagerConfig() {
		return botErrorHandlerManagerConfig;
	}

	/**
	 * 
	 * @param botErrorHandlerManagerConfig
	 */
	public void setBotErrorHandlerManagerConfig(BotErrorHandlerManagerConfig botErrorHandlerManagerConfig) {
		this.botErrorHandlerManagerConfig = botErrorHandlerManagerConfig;
	}

	public TesterManagerConfig getTesterManagerConfig() {
		return testerManagerConfig;
	}

	public void setTesterManagerConfig(TesterManagerConfig testerManagerConfig) {
		this.testerManagerConfig = testerManagerConfig;
	}

	public SystemErrorTestManagerConfig getSystemErrorTestManagerConfig() {
		return systemErrorTestManagerConfig;
	}

	public void setSystemErrorTestManagerConfig(SystemErrorTestManagerConfig systemErrorTestManagerConfig) {
		this.systemErrorTestManagerConfig = systemErrorTestManagerConfig;
	}
 
	public boolean isLoadNotFinishedState() {
		return loadNotFinishedState;
	}

	public void setLoadNotFinishedState(boolean loadNotFinishedState) {
		this.loadNotFinishedState = loadNotFinishedState;
	}

	public int getLoadNotFinishedStateDurationMin() {
		return loadNotFinishedStateDurationMin;
	}

	public void setLoadNotFinishedStateDurationMin(int loadNotFinishedStateDurationMin) {
		this.loadNotFinishedStateDurationMin = loadNotFinishedStateDurationMin;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
}
