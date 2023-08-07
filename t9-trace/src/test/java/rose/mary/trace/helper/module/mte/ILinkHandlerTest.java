/**
 * Copyright 2020 t9.whoami.com All Rights Reserved.
 */
package rose.mary.trace.helper.module.mte;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.common.util.Util;
import rose.mary.trace.core.helper.module.mte.ILinkMsgHandler;
import rose.mary.trace.core.helper.module.mte.MQMsgHandler;
import rose.mary.trace.core.helper.module.mte.MsgHandler;
import rose.mary.trace.core.monitor.ThroughputMonitor;

/**
 * <pre>
 * rose.mary.trace.helper.module.mte
 * ILinkHandlerTest.java
 * </pre>
 * 
 * @author whoana
 * @date Aug 16, 2019
 */
public class ILinkHandlerTest {

	static Logger logger = LoggerFactory.getLogger(ILinkHandlerTest.class);

	static AtomicInteger totoalMsgCnt = new AtomicInteger(0);
	
	public static void main(String args[]) {
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date d = sdf.parse("20230727102009");
			System.out.println(d.toString());

			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(sdf3.format(d));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void mainb(String args[]) {
		try { 
			 
			
			// new GetMessageHandler().start("gmh1");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh2");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh3");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh4");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh5");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh6");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh7");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh8");
			// Thread.sleep(1000);
			// new GetMessageHandler().start("gmh9");
			// Thread.sleep(1000);
		} catch (Throwable e) { 
			e.printStackTrace();
		}

	}

	static class GetMessageHandler implements Runnable {
		Thread thread;
		ILinkMsgHandler handler;
		int waitTime = 1000;
		String qmgrName = "TEST";
		String hostName = "10.10.1.10";
		int port = 11111;
		String channelName = "IIP.SVRCONN";
		String queueName = "TEST.LQ"; 

		public void start(String name) throws Exception{
			
			while(true){
				try{
					handler = new ILinkMsgHandler(qmgrName, hostName, port, channelName);
					handler.open(queueName, MsgHandler.Q_QPEN_OPT_GET); 
					break;
				}catch(Exception e){
					Thread.sleep(1000);
					System.out.println(name + " retry iLink open");
				}
			}
			thread = new Thread(this, name);
			thread.start();
		}
		
		@Override
		public void run() {
			while(true){
				try{
					Object msg = handler.get(waitTime);
					if(msg != null){
						System.out.println(Thread.currentThread().getName() + " " + msg.toString());	
					}
					System.out.println(Thread.currentThread().getName() + " finished to get msg");
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {  
					}
				}
			}
		}
		
	}

	public static void main2(String args[]) {
		try {
			int testCount = 1;

			int waitTime = 0;
			String qmgrName = "IIP";
			String hostName = "10.10.1.10";
			int port = 10000;
			String channelName = "IIP.SVRCONN";
			String queueName = "TRACE.EQ";
			String userId = null;
			String password = null;
			int ccsid = 1208;
			int characterSet = 1208;
			boolean bindMode = false;
			boolean autoCommit = false;

			ThroughputMonitor tpm = new ThroughputMonitor(1000);

			for (int i = 0; i < testCount; i++) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {

							ILinkMsgHandler handler = new ILinkMsgHandler(qmgrName, hostName, port, channelName);

							handler.open(queueName, MQMsgHandler.Q_QPEN_OPT_GET);

							System.out.println(Thread.currentThread().getName() + " start");

							int commitCount = 100;
							int uncommittedCount = 0;
							while (true) {

								Enumeration msgs = handler.browse(waitTime);

								while (msgs.hasMoreElements()) {
									Object msg = msgs.nextElement();
									if (msg != null) {
										String msgid = ((Message) msg).getJMSMessageID();
										// logger.debug(Thread.currentThread() + " msgid:"+msgid);
										Session session = handler.getSession();
										MessageConsumer consumer = session.createConsumer(handler.getQueue(),
												"JMSMessageID='" + msgid + "'");

										Message message = consumer.receive(0);

										tpm.count();
										totoalMsgCnt.incrementAndGet();
										// uncommittedCount++;
										// if (uncommittedCount % commitCount == 0) {
										// handler.commit();
										// logger.debug(Thread.currentThread().getName() + " committed:" +
										// uncommittedCount);
										// uncommittedCount = 0;
										// }
									}
								}
								handler.commit();

							}

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}

			tpm.start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						int cnt = 0;
						int current = 0;
						try {
							cnt = totoalMsgCnt.get();
							Thread.sleep(10 * 1000);
							current = totoalMsgCnt.get();
							logger.debug("\n***************totoalMsgCnt:" + current + "\n" + "tps:"
									+ (double) ((current - cnt) / 10));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main4(String args[]) {
		try {
			int testCount = 1;

			int waitTime = 0;
			String qmgrName = "TEST";
			String hostName = "10.10.1.10";
			int port = 11111;
			String channelName = "IIP.SVRCONN";
			String queueName = "TEST.LQ";
			String userId = null;
			String password = null;
			int ccsid = 1208;
			int characterSet = 1208;
			boolean bindMode = false;
			boolean autoCommit = false;

			ThroughputMonitor tpm = new ThroughputMonitor(1000);

			for (int i = 0; i < testCount; i++) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {

							ILinkMsgHandler handler = new ILinkMsgHandler(qmgrName, hostName, port, channelName);

							handler.open(queueName, MsgHandler.Q_OPEN_OPT_PUT);
							//handler.open(queueName, MQMsgHandler.Q_QPEN_OPT_GET);
							// System.out.println(Thread.currentThread().getName() + " start");

							int commitCount = 100;
							int uncommittedCount = 0;
							while (true) {
								long elapsed = System.currentTimeMillis();
								Object msg = handler.get(0);
								logger.debug("elapsed get:" + (System.currentTimeMillis() - elapsed));
								if (msg != null) {
									tpm.count();
									totoalMsgCnt.incrementAndGet();
									uncommittedCount++;

									if (uncommittedCount % commitCount == 0) {

										handler.commit();
										logger.debug(
												Thread.currentThread().getName() + " committed:" + uncommittedCount);
										uncommittedCount = 0;
									}

								}
								System.out.println(msg.toString());
								Thread.sleep(10 * 1000);
							}

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}

			tpm.start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						int cnt = 0;
						int current = 0;
						try {
							cnt = totoalMsgCnt.get();
							Thread.sleep(10 * 1000);
							current = totoalMsgCnt.get();
							logger.debug("\n***************totoalMsgCnt:" + current + "\n" + "tps:"
									+ (double) ((current - cnt) / 10));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main3(String args[]) {
		try {
			int testCount = 10;

			int waitTime = 0;
			String qmgrName = "IIP";
			String hostName = "10.10.1.10";
			int port = 10000;
			String channelName = "IIP.SVRCONN";
			String queueName = "TRACE.EQ";
			String userId = null;
			String password = null;
			int ccsid = 1208;
			int characterSet = 1208;
			boolean bindMode = false;
			boolean autoCommit = false;

			ThroughputMonitor tpm = new ThroughputMonitor(1000);

			for (int i = 0; i < testCount; i++) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {

							ILinkMsgHandler handler = new ILinkMsgHandler(qmgrName, hostName, port, channelName);

							handler.open(queueName, MQMsgHandler.Q_QPEN_OPT_GET);

							handler.setListener(new MessageListener() {

								int commitCount = 100;

								int uncommittedCount = 0;

								@Override
								public void onMessage(Message message) {
									// try {
									// logger.debug(Thread.currentThread().getName() + " received message " +
									// message.getJMSMessageID());
									// } catch (JMSException e) {
									// e.printStackTrace();
									// }
									tpm.count();
									totoalMsgCnt.incrementAndGet();
									uncommittedCount++;
									// if (uncommittedCount % commitCount == 0) {
									//
									// handler.commit();
									// logger.debug(Thread.currentThread().getName() + " committed:" +
									// uncommittedCount);
									// uncommittedCount = 0;
									// }

								}
							});

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}

			tpm.start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						int cnt = 0;
						int current = 0;
						try {
							cnt = totoalMsgCnt.get();
							Thread.sleep(10 * 1000);
							current = totoalMsgCnt.get();
							logger.debug("\n***************totoalMsgCnt:" + current + "\n" + "tps:"
									+ (double) ((current - cnt) / 10));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
