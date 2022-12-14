/**
 * Copyright 2020 portal.mocomsys.com All Rights Reserved.
 */
package rose.mary.trace.core.helper.module.ilink.api;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mococo.ILinkAPI.jms.ILinkMessage;
import com.mococo.ILinkAPI.jms.ILinkRequestMessage;

import rose.mary.trace.core.monitor.ThroughputMonitor;

/**
 * <pre>
 * rose.mary.trace.ilink.api
 * GetMessageApi.java
 * </pre>
 * 
 * @author whoana
 * @date Aug 20, 2019
 */
public class GetMessageApi {

	Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 읽기 버퍼 최대 사이즈 설정
	 * 
	 */
	static final int READ_BUFFER_CAPACITY = 10000000;

	/**
	 * queue 사이즈 산정 메시지 당 사이즈 : 1000 bytes(1k) 메시지 건수 = 1000 개 큐 사이즈 = 메시지 당 사이즈 *
	 * 메시지 건수 = 1000 bytes * 1000 개 = 1M
	 */
	static final int DEFAULT_Q_LIMIT_SZ = 100000;

	Queue wq = new ConcurrentLinkedQueue();

	Queue rq = new ConcurrentLinkedQueue();

	String host = "10.10.1.10";

	int port = 10000;

	int readTimeOut = 10;

	int rqLimitSize = DEFAULT_Q_LIMIT_SZ;
	int wqLimitSize = DEFAULT_Q_LIMIT_SZ;

	private AsynchronousSocketChannel client;

	private Future<Void> future;

	Thread rThread = null;

	Thread wThread = null;

	private boolean isStop = false;

	MessageListener messageListener;

	public static String truncateCString(String cString) {
		int i = 0;
		for (i = 0; i < cString.length() && cString.charAt(i) != ' '; ++i) {
		}
		return cString.substring(0, i);
	}
	
	public static void main(String args[]) {
		
	
		ThroughputMonitor tpm = new ThroughputMonitor(1000);

		byte[] newline = new byte[] { 10 };

		GetMessageApi gmapi = null;
		long connectionTimeout = 5 * 1000;
		int tryCount = 100;
		String host = "10.10.1.10";
		int port = 10000;
		try {
			gmapi = new GetMessageApi(host, port);
			gmapi.setMessageListener(new ILinkMessageListener(gmapi, tpm));
			gmapi.connect(connectionTimeout, tryCount);

			ILinkMessage messageBuffer = new ILinkMessage(2001);
			ILinkRequestMessage requestMessage = new ILinkRequestMessage(3008, "", "", "");
			messageBuffer.setInnerMessage(requestMessage);
			byte[] header = messageBuffer.getHeaderByteArray();
			byte[] body = requestMessage.toByteArray();
			byte[] data = new byte[header.length + body.length + newline.length];
			
			System.arraycopy(header,  0, data,             0, header.length);
			System.arraycopy(body,    0, data, header.length, body.length);
			System.arraycopy(newline, 0, data, header.length + body.length, newline.length);
			
//			gmapi.send(header);
//			gmapi.send(body);
//			gmapi.send(newline);
			gmapi.send(data);
			
			tpm.start();

			while (true) {
				try {
					ILinkMessage headerMessage = new ILinkMessage(2001);
					ILinkRequestMessage bodyMessage = new ILinkRequestMessage(3000, "", "TRACE.EQ", "-99");

					headerMessage.setInnerMessage(bodyMessage);
					byte[] h = headerMessage.getHeaderByteArray();
					byte[] b = bodyMessage.toByteArray();
					byte[] d = new byte[h.length + b.length + newline.length];
					System.arraycopy(h,0, d,             0, h.length);
					System.arraycopy(b,    0, d, header.length, b.length);
					System.arraycopy(newline, 0, d, header.length + b.length, newline.length);
					
//					gmapi.send(h);
//					gmapi.send(b);
//					gmapi.send(newline);
					gmapi.send(d);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (gmapi != null)
				gmapi.stop();
		}

	}

	public GetMessageApi(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void send(byte[] data) throws Exception {
		wq.offer(data);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void connect(long connectionTimeout, int tryCount) throws Exception {

		if (rThread != null) {
			rThread.interrupt();
			rThread = null;
		}

		if (wThread != null) {
			wThread.interrupt();
			wThread = null;
		}

		if (client != null) {
			try {
				client.close();
			} catch (IOException ex) {
			}
			client = null;
		}

		Exception ex = null;
		for (int i = 0; i < tryCount; i++) {
			try {
				client = AsynchronousSocketChannel.open();
				InetSocketAddress hostAddress = new InetSocketAddress(host, port);
				future = client.connect(hostAddress);
				future.get();
				isStop = false;
				break;
			} catch (Exception e) {
				ex = e;
				try {
					Thread.sleep(connectionTimeout);
				} catch (InterruptedException e1) {
				}
			}
		}
		if (ex != null)
			throw ex;
		startReader();
		startWriter();
	}

	/**
	 * 
	 */
	private void startWriter() {
		wThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (isStop) {
							logger.debug("Writer thread stopping.");
							break;
						}
						byte[] msg = (byte[]) wq.poll();
						if (msg != null) {
							ByteBuffer buffer = ByteBuffer.wrap(msg);
							Future<Integer> writeResult = client.write(buffer);
							writeResult.get();
							buffer.clear();
							
							//logger.debug("write msg:" + msg.length);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		wThread.start();
	}

	/**
	 * 
	 */
//	private void startReader() {
//
//		//final ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_CAPACITY);
//		final ByteBuffer lenBuffer = ByteBuffer.allocate(4);
//		ByteBuffer dataBuffer = null;
//		
//		rThread = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				while (!Thread.currentThread().isInterrupted()) {
//					try {
//						if (isStop) {
//							logger.debug("Reader thread stopping.");
//							break;
//						}
//						Future<Integer> lenResult = client.read(lenBuffer);
//						Integer res = lenResult.get();
//						 
//						if (res == -1) {
//							logger.debug("connecction closed on reading:" + res);
//							try {
//								client.close();
//							} catch (IOException ex) {
//							}
//							client = null;// isConnected 에서 null 체크로직을 사용하므로
//							break;
////							} 
//						}
//						// ----------------------------------------------------------
//						// 버퍼사이즈만큼 읽어들여서 수정후 주석처리함.
//						// byte [] data = buffer.array();
//						// ----------------------------------------------------------
//						lenBuffer.flip();
//						byte[] bLen = new byte[lenBuffer.limit()];
//						lenBuffer.get(bLen);
//						
//						String sLen = new String(bLen);
//						int len = Integer.parseInt(sLen.trim());
//						System.out.println("sLen:" + sLen);
//						
//						ByteBuffer dataBuffer = ByteBuffer.allocate(len - 4);
//						Future<Integer> dataResult = client.read(dataBuffer);
//						res = dataResult.get();
//						 
//						if (res == -1) {
//							logger.debug("connecction closed on reading:" + res);
//							try {
//								client.close();
//							} catch (IOException ex) {
//							}
//							client = null;// isConnected 에서 null 체크로직을 사용하므로
//							break;
////							} 
//						}
//						// ----------------------------------------------------------
//						// 버퍼사이즈만큼 읽어들여서 수정후 주석처리함.
//						// byte [] data = buffer.array();
//						// ----------------------------------------------------------
//						dataBuffer.flip();
//						byte[] data = new byte[dataBuffer.limit()];
//						dataBuffer.get(data);
//						
//						byte [] msg = new byte[bLen.length + data.length];
//						System.arraycopy(bLen, 0, msg, 0, 4);
//						System.arraycopy(data, 0, msg, 4, data.length);
//						
//						if (messageListener != null)
//							try {
//								messageListener.onMessage(data);
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
////						logger.debug( "{dataLength:" + data.length + ", data:" + new String(data) + "}");
////						
////						receiveQueue.offer(data);
////						
////						if(logger != null) {
////							logger.debug( "{class:ksnbct2, method:listen, queueSize:" + receiveQueue.size() + ", dataLength:" + data.length + ", data:" + new String(data) + "}");
////						}else {							
////							if(systemout) {
////								System.out.println("{class:ksnbct2, method:listen, queueSize:" + receiveQueue.size() + ", dataLength:" + data.length + ", data:" + new String(data) + "}");
////							}
////						}
//					} catch (InterruptedException e) {
//						logger.debug("listener thread stop.");
//						break;
//					} catch (ExecutionException e) {
//						logger.error("listener exception", e);
//					} finally {
//						lenBuffer.clear();
//						dataBuffer.clear();
//					}
//				}
//			}
//
//		});
//		rThread.start();
//	}

	
	private void startReader() {

		final ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_CAPACITY);

		rThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (isStop) {
							logger.debug("Reader thread stopping.");
							break;
						} 
						
						Future<Integer> readResult = client.read(buffer);
						
						
						Integer res = readResult.get();
						
						
						if (res == -1) {
							logger.debug("connecction closed on reading:" + res);
//							if(connectionCheckListener != null) {
//								connectionCheckListener.check();
//							}else {
							try {
								client.close();
							} catch (IOException ex) {
							}
							client = null;// isConnected 에서 null 체크로직을 사용하므로
							break;
//							} 
						}
						// ----------------------------------------------------------
						// 버퍼사이즈만큼 읽어들여서 수정후 주석처리함.
						// byte [] data = buffer.array();
						// ----------------------------------------------------------
						buffer.flip();
						byte[] data = new byte[buffer.limit()];
						buffer.get(data);
//						if(receiveQueue.size() >= queueLimitSize) {
//							byte[] b = receiveQueue.remove();
//							if(logger != null) {
//								logger.debug("reached queue max limit:" + new String(b));
//							}else {
//								if(systemout) System.out.println("reached queue max limit:" + new String(b));
//							}
//						}
						if (data != null && data.length > 0 && messageListener != null)
							try {
								messageListener.onMessage(data);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//						logger.debug( "{dataLength:" + data.length + ", data:" + new String(data) + "}");
//						
//						receiveQueue.offer(data);
//						
//						if(logger != null) {
//							logger.debug( "{class:ksnbct2, method:listen, queueSize:" + receiveQueue.size() + ", dataLength:" + data.length + ", data:" + new String(data) + "}");
//						}else {							
//							if(systemout) {
//								System.out.println("{class:ksnbct2, method:listen, queueSize:" + receiveQueue.size() + ", dataLength:" + data.length + ", data:" + new String(data) + "}");
//							}
//						}
					} catch (InterruptedException e) {
						logger.debug("listener thread stop.");
						break;
					} catch (ExecutionException e) {
						logger.error("listener exception", e);
					} finally {
						buffer.clear();
					}
				}
			}

		});
		rThread.start();
	}
	
	/**
	 * 
	 */
	public void stop() {

		logger.debug("The client socket will stop.");

		isStop = true;
		if (rThread != null) {
			rThread.interrupt();
			rThread = null;
		}

		if (wThread != null) {
			wThread.interrupt();
			wThread = null;
		}

		if (client != null) {
			try {
				client.close();
			} catch (IOException ex) {
			}
			client = null;
		}

	}

	/**
	 * @param messageListener the messageListener to set
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

}
