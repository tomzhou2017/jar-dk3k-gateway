package com.dk3k.framework.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import lombok.extern.java.Log;

/**
 * 
 * @author lait.zhang@gmail.com
 * @Date 2016年8月19日-下午5:38:22
 * @Description:<br>
 * 					线程消费</br>
 *
 */
@Log
public class ThreadPoolConsumer<T> {
	private ExecutorService executor;
	private volatile boolean stop = false;
	private final ThreadPoolConsumerBuilder<T> infoHolder;

	// 构造器
	public static class ThreadPoolConsumerBuilder<T> {
		//线程数量
		int threadCount;
		//处理间隔(每个线程处理完成后休息的时间)
		long intervalMils;
		MQBuilder mqBuilder;
		MQItem mqItem;
		//自定义处理接口
		MQProcess<T> mQProcess;

		public ThreadPoolConsumerBuilder<T> setThreadCount(int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public ThreadPoolConsumerBuilder<T> setIntervalMils(long intervalMils) {
			this.intervalMils = intervalMils;
			return this;
		}

		public ThreadPoolConsumerBuilder<T> setMQBuilder(MQBuilder mqBuilder) {
			this.mqBuilder = mqBuilder;
			return this;
		}

		public ThreadPoolConsumerBuilder<T> setMessageProcess(MQProcess<T> messageProcess) {
			this.mQProcess = messageProcess;
			return this;
		}
		
		public ThreadPoolConsumerBuilder<T> setMQItem(MQItem mqItem) {
			this.mqItem = mqItem;
			return this;
		}
		

		public ThreadPoolConsumer<T> build() {
			return new ThreadPoolConsumer<T>(this);
		}
	}

	private ThreadPoolConsumer(ThreadPoolConsumerBuilder<T> threadPoolConsumerBuilder) {
		this.infoHolder = threadPoolConsumerBuilder;
		executor = Executors.newFixedThreadPool(threadPoolConsumerBuilder.threadCount);
	}

	public void start() throws IOException, TimeoutException {
		for (int i = 0; i < infoHolder.threadCount; i++) {
			// 构造messageConsumer
			final MQConsumer mQConsumer = infoHolder.mqBuilder.buildMessageConsumer(infoHolder.mqItem, infoHolder.mQProcess);
			executor.execute(new Runnable() {
				@Override
				public void run() {
					//int i = 0;
					while (!stop) {
						//final Transaction trans = Cat.newTransaction("Service", "ThreadPoolConsumer.start()");
						try {
							// 执行consume
							MQResponse mqResult = mQConsumer.consume();
							// 埋点统计数量
							//Cat.logMetricForCount("MessageConsumer.count");
							//System.out.println("consume is success = " + mqResult.isSuccess() + "-" + i++);
							if (infoHolder.intervalMils > 0) {
								try {
									Thread.sleep(infoHolder.intervalMils);
								} catch (InterruptedException e) {
									e.printStackTrace();
									log.info("interrupt " + e);
								}
							}
							if (!mqResult.isSuccess()) {
								log.info("run error " + mqResult.getErrMsg());
							}
							//trans.setStatus("0");
						} catch (Exception e) {
							e.printStackTrace();
							log.info("run exception " + e);
							//trans.setStatus(e);
							throw e;
						} finally{
							//trans.complete();
						}
					}
				}
			});
		}
	}

	/**
	 * 
	 * @author lait.zhang@gmail.com
	 * @Date Oct 11, 2016
	 * @Description:<code>停止消费</code>
	 */
	public void stop() {
		this.stop = true;
	}
	
	/**
	 * 
	 * @author lait.zhang@gmail.com
	 * @Date Oct 11, 2016
	 * @Description:<code>重新启动消费</code>
	 */
	public void reStart(){
		this.stop = false;
	}
}
