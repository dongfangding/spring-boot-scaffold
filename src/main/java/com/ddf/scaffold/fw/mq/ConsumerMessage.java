package com.ddf.scaffold.fw.mq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * 如果配置了多个queues那么如何在接收消息的时候判断是从哪个queue中读取的呢，{@link com.ddf.scaffold.fw.mq.ConsumerMessage#receiveStringFromQueue(java.lang.String, org.springframework.amqp.core.Queue)}
 *
 * @author DDf on 2019/4/21
 */
@Component
@RabbitListener(queues = {ProductConfig.HELLO_QUEUE, ProductConfig.FANOUT_QUEUE, ProductConfig.FANOUT_QUEUE2})
public class ConsumerMessage {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerMessage.class);


	/**
	 * @param msg
	 * @param queue
	 */
	@RabbitHandler
	@Async("taskExecutor")
	public void receiveStringFromQueue(String msg, Channel channel, Message message) throws IOException {
		try {
			if (true) {
				throw new RuntimeException("处理任务失败！");
			}
			// 开启了手动确认之后，要自己编码确认消息已收到,如果有自己的业务逻辑，则处理完业务逻辑之后再手动确认？
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			logger.info("receiveStringFromQueue队列消费到消息.....{}", msg);
		} catch (IOException e) {
			e.printStackTrace();
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
		}
	}
}
