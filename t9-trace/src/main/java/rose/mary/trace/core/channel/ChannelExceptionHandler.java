/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.core.channel;

/**
 * <pre>
 * rose.mary.trace.listener
 * ExceptionHandler.java
 * </pre>
 * 
 * @author whoana
 * @date Jul 31, 2019
 */
public interface ChannelExceptionHandler {
	public void handleException(String msg, Exception e);

	public void handleParserException(String msg, Exception e, Object trace);
}
